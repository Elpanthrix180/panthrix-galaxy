package com.panthrixsgalaxy.menu;

import com.panthrixsgalaxy.item.PGBackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * El inventario de una mochila mientras está abierta.
 * Lee los objetos guardados en la mochila al abrirla y los vuelve a guardar
 * cada vez que algo cambia (así no se pierde nada aunque el juego se cierre).
 */
public class BackpackContainer extends SimpleContainer {

    /** Nombre del dato donde la mochila guarda su inventario. */
    public static final String INVENTORY_TAG = "Inventory";

    private final ItemStack backpack;
    private boolean loading;

    public BackpackContainer(ItemStack backpack, int size) {
        super(size);
        this.backpack = backpack;

        // Cargar los objetos guardados
        loading = true;
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        if (backpack.hasTag()) {
            ContainerHelper.loadAllItems(backpack.getTag().getCompound(INVENTORY_TAG), items);
        }
        for (int i = 0; i < size; i++) {
            setItem(i, items.get(i));
        }
        loading = false;
    }

    /** No se pueden meter mochilas dentro de mochilas. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof PGBackpackItem);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) {
            save();
        }
    }

    private void save() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < getContainerSize(); i++) {
            items.set(i, getItem(i));
        }
        CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, items);
        backpack.getOrCreateTag().put(INVENTORY_TAG, inventoryTag);
    }
}
