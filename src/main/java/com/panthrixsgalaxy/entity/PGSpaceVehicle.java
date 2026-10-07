package com.panthrixsgalaxy.entity;

import net.minecraft.core.BlockPos;

/**
 * Lo que tienen en común todos los vehículos espaciales (cohete, naves...).
 * Gracias a esto, el cielo del Espacio, la cabina presurizada y otras partes del mod
 * funcionan igual para cualquier vehículo, sin repetir código.
 */
public interface PGSpaceVehicle {

    /** Punto de llegada al Espacio desde la Tierra: los planetas se colocan contando desde aquí. */
    BlockPos getSpaceOrigin();

    /** ¿La cabina tiene aire respirable ahora mismo? */
    boolean isPressurized();

    /** ¿Está en vuelo? (no se puede salir del vehículo) */
    boolean isInFlight();
}
