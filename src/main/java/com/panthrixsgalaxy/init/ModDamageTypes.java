package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Tipos de daño del mod. Cada uno se define en data/panthrixsgalaxy/damage_type/<nombre>.json
 * y su mensaje de muerte está en lang ("death.attack.panthrixsgalaxy.<nombre>").
 */
public final class ModDamageTypes {

    /** Daño por falta de oxígeno. Atraviesa la armadura. */
    public static final ResourceKey<DamageType> NO_OXYGEN =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PanthrixsGalaxy.MOD_ID, "no_oxygen"));

    /** Daño por temperatura extrema (sin traje completo donde no hay atmósfera). Atraviesa la armadura. */
    public static final ResourceKey<DamageType> EXTREME_TEMPERATURE =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PanthrixsGalaxy.MOD_ID, "extreme_temperature"));

    /** Disparo de un arma láser (Fase 15). Lo bloquea un escudo y lo reduce la armadura. */
    public static final ResourceKey<DamageType> LASER =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PanthrixsGalaxy.MOD_ID, "laser"));

    /** Daño de un rayo láser: "direct" = el rayo, "owner" = quien disparó. */
    public static DamageSource laser(Level level, Entity direct, @Nullable Entity owner) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(LASER),
                direct, owner);
    }

    public static DamageSource noOxygen(Level level) {
        return source(level, NO_OXYGEN);
    }

    public static DamageSource extremeTemperature(Level level) {
        return source(level, EXTREME_TEMPERATURE);
    }

    private static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }

    private ModDamageTypes() {
    }
}
