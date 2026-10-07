package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cable energético. No guarda energía: cuando alguien le mete energía, la reparte en el
 * momento entre todas las máquinas conectadas a la red de cables.
 *
 * Cómo encuentra las máquinas: recorre todos los cables unidos (como seguir una tubería)
 * y apunta cada máquina que esté pegada a algún cable de la red.
 *
 * Reglas:
 *   - Nunca devuelve la energía a la máquina que la ha enviado.
 *   - Si la energía viene de una celda, no se la da a otras celdas (así no rebota entre ellas).
 *   - Como mucho MAX_TRANSFER FE por cada envío.
 */
public class PGCableBlockEntity extends BlockEntity {

    /** Máximo de energía que pasa por el cable en cada envío (por tick). */
    public static final int MAX_TRANSFER = 2_000;
    /** Máximo de cables que se recorren en una red (para que no se cuelgue el juego). */
    private static final int MAX_CABLES = 512;

    /** Un "enchufe" distinto por cada cara, para saber de dónde viene la energía. */
    private final Map<Direction, LazyOptional<IEnergyStorage>> sideCapabilities = new LinkedHashMap<>();
    private final LazyOptional<IEnergyStorage> unknownSideCapability = LazyOptional.of(() -> new CableInput(null));

    public PGCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sideCapabilities.put(direction, LazyOptional.of(() -> new CableInput(direction)));
        }
    }

    /** Una máquina pegada a la red: su posición y la dirección desde el cable hacia ella. */
    private record Endpoint(BlockPos pos, Direction direction) {
    }

    /** Busca todas las máquinas conectadas a esta red de cables. */
    private List<Endpoint> findEndpoints() {
        Map<BlockPos, Endpoint> endpoints = new LinkedHashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> toVisit = new ArrayDeque<>();
        toVisit.add(worldPosition);
        visited.add(worldPosition);

        while (!toVisit.isEmpty() && visited.size() <= MAX_CABLES) {
            BlockPos cablePos = toVisit.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = cablePos.relative(direction);
                if (level == null || !level.isLoaded(next) || visited.contains(next)) {
                    continue;
                }
                BlockEntity neighbor = level.getBlockEntity(next);
                if (neighbor instanceof PGCableBlockEntity) {
                    visited.add(next);
                    toVisit.add(next);
                } else if (neighbor != null && !endpoints.containsKey(next)
                        && neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).isPresent()) {
                    endpoints.put(next, new Endpoint(next, direction));
                }
            }
        }
        return new ArrayList<>(endpoints.values());
    }

    /**
     * Reparte energía entre las máquinas de la red.
     * @param fromSide cara del cable por la que entra la energía (null si no se sabe)
     * @return cuánta energía se ha entregado
     */
    private int distribute(int amount, @Nullable Direction fromSide, boolean simulate) {
        if (level == null || amount <= 0) {
            return 0;
        }
        BlockPos origin = fromSide == null ? null : worldPosition.relative(fromSide);
        boolean fromCell = origin != null && level.getBlockEntity(origin) instanceof PGEnergyCellBlockEntity;

        int remaining = Math.min(amount, MAX_TRANSFER);
        int offered = remaining;
        for (Endpoint endpoint : findEndpoints()) {
            if (endpoint.pos().equals(origin)) {
                continue; // no devolver la energía a quien la envía
            }
            BlockEntity target = level.getBlockEntity(endpoint.pos());
            if (target == null || (fromCell && target instanceof PGEnergyCellBlockEntity)) {
                continue;
            }
            IEnergyStorage storage = target.getCapability(ForgeCapabilities.ENERGY,
                    endpoint.direction().getOpposite()).orElse(null);
            if (storage == null || !storage.canReceive()) {
                continue;
            }
            remaining -= storage.receiveEnergy(remaining, simulate);
            if (remaining <= 0) {
                break;
            }
        }
        return offered - remaining;
    }

    /** El "enchufe" de una cara del cable: solo acepta energía y la reparte por la red. */
    private class CableInput implements IEnergyStorage {
        @Nullable
        private final Direction side;

        CableInput(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return distribute(maxReceive, side, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_TRANSFER;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return side == null ? unknownSideCapability.cast() : sideCapabilities.get(side).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sideCapabilities.values().forEach(LazyOptional::invalidate);
        unknownSideCapability.invalidate();
    }
}
