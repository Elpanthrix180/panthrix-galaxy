package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
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

    /** El astronauta va DENTRO del cohete o la nave: no se dibuja (si no, atravesaría las paredes). */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof PGSpaceVehicle) {
            event.setCanceled(true);
        }
    }

    /** Tormenta de polvo: la niebla se vuelve rojiza. */
    @SubscribeEvent
    public static void onMarsFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float intensity = PGMarsWeather.getStormIntensity(minecraft.level);
        if (intensity <= 0.0f) {
            return;
        }
        event.setRed(event.getRed() + (0.72f - event.getRed()) * intensity);
        event.setGreen(event.getGreen() + (0.40f - event.getGreen()) * intensity);
        event.setBlue(event.getBlue() + (0.24f - event.getBlue()) * intensity);
    }

    /** Tormenta de polvo: solo se ve a unos 30 bloques. */
    @SubscribeEvent
    public static void onMarsFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float intensity = PGMarsWeather.getStormIntensity(minecraft.level);
        if (intensity <= 0.0f) {
            return;
        }
        float far = event.getFarPlaneDistance() + (28.0f - event.getFarPlaneDistance()) * intensity;
        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(far * 0.05f);
        event.setCanceled(true); // necesario para que Minecraft use nuestros valores
    }

    /** Venus (Fase 21): atmósfera espesa y amarilla, solo se ve a unos 48 bloques. */
    @SubscribeEvent
    public static void onVenusFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().equals(PGPlanets.VENUS_LEVEL)
                || !PGConfig.venusFog.get()) {
            return;
        }
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 48.0f));
        event.setNearPlaneDistance(2.0f);
        event.setCanceled(true);
    }

    /**
     * Al subir con el cohete por encima de las nubes, el horizonte se oscurece poco a poco:
     * cada vez queda menos atmósfera entre nosotros y el espacio negro.
     */
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || !minecraft.level.dimension().equals(Level.OVERWORLD)
                || !(minecraft.player.getVehicle() instanceof PGSpaceVehicle)) {
            return;
        }
        double height = minecraft.player.getY();
        float darkness = (float) Math.max(0.0, Math.min(1.0,
                (height - PGRocketEntity.CLOUD_HEIGHT) / (PGRocketEntity.ATMOSPHERE_TOP - PGRocketEntity.CLOUD_HEIGHT)));
        float keep = 1.0f - 0.9f * darkness;
        event.setRed(event.getRed() * keep);
        event.setGreen(event.getGreen() * keep);
        event.setBlue(event.getBlue() * keep);
    }

    /** La cámara tiembla con los motores encendidos. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !PGConfig.cameraShake.get()
                || !(minecraft.player.getVehicle() instanceof PGRocketEntity rocket)) {
            return;
        }
        LaunchState state = rocket.getLaunchState();
        float strength;
        if (state == LaunchState.COUNTDOWN && rocket.getTimer() <= PGRocketEntity.IGNITION_TICKS) {
            strength = 0.4f * (1.0f - rocket.getTimer() / (float) PGRocketEntity.IGNITION_TICKS);
        } else if (state == LaunchState.ASCENDING) {
            strength = rocket.getTimer() < 60 ? 1.2f : 0.5f; // más fuerte justo al despegar
        } else if (state == LaunchState.FALLING) {
            strength = 0.8f;
        } else {
            return;
        }
        float time = (float) (minecraft.player.tickCount + event.getPartialTick());
        event.setPitch(event.getPitch() + (float) Math.sin(time * 2.7f) * strength);
        event.setRoll(event.getRoll() + (float) Math.cos(time * 3.1f) * strength);
    }

    private PGClientEvents() {
    }
}
