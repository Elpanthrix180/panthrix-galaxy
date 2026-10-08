package com.panthrixsgalaxy.entity.rocket;

import com.panthrixsgalaxy.init.ModItems;
import net.minecraft.world.item.Item;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Niveles de cohete. Para añadir uno nuevo (por ejemplo para Júpiter) basta con
 * añadir una línea aquí, su objeto en ModItems y su textura.
 *
 *   reach         = hasta qué "distancia" puede viajar (1 = Luna, 2 = Marte...). Se usará en la Fase 11.
 *   fuelCapacity  = combustible máximo (mB).
 *   minLaunchFuel = combustible mínimo para poder despegar (mB).
 *   fuelPerTick   = combustible que gastan los motores en cada tick de ascenso.
 *   maxSpeed      = velocidad máxima de subida (bloques por tick; 1 = 20 bloques/segundo).
 *   acceleration  = cuánto aumenta la velocidad en cada tick.
 */
public enum RocketTier {
    /** Cohete básico: Tierra -> Luna. */
    BASIC(1, 2_000, 1_000, 2, 1.0, 0.010, () -> ModItems.PG_BASIC_ROCKET.get()),
    /** Cohete avanzado: Tierra -> Marte. */
    ADVANCED(2, 5_000, 2_500, 3, 1.4, 0.015, () -> ModItems.PG_ADVANCED_ROCKET.get());

    private final int reach;
    private final int fuelCapacity;
    private final int minLaunchFuel;
    private final int fuelPerTick;
    private final double maxSpeed;
    private final double acceleration;
    private final Supplier<Item> item;

    RocketTier(int reach, int fuelCapacity, int minLaunchFuel, int fuelPerTick, double maxSpeed, double acceleration,
               Supplier<Item> item) {
        this.reach = reach;
        this.fuelCapacity = fuelCapacity;
        this.minLaunchFuel = minLaunchFuel;
        this.fuelPerTick = fuelPerTick;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.item = item;
    }

    public int getReach() {
        return reach;
    }

    public int getFuelCapacity() {
        return fuelCapacity;
    }

    public int getMinLaunchFuel() {
        return minLaunchFuel;
    }

    public int getFuelPerTick() {
        return fuelPerTick;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAcceleration() {
        return acceleration;
    }

    /** El objeto que se recupera al recoger el cohete. */
    public Item getItem() {
        return item.get();
    }

    /** "basic", "advanced"... (para texturas y traducciones). */
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static RocketTier byId(int id) {
        RocketTier[] values = values();
        return id >= 0 && id < values.length ? values[id] : BASIC;
    }
}
