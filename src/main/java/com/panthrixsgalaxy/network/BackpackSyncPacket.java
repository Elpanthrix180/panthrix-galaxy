package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public void handle(Supplier<NetworkEvent.Context> context) {
        // Este código usa cosas que solo existen en la pantalla del jugador,
        // por eso se ejecuta a través de DistExecutor (nunca en un servidor dedicado).
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleBackpackSync(entityId, backpack));
        context.get().setPacketHandled(true);
    }
}
