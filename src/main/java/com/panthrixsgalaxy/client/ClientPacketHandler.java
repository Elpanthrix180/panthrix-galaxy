package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Qué hacer en la pantalla del jugador cuando llegan ciertos paquetes. Solo existe en el cliente. */
public final class ClientPacketHandler {

    public static void handleBackpackSync(int entityId, ItemStack backpack) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (entity instanceof Player player) {
            PGBackpackSlot.setEquipped(player, backpack);
        }
    }

    private ClientPacketHandler() {
    }
}
