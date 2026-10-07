package com.panthrixsgalaxy.item;

import com.panthrixsgalaxy.init.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MÓDULO HABITABLE: una base sellada lista para vivir, construida de un clic.
 *
 * Construye delante de ti una sala de 7 x 5 x 7 bloques (dentro caben 5 x 3 x 5):
 *   suelo, paredes de paneles con ventanas, techo con luces, PUERTA HERMÉTICA hacia ti,
 *   un DISTRIBUIDOR DE OXÍGENO en el techo con un PANEL SOLAR encima, un TANQUE DE OXÍGENO
 *   de reserva, un DEPÓSITO y un RECARGADOR DE OXÍGENO dentro.
 *
 *   - Clic derecho en el suelo: la construye encima del suelo.
 *   - Clic derecho al aire: la construye a la altura de tus pies (para estaciones en el Espacio).
 * Necesita que todo el sitio esté libre (aire, hierba...).
 */
public class PGHabitatModuleItem extends Item {

    private static final int HALF = 3;   // de -3 a 3 = 7 bloques
    private static final int HEIGHT = 5; // suelo, 3 de hueco y techo

    public PGHabitatModuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return tryBuild(context.getLevel(), player, context.getItemInHand(), context.getClickedPos().above())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide) : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return tryBuild(level, player, stack, player.blockPosition().below())
                ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide) : InteractionResultHolder.fail(stack);
    }

    private boolean tryBuild(Level level, Player player, ItemStack stack, BlockPos floor) {
        Direction forward = player.getDirection();
        BlockPos center = floor.relative(forward, HALF + 1);
        if (!isFree(level, center, forward)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.habitat_no_room")
                        .withStyle(ChatFormatting.RED), true);
            }
            return false;
        }
        if (!level.isClientSide) {
            build(level, center, forward);
            level.playSound(null, center, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.habitat_built")
                    .withStyle(ChatFormatting.AQUA), false);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }

    /** Posición de un bloque del módulo: x = derecha/izquierda, y = altura, z = delante/detrás. */
    private static BlockPos at(BlockPos center, Direction forward, int x, int y, int z) {
        return center.relative(forward.getClockWise(), x).relative(forward, z).above(y);
    }

    private static boolean isFree(Level level, BlockPos center, Direction forward) {
        for (int x = -HALF; x <= HALF; x++) {
            for (int y = 0; y <= HEIGHT; y++) {
                for (int z = -HALF; z <= HALF; z++) {
                    BlockPos pos = at(center, forward, x, y, z);
                    if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void build(Level level, BlockPos center, Direction forward) {
        BlockState floor = ModBlocks.PG_STATION_FLOOR.get().defaultBlockState();
        BlockState roof = ModBlocks.PG_STATION_ROOF.get().defaultBlockState();
        BlockState panel = ModBlocks.PG_STATION_PANEL.get().defaultBlockState();
        BlockState hazard = ModBlocks.PG_STATION_HAZARD_PANEL.get().defaultBlockState();
        BlockState window = ModBlocks.PG_STATION_WINDOW.get().defaultBlockState();
        BlockState light = ModBlocks.PG_STATION_LIGHT.get().defaultBlockState();
        for (int x = -HALF; x <= HALF; x++) {
            for (int z = -HALF; z <= HALF; z++) {
                boolean edgeX = Math.abs(x) == HALF;
                boolean edgeZ = Math.abs(z) == HALF;
                set(level, center, forward, x, 0, z, floor);
                set(level, center, forward, x, 4, z, Math.abs(x) == 2 && Math.abs(z) == 2 ? light : roof);
                for (int y = 1; y <= 3; y++) {
                    if (!edgeX && !edgeZ) {
                        continue; // el interior se queda con aire
                    }
                    boolean windowSpot = y == 2 && ((edgeX && Math.abs(z) <= 1) || (z == HALF && Math.abs(x) <= 1));
                    set(level, center, forward, x, y, z, edgeX && edgeZ ? hazard : windowSpot ? window : panel);
                }
            }
        }
        // Puerta hermética en la pared que mira hacia ti
        Direction doorFacing = forward;
        BlockState door = ModBlocks.PG_AIRLOCK_DOOR.get().defaultBlockState()
                .setValue(DoorBlock.FACING, doorFacing)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        set(level, center, forward, 0, 1, -HALF, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, center, forward, 0, 2, -HALF, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        // Aire: distribuidor en el techo, panel solar encima y tanque de reserva al lado
        set(level, center, forward, 0, 4, 0, ModBlocks.PG_OXYGEN_DISTRIBUTOR.get().defaultBlockState());
        set(level, center, forward, 0, 5, 0, ModBlocks.PG_SOLAR_PANEL.get().defaultBlockState());
        set(level, center, forward, 1, 4, 0, ModBlocks.PG_OXYGEN_STORAGE.get().defaultBlockState());
        // Muebles
        set(level, center, forward, 2, 1, 2, ModBlocks.PG_STORAGE_CRATE.get().defaultBlockState());
        set(level, center, forward, -2, 1, 2, ModBlocks.PG_OXYGEN_RECHARGER.get().defaultBlockState());
    }

    private static void set(Level level, BlockPos center, Direction forward, int x, int y, int z, BlockState state) {
        level.setBlock(at(center, forward, x, y, z), state, Block.UPDATE_ALL);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.habitat_module").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.habitat_module_hint").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
