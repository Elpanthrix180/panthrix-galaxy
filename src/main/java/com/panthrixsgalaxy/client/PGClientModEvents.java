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
import com.panthrixsgalaxy.client.renderer.PGScorpionRenderer;
import com.panthrixsgalaxy.client.renderer.PGRocketRenderer;
import com.panthrixsgalaxy.client.renderer.PGShipRenderer;
import com.panthrixsgalaxy.client.screen.PGBackpackScreen;
import com.panthrixsgalaxy.client.sky.PGAirlessSkyEffects;
import com.panthrixsgalaxy.client.sky.PGAsteroidEffects;
import com.panthrixsgalaxy.client.sky.PGMarsEffects;
import com.panthrixsgalaxy.client.sky.PGMoonEffects;
import com.panthrixsgalaxy.client.sky.PGSpaceEffects;
import com.panthrixsgalaxy.client.screen.PGEngineeringBenchScreen;
import com.panthrixsgalaxy.client.screen.PGShipScreen;
import com.panthrixsgalaxy.init.ModEntities;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.init.ModMenuTypes;
import com.panthrixsgalaxy.init.ModRecipes;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Vector3f;

/**
 * Preparación de la parte visual del mod (solo en la pantalla del jugador):
 * pantallas, teclas, modelos extra y capas de dibujo del jugador.
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PGClientModEvents {

    /** Une cada tipo de ventana con la pantalla que la dibuja. */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.<ChestMenu, PGBackpackScreen>register(ModMenuTypes.BACKPACK.get(), PGBackpackScreen::new);
            MenuScreens.register(ModMenuTypes.ENGINEERING_BENCH.get(), PGEngineeringBenchScreen::new);
            MenuScreens.<ChestMenu, PGShipScreen>register(ModMenuTypes.SHIP.get(), PGShipScreen::new);
            // Espadas láser: el modelo cambia a "con hoja" cuando están encendidas (Fase 16)
            ModItems.ITEMS.getEntries().forEach(entry -> {
                if (entry.get() instanceof PGLaserSwordItem) {
                    ItemProperties.register(entry.get(), new ResourceLocation(PanthrixsGalaxy.MOD_ID, "active"),
                            (stack, level, entity, seed) -> PGLaserSwordItem.isActive(stack) ? 1.0f : 0.0f);
                }
            });
        });
    }

    /**
     * Las recetas del Banco no salen en el libro de recetas de Minecraft (tienen su propia guía).
     * Esto evita avisos en el registro del juego por ese motivo.
     */
    @SubscribeEvent
    public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(ModRecipes.ENGINEERING_TYPE.get(), recipe -> RecipeBookCategories.UNKNOWN);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(PGKeyBindings.BACKPACK);
    }

    /** Carga los modelos 3D de las mochilas puestas (no son objetos normales, hay que pedirlos). */
    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof PGBackpackItem backpack) {
                event.register(BackpackRenderLayer.wornModelLocation(backpack));
            }
        });
    }

    /** Aspecto especial del Espacio, la Luna, Marte y el cinturón de asteroides. */
    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "space"), new PGSpaceEffects());
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "moon"), new PGMoonEffects());
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "mars"), new PGMarsEffects());
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "asteroids"), new PGAsteroidEffects());
        // Fase 21: Mercurio ve a Venus brillante; desde Plutón, Neptuno es un puntito azul
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "mercury"),
                new PGAirlessSkyEffects(PGPlanets.VENUS.texture(), new Vector3f(0.6f, 0.5f, -0.5f), 6.0f));
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "pluto"),
                new PGAirlessSkyEffects(PGPlanets.NEPTUNE.texture(), new Vector3f(-0.4f, 0.7f, 0.3f), 4.0f));
        // Fase 22: desde el planeta secreto Nyx se ve Xenoria a lo lejos
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "nyx"),
                new PGAirlessSkyEffects(PGPlanets.XENORIA.texture(), new Vector3f(0.3f, 0.6f, -0.7f), 5.0f));
    }

    /** Une cada entidad con la clase que la dibuja. */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ROCKET.get(), PGRocketRenderer::new);
        event.registerEntityRenderer(ModEntities.SHIP.get(), PGShipRenderer::new);
        event.registerEntityRenderer(ModEntities.LASER_BOLT.get(), PGLaserBoltRenderer::new);
        // Criaturas (Fase 17)
        event.registerEntityRenderer(ModEntities.LUNAR_CRAWLER.get(), PGCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntities.MARTIAN_CRAWLER.get(), PGCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntities.LUNAR_SCORPION.get(), PGScorpionRenderer::new);
        event.registerEntityRenderer(ModEntities.MARTIAN_SCORPION.get(), PGScorpionRenderer::new);
        event.registerEntityRenderer(ModEntities.MARTIAN_WORM.get(), PGMartianWormRenderer::new);
        event.registerEntityRenderer(ModEntities.ALIEN_CREATURE.get(), PGAlienCreatureRenderer::new);
        event.registerEntityRenderer(ModEntities.ALIEN_EXPLORER.get(), context -> new PGAlienRenderer<>(context, "alien_explorer", 1.0f));
        event.registerEntityRenderer(ModEntities.ALIEN_SOLDIER.get(), context -> new PGAlienRenderer<>(context, "alien_soldier", 1.0f));
        event.registerEntityRenderer(ModEntities.ALIEN_PREDATOR.get(), context -> new PGAlienRenderer<>(context, "alien_predator", 1.15f));
        // Jefes (Fase 18)
        event.registerEntityRenderer(ModEntities.ALIEN_QUEEN.get(), PGAlienQueenRenderer::new);
    }

    /** Registra los modelos 3D de las entidades. */
    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PGRocketModel.LAYER_LOCATION, PGRocketModel::createBodyLayer);
        event.registerLayerDefinition(PGShipModel.LAYER_LOCATION, PGShipModel::createBodyLayer);
        event.registerLayerDefinition(PGScorpionModel.LAYER_LOCATION, PGScorpionModel::createBodyLayer);
        event.registerLayerDefinition(PGMartianWormModel.LAYER_LOCATION, PGMartianWormModel::createBodyLayer);
        event.registerLayerDefinition(PGAlienCreatureModel.LAYER_LOCATION, PGAlienCreatureModel::createBodyLayer);
        event.registerLayerDefinition(PGAlienModels.ALIEN, PGAlienModels::createAlienLayer);
        event.registerLayerDefinition(PGAlienQueenModel.LAYER_LOCATION, PGAlienQueenModel::createBodyLayer);
    }

    /** Añade la capa "mochila en la espalda" a los dos tipos de jugador (brazos normales y finos). */
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new BackpackRenderLayer(renderer));
            }
        }
    }

    private PGClientModEvents() {
    }
}
