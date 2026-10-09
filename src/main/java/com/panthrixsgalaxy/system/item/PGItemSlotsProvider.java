package com.panthrixsgalaxy.system.item;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/** Un bloque con huecos que pueden llenar las tolvas y tuberías (también de otros mods). */
public interface PGItemSlotsProvider {

    @Nullable
    PGItemSlots getItemSlots(@Nullable Direction side);
}
