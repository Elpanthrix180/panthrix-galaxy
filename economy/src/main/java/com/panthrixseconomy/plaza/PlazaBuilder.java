package com.panthrixseconomy.plaza;

import com.panthrixseconomy.registry.PERegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

/**
 * Construye la Gran Plaza (41×41) con bloques vanilla, centrada en "center" (altura del suelo).
 *
 *              N: Ayuntamiento (Núcleo del Pueblo)
 *     jardín        │         jardín
 *  O: Mercado ── FUENTE ── E: Banco
 *     jardín        │         jardín
 *              S: entrada, tablón de anuncios y punto de llegada
 *
 * Solo se llama después de que PlazaSite haya comprobado que la zona es natural y segura.
 */
public final class PlazaBuilder {

    public static final int R = 20;

    /** Brazo 0 = sur (+z), 1 = este (+x), 2 = norte (−z), 3 = oeste (−x). */
    private static final Rotation[] ROT = {Rotation.NONE, Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.CLOCKWISE_90};
    private static final Block[] FLOWERS = {Blocks.POPPY, Blocks.DANDELION, Blocks.AZURE_BLUET, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER,
            Blocks.ALLIUM, Blocks.LILY_OF_THE_VALLEY, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.PINK_TULIP, Blocks.WHITE_TULIP};

    private final ServerLevel level;
    private final BlockPos c;
    private final RandomSource rnd;
    private final List<BlockPos> water = new ArrayList<>();

    public BlockPos corePos;
    public BlockPos bankPos;
    public BlockPos marketPos;
    public BlockPos spawnPos;

    public PlazaBuilder(ServerLevel level, BlockPos center) {
        this.level = level;
        this.c = center;
        this.rnd = RandomSource.create(center.asLong());
    }

    public void build() {
        groundAndFloor();
        fountain();
        for (int k = 0; k < 4; k++) {
            arm(k);
        }
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                garden(13 * sx, 13 * sz);
                lamp(7 * sx, 7 * sz);
            }
        }
        pavilion(2, "Ayuntamiento", "Núcleo del", "Pueblo");
        pavilion(1, "Banco", "Depósitos y", "transferencias");
        pavilion(3, "Mercado", "Comercios", "autorizados");
        townHall();
        bank();
        market();
        entrance();
        for (BlockPos p : water) {
            level.setBlock(p, Blocks.WATER.defaultBlockState(), 3);
        }
    }

    // ===================== Utilidades =====================

    private void set(int dx, int dy, int dz, BlockState s) {
        level.setBlock(c.offset(dx, dy, dz), s, 2);
    }

    private BlockPos pos(int k, int lx, int ly, int lz) {
        int x = switch (k) {
            case 1 -> lz;
            case 2 -> -lx;
            case 3 -> -lz;
            default -> lx;
        };
        int z = switch (k) {
            case 1 -> -lx;
            case 2 -> -lz;
            case 3 -> lx;
            default -> lz;
        };
        return c.offset(x, ly, z);
    }

    /** Coloca en coordenadas del brazo k (diseñado para el brazo sur y girado). */
    private void setR(int k, int lx, int ly, int lz, BlockState s) {
        level.setBlock(pos(k, lx, ly, lz), s.rotate(ROT[k]), 2);
    }

    private static BlockState st(Block b) {
        return b.defaultBlockState();
    }

    private static BlockState leaves() {
        return Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    private void sign(BlockPos p, String... lines) {
        if (level.getBlockEntity(p) instanceof SignBlockEntity sign) {
            SignText text = new SignText();
            for (int i = 0; i < Math.min(4, lines.length); i++) {
                text = text.setMessage(i, Component.literal(lines[i]));
            }
            sign.setText(text, true);
            sign.setWaxed(true);
            sign.setChanged();
            BlockState s = level.getBlockState(p);
            level.sendBlockUpdated(p, s, s, 3);
        }
    }

    // ===================== Suelo =====================

    private void groundAndFloor() {
        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                for (int dy = 1; dy <= PlazaSite.CLEAR_HEIGHT; dy++) {
                    if (!level.getBlockState(c.offset(dx, dy, dz)).isAir()) {
                        set(dx, dy, dz, st(Blocks.AIR));
                    }
                }
                for (int dy = -1; dy >= -(PlazaSite.RELAXED_STEP + 2); dy--) {
                    BlockState below = level.getBlockState(c.offset(dx, dy, dz));
                    if (below.isAir() || below.canBeReplaced() || !below.getFluidState().isEmpty()) {
                        set(dx, dy, dz, st(Blocks.STONE));
                    } else {
                        break;
                    }
                }
                set(dx, 0, dz, floor(dx, dz));
            }
        }
    }

    private static BlockState floor(int dx, int dz) {
        int ax = Math.abs(dx);
        int az = Math.abs(dz);
        double d = Math.sqrt(dx * dx + dz * dz);
        if (ax == R || az == R) {
            return st(Blocks.MOSSY_STONE_BRICKS);
        }
        if (d > 7.5 && d <= 8.5) {
            return st(Blocks.SMOOTH_STONE);
        }
        if (ax <= 1 || az <= 1) {
            return st(Blocks.STONE_BRICKS);
        }
        if (Math.round(d) % 6 == 0) {
            return st(Blocks.POLISHED_DIORITE);
        }
        return ((dx + dz) & 1) == 0 ? st(Blocks.POLISHED_ANDESITE) : st(Blocks.ANDESITE);
    }

    // ===================== Fuente (3 niveles) =====================

    private void fountain() {
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                boolean axis = dx == 0 || dz == 0;
                if (d <= 6.5) {
                    boolean glow = d > 3.6 && d <= 5.6 && dx % 2 == 0 && dz % 2 == 0;
                    set(dx, 0, dz, st(glow ? Blocks.SEA_LANTERN : Blocks.STONE_BRICKS));
                }
                if (d > 3.5 && d <= 6.5) {
                    water.add(c.offset(dx, 1, dz));                             // nivel 1
                } else if (d > 6.5 && d <= 7.5) {
                    set(dx, 1, dz, st(Blocks.STONE_BRICKS));                     // borde exterior
                    if (axis) {
                        set(dx, 2, dz, st(Blocks.CHISELED_STONE_BRICKS));
                        set(dx, 3, dz, st(Blocks.LANTERN));
                    } else {
                        set(dx, 2, dz, Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                    }
                }
                if (d <= 3.5) {                                                    // pedestal
                    set(dx, 1, dz, st(Blocks.STONE_BRICKS));
                    set(dx, 2, dz, st(d > 1.5 && d <= 2.5 ? Blocks.SEA_LANTERN : Blocks.STONE_BRICKS));
                }
                if (d > 2.5 && d <= 3.5) {                                         // borde del nivel 2 (con 4 cascadas)
                    if (axis) {
                        water.add(c.offset(dx, 3, dz));
                    } else {
                        set(dx, 3, dz, st(Blocks.CHISELED_STONE_BRICKS));
                    }
                } else if (d <= 2.5 && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) {
                    water.add(c.offset(dx, 3, dz));                             // nivel 2
                }
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {                      // columna central
                    set(dx, 3, dz, st(Blocks.STONE_BRICKS));
                    set(dx, 4, dz, st(Blocks.STONE_BRICKS));
                    set(dx, 5, dz, st(Blocks.CHISELED_STONE_BRICKS));
                }
            }
        }
        water.add(c.offset(0, 6, 0));                                           // nivel 3: surtidor
    }

    // ===================== Brazos: bancos, farolas, setos =====================

    private void arm(int k) {
        BlockState benchE = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState benchW = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
        for (int z = 9; z <= 10; z++) {
            setR(k, 3, 1, z, benchE);
            setR(k, -3, 1, z, benchW);
        }
        lampR(k, 3, 11);
        lampR(k, -3, 11);
        for (int x = -R; x <= R; x++) {
            if (Math.abs(x) > 2) {
                setR(k, x, 1, R, leaves());
            }
        }
    }

    private void lampR(int k, int lx, int lz) {
        for (int y = 1; y <= 3; y++) {
            setR(k, lx, y, lz, st(Blocks.SPRUCE_FENCE));
        }
        setR(k, lx, 4, lz, st(Blocks.LANTERN));
    }

    private void lamp(int dx, int dz) {
        for (int y = 1; y <= 3; y++) {
            set(dx, y, dz, st(Blocks.SPRUCE_FENCE));
        }
        set(dx, 4, dz, st(Blocks.LANTERN));
    }

    // ===================== Jardines =====================

    private void garden(int gx, int gz) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= 4.5) {
                    set(gx + dx, 0, gz + dz, st(Blocks.GRASS_BLOCK));
                    if (d > 1.5 && d <= 4.2 && rnd.nextFloat() < 0.55F) {
                        set(gx + dx, 1, gz + dz, st(FLOWERS[rnd.nextInt(FLOWERS.length)]));
                    }
                } else if (d <= 5.3) {
                    set(gx + dx, 0, gz + dz, st(Blocks.MOSSY_STONE_BRICKS));
                }
            }
        }
        for (int y = 1; y <= 4; y++) {
            set(gx, y, gz, st(Blocks.OAK_LOG));
        }
        for (int y = 3; y <= 6; y++) {
            int r = y <= 4 ? 2 : 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    boolean corner = Math.abs(dx) == r && Math.abs(dz) == r && r > 1;
                    if (!corner && !(dx == 0 && dz == 0 && y <= 4)) {
                        set(gx + dx, y, gz + dz, leaves());
                    }
                }
            }
        }
    }

    // ===================== Edificios públicos =====================

    private void pavilion(int k, String title, String line2, String line3) {
        for (int x = -4; x <= 4; x++) {
            for (int z = 13; z <= 19; z++) {
                setR(k, x, 0, z, st(Blocks.SMOOTH_STONE));
            }
        }
        for (int sx = -4; sx <= 4; sx += 8) {
            for (int z : new int[]{13, 19}) {
                for (int y = 1; y <= 3; y++) {
                    setR(k, sx, y, z, st(Blocks.STONE_BRICKS));
                }
                setR(k, sx, 4, z, st(Blocks.CHISELED_STONE_BRICKS));
            }
            for (int z = 14; z <= 18; z++) {
                for (int y = 1; y <= 3; y++) {
                    setR(k, sx, y, z, st(y == 2 && z == 16 ? Blocks.GLASS : Blocks.STONE_BRICKS));
                }
                setR(k, sx, 4, z, st(Blocks.STONE_BRICKS));
            }
        }
        for (int x = -3; x <= 3; x++) {
            for (int y = 1; y <= 4; y++) {
                setR(k, x, y, 19, st(y == 2 && Math.abs(x) == 2 ? Blocks.GLASS : Blocks.STONE_BRICKS));
            }
            setR(k, x, 4, 13, st(Blocks.STONE_BRICKS));                         // dintel
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = 12; z <= 20; z++) {
                setR(k, x, 5, z, st(Blocks.DARK_OAK_PLANKS));
            }
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = 14; z <= 18; z++) {
                setR(k, x, 6, z, st(Blocks.DARK_OAK_PLANKS));
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = 15; z <= 17; z++) {
                setR(k, x, 7, z, Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            }
        }
        setR(k, 0, 4, 16, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Cartel en la columna de la entrada, mirando a la fuente
        setR(k, 4, 3, 12, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.NORTH));
        sign(pos(k, 4, 3, 12), title, line2, line3);
        setR(k, -4, 3, 12, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.NORTH));
        sign(pos(k, -4, 3, 12), title, line2, line3);
    }

    private void townHall() {
        int k = 2;
        setR(k, 0, 0, 17, st(Blocks.CHISELED_STONE_BRICKS));
        corePos = pos(k, 0, 1, 17);
        level.setBlock(corePos, PERegistry.TOWN_CORE.get().defaultBlockState(), 3);
        setR(k, -2, 1, 18, st(Blocks.POTTED_RED_TULIP));
        setR(k, 2, 1, 18, st(Blocks.POTTED_BLUE_ORCHID));
    }

    private void bank() {
        int k = 1;
        bankPos = pos(k, 0, 1, 17);
        level.setBlock(bankPos, PERegistry.BANK.get().defaultBlockState(), 3);
        BlockState counter = Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        for (int x = 1; x <= 3; x++) {
            setR(k, x, 1, 16, counter);
            setR(k, -x, 1, 16, counter);
        }
        setR(k, 3, 2, 16, st(Blocks.LANTERN));
        setR(k, -3, 2, 16, st(Blocks.LANTERN));
    }

    private void market() {
        int k = 3;
        marketPos = pos(k, 0, 1, 15);
        setR(k, -3, 1, 18, st(Blocks.BARREL));
        setR(k, 3, 1, 18, st(Blocks.BARREL));
        setR(k, -2, 1, 18, st(Blocks.HAY_BLOCK));
        setR(k, 2, 1, 18, st(Blocks.HAY_BLOCK));
        setR(k, 0, 1, 18, st(Blocks.COMPOSTER));
        setR(k, -3, 1, 17, st(Blocks.MELON));
        setR(k, 3, 1, 17, st(Blocks.PUMPKIN));
        for (int x = -5; x <= 5; x++) {                                           // toldo de colores
            setR(k, x, 5, 12, st((x & 1) == 0 ? Blocks.RED_WOOL : Blocks.WHITE_WOOL));
        }
    }

    private void entrance() {
        int k = 0;
        for (int x = -6; x <= -4; x++) {                                         // tablón de anuncios
            setR(k, x, 1, 15, st(Blocks.SPRUCE_PLANKS));
            setR(k, x, 2, 15, st(Blocks.SPRUCE_PLANKS));
            setR(k, x, 3, 15, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            setR(k, x, 2, 16, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH));
        }
        sign(pos(k, -6, 2, 16), "BANCO (este)", "Deposita tus", "monedas: usa", "/banco");
        sign(pos(k, -5, 2, 16), "Bienvenido a", "la Gran Plaza", "Panthrix's", "Economy");
        sign(pos(k, -4, 2, 16), "MERCADO (oeste)", "Compra y vende", "en comercios", "autorizados");
        setR(k, 4, 1, 15, Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 0));
        sign(pos(k, 4, 1, 15), "↑ Fuente y", "Ayuntamiento", "→ Banco", "← Mercado");
        lampR(k, 3, 18);
        lampR(k, -3, 18);
        spawnPos = pos(k, 0, 1, 17);
    }
}
