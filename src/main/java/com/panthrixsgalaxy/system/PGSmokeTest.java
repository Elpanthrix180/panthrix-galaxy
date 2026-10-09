package com.panthrixsgalaxy.system;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.platform.PGPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * Prueba de arranque automática (la usa GitHub en cada subida).
 *
 * Si el servidor se arranca con -Dpanthrixsgalaxy.smokeTest=true, en cuanto termina de cargar
 * escribe "SMOKE TEST OK" con las dimensiones cargadas y se apaga solo. Si algo falla al arrancar
 * (un mixin, un registro, un archivo de datos...), ese mensaje no aparece y GitHub lo marca en rojo.
 * Jugando normalmente no hace nada.
 */
public final class PGSmokeTest {

    public static boolean isEnabled() {
        return Boolean.getBoolean("panthrixsgalaxy.smokeTest");
    }

    /** Lo llaman Forge y Fabric cuando el servidor termina de arrancar. */
    public static void onServerStarted(MinecraftServer server) {
        if (!isEnabled()) {
            return;
        }
        StringBuilder levels = new StringBuilder();
        for (ServerLevel level : server.getAllLevels()) {
            levels.append(' ').append(level.dimension().location());
        }
        PanthrixsGalaxy.LOGGER.info("[Panthrixs Galaxy] SMOKE TEST OK ({}):{}", PGPlatform.getLoaderName(), levels);
        server.halt(false);
    }

    private PGSmokeTest() {
    }
}
