package com.panthrixseconomy.block;

import com.panthrixseconomy.command.Menus;
import com.panthrixseconomy.config.PEConfig;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.data.SettlementType;
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

/**
 * Núcleo del Pueblo: centro administrativo de un pueblo o ciudad oficial.
 * Solo un administrador puede colocarlo (PEEvents) y, al hacerlo, se enlaza con el pueblo de esa
 * zona o se crea uno nuevo. Clic derecho: información pública (y opciones de admin si lo eres).
 */
public class TownCoreBlock extends BaseEntityBlock {

    public TownCoreBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TownCoreBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || !(placer instanceof ServerPlayer player) || !PEPermissions.isAdmin(player)) {
            return;  // un no-admin no crea nada: PEEvents además deshace la colocación
        }
        if (!(level.getBlockEntity(pos) instanceof TownCoreBlockEntity core)) {
            return;
        }
        EconomyData data = EconomyData.get(player.server);
        Loc here = EconomyData.loc(level.dimension().location(), pos);
        Settlement s = data.settlements.values().stream().filter(x -> x.core == null && x.contains(here)).findFirst().orElse(null);
        if (s == null) {
            int n = data.settlements.size() + 1;
            String name = "Nuevo Pueblo " + n;
            while (data.settlementNameTaken(name, null)) {
                name = "Nuevo Pueblo " + (++n);
            }
            s = data.createSettlement(name, SettlementType.TOWN, here, PEConfig.TOWN_RADIUS.get());
        }
        s.core = here;
        data.setDirty();
        core.setSettlementId(s.id);
        Menus.townCore(player, data, s, here);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer sp && level.getBlockEntity(pos) instanceof TownCoreBlockEntity core) {
            EconomyData data = EconomyData.get(sp.server);
            Settlement s = core.getSettlementId() == null ? null : data.settlements.get(core.getSettlementId());
            Menus.townCore(sp, data, s, EconomyData.loc(level.dimension().location(), pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && level.getServer() != null) {
            EconomyData data = EconomyData.get(level.getServer());
            Loc here = EconomyData.loc(level.dimension().location(), pos);
            for (Settlement s : data.settlements.values()) {
                if (here.equals(s.core)) {
                    s.core = null;  // el pueblo y sus datos se quedan; solo se quita el bloque
                    data.setDirty();
                }
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
