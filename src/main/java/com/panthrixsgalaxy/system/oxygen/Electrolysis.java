package com.panthrixsgalaxy.system.oxygen;

/**
 * Electrólisis: separar el agua en oxígeno e hidrógeno usando electricidad.
 * Así se puede conseguir oxígeno donde no hay aire (Luna, Marte...).
 *
 * Los "precios" de 1 unidad de oxígeno (= 1 segundo respirando):
 */
public final class Electrolysis {

    /** En el recargador eléctrico (eficiente). */
    public static final int WATER_PER_OXYGEN = 1;     // mB de agua
    public static final int ENERGY_PER_OXYGEN = 20;   // FE

    /** Emergencia: la mochila lo hace sola mientras respiras (menos eficiente). */
    public static final int EMERGENCY_WATER_PER_OXYGEN = 2;
    public static final int EMERGENCY_ENERGY_PER_OXYGEN = 50;

    private Electrolysis() {
    }
}
