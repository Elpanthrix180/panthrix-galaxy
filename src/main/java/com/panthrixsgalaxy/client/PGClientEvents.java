package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.network.BackpackActionPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import com.panthrixsgalaxy.network.RocketLaunchPacket;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** Eventos que ocurren en la pantalla del jugador durante la partida (teclas pulsadas...). */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, value = Dist.CLIENT)
public final class PGClientEvents {

    /** ¿Estaba pulsado ESPACIO en el tick anterior? (para detectar solo el momento de pulsar) */
    private static boolean jumpWasDown;
    /** Dimensión del tick anterior (para detectar el cambio y hacer el fundido a negro). */
    private static ResourceKey<Level> lastDimension;
    /** ¿Había tormenta de polvo en el tick anterior? (para avisar al empezar y acabar) */
    private static boolean lastStorm;
    /** Color del polvo marciano. */
    private static final DustParticleOptions MARS_DUST = new DustParticleOptions(new Vector3f(0.8f, 0.42f, 0.22f), 1.6f);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        // Fundido a negro al cambiar de dimensión (Tierra <-> Espacio)
        if (minecraft.level != null) {
            ResourceKey<Level> dimension = minecraft.level.dimension();
            if (lastDimension != null && !lastDimension.equals(dimension)) {
                RocketHudOverlay.startFade();
            }
            lastDimension = dimension;
        }
        RocketHudOverlay.tickFade();
        tickMarsStorm(minecraft);

        // ESPACIO dentro del cohete: iniciar / cancelar la cuenta atrás
        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (jumpDown && !jumpWasDown && minecraft.screen == null && minecraft.player != null
                && minecraft.player.getVehicle() instanceof PGSpaceVehicle) {
            PGNetwork.sendToServer(new RocketLaunchPacket());
        }
        jumpWasDown = jumpDown;

        while (PGKeyBindings.BACKPACK.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                BackpackActionPacket.Action action = Screen.hasShiftDown()
                        ? BackpackActionPacket.Action.UNEQUIP
                        : BackpackActionPacket.Action.OPEN;
                PGNetwork.sendToServer(new BackpackActionPacket(action));
            }
        }
    }

    /** El astronauta va DENTRO del cohete o la nave: no se dibuja (si no, atravesaría las paredes). */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof PGSpaceVehicle) {
            event.setCanceled(true);
        }
    }

    /** Tormenta de polvo: polvo rojo volando con el viento y avisos al empezar y acabar. */
    private static void tickMarsStorm(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        float intensity = PGMarsWeather.getStormIntensity(minecraft.level);
        boolean storm = intensity > 0.0f;
        if (storm != lastStorm && minecraft.level.dimension().equals(PGPlanets.MARS_LEVEL)) {
            minecraft.player.displayClientMessage(Component.translatable(storm
                    ? "message.panthrixsgalaxy.dust_storm_start"
                    : "message.panthrixsgalaxy.dust_storm_end").withStyle(storm ? ChatFormatting.GOLD : ChatFormatting.GREEN), true);
        }
        lastStorm = storm;
        if (!storm) {
            return;
        }
        double angle = PGMarsWeather.getWindAngle(minecraft.level);
        double windX = Math.cos(angle) * 0.6;
        double windZ = Math.sin(angle) * 0.6;
        int count = (int) (intensity * PGConfig.stormParticles.get());
        var random = minecraft.level.random;
        for (int i = 0; i < count; i++) {
            double x = minecraft.player.getX() + (random.nextDouble() - 0.5) * 24.0;
            double y = minecraft.player.getY() + random.nextDouble() * 8.0 - 1.0;
            double z = minecraft.player.getZ() + (random.nextDouble() - 0.5) * 24.0;
            minecraft.level.addParticle(MARS_DUST, x, y, z, windX, 0.0, windZ);
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
