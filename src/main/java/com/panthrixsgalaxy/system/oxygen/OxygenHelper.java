package com.panthrixsgalaxy.system.oxygen;

import com.panthrixsgalaxy.item.PGOxygenTankItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuenta y gasta el oxígeno de las bombonas que lleva el jugador.
 * Busca en el inventario principal y en la mano secundaria.
 * (En la Fase 6 añadiremos aquí los depósitos de la mochila.)
 */
public final class OxygenHelper {

    /** Todas las bombonas de oxígeno que lleva el jugador. */
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

    /** Oxígeno total disponible (suma de todas las bombonas). */
    public static int getTotalOxygen(Player player) {
        int total = 0;
        for (ItemStack tank : findTanks(player)) {
            total += PGOxygenTankItem.getOxygen(tank);
        }
        return total;
    }

    /** Capacidad total (suma del máximo de todas las bombonas). */
    public static int getTotalCapacity(Player player) {
        int total = 0;
        for (ItemStack tank : findTanks(player)) {
            total += ((PGOxygenTankItem) tank.getItem()).getCapacity();
        }
        return total;
    }

    /**
     * Gasta oxígeno, empezando por la primera bombona que tenga.
     * @return cuánto se ha podido gastar realmente.
     */
    public static int consume(Player player, int amount) {
        int remaining = amount;
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

    private OxygenHelper() {
    }
}
