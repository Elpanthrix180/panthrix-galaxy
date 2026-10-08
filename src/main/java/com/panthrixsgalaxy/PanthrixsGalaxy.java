package com.panthrixsgalaxy;

import com.mojang.logging.LogUtils;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModCreativeTabs;
import com.panthrixsgalaxy.init.ModEntities;
import com.panthrixsgalaxy.init.ModFeatures;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.init.ModMenuTypes;
import com.panthrixsgalaxy.init.ModRecipes;
import com.panthrixsgalaxy.network.PGNetwork;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import com.panthrixsgalaxy.tool.PGToolTiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Clase principal de Panthrixs Galaxy.
 * Forge la encuentra gracias a la anotación @Mod y la ejecuta al arrancar el juego.
 * Aquí solo "conectamos" los registros; cada sistema vive en su propia clase.
 */
@Mod(PanthrixsGalaxy.MOD_ID)
public class PanthrixsGalaxy {

    /** Identificador del mod. Debe coincidir con mod_id de gradle.properties. */
    public static final String MOD_ID = "panthrixsgalaxy";

    /** Escribe mensajes en la consola / logs para depurar. */
    public static final Logger LOGGER = LogUtils.getLogger();

    public PanthrixsGalaxy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Configuración (Fase 23): config/panthrixsgalaxy-common.toml y -client.toml
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PGConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PGConfig.CLIENT_SPEC);

        // Niveles de herramienta: deben registrarse al arrancar, antes que nada
        PGToolTiers.register();

        // Registros del mod (el orden no importa, Forge los procesa en su momento)
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        // Hueco de mochila de los jugadores (Fase 6)
        modEventBus.addListener(PGBackpackSlot::registerCapability);

        // Canal de comunicación servidor <-> pantalla del jugador (indicador de oxígeno...)
        PGNetwork.register();

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // El Espacio no tiene aire respirable (Fase 11)
        event.enqueueWork(() -> {
            PGAtmosphere.addAirlessDimension(PGPlanets.SPACE);
            // Todos los planetas sin aire respirable (la Luna, y en el futuro Marte...)
            PGPlanets.ALL.forEach(planet -> {
                if (!planet.breathable() && planet.dimension() != null) {
                    PGAtmosphere.addAirlessDimension(planet.dimension());
                }
            });
        });
        LOGGER.info("[Panthrixs Galaxy] Mod cargado correctamente. ¡Preparados para el despegue!");
    }
}
