package com.panthrixsgalaxy.system.energy;

import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.platform.PGPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

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
        Optional<PGEnergyHandler> storage = itemStorage(target);
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
        Optional<PGEnergyHandler> storage = itemStorage(source);
        if (storage.isEmpty()) {
            return 0;
        }
        int available = storage.get().extractEnergy(target.getSpace(), true);
        int moved = target.generate(available);
        storage.get().extractEnergy(moved, false);
        return moved;
    }

    /**
     * La energía de un bloque por un lado: las máquinas del mod directamente,
     * y las de otros mods a través de Forge/Fabric. null = no tiene energía por ese lado.
     */
    public static @Nullable PGEnergyHandler findBlockEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        if (blockEntity instanceof PGEnergyProvider provider) {
            return provider.getEnergyHandler(side);
        }
        return PGPlatform.findBlockEnergy(level, pos, blockEntity, side);
    }

    /** La energía de un objeto: las del mod directamente, y las de otros mods a través de Forge/Fabric. */
    public static Optional<PGEnergyHandler> itemStorage(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof PGEnergyItem item) {
            return Optional.of(item.createEnergyHandler(stack));
        }
        return Optional.ofNullable(PGPlatform.findItemEnergy(stack));
    }

    private EnergyHelper() {
    }
}
