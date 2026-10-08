package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paquete jugador -> servidor: "he pulsado ESPACIO dentro de un vehículo".
 *   Cohete: iniciar o cancelar la cuenta atrás (en el Espacio: cambiar de destino).
 *   Nave: encender o apagar los motores (Fase 14).
 */
public class RocketLaunchPacket {

    public RocketLaunchPacket() {
    }

    public RocketLaunchPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null && player.getVehicle() instanceof PGRocketEntity rocket) {
            rocket.toggleLaunch(player);
        } else if (player != null && player.getVehicle() instanceof PGShipEntity ship) {
            ship.toggleEngines(player);
        }
        context.get().setPacketHandled(true);
    }
}
