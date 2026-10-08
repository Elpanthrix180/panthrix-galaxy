package com.panthrixsgalaxy.fabric;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Solo Fabric: lo que el mixin PlayerMixin añade a cada jugador.
 *   - El hueco de mochila.
 *   - Datos extra del mod (en Forge se usa player.getPersistentData()).
 */
public interface PGPlayerData {

    /** Parte de los datos que se copia al reaparecer tras morir (igual que en Forge). */
    String PERSISTED_TAG = "PlayerPersisted";

    ItemStack panthrixsgalaxy$getBackpack();

    void panthrixsgalaxy$setBackpack(ItemStack backpack);

    CompoundTag panthrixsgalaxy$getData();
}
