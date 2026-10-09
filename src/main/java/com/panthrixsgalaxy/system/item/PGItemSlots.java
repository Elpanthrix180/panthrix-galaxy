package com.panthrixsgalaxy.system.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Huecos para objetos dentro de una máquina (el combustible del generador, por ejemplo).
 *
 * Es del propio mod para que sirva en Forge y en Fabric. Cada cargador lo conecta con
 * tolvas y tuberías de otros mods (PGItemSlotsProvider).
 *
 * Se guarda con el MISMO formato que usaba Forge (ItemStackHandler): así los mundos
 * antiguos conservan el combustible que ya tenían las máquinas.
 */
public class PGItemSlots {

    private final NonNullList<ItemStack> stacks;

    public PGItemSlots(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public int getSlots() {
        return stacks.size();
    }

    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    /** ¿Se puede poner este objeto en este hueco? (las máquinas lo cambian) */
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    /** Se llama cada vez que cambia un hueco (las máquinas lo usan para guardarse). */
    protected void onContentsChanged(int slot) {
    }

    /** Mete objetos. Devuelve lo que NO ha cabido. */
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        boolean tooMany = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, tooMany ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(tooMany ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return tooMany ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    /** Saca objetos. Devuelve lo que ha salido. */
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack existing = stacks.get(slot);
        if (amount <= 0 || existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getCount());
        if (simulate) {
            return existing.copyWithCount(toExtract);
        }
        ItemStack extracted = existing.split(toExtract);
        if (existing.isEmpty()) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        onContentsChanged(slot);
        return extracted;
    }

    // ===== Guardado (mismo formato que el ItemStackHandler de Forge) =====

    public CompoundTag serializeNBT() {
        ListTag items = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag item = new CompoundTag();
                item.putInt("Slot", i);
                stacks.get(i).save(item);
                items.add(item);
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", items);
        tag.putInt("Size", stacks.size());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.of(item));
            }
        }
    }
}
