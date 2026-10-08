package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Canal de comunicación entre el servidor y la pantalla del jugador.
 *
 * VERSIÓN FABRIC. La versión Forge (src/main/java/...) tiene los mismos métodos públicos:
 * sendToPlayer, sendToTrackingAndSelf y sendToServer.
 */
public final class PGNetwork {

    /** Un tipo de paquete: su nombre, cómo se escribe y cómo se lee. */
    public record PacketType<P>(ResourceLocation id, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder,
                                Function<FriendlyByteBuf, P> decoder) {
        @SuppressWarnings("unchecked")
        void write(Object packet, FriendlyByteBuf buf) {
            encoder.accept((P) packet, buf);
        }
    }

    private static final Map<Class<?>, PacketType<?>> TYPES = new HashMap<>();

    /** Lo pone la parte de pantalla del mod (en un servidor dedicado no existe). */
    @Nullable
    private static BiConsumer<ResourceLocation, FriendlyByteBuf> clientSender;

    /** Registra todos los tipos de paquete. */
    public static void register() {
        // Servidor -> jugador
        toClient("oxygen_sync", OxygenSyncPacket.class, OxygenSyncPacket::encode, OxygenSyncPacket::new);
        toClient("backpack_sync", BackpackSyncPacket.class, BackpackSyncPacket::encode, BackpackSyncPacket::new);
        // Jugador -> servidor
        toServer("rocket_launch", RocketLaunchPacket.class, RocketLaunchPacket::encode, RocketLaunchPacket::new,
                RocketLaunchPacket::handleOnServer);
        toServer("backpack_action", BackpackActionPacket.class, BackpackActionPacket::encode, BackpackActionPacket::new,
                BackpackActionPacket::handleOnServer);
        toServer("ship_action", ShipActionPacket.class, ShipActionPacket::encode, ShipActionPacket::new,
                ShipActionPacket::handleOnServer);
    }

    private static <P> void toClient(String name, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, P> decoder) {
        PacketType<P> packetType = new PacketType<>(new ResourceLocation(PanthrixsGalaxy.MOD_ID, name), type, encoder, decoder);
        TYPES.put(type, packetType); // los recibe la parte de pantalla: fabric/client/PGFabricClient
    }

    private static <P> void toServer(String name, Class<P> type, BiConsumer<P, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> handler) {
        PacketType<P> packetType = new PacketType<>(new ResourceLocation(PanthrixsGalaxy.MOD_ID, name), type, encoder, decoder);
        TYPES.put(type, packetType);
        ServerPlayNetworking.registerGlobalReceiver(packetType.id(), (server, player, listener, buf, sender) -> {
            P packet = decoder.apply(buf);
            server.execute(() -> handler.accept(packet, player));
        });
    }

    private static <P> PacketType<?> typeOf(Object packet) {
        PacketType<?> type = TYPES.get(packet.getClass());
        if (type == null) {
            throw new IllegalArgumentException("Paquete sin registrar: " + packet.getClass().getName());
        }
        return type;
    }

    private static FriendlyByteBuf write(PacketType<?> type, Object packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        type.write(packet, buf);
        return buf;
    }

    /** Envía un paquete del servidor a un jugador concreto. */
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        PacketType<?> type = typeOf(packet);
        ServerPlayNetworking.send(player, type.id(), write(type, packet));
    }

    /** Envía un paquete del servidor al jugador y a todos los que lo ven. */
    public static void sendToTrackingAndSelf(ServerPlayer player, Object packet) {
        sendToPlayer(player, packet);
        for (ServerPlayer watcher : PlayerLookup.tracking(player)) {
            if (watcher != player) {
                sendToPlayer(watcher, packet);
            }
        }
    }

    /** Envía un paquete de la pantalla del jugador al servidor. */
    public static void sendToServer(Object packet) {
        if (clientSender != null) {
            PacketType<?> type = typeOf(packet);
            clientSender.accept(type.id(), write(type, packet));
        }
    }

    /** Solo la parte de pantalla del mod: cómo enviar al servidor. */
    public static void setClientSender(BiConsumer<ResourceLocation, FriendlyByteBuf> sender) {
        clientSender = sender;
    }

    private PGNetwork() {
    }
}
