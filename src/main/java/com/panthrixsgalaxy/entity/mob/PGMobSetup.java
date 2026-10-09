package com.panthrixsgalaxy.entity.mob;

import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import com.panthrixsgalaxy.init.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lista ÚNICA de las criaturas del mod: sus atributos (vida, daño...) y dónde pueden aparecer.
 * Forge (PGMobEvents) y Fabric (PGFabricMobs) la recorren, cada uno con su forma de registrar.
 */
public final class PGMobSetup {

    /** Atributos de cada criatura (sin ellos el juego se cierra al crearla). */
    public static void attributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> register) {
        register.accept(ModEntities.LUNAR_CRAWLER.get(), PGCrawlerEntity.lunarAttributes());
        register.accept(ModEntities.MARTIAN_CRAWLER.get(), PGCrawlerEntity.martianAttributes());
        register.accept(ModEntities.LUNAR_SCORPION.get(), PGScorpionEntity.lunarAttributes());
        register.accept(ModEntities.MARTIAN_SCORPION.get(), PGScorpionEntity.martianAttributes());
        register.accept(ModEntities.MARTIAN_WORM.get(), PGMartianWormEntity.createAttributes());
        register.accept(ModEntities.ALIEN_EXPLORER.get(), PGAlienExplorerEntity.createAttributes());
        register.accept(ModEntities.ALIEN_SOLDIER.get(), PGAlienSoldierEntity.createAttributes());
        register.accept(ModEntities.ALIEN_CREATURE.get(), PGAlienCreatureEntity.createAttributes());
        register.accept(ModEntities.ALIEN_PREDATOR.get(), PGAlienPredatorEntity.createAttributes());
        register.accept(ModEntities.ALIEN_QUEEN.get(), PGAlienQueenEntity.createAttributes());
    }

    /** Criaturas que aparecen solas en el suelo de los planetas (reglas en PGSpawnRules). */
    public static void groundSpawns(Consumer<EntityType<? extends Mob>> register) {
        register.accept(ModEntities.LUNAR_CRAWLER.get());
        register.accept(ModEntities.MARTIAN_CRAWLER.get());
        register.accept(ModEntities.LUNAR_SCORPION.get());
        register.accept(ModEntities.MARTIAN_SCORPION.get());
        register.accept(ModEntities.MARTIAN_WORM.get());
        register.accept(ModEntities.ALIEN_EXPLORER.get());
        register.accept(ModEntities.ALIEN_SOLDIER.get());
        register.accept(ModEntities.ALIEN_CREATURE.get());
        register.accept(ModEntities.ALIEN_PREDATOR.get());
    }

    private PGMobSetup() {
    }
}
