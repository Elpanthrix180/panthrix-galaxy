package com.panthrixsgalaxy.system.energy;

import net.minecraftforge.energy.EnergyStorage;

/**
 * Almacén de "PG Energía" de una máquina.
 *
 * Usa el sistema de energía de Forge (FE = Forge Energy), así es compatible con
 * cables y máquinas de otros mods sin necesitar ninguna API externa.
 *
 * Además de lo que trae Forge, añade:
 *   - generate(): para que los generadores produzcan energía aunque no "reciban" de fuera.
 *   - take():     para sacar energía al cargar objetos a mano.
 *   - un aviso (onChanged) para que la máquina se guarde cada vez que cambia.
 */
public class PGEnergyStorage extends EnergyStorage {

    private final Runnable onChanged;

    public PGEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            onChanged.run();
        }
        return extracted;
    }

    /** Produce energía dentro de la máquina (sin límite de entrada). Devuelve cuánta cabía. */
    public int generate(int amount) {
        int added = Math.min(capacity - energy, Math.max(0, amount));
        if (added > 0) {
            energy += added;
            onChanged.run();
        }
        return added;
    }

    /** Saca energía sin límite de salida (para cargar objetos con clic derecho). */
    public int take(int amount) {
        int taken = Math.min(energy, Math.max(0, amount));
        if (taken > 0) {
            energy -= taken;
            onChanged.run();
        }
        return taken;
    }

    /** Espacio libre. */
    public int getSpace() {
        return capacity - energy;
    }

    /** Fija la energía directamente (al cargar la máquina desde el disco). */
    public void setEnergy(int amount) {
        energy = Math.max(0, Math.min(amount, capacity));
    }
}
