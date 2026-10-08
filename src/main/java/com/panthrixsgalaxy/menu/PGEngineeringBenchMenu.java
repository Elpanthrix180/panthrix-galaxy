package com.panthrixsgalaxy.menu;

import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModMenuTypes;
import com.panthrixsgalaxy.init.ModRecipes;
import com.panthrixsgalaxy.recipe.PGEngineeringRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Ventana del Banco de Ingeniería Espacial: cuadrícula de 3x3, casilla de resultado
 * e inventario del jugador. Funciona como la mesa de trabajo, pero con las recetas
 * de tipo "panthrixsgalaxy:engineering".
 *
 * Números de los huecos: 0 = resultado, 1-9 = cuadrícula, 10-36 = inventario, 37-45 = barra rápida.
 */
public class PGEngineeringBenchMenu extends AbstractContainerMenu {

    private static final int RESULT_SLOT = 0;
    private static final int GRID_START = 1;
    private static final int GRID_END = 10;
    private static final int INVENTORY_START = 10;
    private static final int HOTBAR_START = 37;
    private static final int HOTBAR_END = 46;

    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;

    /** Constructor del CLIENTE (lo usa Forge al abrir la ventana). */
    public PGEngineeringBenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    /** Constructor del SERVIDOR. */
    public PGEngineeringBenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.ENGINEERING_BENCH.get(), containerId);
        this.access = access;

        addSlot(new EngineeringResultSlot(playerInventory.player, craftSlots, resultSlots, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(craftSlots, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    public CraftingContainer getCraftSlots() {
        return craftSlots;
    }

    /** Cada vez que cambia la cuadrícula, se busca qué receta coincide. */
    @Override
    public void slotsChanged(Container container) {
        if (container == craftSlots) {
            access.execute((level, pos) -> updateResult(level));
        }
    }

    private void updateResult(Level level) {
        if (level.isClientSide) {
            return;
        }
        Optional<PGEngineeringRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ENGINEERING_TYPE.get(), craftSlots, level);
        ItemStack result = recipe.map(r -> r.assemble(craftSlots, level.registryAccess())).orElse(ItemStack.EMPTY);
        resultSlots.setItem(0, result);
        broadcastChanges();
    }

    /** Al cerrar la ventana, lo que quede en la cuadrícula vuelve al jugador. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.PG_ENGINEERING_BENCH.get());
    }

    /** Mayús + clic: mover objetos rápido (copiado del funcionamiento de la mesa de trabajo). */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index == RESULT_SLOT) {
                access.execute((level, pos) -> stack.getItem().onCraftedBy(stack, level, player));
                if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, original);
            } else if (index >= INVENTORY_START && index < HOTBAR_END) {
                if (!moveItemStackTo(stack, GRID_START, GRID_END, false)) {
                    if (index < HOTBAR_START) {
                        if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
            if (index == RESULT_SLOT) {
                player.drop(stack, false);
            }
        }
        return original;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    /** Casilla de resultado: al coger el objeto, se gastan los ingredientes. */
    private static class EngineeringResultSlot extends Slot {
        private final Player player;
        private final CraftingContainer craftSlots;

        EngineeringResultSlot(Player player, CraftingContainer craftSlots, Container result, int x, int y) {
            super(result, 0, x, y);
            this.player = player;
            this.craftSlots = craftSlots;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player taker, ItemStack stack) {
            stack.onCraftedBy(taker.level(), taker, stack.getCount());
            NonNullList<ItemStack> remaining = taker.level().getRecipeManager()
                    .getRemainingItemsFor(ModRecipes.ENGINEERING_TYPE.get(), craftSlots, taker.level());
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack inSlot = craftSlots.getItem(i);
                ItemStack leftover = remaining.get(i);
                if (!inSlot.isEmpty()) {
                    craftSlots.removeItem(i, 1);
                    inSlot = craftSlots.getItem(i);
                }
                if (!leftover.isEmpty()) {
                    if (inSlot.isEmpty()) {
                        craftSlots.setItem(i, leftover);
                    } else if (ItemStack.isSameItemSameTags(inSlot, leftover)) {
                        leftover.grow(inSlot.getCount());
                        craftSlots.setItem(i, leftover);
                    } else if (!player.getInventory().add(leftover)) {
                        player.drop(leftover, false);
                    }
                }
            }
        }
    }
}
