package com.panthrixseconomy.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Un movimiento del historial bancario. "identity" es el banco (país) donde ocurrió. */
public record Transaction(long time, String type, UUID from, UUID to, long amount, String identity, String note) {

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Time", time);
        tag.putString("Type", type);
        if (from != null) {
            tag.putUUID("From", from);
        }
        if (to != null) {
            tag.putUUID("To", to);
        }
        tag.putLong("Amount", amount);
        tag.putString("Identity", identity);
        tag.putString("Note", note == null ? "" : note);
        return tag;
    }

    static Transaction load(CompoundTag tag) {
        return new Transaction(tag.getLong("Time"), tag.getString("Type"),
                tag.hasUUID("From") ? tag.getUUID("From") : null,
                tag.hasUUID("To") ? tag.getUUID("To") : null,
                tag.getLong("Amount"), tag.getString("Identity"), tag.getString("Note"));
    }

    public boolean involves(UUID player) {
        return player.equals(from) || player.equals(to);
    }
}
