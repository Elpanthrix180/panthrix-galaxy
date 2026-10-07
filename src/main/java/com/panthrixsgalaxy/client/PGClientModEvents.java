package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGRocketModel;
import com.panthrixsgalaxy.client.model.PGShipModel;
import com.panthrixsgalaxy.client.renderer.PGRocketRenderer;
import com.panthrixsgalaxy.client.renderer.PGShipRenderer;
import com.panthrixsgalaxy.client.screen.PGBackpackScreen;
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
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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

    /** Aspecto especial del Espacio, la Luna y Marte. */
    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "space"), new PGSpaceEffects());
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "moon"), new PGMoonEffects());
        event.register(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "mars"), new PGMarsEffects());
    }

    /** Une cada entidad con la clase que la dibuja. */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ROCKET.get(), PGRocketRenderer::new);
        event.registerEntityRenderer(ModEntities.SHIP.get(), PGShipRenderer::new);
    }

    /** Registra los modelos 3D de las entidades. */
    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PGRocketModel.LAYER_LOCATION, PGRocketModel::createBodyLayer);
        event.registerLayerDefinition(PGShipModel.LAYER_LOCATION, PGShipModel::createBodyLayer);
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
