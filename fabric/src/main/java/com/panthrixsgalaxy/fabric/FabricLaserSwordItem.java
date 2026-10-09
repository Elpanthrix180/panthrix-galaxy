package com.panthrixsgalaxy.fabric;

import com.google.common.collect.Multimap;
import com.panthrixsgalaxy.weapon.LaserSwordTier;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Solo Fabric: la espada láser con lo que Fabric pregunta a los objetos (FabricItem).
 * Todo lo demás está en PGLaserSwordItem (común a Forge y Fabric).
 */
public class FabricLaserSwordItem extends PGLaserSwordItem {

    public FabricLaserSwordItem(LaserSwordTier tier, Properties properties) {
        super(tier, properties);
    }

    /** Daño según esté encendida o apagada. */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = getStackModifiers(slot, stack);
        return modifiers != null ? modifiers : super.getAttributeModifiers(stack, slot);
    }

    /** Que no "rebote" en la mano cada vez que cambia su energía o se enciende. */
    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }
}
