package com.panthrixsgalaxy;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Datos básicos del mod que usa todo el código común (MOD_ID y LOGGER).
 *
 * VERSIÓN FABRIC: aquí no se arranca nada. El arranque está en
 * fabric/PanthrixsGalaxyFabric (y en Forge, en la clase PanthrixsGalaxy de src/main/java).
 */
public final class PanthrixsGalaxy {

    /** Identificador del mod. Debe coincidir con mod_id de gradle.properties. */
    public static final String MOD_ID = "panthrixsgalaxy";

    /** Escribe mensajes en la consola / logs para depurar. */
    public static final Logger LOGGER = LogUtils.getLogger();

    private PanthrixsGalaxy() {
    }
}
