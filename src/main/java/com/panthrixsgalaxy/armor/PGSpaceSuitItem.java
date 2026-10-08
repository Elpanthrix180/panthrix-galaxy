package com.panthrixsgalaxy.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Pieza del traje espacial (casco, pechera, pantalones o botas).
 *
 * Además de ser una armadura normal, incluye dos comprobaciones que usarán
 * otros sistemas (oxígeno en la Fase 5, temperatura, etc.):
 *   - hasSpaceHelmet(...)  -> ¿lleva puesto un casco espacial?
 *   - hasFullSpaceSuit(...) -> ¿lleva las 4 piezas?
 */
public class PGSpaceSuitItem extends ArmorItem {

    public PGSpaceSuitItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** ¿La entidad lleva un casco espacial en la cabeza? */
    public static boolean hasSpaceHelmet(LivingEntity entity) {
        return isSuitPiece(entity.getItemBySlot(EquipmentSlot.HEAD));
    }

    /** ¿La entidad lleva las 4 piezas del traje espacial? */
    public static boolean hasFullSpaceSuit(LivingEntity entity) {
        return isSuitPiece(entity.getItemBySlot(EquipmentSlot.HEAD))
                && isSuitPiece(entity.getItemBySlot(EquipmentSlot.CHEST))
                && isSuitPiece(entity.getItemBySlot(EquipmentSlot.LEGS))
                && isSuitPiece(entity.getItemBySlot(EquipmentSlot.FEET));
    }

    /** ¿Este objeto es una pieza del traje espacial? */
    public static boolean isSuitPiece(ItemStack stack) {
        return stack.getItem() instanceof PGSpaceSuitItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (getType() == Type.HELMET) {
            tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.space_helmet").withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.space_suit_piece").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
