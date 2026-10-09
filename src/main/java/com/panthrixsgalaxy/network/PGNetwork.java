package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Canal de comunicación entre el servidor y la pantalla del jugador.
 *
 * ¿Por qué hace falta? El servidor es quien sabe cuánto oxígeno queda y si hay aire.
 * La pantalla (el "cliente") necesita esa información para dibujar el indicador.
 * Un "paquete" es un pequeño mensaje con esos datos.
 *
 * VERSIÓN FORGE. La versión Fabric (fabric/src/...) tiene los mismos métodos públicos:
 * sendToPlayer, sendToTrackingAndSelf y sendToServer.
 */
public final class PGNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    /** Registra todos los tipos de paquete. Cada uno con un número distinto. */
    public static void register() {
        int id = 0;
        // Servidor -> jugador (lo de la pantalla solo se ejecuta en el cliente)
        CHANNEL.messageBuilder(OxygenSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OxygenSyncPacket::encode)
                .decoder(OxygenSyncPacket::new)
                .consumerMainThread((packet, context) ->
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> packet::handleOnClient))
                .add();
        CHANNEL.messageBuilder(BackpackSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BackpackSyncPacket::encode)
                .decoder(BackpackSyncPacket::new)
                .consumerMainThread((packet, context) ->
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> packet::handleOnClient))
                .add();
        // Jugador -> servidor
        CHANNEL.messageBuilder(RocketLaunchPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RocketLaunchPacket::encode)
                .decoder(RocketLaunchPacket::new)
                .consumerMainThread((packet, context) -> packet.handleOnServer(context.get().getSender()))
                .add();
        CHANNEL.messageBuilder(BackpackActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BackpackActionPacket::encode)
                .decoder(BackpackActionPacket::new)
                .consumerMainThread((packet, context) -> packet.handleOnServer(context.get().getSender()))
                .add();
        CHANNEL.messageBuilder(ShipActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ShipActionPacket::encode)
                .decoder(ShipActionPacket::new)
                .consumerMainThread((packet, context) -> packet.handleOnServer(context.get().getSender()))
                .add();
    }

    /** Envía un paquete del servidor a un jugador concreto. */
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /** Envía un paquete del servidor al jugador y a todos los que lo ven. */
    public static void sendToTrackingAndSelf(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    /** Envía un paquete de la pantalla del jugador al servidor. */
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    private PGNetwork() {
    }
}
