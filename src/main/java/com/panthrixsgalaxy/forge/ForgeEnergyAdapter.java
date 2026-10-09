package com.panthrixsgalaxy.forge;

import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import net.minecraftforge.energy.IEnergyStorage;

/** Traduce entre la energía de Forge (IEnergyStorage) y la del mod (PGEnergyHandler). Solo Forge. */
public final class ForgeEnergyAdapter {

    /** Energía de Forge (de otro mod) vista como la del mod. */
    public static PGEnergyHandler wrap(IEnergyStorage storage) {
        if (storage instanceof ToForge toForge) {
            return toForge.handler(); // es nuestra: no hace falta envolverla dos veces
        }
        return new PGEnergyHandler() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return storage.receiveEnergy(maxReceive, simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return storage.extractEnergy(maxExtract, simulate);
            }

            @Override
            public int getEnergyStored() {
                return storage.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return storage.getMaxEnergyStored();
            }

            @Override
            public boolean canExtract() {
                return storage.canExtract();
            }

            @Override
            public boolean canReceive() {
                return storage.canReceive();
            }
        };
    }

    /** Energía del mod ofrecida a Forge (a cables y máquinas de otros mods). */
    public record ToForge(PGEnergyHandler handler) implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return handler.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return handler.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return handler.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return handler.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return handler.canExtract();
        }

        @Override
        public boolean canReceive() {
            return handler.canReceive();
        }
    }

    private ForgeEnergyAdapter() {
    }
}
