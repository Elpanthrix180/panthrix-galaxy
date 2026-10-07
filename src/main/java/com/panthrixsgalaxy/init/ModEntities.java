package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.laser.PGLaserBoltEntity;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
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

    private ModEntities() {
    }
}
