package com.panthrixsgalaxy.world.feature;

import com.mojang.serialization.Codec;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.mob.PGAlienSoldierEntity;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * PUESTO ALIENÍGENA: un asteroide de piedra alienígena HUECO, con cristales cósmicos que
 * brillan en las paredes, una entrada y un COFRE de tecnología alienígena en el centro
 * (loot_tables/chests/alien_outpost.json). Lo PROTEGEN 2 aliens soldado.
 */
public class PGAlienOutpostFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation LOOT = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "chests/alien_outpost");

    private static final int RADIUS = 8;
    private static final int HOLLOW = 5;

    public PGAlienOutpostFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin();
        BlockState stone = ModBlocks.PG_ALIEN_STONE.get().defaultBlockState();
        BlockState crystal = ModBlocks.PG_COSMIC_CRYSTAL_ORE.get().defaultBlockState();
        BlockState xenite = ModBlocks.PG_XENITE_ORE.get().defaultBlockState();
        Direction entrance = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    double distance = Math.sqrt(x * x + y * y * 1.4 + z * z);
                    if (distance > RADIUS) {
                        continue;
                    }
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (level.isOutsideBuildHeight(pos)) {
                        continue;
                    }
                    // Túnel de entrada
                    int along = x * entrance.getStepX() + z * entrance.getStepZ();
                    int across = Math.abs(x * entrance.getStepZ()) + Math.abs(z * entrance.getStepX());
                    boolean tunnel = along > 0 && across <= 1 && y >= -1 && y <= 1;
                    if (distance < HOLLOW || tunnel) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        boolean innerWall = distance < HOLLOW + 1.2;
                        float roll = random.nextFloat();
                        level.setBlock(pos, innerWall && roll < 0.12f ? crystal : roll < 0.04f ? xenite : stone, 2);
                    }
                }
            }
        }
        // Suelo plano dentro y cofre en el centro
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(center.offset(x, -3, z), stone, 2);
            }
        }
        BlockPos chest = center.below(2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, entrance), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, chest, LOOT);
        // Los guardianes
        for (int i = 0; i < 2; i++) {
            PGAlienSoldierEntity guard = ModEntities.ALIEN_SOLDIER.get().create(level.getLevel());
            if (guard == null) {
                continue;
            }
            guard.moveTo(center.getX() + 0.5 + (i == 0 ? 2 : -2), center.getY() - 2, center.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            guard.finalizeSpawn(level, level.getCurrentDifficultyAt(chest), MobSpawnType.STRUCTURE, null, null);
            guard.setPersistenceRequired();
            level.addFreshEntityWithPassengers(guard);
        }
        return true;
    }
}
