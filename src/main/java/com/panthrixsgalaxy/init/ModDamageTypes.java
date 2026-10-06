package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Tipos de daño del mod. Cada uno se define en data/panthrixsgalaxy/damage_type/<nombre>.json
 * y su mensaje de muerte está en lang ("death.attack.panthrixsgalaxy.<nombre>").
 */
public final class ModDamageTypes {

    /** Daño por falta de oxígeno. Atraviesa la armadura. */
    public static final ResourceKey<DamageType> NO_OXYGEN =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PanthrixsGalaxy.MOD_ID, "no_oxygen"));

    public static DamageSource noOxygen(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NO_OXYGEN));
    }

    private ModDamageTypes() {
    }
}
