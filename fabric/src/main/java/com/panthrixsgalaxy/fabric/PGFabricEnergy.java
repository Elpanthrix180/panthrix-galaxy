package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.system.energy.PGEnergyProvider;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * Solo Fabric: conecta la energía del mod con Team Reborn Energy, la energía estándar de Fabric.
 * Así los cables y máquinas de otros mods de Fabric pueden cargar y descargar las del mod.
 */
public final class PGFabricEnergy {

    /** Ofrece la energía de las máquinas del mod a los demás mods. */
    public static void register() {
        EnergyStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) ->
                blockEntity instanceof PGEnergyProvider provider ? toReborn(provider.getEnergyHandler(side)) : null);
    }

    /** Energía de un bloque de OTRO mod vista como la del mod. */
    public static @Nullable PGEnergyHandler findBlockEnergy(Level level, BlockPos pos, BlockEntity blockEntity,
                                                            @Nullable Direction side) {
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, blockEntity.getBlockState(), blockEntity, side);
        return storage == null ? null : fromReborn(storage);
    }

    /** Objetos de otros mods: necesitarían saber en qué hueco están; de momento solo los del mod. */
    public static @Nullable PGEnergyHandler findItemEnergy(ItemStack stack) {
        return null;
    }

    // ===== Traducción entre los dos sistemas =====

    private static @Nullable EnergyStorage toReborn(@Nullable PGEnergyHandler handler) {
        if (handler == null) {
            return null;
        }
        if (handler instanceof FromReborn fromReborn) {
            return fromReborn.storage();
        }
        return new ToReborn(handler);
    }

    private static PGEnergyHandler fromReborn(EnergyStorage storage) {
        if (storage instanceof ToReborn toReborn) {
            return toReborn.handler(); // es nuestra: no hace falta envolverla dos veces
        }
        return new FromReborn(storage);
    }

    /** La energía del mod ofrecida a Team Reborn Energy (los cambios se hacen al confirmar la transacción). */
    private record ToReborn(PGEnergyHandler handler) implements EnergyStorage {

        @Override
        public boolean supportsInsertion() {
            return handler.canReceive();
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            int amount = handler.receiveEnergy((int) Math.min(maxAmount, Integer.MAX_VALUE), true);
            if (amount > 0) {
                transaction.addOuterCloseCallback(result -> {
                    if (result.wasCommitted()) {
                        handler.receiveEnergy(amount, false);
                    }
                });
            }
            return amount;
        }

        @Override
        public boolean supportsExtraction() {
            return handler.canExtract();
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            int amount = handler.extractEnergy((int) Math.min(maxAmount, Integer.MAX_VALUE), true);
            if (amount > 0) {
                transaction.addOuterCloseCallback(result -> {
                    if (result.wasCommitted()) {
                        handler.extractEnergy(amount, false);
                    }
                });
            }
            return amount;
        }

        @Override
        public long getAmount() {
            return handler.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return handler.getMaxEnergyStored();
        }
    }

    /** Energía de Team Reborn Energy (otro mod) vista como la del mod. */
    private record FromReborn(EnergyStorage storage) implements PGEnergyHandler {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            try (Transaction transaction = open()) {
                long inserted = storage.insert(maxReceive, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return (int) inserted;
            }
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            try (Transaction transaction = open()) {
                long extracted = storage.extract(maxExtract, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return (int) extracted;
            }
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(storage.getAmount(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(storage.getCapacity(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return storage.supportsExtraction();
        }

        @Override
        public boolean canReceive() {
            return storage.supportsInsertion();
        }

        private static Transaction open() {
            return Transaction.isOpen() ? Transaction.openNested(Transaction.getCurrentUnsafe()) : Transaction.openOuter();
        }
    }

    private PGFabricEnergy() {
    }
}
