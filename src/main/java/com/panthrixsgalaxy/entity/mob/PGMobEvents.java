package com.panthrixsgalaxy.entity.mob;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import com.panthrixsgalaxy.init.ModEntities;
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
        event.put(ModEntities.LUNAR_CRAWLER.get(), PGCrawlerEntity.lunarAttributes().build());
        event.put(ModEntities.MARTIAN_CRAWLER.get(), PGCrawlerEntity.martianAttributes().build());
        event.put(ModEntities.LUNAR_SCORPION.get(), PGScorpionEntity.lunarAttributes().build());
        event.put(ModEntities.MARTIAN_SCORPION.get(), PGScorpionEntity.martianAttributes().build());
        event.put(ModEntities.MARTIAN_WORM.get(), PGMartianWormEntity.createAttributes().build());
        event.put(ModEntities.ALIEN_EXPLORER.get(), PGAlienExplorerEntity.createAttributes().build());
        event.put(ModEntities.ALIEN_SOLDIER.get(), PGAlienSoldierEntity.createAttributes().build());
        event.put(ModEntities.ALIEN_CREATURE.get(), PGAlienCreatureEntity.createAttributes().build());
        event.put(ModEntities.ALIEN_PREDATOR.get(), PGAlienPredatorEntity.createAttributes().build());
        event.put(ModEntities.ALIEN_QUEEN.get(), PGAlienQueenEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        onGround(event, ModEntities.LUNAR_CRAWLER.get());
        onGround(event, ModEntities.MARTIAN_CRAWLER.get());
        onGround(event, ModEntities.LUNAR_SCORPION.get());
        onGround(event, ModEntities.MARTIAN_SCORPION.get());
        onGround(event, ModEntities.MARTIAN_WORM.get());
        onGround(event, ModEntities.ALIEN_EXPLORER.get());
        onGround(event, ModEntities.ALIEN_SOLDIER.get());
        onGround(event, ModEntities.ALIEN_CREATURE.get());
        onGround(event, ModEntities.ALIEN_PREDATOR.get());
    }

    private static <T extends Mob> void onGround(SpawnPlacementRegisterEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PGSpawnRules::planetMonster, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private PGMobEvents() {
    }
}
