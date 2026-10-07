package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Celda energética: gran almacén de energía (1 000 000 FE).
 * Recibe la energía de los generadores que tenga al lado y carga objetos con clic derecho.
 * Al romperla conserva la energía guardada.
 * (En la Fase 7B los cables podrán sacar energía de ella hacia las máquinas.)
 */
public class PGEnergyCellBlockEntity extends PGEnergyBlockEntity {

    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER = 5_000;

    public PGEnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CELL.get(), pos, state, CAPACITY, TRANSFER, TRANSFER);
    }

    @Override
    public void serverTick() {
        // Solo almacena: no hace nada por sí misma.
    }

    @Override
    protected Component getStatus() {
        int percent = (int) (100L * energy.getEnergyStored() / energy.getMaxEnergyStored());
        return Component.translatable("status.panthrixsgalaxy.stored_percent", percent);
    }
}
