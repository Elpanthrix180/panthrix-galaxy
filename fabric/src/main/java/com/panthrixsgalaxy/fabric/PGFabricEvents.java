package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.command.PGCommands;
import com.panthrixsgalaxy.command.PGTestCommands;
import com.panthrixsgalaxy.event.PGGuideBookEvents;
import com.panthrixsgalaxy.event.PGSuitEvents;
import com.panthrixsgalaxy.platform.PGPlatform;
import com.panthrixsgalaxy.system.PGSmokeTest;
import com.panthrixsgalaxy.system.backpack.PGBackpackEvents;
import com.panthrixsgalaxy.system.oxygen.PGOxygenEvents;
import com.panthrixsgalaxy.system.oxygen.PGSealedRooms;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Solo Fabric: conecta los eventos de Fabric con la lógica común del mod.
 * (En Forge hace lo mismo forge/PGForgeEvents con los eventos de Forge.)
 */
public final class PGFabricEvents {

    /** Al final de cada tick de un jugador (lo llama el mixin PlayerMixin, en el servidor y en la pantalla). */
    public static void onPlayerTickEnd(Player player) {
        PGMarsWeather.onPlayerTickEnd(player);
        PGOxygenEvents.onPlayerTickEnd(player);
    }

    public static void register() {
        // ===== Servidor =====
        ServerLifecycleEvents.SERVER_STARTED.register(PGSmokeTest::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PGSealedRooms.clearAll());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PGCommands.register(dispatcher);
            PGTestCommands.register(dispatcher);
            PGGuideBookEvents.register(dispatcher);
        });

        // ===== Entrar, salir, reaparecer =====
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            PGGuideBookEvents.onPlayerLogin(listener.player);
            PGBackpackEvents.sync(listener.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> PGOxygenEvents.onLogout(listener.player));

        // Al reaparecer (o volver del End) Minecraft crea un jugador nuevo: se copian sus datos del mod
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CompoundTag newData = PGPlatform.getPersistentData(newPlayer);
            if (alive) {
                newData.merge(PGPlatform.getPersistentData(oldPlayer).copy());
            } else {
                newData.put(PGPlayerData.PERSISTED_TAG, PGPlatform.getPersistedData(oldPlayer).copy());
            }
            PGBackpackEvents.onPlayerClone(oldPlayer, newPlayer, !alive);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PGBackpackEvents.sync(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                PGBackpackEvents.sync(player));
        EntityTrackingEvents.START_TRACKING.register(PGBackpackEvents::onStartTracking);

        // ===== Traje =====
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) ->
                PGSuitEvents.onEquipmentChange(entity, slot, current));
    }

    private PGFabricEvents() {
    }
}
