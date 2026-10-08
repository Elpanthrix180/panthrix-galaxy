package com.panthrixsgalaxy.forge;

import com.google.common.collect.Multimap;
import com.panthrixsgalaxy.weapon.LaserSwordTier;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;

/**
 * Solo Forge: la espada láser con dos cosas que Forge pregunta a los objetos.
 * Todo lo demás está en PGLaserSwordItem (común a Forge y Fabric).
 */
public class ForgeLaserSwordItem extends PGLaserSwordItem {

    public ForgeLaserSwordItem(LaserSwordTier tier, Properties properties) {
        super(tier, properties);
    }

    /** Apagada no hace acciones de espada (barrido, cortar telarañas...). */
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return isActive(stack) && super.canPerformAction(stack, toolAction);
    }

    /** Daño según esté encendida o apagada. */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = getStackModifiers(slot, stack);
        return modifiers != null ? modifiers : super.getAttributeModifiers(slot, stack);
    }
}
