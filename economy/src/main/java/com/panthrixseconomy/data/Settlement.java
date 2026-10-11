package com.panthrixseconomy.data;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Una ciudad o un pueblo oficial. Su territorio es un círculo alrededor del centro. */
public final class Settlement {

    public final UUID id;
    public String name;
    public SettlementType type;
    public UUID countryId;
    public Loc center;
    public int radius;
    public long treasury;
    public UUID managerId;
    public String managerName;
    /** Dónde está su Núcleo del Pueblo (o null). */
    public Loc core;
    public final long serial;
    /** Empleos oficiales del pueblo (los crea un administrador). */
    public final List<Job> jobs = new ArrayList<>();

    /** Un empleo: salario por día de Minecraft, pagado desde la tesorería del pueblo. */
    public record Job(String name, long salary, int slots) {
    }

    public Settlement(UUID id, String name, SettlementType type, Loc center, int radius, long serial) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.center = center;
        this.radius = radius;
        this.serial = serial;
    }

    public boolean contains(Loc loc) {
        return loc.distSqr(center) <= (double) radius * radius;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        tag.putString("Type", type.name());
        if (countryId != null) {
            tag.putUUID("Country", countryId);
        }
        tag.put("Center", center.save());
        tag.putInt("Radius", radius);
        tag.putLong("Treasury", treasury);
        if (managerId != null) {
            tag.putUUID("Manager", managerId);
            tag.putString("ManagerName", managerName == null ? "" : managerName);
        }
        if (core != null) {
            tag.put("Core", core.save());
        }
        tag.putLong("Serial", serial);
        ListTag jl = new ListTag();
        for (Job j : jobs) {
            CompoundTag t = new CompoundTag();
            t.putString("Name", j.name());
            t.putLong("Salary", j.salary());
            t.putInt("Slots", j.slots());
            jl.add(t);
        }
        tag.put("Jobs", jl);
        return tag;
    }

    static Settlement load(CompoundTag tag) {
        SettlementType type;
        try {
            type = SettlementType.valueOf(tag.getString("Type"));
        } catch (IllegalArgumentException e) {
            type = SettlementType.TOWN;
        }
        Settlement s = new Settlement(tag.getUUID("Id"), tag.getString("Name"), type,
                Loc.load(tag.getCompound("Center")), tag.getInt("Radius"), tag.getLong("Serial"));
        s.countryId = tag.hasUUID("Country") ? tag.getUUID("Country") : null;
        s.treasury = tag.getLong("Treasury");
        if (tag.hasUUID("Manager")) {
            s.managerId = tag.getUUID("Manager");
            s.managerName = tag.getString("ManagerName");
        }
        s.core = tag.contains("Core") ? Loc.load(tag.getCompound("Core")) : null;
        for (Tag t : tag.getList("Jobs", Tag.TAG_COMPOUND)) {
            CompoundTag j = (CompoundTag) t;
            s.jobs.add(new Job(j.getString("Name"), j.getLong("Salary"), j.getInt("Slots")));
        }
        return s;
    }
}
