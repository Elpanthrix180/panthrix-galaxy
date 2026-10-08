package com.panthrixsgalaxy.forge;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.system.oxygen.PGSealedRooms;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraftforge.event.TickEvent;
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

    private PGForgeEvents() {
    }
}
