package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Celda energética: gran almacén de energía (1 000 000 FE).
 * Recibe la energía de los generadores o cables que tenga al lado y carga objetos con clic derecho.
 * Envía su energía por los cables conectados (no directamente a otras máquinas pegadas).
 * Al romperla conserva la energía guardada.
 */
public class PGEnergyCellBlockEntity extends PGEnergyBlockEntity {

    public static final int CAPACITY = 1_000_000;
    public static final int TRANSFER = 5_000;

    public PGEnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CELL.get(), pos, state, CAPACITY, TRANSFER, TRANSFER);
    }

    @Override
    public void serverTick() {
        // Envía su energía SOLO a los cables que tenga al lado (Fase 7B).
        // A otras máquinas pegadas no les envía: así no se vacía en otra celda vecina.
        if (level == null || energy.getEnergyStored() <= 0) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor instanceof PGCableBlockEntity cable) {
                PGEnergyHandler input = cable.getEnergyHandler(direction.getOpposite());
                if (input != null && energy.getEnergyStored() > 0) {
                    int sent = input.receiveEnergy(Math.min(TRANSFER, energy.getEnergyStored()), false);
                    energy.take(sent);
                }
            }
        }
    }

    @Override
    protected Component getStatus() {
        int percent = (int) (100L * energy.getEnergyStored() / energy.getMaxEnergyStored());
        return Component.translatable("status.panthrixsgalaxy.stored_percent", percent);
    }
}
