package com.panthrixsgalaxy.weapon;

import com.panthrixsgalaxy.entity.laser.PGLaserBoltEntity;
import com.panthrixsgalaxy.system.energy.ItemEnergyStorage;
import com.panthrixsgalaxy.system.energy.PortableEnergy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Arma láser. Clic derecho = disparar un rayo de energía.
 *
 * Energía (ver PortableEnergy):
 *   1) Primero gasta la energía del arma (barra amarilla bajo el icono).
 *   2) Si está vacía, usa la energía de la MOCHILA equipada (Fase 7).
 *   3) Se recarga como una batería: clic derecho sobre un generador o una celda de energía.
 *
 * Guarda la energía en el dato "Energy" igual que las baterías, así que cualquier
 * máquina del mod (o de otros mods) puede cargarla.
 */
public class PGLaserItem extends Item {

    private static final int BAR_COLOR = 0xFFDD33;

    private final LaserTier tier;

    public PGLaserItem(LaserTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public LaserTier getTier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!PortableEnergy.canAfford(stack, player, tier.getShotCost())) {
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.6f);
                player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.laser_no_energy")
                        .withStyle(ChatFormatting.RED), true);
            }
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            PortableEnergy.consume(stack, player, tier.getShotCost());
            PGLaserBoltEntity bolt = new PGLaserBoltEntity(level, player, tier);
            bolt.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, (float) tier.getSpeed(), 0.0f);
            level.addFreshEntity(bolt);
            level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                    0.6f, tier == LaserTier.PISTOL ? 2.0f : 1.4f);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                    1.0f, tier == LaserTier.PISTOL ? 1.8f : 1.2f);
            showShotsLeft(stack, player);
        }
        player.getCooldowns().addCooldown(this, tier.getCooldown());
        return InteractionResultHolder.consume(stack);
    }

    /** "Láser: 42 disparos (+ 120 con la mochila)" en la barra de mensajes. */
    private void showShotsLeft(ItemStack stack, Player player) {
        int own = PortableEnergy.ownEnergy(stack) / tier.getShotCost();
        int backpack = PortableEnergy.backpackEnergy(player) / tier.getShotCost();
        Component text = backpack > 0
                ? Component.translatable("message.panthrixsgalaxy.laser_shots_backpack", own, backpack)
                : Component.translatable("message.panthrixsgalaxy.laser_shots", own);
        player.displayClientMessage(text.copy().withStyle(own > 5 || backpack > 0 ? ChatFormatting.AQUA : ChatFormatting.GOLD), true);
    }

    /** El arma guarda energía como una batería (capability de Forge). */
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

    /** Arma cargada (para el modo creativo). */
    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, tier.getCapacity());
        return stack;
    }

    /** Que el arma no "rebote" en la mano cada vez que cambia su energía. */
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
        int energy = ItemEnergyStorage.getEnergy(stack);
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.energy_amount", energy, tier.getCapacity())
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.laser_stats",
                formatDamage(tier.getDamage()), tier.getRange(), energy / tier.getShotCost()).withStyle(ChatFormatting.AQUA));
        if (tier.getPierce() > 0) {
            tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.laser_pierce", tier.getPierce())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.laser_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    /** 5.0 -> "5", 5.5 -> "5.5" (en corazones: daño / 2). */
    private static String formatDamage(float damage) {
        float hearts = damage / 2.0f;
        return hearts == Math.floor(hearts) ? String.valueOf((int) hearts) : String.valueOf(hearts);
    }
}
