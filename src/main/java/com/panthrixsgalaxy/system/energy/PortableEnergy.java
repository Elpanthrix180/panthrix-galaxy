package com.panthrixsgalaxy.system.energy;

import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Energía de los aparatos que llevas en la mano (armas láser, espadas láser...).
 *
 * Regla común: primero se gasta la energía del propio aparato; si no le queda bastante,
 * se saca del depósito de energía de la MOCHILA equipada.
 */
public final class PortableEnergy {

    /** Energía guardada en el propio aparato. */
    public static int ownEnergy(ItemStack stack) {
        return ItemEnergyStorage.getEnergy(stack);
    }

    /** Energía del depósito de la mochila equipada (0 si no llevas). */
    public static int backpackEnergy(Player player) {
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        return backpack.getItem() instanceof PGBackpackItem
                ? PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.ENERGY) : 0;
    }

    /** ¿Se puede pagar esta cantidad (del aparato o de la mochila)? */
    public static boolean canAfford(ItemStack stack, Player player, int amount) {
        return player.getAbilities().instabuild || ownEnergy(stack) >= amount || backpackEnergy(player) >= amount;
    }

    /** Gasta la energía. Devuelve false si no había bastante (y no gasta nada). En creativo es gratis. */
    public static boolean consume(ItemStack stack, Player player, int amount) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        int own = ownEnergy(stack);
        if (own >= amount) {
            stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, own - amount);
            return true;
        }
        int inBackpack = backpackEnergy(player);
        if (inBackpack >= amount) {
            PGBackpackItem.setTank(PGBackpackSlot.getEquipped(player), PGBackpackItem.Tank.ENERGY, inBackpack - amount);
            return true;
        }
        return false;
    }

    private PortableEnergy() {
    }
}
