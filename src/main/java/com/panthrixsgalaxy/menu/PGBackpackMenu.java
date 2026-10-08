package com.panthrixsgalaxy.menu;

import com.panthrixsgalaxy.init.ModMenuTypes;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGBackpackItem.Tank;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * La ventana de la mochila equipada.
 *
 * Usa la misma distribución de huecos que un cofre de Minecraft (1 a 4 filas) y
 * además envía a la pantalla los valores de los 4 depósitos para dibujar el panel.
 *
 * Hay dos constructores:
 *   - el del SERVIDOR, que conoce la mochila de verdad;
 *   - el del CLIENTE (la pantalla), que solo sabe cuántas filas tiene.
 */
public class PGBackpackMenu extends ChestMenu {

    /**
     * Minecraft envía los datos de las ventanas en trozos de 16 bits (máximo 65 535),
     * pero la energía puede llegar a 200 000. Por eso cada número viaja en 2 trozos.
     * 4 depósitos x (cantidad + máximo) x 2 trozos = 16 datos.
     */
    private static final int DATA_COUNT = Tank.values().length * 4;

    private final ItemStack backpack;
    private final ContainerData tankData;
    private final int backpackSize;

    /** Constructor del SERVIDOR. */
    public PGBackpackMenu(int containerId, Inventory playerInventory, ItemStack backpack, int rows) {
        super(ModMenuTypes.BACKPACK.get(), containerId, playerInventory, new BackpackContainer(backpack, rows * 9), rows);
        this.backpack = backpack;
        this.backpackSize = rows * 9;
        this.tankData = new ServerTankData(backpack);
        addDataSlots(tankData);
    }

    /** Constructor del CLIENTE (lo usa Forge al abrir la ventana en la pantalla). */
    public PGBackpackMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readVarInt());
    }

    private PGBackpackMenu(int containerId, Inventory playerInventory, int rows) {
        super(ModMenuTypes.BACKPACK.get(), containerId, playerInventory, new SimpleContainer(rows * 9), rows);
        this.backpack = ItemStack.EMPTY;
        this.backpackSize = rows * 9;
        this.tankData = new SimpleContainerData(DATA_COUNT);
        addDataSlots(tankData);
    }

    // ----- Datos de los depósitos (para la pantalla) -----

    public int getTankAmount(Tank tank) {
        return readNumber(tank.ordinal() * 4);
    }

    public int getTankCapacity(Tank tank) {
        return readNumber(tank.ordinal() * 4 + 2);
    }

    /** Junta los dos trozos de 16 bits en un número. */
    private int readNumber(int index) {
        int low = tankData.get(index) & 0xFFFF;
        int high = tankData.get(index + 1) & 0xFFFF;
        return (high << 16) | low;
    }

    /** En el servidor: lee los depósitos directamente de la mochila y los parte en trozos. */
    private static class ServerTankData implements ContainerData {
        private final ItemStack backpack;

        ServerTankData(ItemStack backpack) {
            this.backpack = backpack;
        }

        @Override
        public int get(int index) {
            if (!(backpack.getItem() instanceof PGBackpackItem item)) {
                return 0;
            }
            Tank tank = Tank.values()[index / 4];
            int part = index % 4;
            int value = part < 2 ? PGBackpackItem.getTank(backpack, tank) : item.getCapacity(tank);
            return part % 2 == 0 ? value & 0xFFFF : (value >>> 16) & 0xFFFF;
        }

        @Override
        public void set(int index, int value) {
            // Los datos solo se cambian desde la mochila, no desde la ventana.
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }

    // ----- Protecciones -----

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
        returnNestedBackpacks(player);
    }

    /** No se pueden meter mochilas dentro de la mochila: se devuelven al inventario. */
    private void returnNestedBackpacks(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        for (int i = 0; i < backpackSize; i++) {
            ItemStack stack = getContainer().getItem(i);
            if (stack.getItem() instanceof PGBackpackItem) {
                getContainer().setItem(i, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    /** La ventana se cierra si la mochila deja de estar equipada. */
    @Override
    public boolean stillValid(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        return !backpack.isEmpty() && PGBackpackSlot.getEquipped(player) == backpack;
    }
}
