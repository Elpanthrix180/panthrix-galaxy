package com.panthrixsgalaxy.weapon;

import java.util.Locale;

/**
 * Tipos de espada láser (Fases 16 y 16B). El cristal de la hoja decide el color, la potencia
 * y su habilidad especial:
 *
 *   🔵 BLUE    equilibrada (como diamante)                      Luna
 *   🟢 GREEN   gasta MUY poca energía                           Luna
 *   🔴 RED     QUEMA a los enemigos                             Marte
 *   🟣 PURPLE  PERFORA ARMADURAS (+3 de daño que la ignora)     Asteroides (osmio)
 *   🟡 GOLD    muy rápida                                       Xenita + tecnología alienígena
 *   ⚪ WHITE   muy fuerte; los enemigos BRILLAN y salen volando  Cristal cósmico + tecnología alienígena
 *   ⚫ BLACK   la más poderosa; ROBA VIDA y marchita             Necronita (jefe) + tecnología alienígena
 *
 *   damage      = daño extra encendida (el total = 1 + damage; espada de diamante = 7)
 *   attackSpeed = velocidad de ataque (espadas normales -2.4; más alto = más rápida)
 *   capacity    = energía máxima (FE)
 *   hitCost     = energía por golpe
 *   idleCost    = energía por segundo encendida, aunque no golpees
 *   special     = habilidad especial
 *   color       = color de la hoja y de las chispas
 */
public enum LaserSwordTier {
    BLUE(6, -2.4f, 40_000, 100, 20, Special.NONE, 0x3A8CFF),
    GREEN(5, -2.4f, 40_000, 40, 8, Special.NONE, 0x3AFF5A),
    RED(7, -2.4f, 80_000, 150, 20, Special.FIRE, 0xFF3A3A),
    PURPLE(7, -2.4f, 100_000, 150, 20, Special.PIERCE, 0xB04AFF),
    GOLD(8, -1.8f, 120_000, 150, 25, Special.NONE, 0xFFC830),
    WHITE(10, -2.2f, 200_000, 250, 30, Special.GLOW, 0xF4F8FF),
    BLACK(13, -2.0f, 300_000, 300, 30, Special.VOID, 0x1A0A24);

    /** Habilidades especiales de las hojas. */
    public enum Special {
        NONE,
        /** Prende fuego 3 segundos. */
        FIRE,
        /** +3 de daño que atraviesa la armadura. */
        PIERCE,
        /** El enemigo brilla (se ve a través de las paredes) y sale despedido. */
        GLOW,
        /** Marchitamiento y recuperas vida (el 25 % del daño). */
        VOID
    }

    /** Energía que gasta al desviar un ataque en guardia. */
    public static final int GUARD_COST = 50;

    private final int damage;
    private final float attackSpeed;
    private final int capacity;
    private final int hitCost;
    private final int idleCost;
    private final Special special;
    private final int color;

    LaserSwordTier(int damage, float attackSpeed, int capacity, int hitCost, int idleCost, Special special, int color) {
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.capacity = capacity;
        this.hitCost = hitCost;
        this.idleCost = idleCost;
        this.special = special;
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

    /** Energía por segundo encendida. */
    public int getIdleCost() {
        return idleCost;
    }

    public Special getSpecial() {
        return special;
    }

    public int getColor() {
        return color;
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
