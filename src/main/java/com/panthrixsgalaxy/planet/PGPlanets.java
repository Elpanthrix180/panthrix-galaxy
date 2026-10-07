package com.panthrixsgalaxy.planet;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Lista de cuerpos celestes del mod y la dimensión Espacio.
 *
 * Mapa del Espacio (vista desde arriba, "llegada" = donde apareces al salir de la Tierra):
 *
 *            Marte (1400, 0, -900)
 *                    ●
 *     Luna (0, 0, -600)
 *          ●
 *          |
 *       llegada ← la Tierra está DEBAJO (baja para volver)
 *
 * Para añadir un planeta: crea su línea aquí y añádelo a ALL (Fases 12, 13 y 21).
 */
public final class PGPlanets {

    /** La dimensión Espacio (data/panthrixsgalaxy/dimension/space.json). */
    public static final ResourceKey<Level> SPACE = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "space"));

    /** La dimensión Luna (data/panthrixsgalaxy/dimension/moon.json). */
    public static final ResourceKey<Level> MOON_LEVEL = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "moon"));

    /** Altura a la que se aparece en el Espacio. */
    public static final int SPACE_ARRIVAL_Y = 100;
    /** Si el cohete baja de esta altura en el Espacio, vuelve a entrar en la atmósfera de la Tierra. */
    public static final int EARTH_REENTRY_Y = 40;
    /** Techo del Espacio (no se puede subir más). */
    public static final int SPACE_CEILING_Y = 300;

    public static final PGPlanet EARTH = new PGPlanet("earth", Level.OVERWORLD, 0,
            new Vec3(0, -2400, 0), 2000.0f, 0.0f, 1.0, true, 450, texture("earth"));
    /** La Luna (Fase 12): gravedad 0,17, sin aire. Sale al Espacio a Y 250. */
    public static final PGPlanet MOON = new PGPlanet("moon", MOON_LEVEL, 1,
            new Vec3(0, 0, -600), 60.0f, 90.0f, 0.17, false, 250, texture("moon"));
    /** Marte: su dimensión llegará en la Fase 13. */
    public static final PGPlanet MARS = new PGPlanet("mars", null, 2,
            new Vec3(1400, 0, -900), 110.0f, 150.0f, 0.38, false, 350, texture("mars"));

    /** Todos los cuerpos celestes, en el orden en que se eligen como destino. */
    public static final List<PGPlanet> ALL = List.of(EARTH, MOON, MARS);

    public static PGPlanet byIndex(int index) {
        return ALL.get(Math.floorMod(index, ALL.size()));
    }

    /** El planeta al que pertenece una dimensión (null si no es ninguno, por ejemplo el Espacio). */
    @Nullable
    public static PGPlanet fromDimension(ResourceKey<Level> dimension) {
        for (PGPlanet planet : ALL) {
            if (dimension.equals(planet.dimension())) {
                return planet;
            }
        }
        return null;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/environment/" + name + ".png");
    }

    private PGPlanets() {
    }
}
