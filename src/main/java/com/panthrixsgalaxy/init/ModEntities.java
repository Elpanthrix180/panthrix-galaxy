package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import com.panthrixsgalaxy.entity.laser.PGLaserBoltEntity;
import com.panthrixsgalaxy.entity.mob.PGAlienCreatureEntity;
import com.panthrixsgalaxy.entity.mob.PGAlienExplorerEntity;
import com.panthrixsgalaxy.entity.mob.PGAlienPredatorEntity;
import com.panthrixsgalaxy.entity.mob.PGAlienSoldierEntity;
import com.panthrixsgalaxy.entity.mob.PGCrawlerEntity;
import com.panthrixsgalaxy.entity.mob.PGMartianWormEntity;
import com.panthrixsgalaxy.entity.mob.PGScorpionEntity;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import com.panthrixsgalaxy.platform.PGHolder;
import com.panthrixsgalaxy.platform.PGRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Entidades del mod (cohetes, naves, criaturas...). */
public final class ModEntities {

    public static final PGRegistry<EntityType<?>> ENTITIES = PGRegistry.create(Registries.ENTITY_TYPE);

    /** Cohete: 1 bloque de ancho y 3 de alto. */
    public static final PGHolder<EntityType<PGRocketEntity>> ROCKET = ENTITIES.register("rocket",
            () -> EntityType.Builder.<PGRocketEntity>of(PGRocketEntity::new, MobCategory.MISC)
                    .sized(1.0f, 3.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1) // posición enviada cada tick: vuelo suave
                    .build("rocket"));

    /** Nave espacial: 2,75 bloques de ancho y 1,3 de alto (Fase 14). */
    public static final PGHolder<EntityType<PGShipEntity>> SHIP = ENTITIES.register("ship",
            () -> EntityType.Builder.<PGShipEntity>of(PGShipEntity::new, MobCategory.MISC)
                    .sized(2.75f, 1.3f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("ship"));

    /** Rayo de un arma láser (Fase 15): pequeñito y muy rápido. */
    public static final PGHolder<EntityType<PGLaserBoltEntity>> LASER_BOLT = ENTITIES.register("laser_bolt",
            () -> EntityType.Builder.<PGLaserBoltEntity>of(PGLaserBoltEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("laser_bolt"));

    // ===== CRIATURAS (Fase 17) =====

    // Luna
    public static final PGHolder<EntityType<PGCrawlerEntity>> LUNAR_CRAWLER =
            monster("lunar_crawler", PGCrawlerEntity::new, 1.2f, 0.7f);
    public static final PGHolder<EntityType<PGScorpionEntity>> LUNAR_SCORPION =
            monster("lunar_scorpion", PGScorpionEntity::new, 1.2f, 0.8f);

    // Marte
    public static final PGHolder<EntityType<PGMartianWormEntity>> MARTIAN_WORM =
            monster("martian_worm", PGMartianWormEntity::new, 1.0f, 2.4f);
    public static final PGHolder<EntityType<PGScorpionEntity>> MARTIAN_SCORPION =
            monster("martian_scorpion", PGScorpionEntity::new, 1.4f, 0.9f);
    public static final PGHolder<EntityType<PGCrawlerEntity>> MARTIAN_CRAWLER =
            monster("martian_crawler", PGCrawlerEntity::new, 1.4f, 0.9f);

    // Alienígenas
    public static final PGHolder<EntityType<PGAlienExplorerEntity>> ALIEN_EXPLORER =
            monster("alien_explorer", PGAlienExplorerEntity::new, 0.6f, 1.95f);
    public static final PGHolder<EntityType<PGAlienSoldierEntity>> ALIEN_SOLDIER =
            monster("alien_soldier", PGAlienSoldierEntity::new, 0.6f, 1.95f);
    public static final PGHolder<EntityType<PGAlienCreatureEntity>> ALIEN_CREATURE =
            monster("alien_creature", PGAlienCreatureEntity::new, 1.0f, 0.9f);
    public static final PGHolder<EntityType<PGAlienPredatorEntity>> ALIEN_PREDATOR =
            monster("alien_predator", PGAlienPredatorEntity::new, 0.7f, 2.25f);

    // ===== JEFES (Fase 18) =====

    /** 👑 Reina alienígena: 1,6 bloques de ancho y 3,2 de alto. No le afecta el fuego ni la lava. */
    public static final PGHolder<EntityType<PGAlienQueenEntity>> ALIEN_QUEEN = ENTITIES.register("alien_queen",
            () -> EntityType.Builder.of(PGAlienQueenEntity::new, MobCategory.MONSTER)
                    .sized(1.6f, 3.2f)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("alien_queen"));

    /** Registra una criatura hostil (categoría MONSTER). */
    private static <T extends Entity> PGHolder<EntityType<T>> monster(String name, EntityType.EntityFactory<T> factory,
                                                                             float width, float height) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name));
    }

    private ModEntities() {
    }
}
