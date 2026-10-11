package com.panthrixseconomy.block;

import com.panthrixseconomy.registry.PERegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** Guarda a qué pueblo/ciudad pertenece este Núcleo. Los datos del pueblo están en EconomyData. */
public class TownCoreBlockEntity extends BlockEntity {

    private UUID settlementId;

    public TownCoreBlockEntity(BlockPos pos, BlockState state) {
        super(PERegistry.TOWN_CORE_BE.get(), pos, state);
    }

    public UUID getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(UUID id) {
        this.settlementId = id;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (settlementId != null) {
            tag.putUUID("Settlement", settlementId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        settlementId = tag.hasUUID("Settlement") ? tag.getUUID("Settlement") : null;
    }
}
