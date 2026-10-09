package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.system.energy.PGEnergyItem;
import com.panthrixsgalaxy.system.energy.PGEnergyProvider;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
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
 * Así los cables, máquinas y cargadores de otros mods de Fabric pueden cargar y descargar las
 * máquinas y objetos del mod (y al revés).
 */
public final class PGFabricEnergy {

    /** Ofrece la energía de las máquinas y objetos del mod a los demás mods. */
    public static void register() {
        EnergyStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) ->
                blockEntity instanceof PGEnergyProvider provider ? toReborn(provider.getEnergyHandler(side)) : null);
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof PGEnergyItem item) {
                EnergyStorage.ITEM.registerForItems((stack, context) -> new ItemInContext(item, context), entry.get());
            }
        });
    }

    /** Energía de un bloque de OTRO mod vista como la del mod. */
    public static @Nullable PGEnergyHandler findBlockEnergy(Level level, BlockPos pos, BlockEntity blockEntity,
                                                            @Nullable Direction side) {
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, blockEntity.getBlockState(), blockEntity, side);
        return storage == null ? null : fromReborn(storage);
    }

    /**
     * Objeto de OTRO mod con energía (una batería, por ejemplo). En Fabric los objetos se cambian a
     * través de un "contexto"; aquí se usa uno temporal y al final se copian sus datos al objeto.
     */
    public static @Nullable PGEnergyHandler findItemEnergy(ItemStack stack) {
        if (EnergyStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack)) == null) {
            return null;
        }
        return new ForeignItem(stack);
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
            return Transaction.openNested(Transaction.getCurrentUnsafe());
        }
    }

    /** Un objeto del mod (batería, arma láser...) visto por otros mods a través de su contexto. */
    private static final class ItemInContext implements EnergyStorage {

        private final PGEnergyItem item;
        private final ContainerItemContext context;

        ItemInContext(PGEnergyItem item, ContainerItemContext context) {
            this.item = item;
            this.context = context;
        }

        /** Lo que haría el objeto con una copia de sí mismo (sin tocar el original). */
        private int simulate(boolean insert, long maxAmount, ItemStack[] result) {
            ItemVariant variant = context.getItemVariant();
            ItemStack copy = variant.toStack();
            PGEnergyHandler handler = item.createEnergyHandler(copy);
            int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int moved = insert ? handler.receiveEnergy(amount, false) : handler.extractEnergy(amount, false);
            result[0] = copy;
            return moved;
        }

        private long move(boolean insert, long maxAmount, TransactionContext transaction) {
            if (context.getAmount() != 1) {
                return 0; // solo objetos sueltos (las baterías no se apilan)
            }
            ItemStack[] result = new ItemStack[1];
            int moved = simulate(insert, maxAmount, result);
            if (moved <= 0) {
                return 0;
            }
            ItemVariant changed = ItemVariant.of(result[0]);
            return context.exchange(changed, 1, transaction) == 1 ? moved : 0;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return move(true, maxAmount, transaction);
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return move(false, maxAmount, transaction);
        }

        @Override
        public long getAmount() {
            return item.createEnergyHandler(context.getItemVariant().toStack()).getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return item.createEnergyHandler(context.getItemVariant().toStack()).getMaxEnergyStored();
        }
    }

    /** Un objeto de otro mod: cada operación usa un contexto temporal y luego copia el resultado. */
    private record ForeignItem(ItemStack stack) implements PGEnergyHandler {

        private int move(boolean insert, int amount, boolean simulate) {
            ContainerItemContext context = ContainerItemContext.withInitial(stack);
            EnergyStorage storage = EnergyStorage.ITEM.find(stack, context);
            if (storage == null) {
                return 0;
            }
            try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                long moved = insert ? storage.insert(amount, transaction) : storage.extract(amount, transaction);
                if (!simulate && moved > 0) {
                    transaction.commit();
                    // Copiar al objeto real lo que ha cambiado en el contexto
                    ItemStack changed = context.getItemVariant().toStack();
                    stack.setTag(changed.getTag());
                }
                return (int) moved;
            }
        }

        private EnergyStorage storage() {
            return EnergyStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return move(true, maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return move(false, maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            EnergyStorage storage = storage();
            return storage == null ? 0 : (int) Math.min(storage.getAmount(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            EnergyStorage storage = storage();
            return storage == null ? 0 : (int) Math.min(storage.getCapacity(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            EnergyStorage storage = storage();
            return storage != null && storage.supportsExtraction();
        }

        @Override
        public boolean canReceive() {
            EnergyStorage storage = storage();
            return storage != null && storage.supportsInsertion();
        }
    }

    private PGFabricEnergy() {
    }
}
