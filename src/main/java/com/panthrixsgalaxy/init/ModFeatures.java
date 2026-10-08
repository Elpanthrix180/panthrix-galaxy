package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.platform.PGHolder;
import com.panthrixsgalaxy.platform.PGRegistry;
import com.panthrixsgalaxy.world.feature.PGAlienHiveFeature;
import com.panthrixsgalaxy.world.feature.PGAlienOutpostFeature;
import com.panthrixsgalaxy.world.feature.PGAlienVillageFeature;
import com.panthrixsgalaxy.world.feature.PGAsteroidFeature;
import com.panthrixsgalaxy.world.feature.PGCraterFeature;
import com.panthrixsgalaxy.world.feature.PGLunarRuinFeature;
import com.panthrixsgalaxy.world.feature.PGShipwreckFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * "Features" de generación del mundo hechos en Java (lo que no se puede hacer solo con JSON).
 * Dónde y cuántas veces aparecen se decide en data/panthrixsgalaxy/worldgen/placed_feature/.
 */
public final class ModFeatures {

    public static final PGRegistry<Feature<?>> FEATURES = PGRegistry.create(Registries.FEATURE);

    /** Cráter de impacto ("type": "panthrixsgalaxy:crater"). */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> CRATER = FEATURES.register("crater",
            () -> new PGCraterFeature(NoneFeatureConfiguration.CODEC));

    // ===== Cinturón de asteroides (Fase 20) =====

    /** Asteroide con minerales ("type": "panthrixsgalaxy:asteroid"). */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> ASTEROID = FEATURES.register("asteroid",
            () -> new PGAsteroidFeature(NoneFeatureConfiguration.CODEC));
    /** Restos de una nave con un cofre. */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> SHIPWRECK = FEATURES.register("shipwreck",
            () -> new PGShipwreckFeature(NoneFeatureConfiguration.CODEC));
    /** Puesto alienígena: asteroide hueco con tecnología y guardianes. */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> ALIEN_OUTPOST = FEATURES.register("alien_outpost",
            () -> new PGAlienOutpostFeature(NoneFeatureConfiguration.CODEC));

    // ===== Planetas adicionales (Fase 21) =====

    /** Colmena de la Reina alienígena (Xenoria). */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> ALIEN_HIVE = FEATURES.register("alien_hive",
            () -> new PGAlienHiveFeature(NoneFeatureConfiguration.CODEC));

    // ===== Secretos (Fase 22) =====

    /** Base lunar abandonada (a veces con el monolito). */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> LUNAR_RUIN = FEATURES.register("lunar_ruin",
            () -> new PGLunarRuinFeature(NoneFeatureConfiguration.CODEC));
    /** Aldea alienígena con el archivo alienígena (Xenoria). */
    public static final PGHolder<Feature<NoneFeatureConfiguration>> ALIEN_VILLAGE = FEATURES.register("alien_village",
            () -> new PGAlienVillageFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
