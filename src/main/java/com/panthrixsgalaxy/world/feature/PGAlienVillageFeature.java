package com.panthrixsgalaxy.world.feature;

import com.mojang.serialization.Codec;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.mob.PGAlienExplorerEntity;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * ALDEA ALIENÍGENA (Xenoria): 4 cabañas-cúpula alrededor de una plaza con el ARCHIVO ALIENÍGENA
 * (logro secreto "No estamos solos") y 3 aliens exploradores, que son pacíficos si no les atacas.
 * Cada cabaña puede tener un cofre (loot_tables/chests/alien_village.json).
 */
public class PGAlienVillageFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation LOOT = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "chests/alien_village");
    private static final int HUT_RADIUS = 3;

    public PGAlienVillageFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin();
        BlockState stone = ModBlocks.PG_ALIEN_STONE.get().defaultBlockState();
        BlockState soil = ModBlocks.PG_ALIEN_SOIL.get().defaultBlockState();
        BlockState crystal = ModBlocks.PG_COSMIC_CRYSTAL_ORE.get().defaultBlockState();

        // Plaza y archivo
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(center.offset(x, -1, z), stone, 2);
            }
        }
        level.setBlock(center, ModBlocks.PG_ALIEN_ARCHIVE.get().defaultBlockState(), 2);
        level.setBlock(center.above(), crystal, 2);
        // 4 cabañas
        int[][] spots = {{8, 0}, {-8, 0}, {0, 8}, {0, -8}};
        for (int[] spot : spots) {
            int x = center.getX() + spot[0];
            int z = center.getZ() + spot[1];
            BlockPos base = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
            hut(level, random, base, stone, soil, crystal, -spot[0], -spot[1]);
        }
        // Los habitantes
        for (int i = 0; i < 3; i++) {
            PGAlienExplorerEntity alien = ModEntities.ALIEN_EXPLORER.get().create(level.getLevel());
            if (alien == null) {
                continue;
            }
            alien.moveTo(center.getX() + 0.5 + random.nextInt(5) - 2, center.getY(), center.getZ() + 0.5 + random.nextInt(5) - 2,
                    random.nextFloat() * 360.0f, 0.0f);
            alien.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.STRUCTURE, null, null);
            alien.setPersistenceRequired();
            alien.restrictTo(center, 16);
            level.addFreshEntityWithPassengers(alien);
        }
        return true;
    }

    /** Una cúpula hueca con la puerta mirando a la plaza. */
    private static void hut(WorldGenLevel level, RandomSource random, BlockPos base, BlockState stone, BlockState soil,
                            BlockState crystal, int towardX, int towardZ) {
        int doorX = Integer.signum(towardX);
        int doorZ = Integer.signum(towardZ);
        for (int x = -HUT_RADIUS; x <= HUT_RADIUS; x++) {
            for (int y = 0; y <= HUT_RADIUS; y++) {
                for (int z = -HUT_RADIUS; z <= HUT_RADIUS; z++) {
                    double distance = Math.sqrt(x * x + y * y * 1.2 + z * z);
                    if (distance > HUT_RADIUS + 0.4) {
                        continue;
                    }
                    BlockPos pos = base.offset(x, y, z);
                    boolean door = y <= 1 && x * doorX + z * doorZ >= HUT_RADIUS - 1
                            && Math.abs(x * doorZ) + Math.abs(z * doorX) == 0;
                    if (distance < HUT_RADIUS - 0.6 || door) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, y == HUT_RADIUS ? crystal : random.nextBoolean() ? soil : stone, 2);
                    }
                }
            }
        }
        level.setBlock(base.below(), stone, 2);
        if (random.nextBoolean()) {
            BlockPos chest = base.offset(-doorX, 0, -doorZ);
            level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
            RandomizableContainerBlockEntity.setLootTable(level, random, chest, LOOT);
        }
    }
}
