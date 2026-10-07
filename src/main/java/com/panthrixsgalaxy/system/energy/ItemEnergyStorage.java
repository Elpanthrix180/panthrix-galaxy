package com.panthrixsgalaxy.system.energy;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Energía guardada dentro de un objeto (batería). Lee y escribe el dato "Energy" del objeto,
 * así cualquier máquina (también de otros mods) puede cargarla o descargarla.
 */
public class ItemEnergyStorage implements IEnergyStorage {

    public static final String ENERGY_TAG = "Energy";

    private final ItemStack stack;
    private final int capacity;
    private final int maxTransfer;

    public ItemEnergyStorage(ItemStack stack, int capacity, int maxTransfer) {
        this.stack = stack;
        this.capacity = capacity;
        this.maxTransfer = maxTransfer;
    }

    public static int getEnergy(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(ENERGY_TAG) : 0;
    }

    private void setEnergy(int energy) {
        stack.getOrCreateTag().putInt(ENERGY_TAG, Math.max(0, Math.min(energy, capacity)));
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int stored = getEnergy(stack);
        int received = Math.min(capacity - stored, Math.min(maxTransfer, maxReceive));
        if (received > 0 && !simulate) {
            setEnergy(stored + received);
        }
        return Math.max(0, received);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int stored = getEnergy(stack);
        int extracted = Math.min(stored, Math.min(maxTransfer, maxExtract));
        if (extracted > 0 && !simulate) {
            setEnergy(stored - extracted);
        }
        return Math.max(0, extracted);
    }

    @Override
    public int getEnergyStored() {
        return getEnergy(stack);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
