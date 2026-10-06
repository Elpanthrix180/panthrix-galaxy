package com.panthrixsgalaxy.item;

import com.panthrixsgalaxy.menu.BackpackContainer;
import com.panthrixsgalaxy.menu.PGBackpackMenu;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Mochila espacial.
 *
 * Tiene DOS partes independientes, guardadas dentro del propio objeto:
 *   1. Inventario: 9, 18, 27 o 36 huecos (según el modelo).
 *   2. Depósitos: oxígeno, energía, combustible y agua (cada uno con su máximo).
 *
 * Se lleva en el HUECO DE MOCHILA del jugador (ver PGBackpackSlot):
 *   - Clic derecho con ella en la mano: equiparla (si ya llevas una, se intercambian).
 *   - Tecla B: abrirla.   Mayús + B: quitarla.
 */
public class PGBackpackItem extends Item {

    /** Los cuatro depósitos de la mochila. */
    public enum Tank {
        OXYGEN("Oxygen", ChatFormatting.AQUA, 0xFF55CCFF),
        ENERGY("Energy", ChatFormatting.YELLOW, 0xFFFFDD33),
        FUEL("Fuel", ChatFormatting.GOLD, 0xFFFF9922),
        WATER("Water", ChatFormatting.BLUE, 0xFF3377FF);

        /** Nombre del dato dentro de la mochila. */
        private final String tagName;
        private final ChatFormatting textColor;
        /** Color de la barra en el panel de la mochila (ARGB). */
        private final int barColor;

        Tank(String tagName, ChatFormatting textColor, int barColor) {
            this.tagName = tagName;
            this.textColor = textColor;
            this.barColor = barColor;
        }

        public int getBarColor() {
            return barColor;
        }

        /** Parte final de las claves de traducción: "oxygen", "energy"... */
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Número de filas del inventario (1 fila = 9 huecos). */
    private final int rows;
    private final int oxygenCapacity;
    private final int energyCapacity;
    private final int fuelCapacity;
    private final int waterCapacity;
    /** Nombre del modelo 3D que se ve en la espalda (models/item/worn/<nombre>.json). */
    private final String wornModel;

    public PGBackpackItem(int rows, int oxygenCapacity, int energyCapacity, int fuelCapacity, int waterCapacity,
                          String wornModel, Properties properties) {
        super(properties.stacksTo(1));
        this.rows = rows;
        this.oxygenCapacity = oxygenCapacity;
        this.energyCapacity = energyCapacity;
        this.fuelCapacity = fuelCapacity;
        this.waterCapacity = waterCapacity;
        this.wornModel = wornModel;
    }

    public int getRows() {
        return rows;
    }

    public int getSlotCount() {
        return rows * 9;
    }

    public String getWornModel() {
        return wornModel;
    }

    /** Máximo de un depósito. */
    public int getCapacity(Tank tank) {
        return switch (tank) {
            case OXYGEN -> oxygenCapacity;
            case ENERGY -> energyCapacity;
            case FUEL -> fuelCapacity;
            case WATER -> waterCapacity;
        };
    }

    // ===== DEPÓSITOS =====

    /** Cantidad que hay en un depósito de la mochila. */
    public static int getTank(ItemStack stack, Tank tank) {
        return stack.hasTag() ? stack.getTag().getInt(tank.tagName) : 0;
    }

    /** Cambia la cantidad de un depósito (entre 0 y su máximo). */
    public static void setTank(ItemStack stack, Tank tank, int amount) {
        if (stack.getItem() instanceof PGBackpackItem backpack) {
            int clamped = Math.max(0, Math.min(amount, backpack.getCapacity(tank)));
            stack.getOrCreateTag().putInt(tank.tagName, clamped);
        }
    }

    // ===== EQUIPAR, ABRIR Y QUITAR =====

    /** Clic derecho: equipar la mochila (intercambiándola con la que llevaras). */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        ItemStack previous = PGBackpackSlot.getEquipped(serverPlayer);
        PGBackpackSlot.setEquipped(serverPlayer, stack);
        PGBackpackSlot.sync(serverPlayer);
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0f, 1.0f);
        serverPlayer.displayClientMessage(Component.translatable("message.panthrixsgalaxy.backpack_equipped")
                .withStyle(ChatFormatting.GREEN), true);
        // Lo que queda en la mano: la mochila anterior (o nada)
        return InteractionResultHolder.success(previous);
    }

    /** Tecla B: abrir la mochila equipada. */
    public static void openEquipped(ServerPlayer player) {
        ItemStack stack = PGBackpackSlot.getEquipped(player);
        if (!(stack.getItem() instanceof PGBackpackItem backpack)) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.no_backpack")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider((containerId, playerInventory, p) ->
                        new PGBackpackMenu(containerId, playerInventory, stack, backpack.getRows()),
                        stack.getHoverName()),
                buf -> buf.writeVarInt(backpack.getRows()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER,
                SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    /** Mayús + B: quitar la mochila y ponerla en el inventario (o en el suelo si está lleno). */
    public static void unequip(ServerPlayer player) {
        ItemStack stack = PGBackpackSlot.getEquipped(player);
        if (stack.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.no_backpack")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        PGBackpackSlot.setEquipped(player, ItemStack.EMPTY);
        PGBackpackSlot.sync(player);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.backpack_unequipped")
                .withStyle(ChatFormatting.YELLOW), true);
    }

    // ===== BARRA E INFORMACIÓN =====

    /** Barra azul bajo el icono: oxígeno del depósito. */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getTank(stack, Tank.OXYGEN) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getTank(stack, Tank.OXYGEN) / oxygenCapacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55CCFF;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int used = 0;
        if (stack.hasTag()) {
            used = stack.getTag().getCompound(BackpackContainer.INVENTORY_TAG).getList("Items", Tag.TAG_COMPOUND).size();
        }
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.backpack_slots", used, getSlotCount())
                .withStyle(ChatFormatting.WHITE));
        for (Tank tank : Tank.values()) {
            tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.tank." + tank.key(),
                    getTank(stack, tank), getCapacity(tank)).withStyle(tank.textColor));
        }
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.backpack_controls").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
