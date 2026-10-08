package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.block.entity.PGEnergyBlockEntity;
import com.panthrixsgalaxy.platform.PGPlatform;
import com.panthrixsgalaxy.system.energy.EnergyHelper;
import com.panthrixsgalaxy.system.energy.ItemEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Bloque de máquina con energía. Un solo tipo de bloque sirve para todas las máquinas:
 * cada una indica qué "block entity" usa (su cerebro) y, si quiere, una forma especial.
 *
 * Propiedad LIT ("encendido"): el generador y el reactor la activan mientras producen,
 * y entonces el bloque cambia de textura y da luz.
 */
public class PGEnergyBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory;
    private final VoxelShape shape;

    public PGEnergyBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory) {
        this(properties, blockEntityFactory, Shapes.block());
    }

    public PGEnergyBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory,
                         VoxelShape shape) {
        super(properties);
        this.blockEntityFactory = blockEntityFactory;
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityFactory.apply(pos, state);
    }

    /** Sin esto, los bloques con block entity serían invisibles. */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    /** Hace que la máquina "piense" cada tick, solo en el servidor. */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof PGEnergyBlockEntity machine) {
                machine.serverTick();
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            // Con un bloque normal en la mano se deja colocarlo (p. ej. un cable pegado a la máquina)
            ItemStack held = player.getItemInHand(hand);
            boolean placeable = held.getItem() instanceof BlockItem && !EnergyHelper.holdsEnergy(held)
                    && PGPlatform.getBurnTime(held) <= 0;
            return placeable ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof PGEnergyBlockEntity machine) {
            return machine.use(player, hand);
        }
        return InteractionResult.PASS;
    }

    /** Al romper el bloque, suelta lo que tuviera dentro (combustible...). */
    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PGEnergyBlockEntity machine) {
            machine.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Descripción en el inventario + energía guardada (si el objeto la conserva, como la celda). */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        CompoundTag data = BlockItem.getBlockEntityData(stack);
        if (data != null && data.contains(ItemEnergyStorage.ENERGY_TAG)) {
            tooltip.add(Component.translatable("tooltip.panthrixsgalaxy.stored_energy",
                    data.getInt(ItemEnergyStorage.ENERGY_TAG)).withStyle(ChatFormatting.YELLOW));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
