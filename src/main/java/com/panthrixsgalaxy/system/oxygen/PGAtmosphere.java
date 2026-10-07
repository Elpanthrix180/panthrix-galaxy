package com.panthrixsgalaxy.system.oxygen;

import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * Decide dónde hay aire respirable y dónde no.
 *
 * - Las dimensiones sin atmósfera (espacio, Luna, Marte...) se añaden a AIRLESS_DIMENSIONS
 *   cuando las creemos (Fases 11, 12 y 13) con addAirlessDimension(...).
 * - Para probar el sistema en la Tierra existe el "modo de prueba de vacío":
 *   el comando /pgvacuum true hace que el jugador se comporte como si estuviera en el espacio.
 */
public final class PGAtmosphere {

    /** Nombre del dato guardado en el jugador para el modo de prueba. */
    public static final String VACUUM_TEST_TAG = "pg_vacuum_test";

    private static final Set<ResourceKey<Level>> AIRLESS_DIMENSIONS = new HashSet<>();

    /** Marca una dimensión como "sin aire respirable". */
    public static void addAirlessDimension(ResourceKey<Level> dimension) {
        AIRLESS_DIMENSIONS.add(dimension);
    }

    /** ¿Esta dimensión no tiene aire respirable? */
    public static boolean isAirlessDimension(Level level) {
        return AIRLESS_DIMENSIONS.contains(level.dimension());
    }

    /**
     * ¿Este jugador está ahora mismo en un lugar sin aire respirable?
     * Dentro de un vehículo con la cabina presurizada hay aire (cohete siempre; nave si tiene energía).
     */
    public static boolean isAirlessFor(Player player) {
        if (player.getVehicle() instanceof PGSpaceVehicle vehicle && vehicle.isPressurized()) {
            return false;
        }
        return isAirlessDimension(player.level()) || player.getPersistentData().getBoolean(VACUUM_TEST_TAG);
    }

    private PGAtmosphere() {
    }
}
