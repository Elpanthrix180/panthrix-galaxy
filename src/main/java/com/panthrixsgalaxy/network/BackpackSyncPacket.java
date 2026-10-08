package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** Paquete servidor -> jugadores: "el jugador X lleva esta mochila" (para dibujarla en su espalda). */
public class BackpackSyncPacket {

    private final int entityId;
    private final ItemStack backpack;

    public BackpackSyncPacket(int entityId, ItemStack backpack) {
        this.entityId = entityId;
        this.backpack = backpack;
    }

    public BackpackSyncPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readItem());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeItem(backpack);
    }

    /**
     * Al llegar a la pantalla del jugador. Usa cosas que solo existen en el cliente:
     * PGNetwork (Forge o Fabric) solo lo llama allí, nunca en un servidor dedicado.
     */
    public void handleOnClient() {
        ClientPacketHandler.handleBackpackSync(entityId, backpack);
    }
}
