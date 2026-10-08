package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.menu.PGEngineeringBenchMenu;
import com.panthrixsgalaxy.platform.PGPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Banco de Ingeniería Espacial: clic derecho para abrir su ventana de fabricación. */
public class PGEngineeringBenchBlock extends Block {

    private static final Component TITLE = Component.translatable("container.panthrixsgalaxy.engineering_bench");

    public PGEngineeringBenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            PGPlatform.openMenu(serverPlayer, new SimpleMenuProvider(
                    (containerId, playerInventory, p) ->
                            new PGEngineeringBenchMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)),
                    TITLE), buf -> { });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
