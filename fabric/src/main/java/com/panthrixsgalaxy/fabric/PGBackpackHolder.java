package com.panthrixsgalaxy.fabric;

import net.minecraft.world.item.ItemStack;

/** Solo Fabric: lo que el mixin PlayerMixin añade a cada jugador (el hueco de mochila). */
public interface PGBackpackHolder {

    ItemStack panthrixsgalaxy$getBackpack();

    void panthrixsgalaxy$setBackpack(ItemStack backpack);
}
