package com.panthrixsgalaxy.entity.rocket;

import com.panthrixsgalaxy.init.ModItems;
import net.minecraft.world.item.Item;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Niveles de cohete. Para añadir uno nuevo (por ejemplo para Júpiter) basta con
 * añadir una línea aquí, su objeto en ModItems y su textura.
 *
 * reach = hasta qué "distancia" puede viajar (1 = Luna, 2 = Marte...). Se usará en la Fase 11.
 */
public enum RocketTier {
    /** Cohete básico: Tierra -> Luna. */
    BASIC(1, 2_000, () -> ModItems.PG_BASIC_ROCKET.get()),
    /** Cohete avanzado: Tierra -> Marte. */
    ADVANCED(2, 5_000, () -> ModItems.PG_ADVANCED_ROCKET.get());

    private final int reach;
    private final int fuelCapacity;
    private final Supplier<Item> item;

    RocketTier(int reach, int fuelCapacity, Supplier<Item> item) {
        this.reach = reach;
        this.fuelCapacity = fuelCapacity;
        this.item = item;
    }

    public int getReach() {
        return reach;
    }

    /** Combustible máximo en mB. */
    public int getFuelCapacity() {
        return fuelCapacity;
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
