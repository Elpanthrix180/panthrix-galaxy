package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.system.energy.EnergyHelper;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.system.energy.PGEnergyProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
public class PGCableBlockEntity extends BlockEntity implements PGEnergyProvider {

    /** Máximo de energía que pasa por el cable en cada envío (por tick). */
    public static final int MAX_TRANSFER = 2_000;
    /** Máximo de cables que se recorren en una red (para que no se cuelgue el juego). */
    private static final int MAX_CABLES = 512;

    /** Un "enchufe" distinto por cada cara, para saber de dónde viene la energía. */
    private final Map<Direction, PGEnergyHandler> sideInputs = new LinkedHashMap<>();
    private final PGEnergyHandler unknownSideInput = new CableInput(null);

    public PGCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sideInputs.put(direction, new CableInput(direction));
        }
    }

    /** Una máquina pegada a la red: su posición y la dirección desde el cable hacia ella. */
    private record Endpoint(BlockPos pos, Direction direction) {
    }

    // ===== Caché de la red (Fase 23: optimización) =====
    // Antes la red se recorría ENTERA cada vez que entraba energía (¡varias veces por tick!).
    // Ahora se recuerda la lista de máquinas y solo se vuelve a recorrer si algún cable o máquina
    // pegada a un cable cambia (networkVersion), o cada cableNetworkRefreshTicks por si acaso.

    /** Sube cada vez que se pone/quita un cable o cambia algo pegado a uno. */
    private static int networkVersion;

    @Nullable
    private List<Endpoint> cachedEndpoints;
    private int cachedVersion = -1;
    private long cachedAt;

    /** Avisa de que alguna red de cables ha cambiado (lo llama PGCableBlock). */
    public static void invalidateNetworks() {
        networkVersion++;
    }

    private List<Endpoint> endpoints() {
        long now = level == null ? 0 : level.getGameTime();
        if (cachedEndpoints == null || cachedVersion != networkVersion
                || now - cachedAt > PGConfig.cableNetworkRefreshTicks.get()) {
            cachedEndpoints = findEndpoints();
            cachedVersion = networkVersion;
            cachedAt = now;
        }
        return cachedEndpoints;
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
                        && EnergyHelper.findBlockEnergy(level, next, direction.getOpposite()) != null) {
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
        for (Endpoint endpoint : endpoints()) {
            if (endpoint.pos().equals(origin)) {
                continue; // no devolver la energía a quien la envía
            }
            BlockEntity target = level.getBlockEntity(endpoint.pos());
            if (target == null || (fromCell && target instanceof PGEnergyCellBlockEntity)) {
                continue;
            }
            PGEnergyHandler storage = EnergyHelper.findBlockEnergy(level, endpoint.pos(),
                    endpoint.direction().getOpposite());
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
    private class CableInput implements PGEnergyHandler {
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

    /** Cada cara tiene su "enchufe", para saber de dónde viene la energía. */
    @Override
    public @Nullable PGEnergyHandler getEnergyHandler(@Nullable Direction side) {
        return side == null ? unknownSideInput : sideInputs.get(side);
    }
}
