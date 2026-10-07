package com.panthrixsgalaxy.entity.mob;

import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import net.minecraft.world.entity.Entity;

/** Utilidades comunes de los alienígenas. */
public final class PGAliens {

    /** ¿Es un alienígena? (no se atacan entre ellos con láseres ni con los golpes de la Reina) */
    public static boolean isAlien(Entity entity) {
        return entity instanceof PGAlienExplorerEntity || entity instanceof PGAlienSoldierEntity
                || entity instanceof PGAlienCreatureEntity || entity instanceof PGAlienPredatorEntity
                || entity instanceof PGAlienQueenEntity;
    }

    private PGAliens() {
    }
}
