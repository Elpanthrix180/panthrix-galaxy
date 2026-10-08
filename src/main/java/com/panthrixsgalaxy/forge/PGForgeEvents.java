package com.panthrixsgalaxy.forge;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.command.PGCommands;
import com.panthrixsgalaxy.command.PGTestCommands;
import com.panthrixsgalaxy.event.PGGuideBookEvents;
import com.panthrixsgalaxy.system.oxygen.PGSealedRooms;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Solo Forge: conecta los eventos de Forge con la lógica común del mod.
 * (En Fabric hace lo mismo fabric/.../PGFabricEvents con los eventos de Fabric.)
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGForgeEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PGMarsWeather.onPlayerTickEnd(event.player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PGSealedRooms.clearAll();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PGCommands.register(event.getDispatcher());
        PGTestCommands.register(event.getDispatcher());
        PGGuideBookEvents.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGGuideBookEvents.onPlayerLogin(player);
        }
    }

    private PGForgeEvents() {
    }
}
