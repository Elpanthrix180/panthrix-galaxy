package com.panthrixsgalaxy.entity.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Cuándo puede aparecer una criatura de otro planeta.
 *
 * Los monstruos de Minecraft solo salen a oscuras. En la Luna siempre es de día (y en Marte
 * de día hay mucha luz), así que con esa regla no saldría ninguno. Nuestra regla:
 *   - no en modo Pacífico;
 *   - sobre un bloque normal;
 *   - IGNORA la luz del sol, pero NO la de antorchas y lámparas (luz de bloques 7 o menos).
 *     Así, iluminar tu base la protege, igual que en la Tierra.
 */
public final class PGSpawnRules {

    public static <T extends Mob> boolean planetMonster(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason,
                                                        BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getBrightness(LightLayer.BLOCK, pos) <= 7
                && Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    private PGSpawnRules() {
    }
}
