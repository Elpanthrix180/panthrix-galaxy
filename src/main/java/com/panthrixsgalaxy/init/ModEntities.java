package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
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
                    .build("rocket"));

    private ModEntities() {
    }
}
