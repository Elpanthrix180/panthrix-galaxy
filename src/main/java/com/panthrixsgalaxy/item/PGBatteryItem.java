package com.panthrixsgalaxy.item;

import com.panthrixsgalaxy.system.energy.ItemEnergyStorage;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.system.energy.PGEnergyItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Batería portátil. Guarda PG Energía (FE) dentro del objeto; una barra amarilla
 * bajo el icono indica la carga. Se carga con clic derecho sobre un generador o una celda.
 * Más adelante alimentará láseres, espadas láser y trajes.
 */
public class PGBatteryItem extends Item implements PGEnergyItem {

    private static final int BAR_COLOR = 0xFFDD33;

    private final int capacity;
    /** Máximo que entra o sale en cada transferencia. */
    private final int maxTransfer;

    public PGBatteryItem(int capacity, int maxTransfer, Properties properties) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
        this.maxTransfer = maxTransfer;
    }

    public int getCapacity() {
        return capacity;
    }

    /** La energía de esta batería (Forge y Fabric la ofrecen a las máquinas de otros mods). */
    @Override
    public PGEnergyHandler createEnergyHandler(ItemStack stack) {
        return new ItemEnergyStorage(stack, capacity, maxTransfer);
    }

    /** Batería llena (para el modo creativo). */
    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, capacity);
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * ItemEnergyStorage.getEnergy(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.energy_amount",
                ItemEnergyStorage.getEnergy(stack), capacity).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.battery_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
