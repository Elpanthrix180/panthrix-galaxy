package com.panthrixsgalaxy.system.energy;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/** Un bloque (block entity) que ofrece energía a cables y máquinas, también de otros mods. */
public interface PGEnergyProvider {

    /** La energía por ese lado (null = el lado no conecta). side es null si no se sabe el lado. */
    @Nullable
    PGEnergyHandler getEnergyHandler(@Nullable Direction side);
}
