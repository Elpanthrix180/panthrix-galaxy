package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.entity.mob.PGMobSetup;
import com.panthrixsgalaxy.entity.mob.PGSpawnRules;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Solo Fabric: atributos de las criaturas y dónde pueden aparecer solas.
 * (La lista es común: entity/mob/PGMobSetup. En Forge: entity/mob/PGMobEvents.)
 */
public final class PGFabricMobs {

    public static void register() {
        PGMobSetup.attributes(FabricDefaultAttributeRegistry::register);
        PGMobSetup.groundSpawns(PGFabricMobs::onGround);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob> void onGround(EntityType<? extends Mob> type) {
        SpawnPlacements.register((EntityType<T>) type, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PGSpawnRules::planetMonster);
    }

    private PGFabricMobs() {
    }
}
