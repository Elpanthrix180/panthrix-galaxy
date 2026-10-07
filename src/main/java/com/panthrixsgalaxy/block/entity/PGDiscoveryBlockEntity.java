package com.panthrixsgalaxy.block.entity;

import com.panthrixsgalaxy.advancement.PGAdvancements;
import com.panthrixsgalaxy.block.PGDiscoveryBlock;
import com.panthrixsgalaxy.init.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Cada 2 segundos mira si hay algún jugador cerca para darle el logro secreto. */
public class PGDiscoveryBlockEntity extends BlockEntity {

    private static final double RADIUS = 7.0;

    public PGDiscoveryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISCOVERY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PGDiscoveryBlockEntity entity) {
        if (level.getGameTime() % 40 != 0 || !(state.getBlock() instanceof PGDiscoveryBlock block)) {
            return;
        }
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(RADIUS))) {
            if (PGAdvancements.award(player, block.getAdvancement())) {
                player.displayClientMessage(Component.translatable(block.getMessageKey())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
                level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
        }
    }
}
