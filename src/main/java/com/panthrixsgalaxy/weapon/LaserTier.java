package com.panthrixsgalaxy.weapon;

import java.util.Locale;

/**
 * Tipos de arma láser (Fases 15 y 16B). Cada color es un arma distinta:
 *
 *   🔴 PISTOL (roja)   daño básico                       Luna
 *   🟢 GREEN           gasta MUY poca energía            Luna
 *   🔵 BLUE            más daño                          Marte
 *   🟣 PURPLE          PERFORA ARMADURAS                 Asteroides (osmio)
 *   🟡 GOLD            tecnología avanzada: ráfaga rápida que atraviesa
 *   ⚪ WHITE           extremadamente poderoso           Alien (necronita)
 *   RIFLE (cian)       lento y potente, atraviesa 2      Marte
 *
 *   capacity    = energía máxima del arma (FE)
 *   shotCost    = energía por disparo (FE)
 *   damage      = daño por impacto (1 = medio corazón)
 *   cooldown    = ticks entre disparos (20 ticks = 1 segundo)
 *   speed       = velocidad del rayo (bloques por tick; máx. 3,9)
 *   lifetime    = ticks que vive el rayo (alcance = speed x lifetime)
 *   pierce      = cuántos enemigos atraviesa además del primero
 *   knockback   = empujón al impactar
 *   armorPiercing = el daño ignora la armadura
 *   color       = color del rayo (0xRRGGBB)
 */
public enum LaserTier {
    /** Pistola láser roja: la básica. */
    PISTOL(20_000, 250, 5.0f, 8, 3.0, 20, 0, 0.0, false, 0xFF3A3A),
    /** Pistola láser verde: el mismo daño, pero 2,5 veces más disparos. */
    GREEN(25_000, 100, 5.0f, 8, 3.0, 20, 0, 0.0, false, 0x3AFF5A),
    /** Pistola láser azul: más daño. */
    BLUE(30_000, 350, 7.0f, 8, 3.2, 20, 0, 0.0, false, 0x3A6BFF),
    /** Pistola láser púrpura: perfora armaduras. */
    PURPLE(30_000, 350, 6.0f, 10, 3.2, 20, 0, 0.0, true, 0xB04AFF),
    /** Pistola láser dorada: dispara muy rápido y atraviesa a 1 enemigo. */
    GOLD(50_000, 400, 9.0f, 5, 3.5, 22, 1, 0.2, false, 0xFFC830),
    /** Pistola láser blanca: extremadamente poderosa. */
    WHITE(100_000, 800, 14.0f, 12, 3.8, 28, 3, 0.8, true, 0xFFFFFF),
    /** Rifle láser: lento, potente, atraviesa 2 enemigos. */
    RIFLE(60_000, 600, 11.0f, 20, 3.8, 24, 2, 0.5, false, 0x3AE6FF);

    private final int capacity;
    private final int shotCost;
    private final float damage;
    private final int cooldown;
    private final double speed;
    private final int lifetime;
    private final int pierce;
    private final double knockback;
    private final boolean armorPiercing;
    private final int color;

    LaserTier(int capacity, int shotCost, float damage, int cooldown, double speed, int lifetime, int pierce,
              double knockback, boolean armorPiercing, int color) {
        this.capacity = capacity;
        this.shotCost = shotCost;
        this.damage = damage;
        this.cooldown = cooldown;
        this.speed = speed;
        this.lifetime = lifetime;
        this.pierce = pierce;
        this.knockback = knockback;
        this.armorPiercing = armorPiercing;
        this.color = color;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getShotCost() {
        return shotCost;
    }

    public float getDamage() {
        return damage;
    }

    public int getCooldown() {
        return cooldown;
    }

    public double getSpeed() {
        return speed;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getPierce() {
        return pierce;
    }

    public double getKnockback() {
        return knockback;
    }

    public boolean isArmorPiercing() {
        return armorPiercing;
    }

    /** Tono del sonido: las armas más fuertes suenan más graves. */
    public float getSoundPitch() {
        return Math.max(0.9f, 2.2f - damage * 0.08f);
    }

    public int getColor() {
        return color;
    }

    /** Alcance aproximado en bloques. */
    public int getRange() {
        return (int) (speed * lifetime);
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
