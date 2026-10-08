package com.panthrixsgalaxy.system.oxygen;

import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGBackpackItem.Tank;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuenta y gasta el oxígeno que lleva el jugador.
 *
 * Fuentes de oxígeno, en este orden:
 *   1. El depósito de oxígeno de la MOCHILA EQUIPADA (Fase 6).
 *   2. Las bombonas del inventario y de la mano secundaria (Fase 5).
 */
public final class OxygenHelper {

    /** Todas las bombonas de oxígeno que lleva el jugador en el inventario. */
    public static List<ItemStack> findTanks(Player player) {
        List<ItemStack> tanks = new ArrayList<>();
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof PGOxygenTankItem) {
                tanks.add(stack);
            }
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PGOxygenTankItem) {
                tanks.add(stack);
            }
        }
        return tanks;
    }

    /** Oxígeno total disponible (mochila + bombonas). */
    public static int getTotalOxygen(Player player) {
        int total = 0;
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        if (backpack.getItem() instanceof PGBackpackItem) {
            total += PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.OXYGEN);
        }
        for (ItemStack tank : findTanks(player)) {
            total += PGOxygenTankItem.getOxygen(tank);
        }
        return total;
    }

    /** Capacidad total (mochila + bombonas). */
    public static int getTotalCapacity(Player player) {
        int total = 0;
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        if (backpack.getItem() instanceof PGBackpackItem item) {
            total += item.getCapacity(PGBackpackItem.Tank.OXYGEN);
        }
        for (ItemStack tank : findTanks(player)) {
            total += ((PGOxygenTankItem) tank.getItem()).getCapacity();
        }
        return total;
    }

    /**
     * Gasta oxígeno: primero de la mochila y después de las bombonas.
     * @return cuánto se ha podido gastar realmente.
     */
    public static int consume(Player player, int amount) {
        int remaining = amount;

        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        if (backpack.getItem() instanceof PGBackpackItem) {
            int inBackpack = PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.OXYGEN);
            int used = Math.min(inBackpack, remaining);
            PGBackpackItem.setTank(backpack, PGBackpackItem.Tank.OXYGEN, inBackpack - used);
            remaining -= used;
        }

        for (ItemStack tank : findTanks(player)) {
            if (remaining <= 0) {
                break;
            }
            int inTank = PGOxygenTankItem.getOxygen(tank);
            int used = Math.min(inTank, remaining);
            PGOxygenTankItem.setOxygen(tank, inTank - used);
            remaining -= used;
        }
        return amount - remaining;
    }

    /**
     * Oxígeno de emergencia (Fase 7B): si la mochila equipada tiene agua y energía,
     * fabrica 1 unidad de oxígeno en el momento. Devuelve true si lo ha conseguido.
     */
    public static boolean tryEmergencyElectrolysis(Player player) {
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        if (!(backpack.getItem() instanceof PGBackpackItem)) {
            return false;
        }
        int water = PGBackpackItem.getTank(backpack, Tank.WATER);
        int energy = PGBackpackItem.getTank(backpack, Tank.ENERGY);
        if (water < Electrolysis.EMERGENCY_WATER_PER_OXYGEN || energy < Electrolysis.EMERGENCY_ENERGY_PER_OXYGEN) {
            return false;
        }
        PGBackpackItem.setTank(backpack, Tank.WATER, water - Electrolysis.EMERGENCY_WATER_PER_OXYGEN);
        PGBackpackItem.setTank(backpack, Tank.ENERGY, energy - Electrolysis.EMERGENCY_ENERGY_PER_OXYGEN);
        return true;
    }

    private OxygenHelper() {
    }
}
