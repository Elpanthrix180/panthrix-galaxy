package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.block.entity.PGCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

/**
 * Bloque del cable energético. Tiene 6 "brazos" (norte, sur, este, oeste, arriba, abajo)
 * que aparecen solos cuando al lado hay otro cable o una máquina con energía.
 */
public class PGCableBlock extends BaseEntityBlock {

    /** Una propiedad verdadero/falso por cada dirección (la misma que usan los muros de Minecraft). */
    private static final java.util.Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;

    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    /** Formas ya calculadas para cada combinación de brazos (2^6 = 64). */
    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static {
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = CORE;
            for (Direction direction : Direction.values()) {
                if ((mask & (1 << direction.ordinal())) != 0) {
                    shape = Shapes.or(shape, armShape(direction));
                }
            }
            SHAPES[mask] = shape;
        }
    }

    private static VoxelShape armShape(Direction direction) {
        return switch (direction) {
            case NORTH -> Block.box(6, 6, 0, 10, 10, 5);
            case SOUTH -> Block.box(6, 6, 11, 10, 10, 16);
            case WEST -> Block.box(0, 6, 6, 5, 10, 10);
            case EAST -> Block.box(11, 6, 6, 16, 10, 10);
            case DOWN -> Block.box(6, 0, 6, 10, 5, 10);
            case UP -> Block.box(6, 11, 6, 10, 16, 10);
        };
    }

    public PGCableBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (BooleanProperty property : CONNECTIONS.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        CONNECTIONS.values().forEach(builder::add);
    }

    /** ¿Hay que dibujar un brazo hacia este vecino? */
    private static boolean canConnect(LevelAccessor level, BlockPos neighborPos, Direction direction) {
        BlockEntity neighbor = level.getBlockEntity(neighborPos);
        return neighbor instanceof PGCableBlockEntity
                || (neighbor != null && neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).isPresent());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = context.getClickedPos().relative(direction);
            state = state.setValue(CONNECTIONS.get(direction), canConnect(context.getLevel(), neighborPos, direction));
        }
        return state;
    }

    /** Cuando cambia un vecino, se recalcula el brazo de ese lado. */
    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        PGCableBlockEntity.invalidateNetworks(); // algo cambió junto al cable: la red se volverá a recorrer
        return state.setValue(CONNECTIONS.get(direction), canConnect(level, neighborPos, direction));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        PGCableBlockEntity.invalidateNetworks();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        PGCableBlockEntity.invalidateNetworks();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(CONNECTIONS.get(direction))) {
                mask |= 1 << direction.ordinal();
            }
        }
        return SHAPES[mask];
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PGCableBlockEntity(pos, state);
    }
}
