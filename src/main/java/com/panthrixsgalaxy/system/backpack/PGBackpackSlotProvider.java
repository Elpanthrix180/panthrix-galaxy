package com.panthrixsgalaxy.system.backpack;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** "Pega" un PGBackpackSlot a un jugador y se encarga de guardarlo y cargarlo. */
public class PGBackpackSlotProvider implements ICapabilitySerializable<CompoundTag> {

    private final PGBackpackSlot slot = new PGBackpackSlot();
    private final LazyOptional<PGBackpackSlot> optional = LazyOptional.of(() -> slot);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        return PGBackpackSlot.CAPABILITY.orEmpty(capability, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return slot.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        slot.deserializeNBT(tag);
    }
}
