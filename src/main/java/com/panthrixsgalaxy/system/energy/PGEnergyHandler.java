package com.panthrixsgalaxy.system.energy;

/**
 * Algo que guarda energía (FE): una máquina, un cable, una batería...
 *
 * Es del propio mod, así sirve igual en Forge y en Fabric. Cada cargador lo conecta
 * con su sistema de energía para hablar con otros mods:
 *   - Forge: capability ENERGY (forge/PGForgeCapabilities).
 *   - Fabric: Team Reborn Energy (fabric/PGFabricEnergy).
 */
public interface PGEnergyHandler {

    /** Mete energía. Devuelve cuánta ha entrado (con simulate = true solo se calcula). */
    int receiveEnergy(int maxReceive, boolean simulate);

    /** Saca energía. Devuelve cuánta ha salido (con simulate = true solo se calcula). */
    int extractEnergy(int maxExtract, boolean simulate);

    int getEnergyStored();

    int getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
