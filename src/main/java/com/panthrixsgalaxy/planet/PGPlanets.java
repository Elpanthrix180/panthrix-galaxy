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
 *   Saturno (-2800,-3000)                         Xenoria (5000,-4500) 👽
 *                                    Júpiter (2600,-2600)
 *   Asteroides (-1000,-1300) ∴∵∴          Marte (1400,-900)
 *                   Luna (0,-600) ●
 *                        llegada ← la Tierra está DEBAJO (baja para volver)
 *   Mercurio (-1600,400)                           Urano (3800,1200)
 *        Venus (-900,800)
 *   Neptuno (-4200,1800)            Plutón (1200,4200)
 *
 * Para añadir un planeta: crea su línea aquí y añádelo a ALL (Fases 12, 13, 20 y 21).
 */
public final class PGPlanets {

    /** La dimensión Espacio (data/panthrixsgalaxy/dimension/space.json). */
    public static final ResourceKey<Level> SPACE = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "space"));

    /** La dimensión Luna (data/panthrixsgalaxy/dimension/moon.json). */
    public static final ResourceKey<Level> MOON_LEVEL = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "moon"));

    /** La dimensión Marte (data/panthrixsgalaxy/dimension/mars.json). */
    public static final ResourceKey<Level> MARS_LEVEL = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "mars"));

    /** El cinturón de asteroides (Fase 20): data/panthrixsgalaxy/dimension/asteroids.json. */
    public static final ResourceKey<Level> ASTEROIDS_LEVEL = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "asteroids"));

    // ===== Planetas adicionales (Fase 21) =====
    public static final ResourceKey<Level> MERCURY_LEVEL = dimension("mercury");
    public static final ResourceKey<Level> VENUS_LEVEL = dimension("venus");
    public static final ResourceKey<Level> PLUTO_LEVEL = dimension("pluto");
    public static final ResourceKey<Level> XENORIA_LEVEL = dimension("xenoria");

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
    /** Marte (Fase 13): gravedad 0,38, sin aire, tormentas de polvo. Necesita el cohete avanzado. */
    public static final PGPlanet MARS = new PGPlanet("mars", MARS_LEVEL, 2,
            new Vec3(1400, 0, -900), 110.0f, 150.0f, 0.38, false, 350, texture("mars"));

    /**
     * Cinturón de asteroides (Fase 20): rocas flotando en el vacío, casi sin gravedad (0,08).
     * No hay suelo: solo se llega con la NAVE (alcance 3). Se sale al Espacio subiendo por encima de Y 300.
     */
    public static final PGPlanet ASTEROIDS = new PGPlanet("asteroids", ASTEROIDS_LEVEL, 3,
            new Vec3(-1000, 0, -1300), 90.0f, 130.0f, 0.08, false, 300, texture("asteroids"));

    // ===== Fase 21: planetas exteriores y alienígenas (necesitan la NAVE AVANZADA) =====

    /** Mercurio: el más cercano al Sol. Gris, lleno de cráteres, siempre de día y muy caliente. Osmio. */
    public static final PGPlanet MERCURY = new PGPlanet("mercury", MERCURY_LEVEL, 4,
            new Vec3(-1600, 0, 400), 50.0f, 80.0f, 0.38, false, 300, texture("mercury"));
    /** Venus: atmósfera amarilla y espesa (niebla), lagos de lava, gravedad casi como la Tierra. Astralita. */
    public static final PGPlanet VENUS = new PGPlanet("venus", VENUS_LEVEL, 4,
            new Vec3(-900, 0, 800), 100.0f, 140.0f, 0.9, false, 300, texture("venus"));
    /** Gigantes gaseosos: se ven desde el Espacio pero NO tienen suelo (no se puede aterrizar). */
    public static final PGPlanet JUPITER = new PGPlanet("jupiter", null, 5,
            new Vec3(2600, 0, -2600), 600.0f, 700.0f, 2.5, false, 300, texture("jupiter"));
    public static final PGPlanet SATURN = new PGPlanet("saturn", null, 5,
            new Vec3(-2800, 0, -3000), 550.0f, 650.0f, 1.1, false, 300, texture("saturn"));
    public static final PGPlanet URANUS = new PGPlanet("uranus", null, 5,
            new Vec3(3800, 0, 1200), 300.0f, 380.0f, 0.9, false, 300, texture("uranus"));
    public static final PGPlanet NEPTUNE = new PGPlanet("neptune", null, 5,
            new Vec3(-4200, 0, 1800), 300.0f, 380.0f, 1.1, false, 300, texture("neptune"));
    /** Plutón: un mundo helado y oscuro, casi sin gravedad. Helio-3. */
    public static final PGPlanet PLUTO = new PGPlanet("pluto", PLUTO_LEVEL, 5,
            new Vec3(1200, 0, 4200), 40.0f, 70.0f, 0.06, false, 300, texture("pluto"));
    /** XENORIA: el planeta alienígena. Cielo morado, xenita, cristal cósmico, aliens... y la colmena de la Reina. */
    public static final PGPlanet XENORIA = new PGPlanet("xenoria", XENORIA_LEVEL, 5,
            new Vec3(5000, 0, -4500), 120.0f, 170.0f, 0.7, false, 320, texture("xenoria"));

    /** Todos los cuerpos celestes, en el orden en que se eligen como destino. */
    public static final List<PGPlanet> ALL = List.of(EARTH, MOON, MARS, ASTEROIDS,
            MERCURY, VENUS, JUPITER, SATURN, URANUS, NEPTUNE, PLUTO, XENORIA);

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

    private static ResourceKey<Level> dimension(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PanthrixsGalaxy.MOD_ID, name));
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/environment/" + name + ".png");
    }

    private PGPlanets() {
    }
}
