package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Efectos visuales de la pantalla: nieblas, color del cielo, temblor de cámara...
 * Es común: Forge (PGClientEvents, con sus eventos) y Fabric (mixins de fabric/mixin/client)
 * llaman a estos métodos.
 */
public final class PGClientVisuals {

    /**
     * Color de la niebla:
     *   - tormenta de polvo en Marte: rojiza;
     *   - subiendo con el cohete por encima de las nubes: cada vez más oscura (queda menos atmósfera).
     * Recibe el color normal (rojo, verde, azul) y lo cambia en el mismo array.
     */
    public static void adjustFogColor(float[] rgb) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float intensity = PGMarsWeather.getStormIntensity(minecraft.level);
        if (intensity > 0.0f) {
            rgb[0] += (0.72f - rgb[0]) * intensity;
            rgb[1] += (0.40f - rgb[1]) * intensity;
            rgb[2] += (0.24f - rgb[2]) * intensity;
        }
        if (minecraft.player != null && minecraft.level.dimension().equals(Level.OVERWORLD)
                && minecraft.player.getVehicle() instanceof PGSpaceVehicle) {
            double height = minecraft.player.getY();
            float darkness = (float) Math.max(0.0, Math.min(1.0,
                    (height - PGRocketEntity.CLOUD_HEIGHT) / (PGRocketEntity.ATMOSPHERE_TOP - PGRocketEntity.CLOUD_HEIGHT)));
            float keep = 1.0f - 0.9f * darkness;
            rgb[0] *= keep;
            rgb[1] *= keep;
            rgb[2] *= keep;
        }
    }

    /**
     * Distancia de la niebla: {cerca, lejos}, o null si no hay que cambiarla.
     *   - tormenta de polvo en Marte: solo se ve a unos 30 bloques;
     *   - Venus (Fase 21): atmósfera espesa y amarilla, solo se ve a unos 48 bloques.
     */
    public static float @Nullable [] fogDistances(float farPlaneDistance) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        if (minecraft.level.dimension().equals(PGPlanets.VENUS_LEVEL) && PGConfig.venusFog.get()) {
            return new float[]{2.0f, Math.min(farPlaneDistance, 48.0f)};
        }
        float intensity = PGMarsWeather.getStormIntensity(minecraft.level);
        if (intensity > 0.0f) {
            float far = farPlaneDistance + (28.0f - farPlaneDistance) * intensity;
            return new float[]{far * 0.05f, far};
        }
        return null;
    }

    /** La cámara tiembla con los motores encendidos: {cabeceo, giro} en grados, o null si no tiembla. */
    public static float @Nullable [] cameraShake(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !PGConfig.cameraShake.get()
                || !(minecraft.player.getVehicle() instanceof PGRocketEntity rocket)) {
            return null;
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
            return null;
        }
        float time = minecraft.player.tickCount + partialTick;
        return new float[]{(float) Math.sin(time * 2.7f) * strength, (float) Math.cos(time * 3.1f) * strength};
    }

    /** El astronauta va DENTRO del cohete o la nave: no se dibuja (si no, atravesaría las paredes). */
    public static boolean hideRider(Player player) {
        return player.getVehicle() instanceof PGSpaceVehicle;
    }

    private PGClientVisuals() {
    }
}
