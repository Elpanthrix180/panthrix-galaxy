package com.panthrixsgalaxy.system.oxygen;

/** Situación del oxígeno del jugador. Se envía a la pantalla para dibujar el indicador. */
public enum OxygenState {
    /** Hay aire respirable: no se consume oxígeno. */
    BREATHABLE,
    /** Sin aire, pero el casco y las bombonas funcionan. */
    OK,
    /** Queda poco oxígeno (menos de 1 minuto). */
    LOW,
    /** Sin oxígeno en las bombonas, pero la mochila lo fabrica con su agua y energía. */
    EMERGENCY,
    /** ¡Sin casco espacial! */
    NO_HELMET,
    /** Con casco, pero las bombonas están vacías. */
    NO_OXYGEN
}
