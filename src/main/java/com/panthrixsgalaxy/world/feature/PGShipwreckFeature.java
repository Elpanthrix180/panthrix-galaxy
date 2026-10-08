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
 * RESTOS DE UNA NAVE: el casco roto de una nave abandonada (11 x 5 x 5), con agujeros,
 * un motor apagado en la cola, trozos de chatarra flotando alrededor y un COFRE con botín
 * (loot_tables/chests/shipwreck.json): placas, baterías, bidones, bombonas, a veces armas...
 */
public class PGShipwreckFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation LOOT = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "chests/shipwreck");

    private static final int HALF_LENGTH = 5;
    private static final int HALF_WIDTH = 2;
    private static final int HEIGHT = 4;

    public PGShipwreckFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Direction forward = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction right = forward.getClockWise();

        BlockState panel = ModBlocks.PG_STATION_PANEL.get().defaultBlockState();
        BlockState hazard = ModBlocks.PG_STATION_HAZARD_PANEL.get().defaultBlockState();
        BlockState window = ModBlocks.PG_STATION_WINDOW.get().defaultBlockState();
        BlockState floor = ModBlocks.PG_STATION_FLOOR.get().defaultBlockState();

        for (int l = -HALF_LENGTH; l <= HALF_LENGTH; l++) {
            for (int w = -HALF_WIDTH; w <= HALF_WIDTH; w++) {
                for (int h = 0; h <= HEIGHT; h++) {
                    boolean shell = Math.abs(w) == HALF_WIDTH || h == 0 || h == HEIGHT || Math.abs(l) == HALF_LENGTH;
                    if (!shell) {
                        continue;
                    }
                    if (random.nextFloat() < 0.22f) {
                        continue; // agujero: la nave está destrozada
                    }
                    BlockState state = h == 0 ? floor
                            : Math.abs(l) == HALF_LENGTH ? hazard
                            : h == 2 && Math.abs(w) == HALF_WIDTH && l % 2 == 0 ? window
                            : panel;
                    level.setBlock(at(origin, forward, right, l, h, w), state, 2);
                }
            }
        }
        // Motor apagado en la cola
        level.setBlock(at(origin, forward, right, -HALF_LENGTH - 1, 2, 0), ModBlocks.PG_GENERATOR.get().defaultBlockState(), 2);
        level.setBlock(at(origin, forward, right, -HALF_LENGTH - 1, 1, 0), ModBlocks.PG_ASTEROID_METAL_BLOCK.get().defaultBlockState(), 2);
        // Cofre con botín
        BlockPos chest = at(origin, forward, right, 2, 1, 0);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, right), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, chest, LOOT);
        // Chatarra flotando alrededor
        for (int i = 0; i < 6 + random.nextInt(6); i++) {
            BlockPos debris = origin.offset(random.nextInt(21) - 10, random.nextInt(11) - 4, random.nextInt(21) - 10);
            if (level.getBlockState(debris).isAir()) {
                level.setBlock(debris, random.nextBoolean() ? panel : hazard, 2);
            }
        }
        return true;
    }

    private static BlockPos at(BlockPos origin, Direction forward, Direction right, int along, int up, int side) {
        return origin.relative(forward, along).relative(right, side).above(up);
    }
}
