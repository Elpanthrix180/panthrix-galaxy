package com.panthrixseconomy.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Una sucursal bancaria. NO guarda su nombre: se genera siempre a partir del país
 * en el que está (o del país asignado a mano por un administrador).
 */
public final class Bank {

    public final UUID id;
    public Loc loc;
    /** País asignado a mano (o null = según la ubicación). */
    public UUID countryOverride;
    public final long serial;

    public Bank(UUID id, Loc loc, long serial) {
        this.id = id;
        this.loc = loc;
        this.serial = serial;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.put("Loc", loc.save());
        if (countryOverride != null) {
            tag.putUUID("Country", countryOverride);
        }
        tag.putLong("Serial", serial);
        return tag;
    }

    static Bank load(CompoundTag tag) {
        Bank b = new Bank(tag.getUUID("Id"), Loc.load(tag.getCompound("Loc")), tag.getLong("Serial"));
        b.countryOverride = tag.hasUUID("Country") ? tag.getUUID("Country") : null;
        return b;
    }
}
