package com.panthrixsgalaxy.fabric;

import com.panthrixsgalaxy.system.item.PGItemSlots;
import com.panthrixsgalaxy.system.item.PGItemSlotsProvider;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Solo Fabric: ofrece los huecos de las máquinas (PGItemSlots) a las tolvas y a las tuberías
 * de otros mods, con la "Transfer API" de Fabric. (En Forge: forge/PGForgeCapabilities.)
 */
public final class PGFabricItems {

    public static void register() {
        ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PGItemSlotsProvider provider) {
                PGItemSlots slots = provider.getItemSlots(side);
                return slots == null ? null : wrap(slots);
            }
            return null;
        });
    }

    private static @Nullable Storage<ItemVariant> wrap(PGItemSlots slots) {
        List<SlotStorage> parts = new ArrayList<>();
        for (int i = 0; i < slots.getSlots(); i++) {
            parts.add(new SlotStorage(slots, i));
        }
        return new CombinedStorage<>(parts);
    }

    /** Un hueco de la máquina visto por Fabric. */
    private static final class SlotStorage extends SingleStackStorage {

        private final PGItemSlots slots;
        private final int slot;

        SlotStorage(PGItemSlots slots, int slot) {
            this.slots = slots;
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return slots.getStackInSlot(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            slots.setStackInSlot(slot, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            return slots.isItemValid(slot, variant.toStack());
        }

        @Override
        protected int getCapacity(ItemVariant variant) {
            return Math.min(slots.getSlotLimit(slot), variant.getItem().getMaxStackSize());
        }
    }

    private PGFabricItems() {
    }
}
