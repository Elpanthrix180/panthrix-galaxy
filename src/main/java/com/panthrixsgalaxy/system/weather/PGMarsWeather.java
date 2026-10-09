package com.panthrixsgalaxy.system.weather;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Tormentas de polvo de Marte.
 *
 * Cada 15 minutos hay una tormenta de 3 minutos (empieza y acaba poco a poco).
 * Se calcula a partir de la hora del día del mundo, así el servidor y todos los jugadores
 * saben a la vez si hay tormenta sin tener que enviarse mensajes.
 * Para probarla: /time add 18000 (o hasta que salga el aviso).
 *
 * Durante la tormenta:
 *   - El viento empuja a los jugadores que están al aire libre.
 *   - Los paneles solares producen menos (PGSolarPanelBlockEntity).
 *   - En la pantalla: polvo rojo, niebla y poca visibilidad (client/PGClientEvents).
 */
public final class PGMarsWeather {

    /** Duración de un ciclo completo: 15 minutos (18 000 ticks). */
    private static final long CYCLE_TICKS = 18_000L;
    /** Duración de la tormenta: 3 minutos. */
    private static final long STORM_TICKS = 3_600L;
    /** Tiempo que tarda en llegar a su máxima fuerza (y en irse): 20 segundos. */
    private static final long FADE_TICKS = 400L;
    /** Fuerza del viento (bloques por tick que empuja cada tick). */
    private static final double WIND_STRENGTH = 0.012;

    /**
     * Intensidad de la tormenta: 0 = no hay tormenta, 1 = tormenta máxima.
     * Fuera de Marte siempre es 0.
     */
    public static float getStormIntensity(Level level) {
        if (!level.dimension().equals(PGPlanets.MARS_LEVEL) || !PGConfig.marsDustStorms.get()) {
            return 0.0f;
        }
        long time = Math.floorMod(level.getDayTime(), CYCLE_TICKS);
        if (time >= STORM_TICKS) {
            return 0.0f;
        }
        float fadeIn = Math.min(1.0f, time / (float) FADE_TICKS);
        float fadeOut = Math.min(1.0f, (STORM_TICKS - time) / (float) FADE_TICKS);
        return Math.min(fadeIn, fadeOut);
    }

    public static boolean isStorm(Level level) {
        return getStormIntensity(level) > 0.0f;
    }

    /**
     * Dirección del viento (cambia lentamente con el tiempo), en radianes.
     */
    public static double getWindAngle(Level level) {
        return Math.floorDiv(level.getDayTime(), CYCLE_TICKS) * 1.7;
    }

    /**
     * El viento empuja a los jugadores al aire libre.
     * Se hace en el servidor y en la pantalla del jugador (porque el movimiento propio lo calcula su juego).
     * Lo llaman Forge y Fabric al final de cada tick del jugador.
     */
    public static void onPlayerTickEnd(Player player) {
        if (player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying) {
            return;
        }
        float intensity = getStormIntensity(player.level());
        if (intensity <= 0.0f || !player.level().canSeeSky(player.blockPosition().above())) {
            return;
        }
        double angle = getWindAngle(player.level());
        double strength = WIND_STRENGTH * intensity;
        player.setDeltaMovement(player.getDeltaMovement().add(Math.cos(angle) * strength, 0.0, Math.sin(angle) * strength));
    }

    private PGMarsWeather() {
    }
}
