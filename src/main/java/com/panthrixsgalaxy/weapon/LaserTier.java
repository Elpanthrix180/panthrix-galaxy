package com.panthrixsgalaxy.weapon;

import java.util.Locale;

/**
 * Tipos de arma láser.
 *
 *   capacity    = energía máxima del arma (FE)
 *   shotCost    = energía por disparo (FE)
 *   damage      = daño por impacto (1 = medio corazón)
 *   cooldown    = ticks entre disparos (20 ticks = 1 segundo)
 *   speed       = velocidad del rayo (bloques por tick)
 *   lifetime    = ticks que vive el rayo (alcance = speed x lifetime)
 *   pierce      = cuántos enemigos atraviesa además del primero
 *   knockback   = empujón al impactar
 *   color       = color del rayo (0xRRGGBB)
 */
public enum LaserTier {
    /** Pistola láser: rápida y ligera. Materiales de la Luna. */
    PISTOL(20_000, 250, 5.0f, 8, 3.0, 20, 0, 0.0, 0xFF3A3A),
    /** Rifle láser: lento, potente, atraviesa 2 enemigos. Materiales de Marte. */
    RIFLE(60_000, 600, 11.0f, 20, 3.8, 24, 2, 0.5, 0x3AE6FF);

    private final int capacity;
    private final int shotCost;
    private final float damage;
    private final int cooldown;
    private final double speed;
    private final int lifetime;
    private final int pierce;
    private final double knockback;
    private final int color;

    LaserTier(int capacity, int shotCost, float damage, int cooldown, double speed, int lifetime, int pierce,
              double knockback, int color) {
        this.capacity = capacity;
        this.shotCost = shotCost;
        this.damage = damage;
        this.cooldown = cooldown;
        this.speed = speed;
        this.lifetime = lifetime;
        this.pierce = pierce;
        this.knockback = knockback;
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
