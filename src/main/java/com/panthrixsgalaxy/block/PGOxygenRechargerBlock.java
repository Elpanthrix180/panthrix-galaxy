package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
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
 * Recargador de oxígeno: comprime el aire de alrededor y llena de oxígeno:
 *   - la bombona o la mochila que tengas en la mano, o
 *   - (con la mano vacía) el depósito de la mochila que llevas equipada.
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
        // ¿Qué vamos a llenar?
        ItemStack target = player.getItemInHand(hand);
        if (target.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            target = PGBackpackSlot.getEquipped(player); // mano vacía: la mochila equipada
        }
        boolean isTank = target.getItem() instanceof PGOxygenTankItem;
        boolean isBackpack = target.getItem() instanceof PGBackpackItem;
        if (!isTank && !isBackpack) {
            return InteractionResult.PASS; // nada que llenar: comportamiento normal
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // En una base sellada (Fase 19) sí funciona: se mira el aire donde está el jugador
        if (PGAtmosphere.isAirlessAt(level, BlockPos.containing(player.getEyePosition()))) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.recharger_no_air")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        boolean full = isTank
                ? PGOxygenTankItem.isFull(target)
                : PGBackpackItem.getTank(target, PGBackpackItem.Tank.OXYGEN)
                        >= ((PGBackpackItem) target.getItem()).getCapacity(PGBackpackItem.Tank.OXYGEN);
        if (full) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.tank_already_full")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.CONSUME;
        }

        if (isTank) {
            PGOxygenTankItem.setOxygen(target, ((PGOxygenTankItem) target.getItem()).getCapacity());
        } else {
            PGBackpackItem.setTank(target, PGBackpackItem.Tank.OXYGEN,
                    ((PGBackpackItem) target.getItem()).getCapacity(PGBackpackItem.Tank.OXYGEN));
        }
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
