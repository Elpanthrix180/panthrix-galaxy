package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Entidades del mod (cohetes, naves, criaturas...). */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PanthrixsGalaxy.MOD_ID);

    /** Cohete: 1 bloque de ancho y 3 de alto. */
    public static final RegistryObject<EntityType<PGRocketEntity>> ROCKET = ENTITIES.register("rocket",
            () -> EntityType.Builder.<PGRocketEntity>of(PGRocketEntity::new, MobCategory.MISC)
                    .sized(1.0f, 3.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1) // posición enviada cada tick: vuelo suave
                    .build("rocket"));

    /** Nave espacial: 2,75 bloques de ancho y 1,3 de alto (Fase 14). */
    public static final RegistryObject<EntityType<PGShipEntity>> SHIP = ENTITIES.register("ship",
            () -> EntityType.Builder.<PGShipEntity>of(PGShipEntity::new, MobCategory.MISC)
                    .sized(2.75f, 1.3f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("ship"));

    /** Rayo de un arma láser (Fase 15): pequeñito y muy rápido. */
    public static final RegistryObject<EntityType<PGLaserBoltEntity>> LASER_BOLT = ENTITIES.register("laser_bolt",
            () -> EntityType.Builder.<PGLaserBoltEntity>of(PGLaserBoltEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("laser_bolt"));

    // ===== CRIATURAS (Fase 17) =====

    // Luna
    public static final RegistryObject<EntityType<PGCrawlerEntity>> LUNAR_CRAWLER =
            monster("lunar_crawler", PGCrawlerEntity::new, 1.2f, 0.7f);
    public static final RegistryObject<EntityType<PGScorpionEntity>> LUNAR_SCORPION =
            monster("lunar_scorpion", PGScorpionEntity::new, 1.2f, 0.8f);

    // Marte
    public static final RegistryObject<EntityType<PGMartianWormEntity>> MARTIAN_WORM =
            monster("martian_worm", PGMartianWormEntity::new, 1.0f, 2.4f);
    public static final RegistryObject<EntityType<PGScorpionEntity>> MARTIAN_SCORPION =
            monster("martian_scorpion", PGScorpionEntity::new, 1.4f, 0.9f);
    public static final RegistryObject<EntityType<PGCrawlerEntity>> MARTIAN_CRAWLER =
            monster("martian_crawler", PGCrawlerEntity::new, 1.4f, 0.9f);

    // Alienígenas
    public static final RegistryObject<EntityType<PGAlienExplorerEntity>> ALIEN_EXPLORER =
            monster("alien_explorer", PGAlienExplorerEntity::new, 0.6f, 1.95f);
    public static final RegistryObject<EntityType<PGAlienSoldierEntity>> ALIEN_SOLDIER =
            monster("alien_soldier", PGAlienSoldierEntity::new, 0.6f, 1.95f);
    public static final RegistryObject<EntityType<PGAlienCreatureEntity>> ALIEN_CREATURE =
            monster("alien_creature", PGAlienCreatureEntity::new, 1.0f, 0.9f);
    public static final RegistryObject<EntityType<PGAlienPredatorEntity>> ALIEN_PREDATOR =
            monster("alien_predator", PGAlienPredatorEntity::new, 0.7f, 2.25f);

    /** Registra una criatura hostil (categoría MONSTER). */
    private static <T extends Entity> RegistryObject<EntityType<T>> monster(String name, EntityType.EntityFactory<T> factory,
                                                                             float width, float height) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name));
    }

    private ModEntities() {
    }
}
