package com.panthrixsgalaxy.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Crea una ventana en la pantalla del jugador con los datos extra que manda el servidor. */
@FunctionalInterface
public interface PGMenuFactory<T extends AbstractContainerMenu> {

    T create(int containerId, Inventory inventory, FriendlyByteBuf extraData);
}
