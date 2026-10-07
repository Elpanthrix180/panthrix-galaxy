package com.panthrixsgalaxy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bidón de combustible de cohete. Se llena en la refinería y se vacía en el cohete.
 * Una barra naranja bajo el icono indica cuánto lleva.
 */
public class PGFuelCanisterItem extends Item {

    private static final String FUEL_TAG = "Fuel";
    private static final int BAR_COLOR = 0xFF9922;

    private final int capacity;

    public PGFuelCanisterItem(int capacity, Properties properties) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public static int getFuel(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(FUEL_TAG) : 0;
    }

    public static void setFuel(ItemStack stack, int fuel) {
        if (stack.getItem() instanceof PGFuelCanisterItem canister) {
            stack.getOrCreateTag().putInt(FUEL_TAG, Math.max(0, Math.min(fuel, canister.capacity)));
        }
    }

    /** Bidón lleno (para el modo creativo). */
    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        setFuel(stack, capacity);
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getFuel(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.fuel_amount", getFuel(stack), capacity)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.fuel_canister_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
