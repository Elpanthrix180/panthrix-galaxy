package com.panthrixsgalaxy.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de Panthrixs Galaxy en Fabric.
 * Fabric la encuentra gracias a "entrypoints → main" de fabric.mod.json.
 */
public class PanthrixsGalaxyFabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("panthrixsgalaxy");

    @Override
    public void onInitialize() {
        LOGGER.info("[Panthrixs Galaxy] Versión Fabric cargada.");
    }
}
