package com.panthrixsgalaxy.system.backpack;

import com.panthrixsgalaxy.fabric.PGBackpackHolder;
import com.panthrixsgalaxy.network.BackpackSyncPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * El HUECO DE MOCHILA del jugador.
 *
 * VERSIÓN FABRIC: el hueco lo añade el mixin fabric/mixin/PlayerMixin.
 * La versión Forge (src/main/java/...) usa una capability y tiene los mismos métodos.
 */
public final class PGBackpackSlot {

    /** La mochila que lleva equipada el jugador (vacío si no lleva). */
    public static ItemStack getEquipped(Player player) {
        return ((PGBackpackHolder) player).panthrixsgalaxy$getBackpack();
    }

    /** Equipa (o quita, con ItemStack.EMPTY) la mochila del jugador. */
    public static void setEquipped(Player player, ItemStack stack) {
        ((PGBackpackHolder) player).panthrixsgalaxy$setBackpack(stack);
    }

    /** Avisa al jugador y a los que lo ven de qué mochila lleva (para dibujarla en su espalda). */
    public static void sync(ServerPlayer player) {
        PGNetwork.sendToTrackingAndSelf(player, new BackpackSyncPacket(player.getId(), getEquipped(player)));
    }

    private PGBackpackSlot() {
    }
}
