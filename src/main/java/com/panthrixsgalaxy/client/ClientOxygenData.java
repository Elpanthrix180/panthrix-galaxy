package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.system.oxygen.OxygenState;

/**
 * Copia en la pantalla del jugador de los datos de oxígeno que manda el servidor.
 * (Solo guarda números; no usa nada exclusivo del cliente, así es seguro en servidores.)
 */
public final class ClientOxygenData {

    private static OxygenState state = OxygenState.BREATHABLE;
    private static int oxygen;
    private static int capacity;
    private static int graceSecondsLeft;

    public static void update(OxygenState newState, int newOxygen, int newCapacity, int newGrace) {
        state = newState;
        oxygen = newOxygen;
        capacity = newCapacity;
        graceSecondsLeft = newGrace;
    }

    public static OxygenState getState() {
        return state;
    }

    public static int getOxygen() {
        return oxygen;
    }

    public static int getCapacity() {
        return capacity;
    }

    public static int getGraceSecondsLeft() {
        return graceSecondsLeft;
    }

    private ClientOxygenData() {
    }
}
