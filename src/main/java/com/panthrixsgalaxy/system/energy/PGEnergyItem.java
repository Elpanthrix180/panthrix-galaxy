package com.panthrixsgalaxy.system.energy;

import net.minecraft.world.item.ItemStack;

/** Un objeto que guarda energía dentro (batería, armas láser...). */
public interface PGEnergyItem {

    /** La energía de ese objeto concreto (se guarda en su dato "Energy"). */
    PGEnergyHandler createEnergyHandler(ItemStack stack);
}
