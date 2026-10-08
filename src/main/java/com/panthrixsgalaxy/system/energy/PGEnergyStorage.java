package com.panthrixsgalaxy.system.energy;


/**
 * Almacén de "PG Energía" de una máquina.
 *
 * Funciona igual en Forge y en Fabric (cada cargador lo conecta con su sistema
 * de energía, así es compatible con cables y máquinas de otros mods).
 *
 * Además de meter y sacar energía, tiene:
 *   - generate(): para que los generadores produzcan energía aunque no "reciban" de fuera.
 *   - take():     para sacar energía al cargar objetos a mano.
 *   - un aviso (onChanged) para que la máquina se guarde cada vez que cambia.
 */
public class PGEnergyStorage implements PGEnergyHandler {

    protected int energy;
    protected final int capacity;
    protected final int maxReceive;
    protected final int maxExtract;

    private final Runnable onChanged;

    public PGEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }
        int received = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (received > 0 && !simulate) {
            energy += received;
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }
        int extracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (extracted > 0 && !simulate) {
            energy -= extracted;
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

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
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
