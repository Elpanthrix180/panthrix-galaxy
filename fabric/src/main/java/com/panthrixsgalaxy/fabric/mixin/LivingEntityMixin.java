package com.panthrixsgalaxy.fabric.mixin;

import com.panthrixsgalaxy.system.gravity.PGGravity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Solo Fabric: la gravedad de los planetas para todas las criaturas.
 * (En Forge se hace con el atributo de gravedad: system/gravity/PGGravity.)
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /** Minecraft tira hacia abajo 0.08 cada tick: en la Luna, mucho menos. */
    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.08))
    private double panthrixsgalaxy$planetGravity(double gravity) {
        return gravity * PGGravity.getGravity(((LivingEntity) (Object) this).level());
    }

    /** Menos gravedad = caídas más suaves. */
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float panthrixsgalaxy$softFall(float fallDistance) {
        double gravity = PGGravity.getGravity(((LivingEntity) (Object) this).level());
        return gravity < 1.0 ? (float) (fallDistance * gravity) : fallDistance;
    }
}
