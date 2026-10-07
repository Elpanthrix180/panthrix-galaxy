package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import com.panthrixsgalaxy.menu.PGShipMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Paquete jugador -> servidor: botones del panel de control de la nave. */
public class ShipActionPacket {

    public enum Action {
        PICKUP
    }

    private final Action action;

    public ShipActionPacket(Action action) {
        this.action = action;
    }

    public ShipActionPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(Action.class));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        // Solo vale si el jugador tiene abierto el panel de una nave (y sigue cerca)
        if (player != null && player.containerMenu instanceof PGShipMenu menu && menu.stillValid(player)) {
            PGShipEntity ship = menu.getShip();
            if (ship != null && action == Action.PICKUP) {
                player.closeContainer();
                ship.pickUp(player);
            }
        }
        context.get().setPacketHandled(true);
    }
}
