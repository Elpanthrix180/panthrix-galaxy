package com.panthrixseconomy.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Un comercio oficial (lo crea un administrador). */
public final class Shop {

    public final UUID id;
    public String name;
    public Loc loc;
    public UUID managerId;
    public String managerName;

    public Shop(UUID id, String name, Loc loc) {
        this.id = id;
        this.name = name;
        this.loc = loc;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        tag.put("Loc", loc.save());
        if (managerId != null) {
            tag.putUUID("Manager", managerId);
            tag.putString("ManagerName", managerName == null ? "" : managerName);
        }
        return tag;
    }

    static Shop load(CompoundTag tag) {
        Shop s = new Shop(tag.getUUID("Id"), tag.getString("Name"), Loc.load(tag.getCompound("Loc")));
        if (tag.hasUUID("Manager")) {
            s.managerId = tag.getUUID("Manager");
            s.managerName = tag.getString("ManagerName");
        }
        return s;
    }
}
