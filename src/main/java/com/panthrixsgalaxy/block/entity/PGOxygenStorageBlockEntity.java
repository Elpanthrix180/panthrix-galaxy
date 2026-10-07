package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Tanque de oxígeno de la base: guarda mucho oxígeno (reserva del distribuidor y depósito para tus bombonas). */
public class PGOxygenStorageBlockEntity extends BlockEntity {

    public static final int CAPACITY = 20_000;

    private int oxygen;

    public PGOxygenStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OXYGEN_STORAGE.get(), pos, state);
    }

    public int getOxygen() {
        return oxygen;
    }

    /** Mete oxígeno. Devuelve cuánto ha cabido. */
    public int fill(int amount) {
        int added = Math.min(amount, CAPACITY - oxygen);
        oxygen += added;
        setChanged();
        return added;
    }

    /** Saca oxígeno. Devuelve cuánto ha sacado. */
    public int extract(int amount) {
        int taken = Math.min(amount, oxygen);
        oxygen -= taken;
        setChanged();
        return taken;
    }

    /** Saca exactamente esta cantidad si la tiene. */
    public boolean drain(int amount) {
        if (oxygen < amount) {
            return false;
        }
        extract(amount);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Oxygen", oxygen);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        oxygen = Math.min(CAPACITY, tag.getInt("Oxygen"));
    }
}
