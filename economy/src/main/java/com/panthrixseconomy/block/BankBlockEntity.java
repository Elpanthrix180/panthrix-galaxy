package com.panthrixseconomy.block;

import com.panthrixseconomy.registry.PERegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** Guarda el id de la sucursal. El nombre NO se guarda aquí: se genera con el país. */
public class BankBlockEntity extends BlockEntity {

    private UUID bankId;

    public BankBlockEntity(BlockPos pos, BlockState state) {
        super(PERegistry.BANK_BE.get(), pos, state);
    }

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID id) {
        this.bankId = id;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (bankId != null) {
            tag.putUUID("Bank", bankId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bankId = tag.hasUUID("Bank") ? tag.getUUID("Bank") : null;
    }
}
