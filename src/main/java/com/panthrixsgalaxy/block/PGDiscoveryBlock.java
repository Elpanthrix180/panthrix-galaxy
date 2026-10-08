package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.block.entity.PGDiscoveryBlockEntity;
import com.panthrixsgalaxy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Bloque de DESCUBRIMIENTO (Fase 22): cuando un jugador se acerca a menos de 7 bloques,
 * consigue un logro secreto y lee un mensaje. Son el monolito de la Luna y el archivo alienígena.
 * No se pueden romper (como la roca madre), así siguen ahí para el siguiente explorador.
 */
public class PGDiscoveryBlock extends BaseEntityBlock {

    private final String advancement;
    private final String messageKey;

    public PGDiscoveryBlock(Properties properties, String advancement, String messageKey) {
        super(properties);
        this.advancement = advancement;
        this.messageKey = messageKey;
    }

    public String getAdvancement() {
        return advancement;
    }

    public String getMessageKey() {
        return messageKey;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PGDiscoveryBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.DISCOVERY.get(), PGDiscoveryBlockEntity::serverTick);
    }
}
