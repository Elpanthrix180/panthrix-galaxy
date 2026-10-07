package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.world.feature.PGCraterFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * "Features" de generación del mundo hechos en Java (lo que no se puede hacer solo con JSON).
 * Dónde y cuántas veces aparecen se decide en data/panthrixsgalaxy/worldgen/placed_feature/.
 */
public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, PanthrixsGalaxy.MOD_ID);

    /** Cráter de impacto ("type": "panthrixsgalaxy:crater"). */
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRATER = FEATURES.register("crater",
            () -> new PGCraterFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
