package com.panthrixseconomy.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** Una posición en una dimensión concreta. */
public record Loc(ResourceLocation dim, BlockPos pos) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dim", dim.toString());
        tag.putLong("Pos", pos.asLong());
        return tag;
    }

    public static Loc load(CompoundTag tag) {
        return new Loc(new ResourceLocation(tag.getString("Dim")), BlockPos.of(tag.getLong("Pos")));
    }

    public double distSqr(Loc other) {
        return dim.equals(other.dim) ? pos.distSqr(other.pos) : Double.MAX_VALUE;
    }

    public String shortText() {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
