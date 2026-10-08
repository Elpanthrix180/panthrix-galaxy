package com.panthrixsgalaxy.system.gravity;

import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.world.level.Level;

/**
 * Gravedad de cada planeta.
 *
 * VERSIÓN FABRIC (de momento solo el valor; aplicarla a las criaturas llega en la Fase F3).
 * La versión Forge (src/main/java/...) usa el atributo de gravedad de Forge.
 */
public final class PGGravity {

    /** Gravedad de la dimensión (1.0 si no es un planeta del mod). */
    public static double getGravity(Level level) {
        PGPlanet planet = PGPlanets.fromDimension(level.dimension());
        return planet == null ? 1.0 : planet.gravity();
    }

    private PGGravity() {
    }
}
