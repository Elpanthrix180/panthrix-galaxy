package com.panthrixseconomy.plaza;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Arrays;

/**
 * Comprueba si un sitio es seguro para la Gran Plaza:
 *  - terreno casi llano, en tierra firme y por encima del nivel del mar
 *  - sin lava, agua, cuevas ni huecos bajo el suelo
 *  - sin bloques colocados por jugadores u otros mods (solo terreno natural y vegetación)
 *  - sin estructuras (aldeas, estructuras de mods...) ni cofres/hornos/etc.
 * Si algo falla, devuelve el motivo y no se toca nada.
 */
public final class PlazaSite {

    public static final int R = PlazaBuilder.R;
    public static final int CLEAR_HEIGHT = 16;
    /** Desnivel máximo permitido: primero se busca un sitio casi llano y, si no hay, uno con más desnivel. */
    public static final int STRICT_STEP = 3;
    public static final int RELAXED_STEP = 7;

    /** Resultado: base (altura del suelo) o motivo del rechazo. */
    public record Result(BlockPos center, String problem) {
        public boolean ok() {
            return problem == null;
        }
    }

    public static Result check(ServerLevel level, int cx, int cz) {
        return check(level, cx, cz, STRICT_STEP);
    }

    public static Result check(ServerLevel level, int cx, int cz, int maxStep) {
        // Cargar (y generar si hace falta) los chunks de la zona
        for (int x = (cx - R) >> 4; x <= (cx + R) >> 4; x++) {
            for (int z = (cz - R) >> 4; z <= (cz + R) >> 4; z++) {
                level.getChunk(x, z);
            }
        }
        int size = 2 * R + 1;
        int[] heights = new int[size * size];
        int i = 0;
        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                // Bajar por troncos, plantas, nieve... hasta el suelo de verdad
                int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx + dx, cz + dz) - 1;
                BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(cx + dx, h, cz + dz);
                while (h > level.getMinBuildHeight() + 1 && isNaturalAbove(level.getBlockState(probe)) && level.getBlockState(probe).getFluidState().isEmpty()) {
                    probe.setY(--h);
                }
                heights[i++] = h;
            }
        }
        int[] sorted = heights.clone();
        Arrays.sort(sorted);
        int base = sorted[sorted.length / 2];
        if (base < level.getSeaLevel()) {
            return new Result(null, "terreno por debajo del nivel del mar");
        }
        if (base + CLEAR_HEIGHT + 4 >= level.getMaxBuildHeight()) {
            return new Result(null, "demasiado alto");
        }
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        i = 0;
        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                int top = heights[i++];
                int x = cx + dx;
                int z = cz + dz;
                if (Math.abs(top - base) > maxStep) {
                    return new Result(null, "terreno demasiado irregular");
                }
                BlockState surface = level.getBlockState(m.set(x, top, z));
                if (!surface.getFluidState().isEmpty()) {
                    return new Result(null, "agua o lava en " + x + " " + top + " " + z);
                }
                if (!isNaturalGround(surface)) {
                    return new Result(null, "bloque no natural en el suelo (" + surface.getBlock().getName().getString() + ") en " + x + " " + top + " " + z);
                }
                // Debajo: suelo firme (sin cuevas, huecos ni fluidos)
                for (int y = Math.min(top, base) - 3; y < top; y++) {
                    BlockState below = level.getBlockState(m.set(x, y, z));
                    if (!below.getFluidState().isEmpty()) {
                        return new Result(null, "agua o lava bajo el suelo en " + x + " " + y + " " + z);
                    }
                    if (below.isAir()) {
                        return new Result(null, "cueva o hueco bajo el suelo en " + x + " " + y + " " + z);
                    }
                }
                // Encima: solo aire y vegetación natural
                for (int y = top + 1; y <= base + CLEAR_HEIGHT; y++) {
                    BlockState above = level.getBlockState(m.set(x, y, z));
                    if (!above.getFluidState().isEmpty()) {
                        return new Result(null, "agua o lava en " + x + " " + y + " " + z);
                    }
                    if (!isNaturalAbove(above)) {
                        return new Result(null, "construcción existente (" + above.getBlock().getName().getString() + ") en " + x + " " + y + " " + z);
                    }
                }
            }
        }
        // Bloques con datos por encima del suelo (cofres, carteles...) y estructuras
        for (int x = (cx - R) >> 4; x <= (cx + R) >> 4; x++) {
            for (int z = (cz - R) >> 4; z <= (cz + R) >> 4; z++) {
                for (BlockPos p : level.getChunk(x, z).getBlockEntitiesPos()) {
                    if (Math.abs(p.getX() - cx) <= R && Math.abs(p.getZ() - cz) <= R && p.getY() >= base - 4 && p.getY() <= base + CLEAR_HEIGHT
                            && !level.getBlockState(p).is(Blocks.BEE_NEST)) {
                        return new Result(null, "bloque con datos en " + p.toShortString());
                    }
                }
            }
        }
        for (int dx = -R; dx <= R; dx += 4) {
            for (int dz = -R; dz <= R; dz += 4) {
                for (int dy = -2; dy <= 8; dy += 5) {
                    BlockPos at = m.set(cx + dx, base + dy, cz + dz).immutable();
                    for (Structure st : level.structureManager().getAllStructuresAt(at).keySet()) {
                        if (level.structureManager().getStructureAt(at, st).isValid()) {
                            return new Result(null, "hay una estructura (aldea, ruinas o de otro mod) cerca de " + (cx + dx) + " " + (cz + dz));
                        }
                    }
                }
            }
        }
        return new Result(new BlockPos(cx, base, cz), null);
    }

    public static boolean isNaturalGround(BlockState s) {
        return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.TERRACOTTA)
                || s.is(Blocks.GRAVEL) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.CLAY) || s.is(Blocks.SANDSTONE)
                || s.is(Blocks.RED_SANDSTONE) || s.is(Blocks.POWDER_SNOW) || s.is(Blocks.CALCITE) || s.is(Blocks.MOSS_BLOCK);
    }

    public static boolean isNaturalAbove(BlockState s) {
        return s.isAir() || s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(BlockTags.REPLACEABLE)
                || s.is(BlockTags.FLOWERS) || s.is(BlockTags.SAPLINGS) || s.is(Blocks.SNOW) || s.is(Blocks.VINE)
                || s.is(Blocks.SUGAR_CANE) || s.is(Blocks.CACTUS) || s.is(Blocks.BAMBOO) || s.is(Blocks.PUMPKIN)
                || s.is(Blocks.MELON) || s.is(Blocks.SWEET_BERRY_BUSH) || s.is(Blocks.BROWN_MUSHROOM) || s.is(Blocks.RED_MUSHROOM)
                || s.is(Blocks.MOSS_CARPET) || s.is(Blocks.BEE_NEST) || s.is(Blocks.MANGROVE_ROOTS) || s.is(Blocks.COCOA);
    }

    private PlazaSite() {
    }
}
