package com.panthrixsgalaxy.entity.mob;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Datos de las criaturas al arrancar el juego:
 *   - sus ATRIBUTOS (vida, daño, velocidad...);
 *   - DÓNDE pueden aparecer (en el suelo, con la regla de PGSpawnRules).
 *
 * En qué planeta aparece cada una se decide en el bioma: data/panthrixsgalaxy/worldgen/biome/<planeta>.json ("spawners").
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PGMobEvents {

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        PGMobSetup.attributes((type, attributes) -> event.put(type, attributes.build()));
    }

    @SubscribeEvent
    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        PGMobSetup.groundSpawns(type -> onGround(event, type));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob> void onGround(SpawnPlacementRegisterEvent event, EntityType<? extends Mob> type) {
        event.register((EntityType<T>) type, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PGSpawnRules::planetMonster, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private PGMobEvents() {
    }
}
