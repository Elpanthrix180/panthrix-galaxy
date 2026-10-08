package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGAlienCreatureModel;
import com.panthrixsgalaxy.client.model.PGAlienModels;
import com.panthrixsgalaxy.client.model.PGAlienQueenModel;
import com.panthrixsgalaxy.client.model.PGMartianWormModel;
import com.panthrixsgalaxy.client.model.PGRocketModel;
import com.panthrixsgalaxy.client.model.PGScorpionModel;
import com.panthrixsgalaxy.client.model.PGShipModel;
import com.panthrixsgalaxy.client.renderer.PGAlienCreatureRenderer;
import com.panthrixsgalaxy.client.renderer.PGAlienQueenRenderer;
import com.panthrixsgalaxy.client.renderer.PGAlienRenderer;
import com.panthrixsgalaxy.client.renderer.PGCrawlerRenderer;
import com.panthrixsgalaxy.client.renderer.PGLaserBoltRenderer;
import com.panthrixsgalaxy.client.renderer.PGMartianWormRenderer;
import com.panthrixsgalaxy.client.renderer.PGRocketRenderer;
import com.panthrixsgalaxy.client.renderer.PGScorpionRenderer;
import com.panthrixsgalaxy.client.renderer.PGShipRenderer;
import com.panthrixsgalaxy.client.screen.PGBackpackScreen;
import com.panthrixsgalaxy.client.screen.PGEngineeringBenchScreen;
import com.panthrixsgalaxy.client.screen.PGShipScreen;
import com.panthrixsgalaxy.client.sky.PGAirlessSkyEffects;
import com.panthrixsgalaxy.client.sky.PGAsteroidEffects;
import com.panthrixsgalaxy.client.sky.PGMarsEffects;
import com.panthrixsgalaxy.client.sky.PGMoonEffects;
import com.panthrixsgalaxy.client.sky.PGSpaceEffects;
import com.panthrixsgalaxy.init.ModEntities;
import com.panthrixsgalaxy.init.ModMenuTypes;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ChestMenu;
import org.joml.Vector3f;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Lista ÚNICA de lo que se registra en la pantalla del jugador: quién dibuja cada entidad,
 * sus modelos 3D, las ventanas y los cielos de los planetas.
 * Forge (PGClientModEvents) y Fabric (PGFabricClient) la recorren, cada uno a su manera.
 */
public final class PGClientRegistrations {

    /** Dónde apuntar "esta entidad la dibuja esta clase". */
    public interface EntityRenderers {
        <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> renderer);
    }

    /** Une cada entidad con la clase que la dibuja. */
    public static void entityRenderers(EntityRenderers event) {
        event.register(ModEntities.ROCKET.get(), PGRocketRenderer::new);
        event.register(ModEntities.SHIP.get(), PGShipRenderer::new);
        event.register(ModEntities.LASER_BOLT.get(), PGLaserBoltRenderer::new);
        // Criaturas (Fase 17)
        event.register(ModEntities.LUNAR_CRAWLER.get(), PGCrawlerRenderer::new);
        event.register(ModEntities.MARTIAN_CRAWLER.get(), PGCrawlerRenderer::new);
        event.register(ModEntities.LUNAR_SCORPION.get(), PGScorpionRenderer::new);
        event.register(ModEntities.MARTIAN_SCORPION.get(), PGScorpionRenderer::new);
        event.register(ModEntities.MARTIAN_WORM.get(), PGMartianWormRenderer::new);
        event.register(ModEntities.ALIEN_CREATURE.get(), PGAlienCreatureRenderer::new);
        event.register(ModEntities.ALIEN_EXPLORER.get(), context -> new PGAlienRenderer<>(context, "alien_explorer", 1.0f));
        event.register(ModEntities.ALIEN_SOLDIER.get(), context -> new PGAlienRenderer<>(context, "alien_soldier", 1.0f));
        event.register(ModEntities.ALIEN_PREDATOR.get(), context -> new PGAlienRenderer<>(context, "alien_predator", 1.15f));
        // Jefes (Fase 18)
        event.register(ModEntities.ALIEN_QUEEN.get(), PGAlienQueenRenderer::new);
    }

    /** Registra los modelos 3D de las entidades. */
    public static void layerDefinitions(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> event) {
        event.accept(PGRocketModel.LAYER_LOCATION, PGRocketModel::createBodyLayer);
        event.accept(PGShipModel.LAYER_LOCATION, PGShipModel::createBodyLayer);
        event.accept(PGScorpionModel.LAYER_LOCATION, PGScorpionModel::createBodyLayer);
        event.accept(PGMartianWormModel.LAYER_LOCATION, PGMartianWormModel::createBodyLayer);
        event.accept(PGAlienCreatureModel.LAYER_LOCATION, PGAlienCreatureModel::createBodyLayer);
        event.accept(PGAlienModels.ALIEN, PGAlienModels::createAlienLayer);
        event.accept(PGAlienQueenModel.LAYER_LOCATION, PGAlienQueenModel::createBodyLayer);
    }

    /** Une cada tipo de ventana con la pantalla que la dibuja. */
    public static void menuScreens() {
        MenuScreens.<ChestMenu, PGBackpackScreen>register(ModMenuTypes.BACKPACK.get(), PGBackpackScreen::new);
        MenuScreens.register(ModMenuTypes.ENGINEERING_BENCH.get(), PGEngineeringBenchScreen::new);
        MenuScreens.<ChestMenu, PGShipScreen>register(ModMenuTypes.SHIP.get(), PGShipScreen::new);
    }

    /** Aspecto especial del Espacio, la Luna, Marte, el cinturón de asteroides y otros planetas. */
    public static void dimensionEffects(BiConsumer<ResourceLocation, DimensionSpecialEffects> event) {
        event.accept(id("space"), new PGSpaceEffects());
        event.accept(id("moon"), new PGMoonEffects());
        event.accept(id("mars"), new PGMarsEffects());
        event.accept(id("asteroids"), new PGAsteroidEffects());
        // Fase 21: Mercurio ve a Venus brillante; desde Plutón, Neptuno es un puntito azul
        event.accept(id("mercury"), new PGAirlessSkyEffects(PGPlanets.VENUS.texture(), new Vector3f(0.6f, 0.5f, -0.5f), 6.0f));
        event.accept(id("pluto"), new PGAirlessSkyEffects(PGPlanets.NEPTUNE.texture(), new Vector3f(-0.4f, 0.7f, 0.3f), 4.0f));
        // Fase 22: desde el planeta secreto Nyx se ve Xenoria a lo lejos
        event.accept(id("nyx"), new PGAirlessSkyEffects(PGPlanets.XENORIA.texture(), new Vector3f(0.3f, 0.6f, -0.7f), 5.0f));
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID, name);
    }

    private PGClientRegistrations() {
    }
}
