package com.panthrixsgalaxy.world.feature;

import com.mojang.serialization.Codec;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
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
 * 👑 COLMENA DE LA REINA (Xenoria): una gran cúpula orgánica de piedra y tierra alienígena,
 * con cristales cósmicos que brillan en las paredes, una entrada y, dentro, la REINA ALIENÍGENA
 * esperando junto a su tesoro (loot_tables/chests/alien_hive.json).
 */
public class PGAlienHiveFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation LOOT = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "chests/alien_hive");

    private static final int RADIUS = 12;
    private static final int HOLLOW = 9;

    public PGAlienHiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos base = context.origin();
        BlockState stone = ModBlocks.PG_ALIEN_STONE.get().defaultBlockState();
        BlockState soil = ModBlocks.PG_ALIEN_SOIL.get().defaultBlockState();
        BlockState crystal = ModBlocks.PG_COSMIC_CRYSTAL_ORE.get().defaultBlockState();
        Direction entrance = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -2; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    double distance = Math.sqrt(x * x + y * y * 1.3 + z * z);
                    if (distance > RADIUS) {
                        continue;
                    }
                    pos.set(base.getX() + x, base.getY() + y, base.getZ() + z);
                    int along = x * entrance.getStepX() + z * entrance.getStepZ();
                    int across = Math.abs(x * entrance.getStepZ()) + Math.abs(z * entrance.getStepX());
                    boolean door = along > 0 && across <= 1 && y >= 0 && y <= 2;
                    if (y < 0) {
                        level.setBlock(pos, stone, 2); // suelo de la colmena
                    } else if (distance < HOLLOW || door) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        float roll = random.nextFloat();
                        boolean inner = distance < HOLLOW + 1.2;
                        level.setBlock(pos, inner && roll < 0.1f ? crystal : roll < 0.45f ? soil : stone, 2);
                    }
                }
            }
        }
        // Tesoro y Reina
        BlockPos chest = base.relative(entrance.getOpposite(), 6);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, entrance), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, chest, LOOT);
        PGAlienQueenEntity queen = ModEntities.ALIEN_QUEEN.get().create(level.getLevel());
        if (queen != null) {
            queen.moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, entrance.toYRot(), 0.0f);
            queen.finalizeSpawn(level, level.getCurrentDifficultyAt(base), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntityWithPassengers(queen);
        }
        return true;
    }
}
