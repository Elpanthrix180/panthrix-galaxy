package com.panthrixseconomy.data;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Un comercio oficial (lo crea un administrador). */
public final class Shop {

    public final UUID id;
    public String name;
    public Loc loc;
    public UUID managerId;
    public String managerName;
    /** Ofertas del comercio (las define un administrador). */
    public final List<Offer> offers = new ArrayList<>();

    /**
     * Una oferta: "sells" = el comercio vende el objeto al jugador; si no, se lo compra.
     * El objeto se guarda con su cantidad (p. ej. 16 de trigo por 5 ₱).
     */
    public record Offer(ItemStack item, long price, boolean sells) {
        CompoundTag save() {
            CompoundTag t = new CompoundTag();
            t.put("Item", item.save(new CompoundTag()));
            t.putLong("Price", price);
            t.putBoolean("Sells", sells);
            return t;
        }

        static Offer load(CompoundTag t) {
            return new Offer(ItemStack.of(t.getCompound("Item")), t.getLong("Price"), t.getBoolean("Sells"));
        }
    }

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
        ListTag list = new ListTag();
        for (Offer o : offers) {
            list.add(o.save());
        }
        tag.put("Offers", list);
        return tag;
    }

    static Shop load(CompoundTag tag) {
        Shop s = new Shop(tag.getUUID("Id"), tag.getString("Name"), Loc.load(tag.getCompound("Loc")));
        if (tag.hasUUID("Manager")) {
            s.managerId = tag.getUUID("Manager");
            s.managerName = tag.getString("ManagerName");
        }
        for (Tag t : tag.getList("Offers", Tag.TAG_COMPOUND)) {
            Offer o = Offer.load((CompoundTag) t);
            if (!o.item().isEmpty()) {
                s.offers.add(o);
            }
        }
        return s;
    }
}
