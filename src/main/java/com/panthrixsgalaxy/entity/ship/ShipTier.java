package com.panthrixsgalaxy.entity.ship;

import com.panthrixsgalaxy.init.ModItems;
import net.minecraft.world.item.Item;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Niveles de nave espacial.
 *
 *   reach           = alcance (1 Luna, 2 Marte, 3 asteroides, 4-5 planetas exteriores y alienígenas)
 *   fuelCapacity    = combustible máximo (mB)
 *   energyCapacity  = energía máxima (FE): soporte vital
 *   maxIntegrity    = resistencia del casco
 *   cargoRows       = filas de la bodega (1 fila = 9 huecos)
 *   atmosphereSpeed = velocidad máxima dentro de un planeta (bloques/tick)
 *   spaceSpeed      = velocidad máxima en el Espacio
 *   acceleration    = empuje de los motores
 */
public enum ShipTier {
    /** Nave espacial: Luna, Marte y asteroides. */
    SHIP(3, 8_000, 100_000, 100, 3, 1.0, 2.5, 0.06, () -> ModItems.PG_SPACE_SHIP.get()),
    /** Nave avanzada: planetas exteriores y alienígenas. */
    ADVANCED(5, 20_000, 400_000, 200, 6, 1.4, 4.0, 0.09, () -> ModItems.PG_ADVANCED_SPACE_SHIP.get());

    private final int reach;
    private final int fuelCapacity;
    private final int energyCapacity;
    private final int maxIntegrity;
    private final int cargoRows;
    private final double atmosphereSpeed;
    private final double spaceSpeed;
    private final double acceleration;
    private final Supplier<Item> item;

    ShipTier(int reach, int fuelCapacity, int energyCapacity, int maxIntegrity, int cargoRows,
             double atmosphereSpeed, double spaceSpeed, double acceleration, Supplier<Item> item) {
        this.reach = reach;
        this.fuelCapacity = fuelCapacity;
        this.energyCapacity = energyCapacity;
        this.maxIntegrity = maxIntegrity;
        this.cargoRows = cargoRows;
        this.atmosphereSpeed = atmosphereSpeed;
        this.spaceSpeed = spaceSpeed;
        this.acceleration = acceleration;
        this.item = item;
    }

    public int getReach() {
        return reach;
    }

    public int getFuelCapacity() {
        return fuelCapacity;
    }

    public int getEnergyCapacity() {
        return energyCapacity;
    }

    public int getMaxIntegrity() {
        return maxIntegrity;
    }

    public int getCargoRows() {
        return cargoRows;
    }

    public double getAtmosphereSpeed() {
        return atmosphereSpeed;
    }

    public double getSpaceSpeed() {
        return spaceSpeed;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public Item getItem() {
        return item.get();
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ShipTier byId(int id) {
        ShipTier[] values = values();
        return id >= 0 && id < values.length ? values[id] : SHIP;
    }
}
