package com.panthrixsgalaxy.weapon;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.panthrixsgalaxy.system.energy.ItemEnergyStorage;
import com.panthrixsgalaxy.system.energy.PortableEnergy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

/**
 * Espada láser.
 *
 *   Mayús + clic derecho  -> encender / apagar
 *   Clic derecho (apagada) -> encender
 *   Clic derecho mantenido (encendida) -> GUARDIA: reduce a la mitad los golpes que vienen de
 *                                         delante y DEVUELVE los rayos láser (gasta energía)
 *
 * Apagada es solo una empuñadura (pega como el puño). Encendida:
 *   - hace el daño de su cristal y gasta energía en cada golpe y mientras está encendida;
 *   - nunca se desgasta (no tiene durabilidad): lo que se gasta es la energía;
 *   - si se queda sin energía (ni en la espada ni en la mochila), se apaga sola.
 */
public class PGLaserSwordItem extends SwordItem {

    public static final String ACTIVE_TAG = "Active";
    private static final int BAR_COLOR = 0xFFDD33;

    /** "Nivel" de herramienta: no se desgasta (0 usos) y corta telarañas como una espada de diamante. */
    private static final Tier LASER_TIER = new ForgeTier(3, 0, 8.0f, 0.0f, 15, BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.EMPTY);

    private final LaserSwordTier tier;
    /** Atributos con la hoja apagada: daño de puño, velocidad normal. */
    private final Multimap<Attribute, AttributeModifier> offModifiers;

    public PGLaserSwordItem(LaserSwordTier tier, Properties properties) {
        super(LASER_TIER, tier.getDamage(), tier.getAttackSpeed(), properties);
        this.tier = tier;
        this.offModifiers = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 0.0,
                        AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4,
                        AttributeModifier.Operation.ADDITION));
    }

    public LaserSwordTier getLaserTier() {
        return tier;
    }

    // ===== Encendida / apagada =====

    public static boolean isActive(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }

    private static void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
    }

    private void toggle(Level level, Player player, ItemStack stack) {
        boolean turnOn = !isActive(stack);
        if (turnOn && !PortableEnergy.canAfford(stack, player, tier.getHitCost())) {
            level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.6f);
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.laser_no_energy")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        setActive(stack, turnOn);
        level.playSound(null, player.blockPosition(), turnOn ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.8f, 2.0f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || !isActive(stack)) {
            if (!level.isClientSide) {
                toggle(level, player, stack);
            }
            return InteractionResultHolder.consume(stack);
        }
        // Encendida: guardia mientras mantengas el botón
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isActive(stack) ? UseAnim.BLOCK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    /** Cada segundo encendida gasta un poco de energía; sin energía se apaga. Zumbido de vez en cuando. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !isActive(stack) || !(entity instanceof Player player)) {
            return;
        }
        boolean inHand = selected || player.getOffhandItem() == stack;
        if (!inHand) {
            setActive(stack, false); // al guardarla se apaga sola
            return;
        }
        if (player.tickCount % 20 == 0 && !PortableEnergy.consume(stack, player, LaserSwordTier.IDLE_COST_PER_SECOND)) {
            shutDown(level, player, stack);
            return;
        }
        if (player.tickCount % 60 == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.25f, 1.8f);
        }
    }

    private static void shutDown(Level level, Player player, ItemStack stack) {
        setActive(stack, false);
        player.stopUsingItem();
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 2.0f);
        player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.laser_sword_empty")
                .withStyle(ChatFormatting.RED), true);
    }

    // ===== Combate =====

    /** Golpe: gasta energía, chispas del color de la hoja y, la roja, prende fuego. Nunca pierde durabilidad. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!isActive(stack)) {
            return true;
        }
        if (attacker instanceof Player player && !PortableEnergy.consume(stack, player, tier.getHitCost())) {
            shutDown(attacker.level(), player, stack);
            return true;
        }
        if (tier.getFireSeconds() > 0) {
            target.setSecondsOnFire(tier.getFireSeconds());
        }
        sparks(attacker.level(), target.position().add(0.0, target.getBbHeight() * 0.6, 0.0), tier.getColor(), 8);
        attacker.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS,
                0.7f, 1.6f);
        return true;
    }

    /** Romper bloques tampoco la desgasta. */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        return true;
    }

    /** Apagada no corta (ni telarañas ni ataque de barrido). */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isActive(stack) ? super.getDestroySpeed(stack, state) : 1.0f;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return isActive(stack) && super.canPerformAction(stack, toolAction);
    }

    /** Daño según esté encendida o apagada. */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && !isActive(stack)) {
            return offModifiers;
        }
        return super.getAttributeModifiers(slot, stack);
    }

    // ===== Guardia =====

    /** ¿Está esta criatura en guardia con una espada láser encendida? */
    public static boolean isGuarding(LivingEntity entity) {
        ItemStack using = entity.getUseItem();
        return entity.isUsingItem() && using.getItem() instanceof PGLaserSwordItem && isActive(using);
    }

    /** ¿El ataque viene de delante? (como un escudo) */
    public static boolean isFacing(LivingEntity entity, Vec3 from) {
        Vec3 look = entity.getViewVector(1.0f).multiply(1.0, 0.0, 1.0).normalize();
        Vec3 toSource = from.subtract(entity.position()).multiply(1.0, 0.0, 1.0).normalize();
        return look.dot(toSource) > 0.2;
    }

    /** Paga la guardia. Devuelve false si no queda energía (y la espada se apaga). */
    public static boolean payGuard(LivingEntity entity) {
        ItemStack using = entity.getUseItem();
        if (!(entity instanceof Player player)) {
            return true;
        }
        if (!PortableEnergy.consume(using, player, LaserSwordTier.GUARD_COST)) {
            shutDown(player.level(), player, using);
            return false;
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.0f, 1.4f);
        return true;
    }

    /** Chispas de un color. */
    public static void sparks(Level level, Vec3 at, int color, int count) {
        if (level instanceof ServerLevel server) {
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(
                    ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f), 1.0f);
            server.sendParticles(dust, at.x, at.y, at.z, count, 0.2, 0.2, 0.2, 0.0);
        }
    }

    // ===== Energía (como una batería) =====

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy =
                    LazyOptional.of(() -> new ItemEnergyStorage(stack, tier.getCapacity(), tier.getCapacity() / 5));

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                return ForgeCapabilities.ENERGY.orEmpty(capability, energy);
            }
        };
    }

    /** Espada cargada (para el modo creativo). */
    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, tier.getCapacity());
        return stack;
    }

    /** Que no "rebote" en la mano cada vez que cambia su energía o se enciende. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * ItemEnergyStorage.getEnergy(stack) / tier.getCapacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(isActive(stack) ? "tooltip.panthrixsgalaxy.laser_sword_on"
                : "tooltip.panthrixsgalaxy.laser_sword_off").withStyle(isActive(stack) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.energy_amount",
                ItemEnergyStorage.getEnergy(stack), tier.getCapacity()).withStyle(ChatFormatting.YELLOW));
        if (tier.getFireSeconds() > 0) {
            tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.laser_sword_fire").withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.laser_sword_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
