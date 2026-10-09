package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos que ocurren en la pantalla del jugador durante la partida (teclas pulsadas...). */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, value = Dist.CLIENT)
public final class PGClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PGClientTick.onClientTickEnd(Minecraft.getInstance());
        }
    }

    /** El astronauta va DENTRO del cohete o la nave: no se dibuja. */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (PGClientVisuals.hideRider(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** Color de la niebla (tormentas de Marte, horizonte que se oscurece al subir). */
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float[] rgb = {event.getRed(), event.getGreen(), event.getBlue()};
        PGClientVisuals.adjustFogColor(rgb);
        event.setRed(rgb[0]);
        event.setGreen(rgb[1]);
        event.setBlue(rgb[2]);
    }

    /** Distancia de la niebla (tormentas de Marte, Venus). */
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float[] distances = PGClientVisuals.fogDistances(event.getFarPlaneDistance());
        if (distances != null) {
            event.setNearPlaneDistance(distances[0]);
            event.setFarPlaneDistance(distances[1]);
            event.setCanceled(true); // necesario para que Minecraft use nuestros valores
        }
    }

    /** La cámara tiembla con los motores encendidos. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float[] shake = PGClientVisuals.cameraShake((float) event.getPartialTick());
        if (shake != null) {
            event.setPitch(event.getPitch() + shake[0]);
            event.setRoll(event.getRoll() + shake[1]);
        }
    }

    private PGClientEvents() {
    }
}
