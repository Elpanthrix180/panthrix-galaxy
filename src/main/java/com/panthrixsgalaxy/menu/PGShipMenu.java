package com.panthrixsgalaxy.menu;

import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import com.panthrixsgalaxy.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Panel de control de la nave: la bodega (3 o 6 filas, como un cofre) y los datos
 * de combustible, energía e integridad para dibujar las barras.
 *
 * Igual que la mochila: los números viajan en trozos de 16 bits (2 trozos por número).
 * 6 números (combustible, máx., energía, máx., integridad, máx.) x 2 trozos = 12 datos.
 */
public class PGShipMenu extends ChestMenu {

    private static final int VALUES = 6;
    private static final int DATA_COUNT = VALUES * 2;

    @Nullable
    private final PGShipEntity ship;
    private final ContainerData shipData;

    /** Constructor del SERVIDOR. */
    public PGShipMenu(int containerId, Inventory playerInventory, PGShipEntity ship, int rows) {
        this(containerId, playerInventory, new CargoView(ship.getCargo(), rows * 9), rows, ship, new ServerShipData(ship));
    }

    /** Constructor del CLIENTE. */
    public PGShipMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readVarInt());
    }

    private PGShipMenu(int containerId, Inventory playerInventory, int rows) {
        this(containerId, playerInventory, new SimpleContainer(rows * 9), rows, null, new SimpleContainerData(DATA_COUNT));
    }

    private PGShipMenu(int containerId, Inventory playerInventory, Container cargo, int rows,
                       @Nullable PGShipEntity ship, ContainerData data) {
        super(ModMenuTypes.SHIP.get(), containerId, playerInventory, cargo, rows);
        this.ship = ship;
        this.shipData = data;
        addDataSlots(data);
    }

    /** La nave de verdad (solo en el servidor; en la pantalla es null). */
    @Nullable
    public PGShipEntity getShip() {
        return ship;
    }

    public int getFuel() {
        return readNumber(0);
    }

    public int getMaxFuel() {
        return readNumber(1);
    }

    public int getEnergy() {
        return readNumber(2);
    }

    public int getMaxEnergy() {
        return readNumber(3);
    }

    public int getIntegrity() {
        return readNumber(4);
    }

    public int getMaxIntegrity() {
        return readNumber(5);
    }

    private int readNumber(int value) {
        int low = shipData.get(value * 2) & 0xFFFF;
        int high = shipData.get(value * 2 + 1) & 0xFFFF;
        return (high << 16) | low;
    }

    /** La ventana se cierra si te alejas de la nave o si desaparece. */
    @Override
    public boolean stillValid(Player player) {
        return ship == null || (!ship.isRemoved() && player.distanceToSqr(ship) < 64.0);
    }

    /** En el servidor: lee los datos de la nave y los parte en trozos. */
    private static class ServerShipData implements ContainerData {
        private final PGShipEntity ship;

        ServerShipData(PGShipEntity ship) {
            this.ship = ship;
        }

        @Override
        public int get(int index) {
            int number = switch (index / 2) {
                case 0 -> ship.getFuel();
                case 1 -> ship.getTier().getFuelCapacity();
                case 2 -> ship.getEnergy();
                case 3 -> ship.getTier().getEnergyCapacity();
                case 4 -> ship.getIntegrity();
                default -> ship.getTier().getMaxIntegrity();
            };
            return index % 2 == 0 ? number & 0xFFFF : (number >>> 16) & 0xFFFF;
        }

        @Override
        public void set(int index, int value) {
            // Solo lectura
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }

    /**
     * La bodega guarda siempre 54 huecos, pero la nave normal solo usa 27.
     * Esta "ventana" enseña solo los primeros huecos de la bodega.
     */
    private static class CargoView implements Container {
        private final SimpleContainer cargo;
        private final int size;

        CargoView(SimpleContainer cargo, int size) {
            this.cargo = cargo;
            this.size = size;
        }

        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < size; i++) {
                if (!cargo.getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return cargo.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return cargo.removeItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return cargo.removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            cargo.setItem(slot, stack);
        }

        @Override
        public void setChanged() {
            cargo.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < size; i++) {
                cargo.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
