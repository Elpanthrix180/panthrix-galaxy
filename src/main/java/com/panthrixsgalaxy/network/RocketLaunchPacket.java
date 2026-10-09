package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

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

    /** Al llegar al servidor (player = quien lo envía). */
    public void handleOnServer(@Nullable ServerPlayer player) {
        if (player != null && player.getVehicle() instanceof PGRocketEntity rocket) {
            rocket.toggleLaunch(player);
        } else if (player != null && player.getVehicle() instanceof PGShipEntity ship) {
            ship.toggleEngines(player);
        }
    }
}
