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
 * Bombona de oxígeno. Guarda su oxígeno dentro del propio objeto (en su NBT, como un
 * libro guarda su texto). Una barra azul debajo del icono indica cuánto queda.
 * 1 unidad de oxígeno = 1 segundo respirando.
 */
public class PGOxygenTankItem extends Item {

    /** Nombre del dato donde se guarda el oxígeno. */
    private static final String OXYGEN_TAG = "Oxygen";
    private static final int BAR_COLOR = 0x55CCFF;

    private final int capacity;

    public PGOxygenTankItem(int capacity, Properties properties) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Oxígeno que tiene esta bombona (0 si es nueva). */
    public static int getOxygen(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(OXYGEN_TAG) : 0;
    }

    /** Cambia el oxígeno de la bombona (nunca por debajo de 0 ni por encima del máximo). */
    public static void setOxygen(ItemStack stack, int oxygen) {
        if (stack.getItem() instanceof PGOxygenTankItem tank) {
            stack.getOrCreateTag().putInt(OXYGEN_TAG, Math.max(0, Math.min(oxygen, tank.capacity)));
        }
    }

    public static boolean isFull(ItemStack stack) {
        return stack.getItem() instanceof PGOxygenTankItem tank && getOxygen(stack) >= tank.capacity;
    }

    /** Crea una bombona llena (para el modo creativo). */
    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        setOxygen(stack, capacity);
        return stack;
    }

    // ----- Barra azul bajo el icono -----

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getOxygen(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int oxygen = getOxygen(stack);
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.oxygen_amount", oxygen, capacity)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.oxygen_time", oxygen / 60, oxygen % 60)
                .withStyle(ChatFormatting.GRAY));
        if (oxygen == 0) {
            tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.oxygen_empty").withStyle(ChatFormatting.RED));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
