package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.item.PGBackpackItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Paquete jugador -> servidor: se ha pulsado B (abrir) o Mayús+B (quitar la mochila). */
public class BackpackActionPacket {

    public enum Action {
        OPEN,
        UNEQUIP
    }

    private final Action action;

    public BackpackActionPacket(Action action) {
        this.action = action;
    }

    public BackpackActionPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(Action.class));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            switch (action) {
                case OPEN -> PGBackpackItem.openEquipped(player);
                case UNEQUIP -> PGBackpackItem.unequip(player);
            }
        }
        context.get().setPacketHandled(true);
    }
}
