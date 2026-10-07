package com.panthrixsgalaxy.system.energy;

import com.panthrixsgalaxy.item.PGBackpackItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Optional;

/**
 * Pasar energía entre una máquina y un objeto (batería, mochila o cualquier objeto
 * con energía de otros mods).
 */
public final class EnergyHelper {

    /** ¿Este objeto puede guardar energía? */
    public static boolean holdsEnergy(ItemStack stack) {
        return stack.getItem() instanceof PGBackpackItem || itemStorage(stack).isPresent();
    }

    /** Carga el objeto con energía de la máquina. Devuelve cuánta energía se ha pasado. */
    public static int chargeItem(PGEnergyStorage source, ItemStack target) {
        if (target.isEmpty() || source.getEnergyStored() <= 0) {
            return 0;
        }
        if (target.getItem() instanceof PGBackpackItem backpack) {
            int current = PGBackpackItem.getTank(target, PGBackpackItem.Tank.ENERGY);
            int space = backpack.getCapacity(PGBackpackItem.Tank.ENERGY) - current;
            int moved = source.take(space);
            PGBackpackItem.setTank(target, PGBackpackItem.Tank.ENERGY, current + moved);
            return moved;
        }
        Optional<IEnergyStorage> storage = itemStorage(target);
        if (storage.isEmpty()) {
            return 0;
        }
        int accepted = storage.get().receiveEnergy(source.getEnergyStored(), true);
        int moved = source.take(accepted);
        storage.get().receiveEnergy(moved, false);
        return moved;
    }

    /** Descarga el objeto dentro de la máquina. Devuelve cuánta energía se ha pasado. */
    public static int dischargeItem(PGEnergyStorage target, ItemStack source) {
        if (source.isEmpty() || target.getSpace() <= 0) {
            return 0;
        }
        if (source.getItem() instanceof PGBackpackItem) {
            int current = PGBackpackItem.getTank(source, PGBackpackItem.Tank.ENERGY);
            int moved = target.generate(current);
            PGBackpackItem.setTank(source, PGBackpackItem.Tank.ENERGY, current - moved);
            return moved;
        }
        Optional<IEnergyStorage> storage = itemStorage(source);
        if (storage.isEmpty()) {
            return 0;
        }
        int available = storage.get().extractEnergy(target.getSpace(), true);
        int moved = target.generate(available);
        storage.get().extractEnergy(moved, false);
        return moved;
    }

    private static Optional<IEnergyStorage> itemStorage(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).resolve();
    }

    private EnergyHelper() {
    }
}
