package com.panthrixsgalaxy.block;

import com.panthrixsgalaxy.block.entity.PGOxygenStorageBlockEntity;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tanque de oxígeno de la base (20 000 de oxígeno).
 *
 *   Clic derecho con una bombona llena  -> la VACÍA en el tanque (guardar oxígeno).
 *   Mayús + clic derecho con una bombona -> la LLENA con el oxígeno del tanque.
 *   Clic derecho con la mano vacía      -> llena el oxígeno de tu mochila y te dice cuánto queda.
 *   Pegado a un distribuidor de oxígeno: es su RESERVA si se queda sin energía.
 */
public class PGOxygenStorageBlock extends BaseEntityBlock {

    public PGOxygenStorageBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PGOxygenStorageBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PGOxygenStorageBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PGOxygenTankItem item) {
            int inItem = PGOxygenTankItem.getOxygen(held);
            if (player.isShiftKeyDown()) {
                int moved = tank.extract(item.getCapacity() - inItem);
                PGOxygenTankItem.setOxygen(held, inItem + moved);
            } else {
                int moved = tank.fill(inItem);
                PGOxygenTankItem.setOxygen(held, inItem - moved);
            }
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6f, 1.6f);
        } else if (held.isEmpty()) {
            ItemStack backpack = PGBackpackSlot.getEquipped(player);
            if (backpack.getItem() instanceof PGBackpackItem item) {
                int inBackpack = PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.OXYGEN);
                int moved = tank.extract(item.getCapacity(PGBackpackItem.Tank.OXYGEN) - inBackpack);
                PGBackpackItem.setTank(backpack, PGBackpackItem.Tank.OXYGEN, inBackpack + moved);
            }
        } else {
            return InteractionResult.PASS;
        }
        player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.oxygen_storage",
                tank.getOxygen(), PGOxygenStorageBlockEntity.CAPACITY).withStyle(ChatFormatting.AQUA), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
