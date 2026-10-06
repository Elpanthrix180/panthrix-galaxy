package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
 * Lo usaremos también para cohetes, máquinas y energía.
 */
public final class PGNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    /** Registra todos los tipos de paquete. Cada uno con un número distinto. */
    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OxygenSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OxygenSyncPacket::encode)
                .decoder(OxygenSyncPacket::new)
                .consumerMainThread(OxygenSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(BackpackSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BackpackSyncPacket::encode)
                .decoder(BackpackSyncPacket::new)
                .consumerMainThread(BackpackSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(BackpackActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BackpackActionPacket::encode)
                .decoder(BackpackActionPacket::new)
                .consumerMainThread(BackpackActionPacket::handle)
                .add();
    }

    /** Envía un paquete del servidor a un jugador concreto. */
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private PGNetwork() {
    }
}
