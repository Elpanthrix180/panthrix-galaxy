package com.panthrixseconomy.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Un país oficial. El nombre se puede cambiar: todo lo demás se enlaza por su id. */
public final class Country {

    public final UUID id;
    public String name;
    /** "de", "del", "de la"... o null = automático según el nombre. */
    public String article;
    public long treasury;
    public int taxPercent;
    public final long serial;

    public Country(UUID id, String name, long serial) {
        this.id = id;
        this.name = name;
        this.serial = serial;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        if (article != null) {
            tag.putString("Article", article);
        }
        tag.putLong("Treasury", treasury);
        tag.putInt("Tax", taxPercent);
        tag.putLong("Serial", serial);
        return tag;
    }

    static Country load(CompoundTag tag) {
        Country c = new Country(tag.getUUID("Id"), tag.getString("Name"), tag.getLong("Serial"));
        c.article = tag.contains("Article") ? tag.getString("Article") : null;
        c.treasury = tag.getLong("Treasury");
        c.taxPercent = tag.getInt("Tax");
        return c;
    }
}
