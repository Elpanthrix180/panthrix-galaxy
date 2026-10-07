package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.advancement.PGAdvancements;
import com.panthrixsgalaxy.block.PGEnergyBlock;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.init.ModBlockEntities;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import com.panthrixsgalaxy.system.oxygen.PGSealedRooms;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Distribuidor de oxígeno (Fase 19): llena de aire la sala sellada en la que está.
 *
 * Cada 2 segundos:
 *   1. Mide la sala (ver PGSealedRooms). Si hay una fuga, no hay aire.
 *   2. Paga el aire: primero con ENERGÍA (5 FE/t + 1 FE/t por cada 40 bloques de sala).
 *      Si no hay energía, gasta OXÍGENO de un Tanque de oxígeno pegado a él (reserva).
 *   3. Si ha podido pagar, la sala tiene aire: dentro se respira sin casco y sin traje.
 *
 * Funciona en la Luna, en Marte y en el Espacio (¡estaciones espaciales!). En la Tierra no hace falta.
 */
public class PGOxygenDistributorBlockEntity extends PGEnergyBlockEntity {

    public static final int CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 500;
    private static final int CHECK_INTERVAL = 40;

    private enum State { IDLE, SEALED, LEAK, NO_POWER, BREATHABLE }

    private State state = State.IDLE;
    private int volume;

    public PGOxygenDistributorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OXYGEN_DISTRIBUTOR.get(), pos, blockState, CAPACITY, MAX_RECEIVE, 0);
    }

    /** Energía por tick que necesita una sala de este tamaño. */
    public static int energyPerTick(int volume) {
        return PGConfig.distributorBaseCost.get() + volume / PGConfig.distributorBlocksPerFe.get();
    }

    @Override
    public void serverTick() {
        if (level == null || level.getGameTime() % CHECK_INTERVAL != 0) {
            return;
        }
        if (!PGAtmosphere.isAirlessDimension(level)) {
            update(State.BREATHABLE, null);
            return;
        }
        // Optimización (Fase 23): si no hay nadie cerca, nadie respira aquí. No se mide ni se gasta nada.
        double range = PGConfig.distributorPlayerRange.get();
        if (!level.hasNearbyAlivePlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, range)) {
            return;
        }
        LongSet room = PGSealedRooms.measure(level, worldPosition);
        if (room == null) {
            update(State.LEAK, null);
            return;
        }
        volume = room.size();
        int cost = energyPerTick(volume) * CHECK_INTERVAL;
        if (energy.getEnergyStored() >= cost) {
            energy.take(cost);
            update(State.SEALED, room);
        } else if (takeReserveOxygen(Math.max(1, volume / 25) * 2)) {
            update(State.SEALED, room);
        } else {
            update(State.NO_POWER, null);
        }
    }

    /** Saca oxígeno de un tanque de oxígeno pegado al distribuidor. */
    private boolean takeReserveOxygen(int amount) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof PGOxygenStorageBlockEntity tank
                    && tank.drain(amount)) {
                return true;
            }
        }
        return false;
    }

    private void update(State newState, LongSet room) {
        state = newState;
        PGSealedRooms.setRoom(level, worldPosition, room);
        boolean lit = newState == State.SEALED;
        if (getBlockState().getValue(PGEnergyBlock.LIT) != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(PGEnergyBlock.LIT, lit), 3);
        }
        if (lit && room != null) {
            awardBaseAdvancements(room);
        }
        // Un soplo de aire saliendo del distribuidor
        if (lit && level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, 3, 0.4, 0.4, 0.4, 0.01);
        }
    }

    /** Logros "Base lunar", "Colono" y "Estación orbital" para quien esté dentro de la sala sellada (Fase 22). */
    private void awardBaseAdvancements(LongSet room) {
        String advancement = level.dimension().equals(PGPlanets.MOON_LEVEL) ? "lunar_base"
                : level.dimension().equals(PGPlanets.MARS_LEVEL) ? "colonist"
                : level.dimension().equals(PGPlanets.SPACE) || level.dimension().equals(PGPlanets.ASTEROIDS_LEVEL) ? "orbital_station"
                : null;
        if (advancement == null) {
            return;
        }
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(24.0))) {
            if (room.contains(BlockPos.containing(player.getEyePosition()).asLong())) {
                PGAdvancements.award(player, advancement);
            }
        }
    }

    @Override
    protected Component getStatus() {
        return switch (state) {
            case SEALED -> Component.translatable("status.panthrixsgalaxy.distributor_sealed", volume, energyPerTick(volume));
            case LEAK -> Component.translatable("status.panthrixsgalaxy.distributor_leak", PGSealedRooms.maxVolume());
            case NO_POWER -> Component.translatable("status.panthrixsgalaxy.distributor_no_power");
            case BREATHABLE -> Component.translatable("status.panthrixsgalaxy.distributor_breathable");
            case IDLE -> Component.translatable("status.panthrixsgalaxy.distributor_starting");
        };
    }

    /** Al romperlo o descargarse el trozo de mundo, la sala se queda sin aire. */
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) { // solo el servidor guarda salas
            PGSealedRooms.setRoom(level, worldPosition, null);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide()) { // solo el servidor guarda salas
            PGSealedRooms.setRoom(level, worldPosition, null);
        }
    }
}
