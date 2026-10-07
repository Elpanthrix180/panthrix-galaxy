package com.panthrixsgalaxy.weapon;

import java.util.Locale;

/**
 * Tipos de espada láser. El cristal de la hoja decide el color y la potencia.
 *
 *   damage        = daño extra encendida (el total = 1 + damage; espada de diamante = 7)
 *   attackSpeed   = velocidad de ataque (espadas normales -2.4; más alto = más rápida)
 *   capacity      = energía máxima (FE)
 *   hitCost       = energía por golpe
 *   fireSeconds   = segundos que prende al enemigo (0 = nada)
 *   color         = color de la hoja y de las chispas
 */
public enum LaserSwordTier {
    /** Hoja azul: cristal de selenita (Luna). */
    BLUE(6, -2.4f, 40_000, 100, 0, 0x3A8CFF),
    /** Hoja roja: cristal marciano (Marte). Quema a los enemigos. */
    RED(7, -2.4f, 80_000, 150, 3, 0xFF3A3A),
    /** Hoja morada: cristal cósmico y xenita (planetas lejanos). La más fuerte y rápida. */
    PURPLE(9, -2.0f, 160_000, 200, 0, 0xB04AFF);

    /** Energía que gasta encendida, cada segundo, aunque no golpees. */
    public static final int IDLE_COST_PER_SECOND = 20;
    /** Energía que gasta al desviar un ataque en guardia. */
    public static final int GUARD_COST = 50;

    private final int damage;
    private final float attackSpeed;
    private final int capacity;
    private final int hitCost;
    private final int fireSeconds;
    private final int color;

    LaserSwordTier(int damage, float attackSpeed, int capacity, int hitCost, int fireSeconds, int color) {
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.capacity = capacity;
        this.hitCost = hitCost;
        this.fireSeconds = fireSeconds;
        this.color = color;
    }

    public int getDamage() {
        return damage;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getHitCost() {
        return hitCost;
    }

    public int getFireSeconds() {
        return fireSeconds;
    }

    public int getColor() {
        return color;
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
