package com.panthrixsgalaxy.world.feature;

import com.mojang.serialization.Codec;
import com.panthrixsgalaxy.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Un asteroide flotando en el vacío: una "patata" de roca de asteroide (radio 3-9)
 * con minerales repartidos por dentro.
 *
 *   Roca normal   ~83 %      Mineral de metal de asteroide 8 %   Meteorito 4 %
 *   Osmio 3 %                Astralita 1,5 %
 *
 * Algunos son especiales:
 *   - 15 % tienen un NÚCLEO DE HIELO (agua para el recargador eléctrico);
 *   - 6 % tienen un NÚCLEO DE OSMIO (¡muchísimo osmio en el centro!).
 */
public class PGAsteroidFeature extends Feature<NoneFeatureConfiguration> {

    public PGAsteroidFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin();

        int radius = 3 + random.nextInt(7);
        // Forma irregular: cada eje un poco distinto
        float sx = 0.75f + random.nextFloat() * 0.5f;
        float sy = 0.6f + random.nextFloat() * 0.5f;
        float sz = 0.75f + random.nextFloat() * 0.5f;
        float roll = random.nextFloat();
        Core core = roll < 0.06f ? Core.OSMIUM : roll < 0.21f ? Core.ICE : Core.NONE;
        float coreRadius = radius * 0.4f;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double dx = x / (radius * sx);
                    double dy = y / (radius * sy);
                    double dz = z / (radius * sz);
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    // Bordes rugosos: un poco de azar en la superficie
                    if (distance > 1.0 - random.nextFloat() * 0.15) {
                        continue;
                    }
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).isAir()) {
                        continue;
                    }
                    double realDistance = Math.sqrt(x * x + y * y + z * z);
                    level.setBlock(pos, pick(random, core, realDistance < coreRadius), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private enum Core { NONE, ICE, OSMIUM }

    private static BlockState pick(RandomSource random, Core core, boolean inCore) {
        if (inCore && core == Core.ICE) {
            return Blocks.ICE.defaultBlockState();
        }
        if (inCore && core == Core.OSMIUM) {
            return random.nextFloat() < 0.6f ? ModBlocks.PG_OSMIUM_ORE.get().defaultBlockState()
                    : ModBlocks.PG_ASTEROID_METAL_ORE.get().defaultBlockState();
        }
        float roll = random.nextFloat();
        if (roll < 0.015f) {
            return ModBlocks.PG_ASTRALITE_ORE.get().defaultBlockState();
        } else if (roll < 0.045f) {
            return ModBlocks.PG_OSMIUM_ORE.get().defaultBlockState();
        } else if (roll < 0.085f) {
            return ModBlocks.PG_METEORITE_ORE.get().defaultBlockState();
        } else if (roll < 0.165f) {
            return ModBlocks.PG_ASTEROID_METAL_ORE.get().defaultBlockState();
        }
        return ModBlocks.PG_ASTEROID_ROCK.get().defaultBlockState();
    }
}
