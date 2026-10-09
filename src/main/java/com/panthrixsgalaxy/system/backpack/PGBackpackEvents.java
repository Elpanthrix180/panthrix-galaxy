package com.panthrixsgalaxy.system.backpack;

import com.panthrixsgalaxy.network.BackpackSyncPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

/**
 * Todo lo que le pasa al hueco de mochila:
 *   - Se conserva al cambiar de dimensión y, con keepInventory, al morir.
 *   - Al morir (sin keepInventory) la mochila cae al suelo, como la armadura.
 *   - Se sincroniza con los demás jugadores para que la vean en tu espalda.
 *
 * Es común: Forge (forge/PGForgeEvents) y Fabric (fabric/PGFabricEvents) llaman a estos métodos.
 */
public final class PGBackpackEvents {

    /** Al reaparecer o volver del End, Minecraft crea un jugador nuevo: copiamos la mochila si toca. */
    public static void onPlayerClone(Player original, Player clone, boolean wasDeath) {
        if (!wasDeath || keepInventory(original)) {
            PGBackpackSlot.setEquipped(clone, PGBackpackSlot.getEquipped(original));
        }
    }

    /**
     * Al morir sin keepInventory, la mochila (con todo lo que lleva dentro) cae al suelo.
     * Devuelve la mochila que hay que soltar (o vacío) y la quita del hueco.
     */
    public static ItemStack takeDeathDrop(Player player) {
        if (player.level().isClientSide || keepInventory(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        PGBackpackSlot.setEquipped(player, ItemStack.EMPTY);
        return backpack;
    }

    /** Al entrar, reaparecer o cambiar de dimensión: avisar a todos de qué mochila lleva. */
    public static void sync(ServerPlayer player) {
        PGBackpackSlot.sync(player);
    }

    /** Cuando alguien empieza a ver a otro jugador, le contamos qué mochila lleva. */
    public static void onStartTracking(Entity target, ServerPlayer watcher) {
        if (target instanceof Player player) {
            PGNetwork.sendToPlayer(watcher, new BackpackSyncPacket(player.getId(), PGBackpackSlot.getEquipped(player)));
        }
    }

    private static boolean keepInventory(Player player) {
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }

    private PGBackpackEvents() {
    }
}
