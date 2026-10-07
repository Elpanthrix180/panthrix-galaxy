package com.panthrixsgalaxy.world.feature;

import com.mojang.serialization.Codec;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * BASE LUNAR ABANDONADA: los restos de una base de 9 x 4 x 9 medio derrumbada (paredes con agujeros,
 * sin techo en algunas partes), con un cofre (loot_tables/chests/lunar_ruin.json).
 *
 * Una de cada 8 bases esconde... el MONOLITO (logro secreto "El lado oscuro").
 */
public class PGLunarRuinFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation LOOT = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "chests/lunar_ruin");
    private static final int HALF = 4;

    public PGLunarRuinFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos floor = context.origin().below();
        BlockState panel = ModBlocks.PG_STATION_PANEL.get().defaultBlockState();
        BlockState hazard = ModBlocks.PG_STATION_HAZARD_PANEL.get().defaultBlockState();
        BlockState floorBlock = ModBlocks.PG_STATION_FLOOR.get().defaultBlockState();
        BlockState roof = ModBlocks.PG_STATION_ROOF.get().defaultBlockState();
        BlockState regolith = ModBlocks.PG_LUNAR_REGOLITH.get().defaultBlockState();

        for (int x = -HALF; x <= HALF; x++) {
            for (int z = -HALF; z <= HALF; z++) {
                level.setBlock(floor.offset(x, 0, z), random.nextFloat() < 0.15f ? regolith : floorBlock, 2);
                boolean wall = Math.abs(x) == HALF || Math.abs(z) == HALF;
                // La altura de las paredes baja hacia un lado: se ha derrumbado
                int height = wall ? Math.max(0, 3 - random.nextInt(3) - (x > 1 ? 1 : 0)) : 0;
                for (int y = 1; y <= height; y++) {
                    if (random.nextFloat() < 0.2f) {
                        continue;
                    }
                    level.setBlock(floor.offset(x, y, z), Math.abs(x) == HALF && Math.abs(z) == HALF ? hazard : panel, 2);
                }
                for (int y = 1; y <= 3; y++) {
                    if (!wall) {
                        level.setBlock(floor.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                if (!wall && x < 0 && random.nextFloat() < 0.6f) {
                    level.setBlock(floor.offset(x, 4, z), roof, 2); // trozo de techo que aguanta
                }
            }
        }
        BlockPos chest = floor.offset(-2, 1, -2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, chest, LOOT);
        // El secreto
        if (random.nextInt(8) == 0) {
            for (int y = 1; y <= 3; y++) {
                level.setBlock(floor.offset(1, y, 1), ModBlocks.PG_MONOLITH.get().defaultBlockState(), 2);
            }
        }
        return true;
    }
}
