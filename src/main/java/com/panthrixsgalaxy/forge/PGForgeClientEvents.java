package com.panthrixsgalaxy.forge;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.OxygenHudOverlay;
import com.panthrixsgalaxy.client.RocketHudOverlay;
import com.panthrixsgalaxy.client.ShipHudOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Solo Forge (pantalla): registra los indicadores comunes del mod como overlays de Forge. */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PGForgeClientEvents {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("oxygen", (gui, graphics, partialTick, width, height) ->
                OxygenHudOverlay.render(graphics, partialTick, width, height));
        event.registerAboveAll("rocket", (gui, graphics, partialTick, width, height) ->
                RocketHudOverlay.render(graphics, partialTick, width, height));
        event.registerAboveAll("ship", (gui, graphics, partialTick, width, height) ->
                ShipHudOverlay.render(graphics, partialTick, width, height));
    }

    private PGForgeClientEvents() {
    }
}
