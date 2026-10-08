package com.panthrixsgalaxy.system.oxygen;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.init.ModTags;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SALAS SELLADAS: el aire de las bases y estaciones espaciales.
 *
 * Un distribuidor de oxígeno "llena de aire" la sala en la que está. Para saber cuál es,
 * se expande desde cada lado del distribuidor bloque a bloque por todos los huecos (aire,
 * antorchas, camas...) hasta chocar con paredes:
 *   - Si la sala está cerrada y es pequeña (como mucho maxVolume() bloques) → SELLADA: hay aire.
 *   - Si el aire "se escapa" (un agujero, una puerta abierta, una losa...) → FUGA: no hay aire.
 *
 * ¿Qué bloques cierran una sala? Los bloques enteros (piedra, paneles, cristal...), las
 * puertas y trampillas CERRADAS y los del tag panthrixsgalaxy:airtight. Las losas, escaleras,
 * vallas, paneles de cristal finos... NO cierran (el aire pasa por el hueco).
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGSealedRooms {

    /** Tamaño máximo de una sala sellada (por distribuidor), de la configuración. Por defecto 2048 (unos 12 x 12 x 14). */
    public static int maxVolume() {
        return PGConfig.maxRoomVolume.get();
    }

    /** Salas con aire de cada dimensión: posición del distribuidor -> bloques de su sala. */
    private static final Map<ResourceKey<Level>, Map<BlockPos, LongSet>> ROOMS = new ConcurrentHashMap<>();

    /**
     * Mide la sala (o salas) de un distribuidor: los bloques con aire, o null si todas tienen fuga.
     * Cada lado del distribuidor se mide por separado, así puede estar EN una pared o en el techo:
     * da aire al lado cerrado (dentro) aunque el otro lado sea el exterior.
     */
    @Nullable
    public static LongSet measure(Level level, BlockPos source) {
        LongSet rooms = new LongOpenHashSet();
        LongSet leaking = new LongOpenHashSet();
        for (Direction direction : Direction.values()) {
            BlockPos start = source.relative(direction);
            long key = start.asLong();
            if (blocksAir(level, start) || rooms.contains(key) || leaking.contains(key)) {
                continue;
            }
            LongSet region = new LongOpenHashSet();
            if (flood(level, start, region)) {
                rooms.addAll(region);
            } else {
                leaking.addAll(region);
            }
        }
        return rooms.isEmpty() ? null : rooms;
    }

    /** Se expande desde un bloque. Devuelve true si la zona está cerrada y no pasa de maxVolume(). */
    private static boolean flood(Level level, BlockPos start, LongSet region) {
        int maxVolume = maxVolume();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        region.add(start.asLong());
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (region.size() > maxVolume || !level.isLoaded(pos) || level.isOutsideBuildHeight(pos)) {
                return false; // demasiado grande o abierta al exterior
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (region.contains(next.asLong())) {
                    continue;
                }
                if (!level.isLoaded(next)) {
                    return false; // no se cargan trozos nuevos: se cuenta como fuga
                }
                if (!blocksAir(level, next)) {
                    region.add(next.asLong());
                    queue.add(next);
                }
            }
        }
        return true;
    }

    /** ¿Este bloque es una pared para el aire? */
    public static boolean blocksAir(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModTags.Blocks.AIRTIGHT)) {
            return true;
        }
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock) {
            return !state.getValue(state.getBlock() instanceof DoorBlock ? DoorBlock.OPEN : TrapDoorBlock.OPEN);
        }
        return state.isCollisionShapeFullBlock(level, pos);
    }

    /** Guarda (o quita, con null) la sala de un distribuidor. */
    public static void setRoom(Level level, BlockPos distributor, @Nullable LongSet room) {
        Map<BlockPos, LongSet> rooms = ROOMS.computeIfAbsent(level.dimension(), key -> new ConcurrentHashMap<>());
        if (room == null) {
            rooms.remove(distributor);
        } else {
            rooms.put(distributor.immutable(), room);
        }
    }

    /** ¿Hay aire de un distribuidor en este bloque? */
    public static boolean isPressurized(Level level, BlockPos pos) {
        Map<BlockPos, LongSet> rooms = ROOMS.get(level.dimension());
        if (rooms == null || rooms.isEmpty()) {
            return false;
        }
        long key = pos.asLong();
        for (LongSet room : rooms.values()) {
            if (room.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /** Al cerrar el mundo se olvidan las salas (los distribuidores las vuelven a medir al cargar). */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ROOMS.clear();
    }

    private PGSealedRooms() {
    }
}
