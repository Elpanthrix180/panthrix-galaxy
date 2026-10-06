package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Recargador de oxígeno: comprime el aire de alrededor y llena la bombona.
 * Uso: clic derecho sobre el bloque con una bombona en la mano.
 *
 * Solo funciona donde hay aire (no en la Luna ni en Marte). En la Fase 7, con energía,
 * haremos una versión que funcione también en planetas sin atmósfera.
 */
public class PGOxygenRechargerBlock extends Block {

    public PGOxygenRechargerBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof PGOxygenTankItem tank)) {
            return InteractionResult.PASS; // no lleva bombona: comportamiento normal
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (PGAtmosphere.isAirlessDimension(level)) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.recharger_no_air")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        if (PGOxygenTankItem.isFull(held)) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.tank_already_full")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.CONSUME;
        }

        PGOxygenTankItem.setOxygen(held, tank.getCapacity());
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.6f);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    8, 0.2, 0.1, 0.2, 0.01);
        }
        player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.tank_filled")
                .withStyle(ChatFormatting.AQUA), true);
        return InteractionResult.CONSUME;
    }
}
