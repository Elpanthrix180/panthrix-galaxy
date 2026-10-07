package com.panthrixsgalaxy.world.feature;

import com.mojang.serialization.Codec;
import com.panthrixsgalaxy.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Cráter de impacto: un cuenco excavado en el suelo con un borde un poco elevado.
 *
 *        ▄▄▄                 ▄▄▄      <- borde (regolito amontonado)
 *   ▀▀▀▀▀   ▀▄▄           ▄▄▀   ▀▀▀▀▀ <- suelo
 *               ▀▀▄▄▄▄▄▀▀             <- fondo del cráter (regolito)
 *
 * 70 % pequeños (radio 3-6) y 30 % grandes (radio 8-12).
 * Se usa en la Luna y servirá también para Marte y los asteroides.
 */
public class PGCraterFeature extends Feature<NoneFeatureConfiguration> {

    public PGCraterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockState surface = ModBlocks.PG_LUNAR_REGOLITH.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int radius = random.nextFloat() < 0.7f ? 3 + random.nextInt(4) : 8 + random.nextInt(5);
        int rim = 2;
        int centerY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        double depth = radius * 0.45;

        for (int dx = -radius - rim; dx <= radius + rim; dx++) {
            for (int dz = -radius - rim; dz <= radius + rim; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (distance <= radius) {
                    // Cuenco: más hondo en el centro
                    double t = distance / radius;
                    int bottom = centerY - 1 - (int) Math.round(depth * (1.0 - t * t));
                    for (int y = top; y > bottom; y--) {
                        level.setBlock(new BlockPos(x, y, z), air, 2);
                    }
                    level.setBlock(new BlockPos(x, bottom, z), surface, 2);
                } else if (distance <= radius + rim && random.nextFloat() < 0.8f) {
                    // Borde: regolito amontonado
                    int height = distance < radius + 1 ? 2 : 1;
                    for (int y = 1; y <= height; y++) {
                        level.setBlock(new BlockPos(x, top + y, z), surface, 2);
                    }
                }
            }
        }
        return true;
    }
}
