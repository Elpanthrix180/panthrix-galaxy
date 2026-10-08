package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.init.ModRecipes;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
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
            PGClientRegistrations.menuScreens();
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
        PGClientRegistrations.dimensionEffects(event::register);
    }

    /** Une cada entidad con la clase que la dibuja. */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        PGClientRegistrations.entityRenderers(new PGClientRegistrations.EntityRenderers() {
            @Override
            public <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> renderer) {
                event.registerEntityRenderer(type, renderer);
            }
        });
    }

    /** Registra los modelos 3D de las entidades. */
    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        PGClientRegistrations.layerDefinitions(event::registerLayerDefinition);
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
