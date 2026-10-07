package com.panthrixsgalaxy.entity.rocket;

/** Etapas del vuelo del cohete. */
public enum LaunchState {
    /** En la plataforma, esperando. */
    IDLE,
    /** Cuenta atrás de 10 segundos. */
    COUNTDOWN,
    /** Motores a tope: subiendo. */
    ASCENDING,
    /** Bajando con los retropropulsores (aterrizaje controlado). */
    DESCENDING,
    /** Sin combustible: cayendo sin control. */
    FALLING;

    public static LaunchState byId(int id) {
        LaunchState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }

    /** ¿Está en el aire? (no se puede bajar del cohete) */
    public boolean isFlying() {
        return this == ASCENDING || this == DESCENDING || this == FALLING;
    }
}
