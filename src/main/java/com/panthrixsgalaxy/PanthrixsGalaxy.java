package com.panthrixsgalaxy;

import com.mojang.logging.LogUtils;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModCreativeTabs;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.tool.PGToolTiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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

        // Niveles de herramienta: deben registrarse al arrancar, antes que nada
        PGToolTiers.register();

        // Registros del mod (el orden no importa, Forge los procesa en su momento)
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Panthrixs Galaxy] Mod cargado correctamente. ¡Preparados para el despegue!");
    }
}
