package com.panthrixseconomy.block;

import com.panthrixseconomy.command.Menus;
import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.permission.PEPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Mostrador de banco oficial. Su nombre sale del país en el que está. */
public class BankBlock extends BaseEntityBlock {

    public BankBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BankBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || !(placer instanceof ServerPlayer player) || !PEPermissions.isAdmin(player)) {
            return;
        }
        register(level, pos);
        EconomyData data = EconomyData.get(player.server);
        Bank b = data.bankAt(EconomyData.loc(level.dimension().location(), pos));
        if (b != null) {
            Menus.bankCreated(player, data, b);
        }
    }

    /** Da de alta la sucursal (también lo usa la Gran Plaza). */
    public static Bank register(Level level, BlockPos pos) {
        if (level.getServer() == null || !(level.getBlockEntity(pos) instanceof BankBlockEntity be)) {
            return null;
        }
        EconomyData data = EconomyData.get(level.getServer());
        Loc here = EconomyData.loc(level.dimension().location(), pos);
        Bank b = data.bankAt(here);
        if (b == null) {
            b = data.createBank(here);
        }
        b.atm = level.getBlockState(pos).is(com.panthrixseconomy.registry.PERegistry.ATM.get());
        data.setDirty();
        be.setBankId(b.id);
        return b;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (state.is(com.panthrixseconomy.registry.PERegistry.ATM.get()) && !com.panthrixseconomy.command.PlayerCommands.holdsOwnCard(sp)) {
                sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("block.panthrixseconomy.atm.need_card")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }
            EconomyData data = EconomyData.get(sp.server);
            Bank b = data.bankAt(EconomyData.loc(level.dimension().location(), pos));
            if (b == null && PEPermissions.isAdmin(sp)) {
                b = register(level, pos);  // bloque antiguo o colocado con /setblock
            }
            if (b == null) {
                Menus.bank(sp, data, null);
            } else {
                com.panthrixseconomy.network.PENetwork.toPlayer(sp, com.panthrixseconomy.network.BankView.of(sp, data, b, net.minecraft.network.chat.Component.empty()));
                if (PEPermissions.isAdmin(sp)) {
                    Menus.bankAdmin(sp);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && level.getServer() != null) {
            EconomyData data = EconomyData.get(level.getServer());
            Bank b = data.bankAt(EconomyData.loc(level.dimension().location(), pos));
            if (b != null) {
                data.removeBank(b);  // las cuentas son del país (identidad), no de la sucursal: no se pierden
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
