package com.panthrixsgalaxy.weapon;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Guardia con la espada láser: los golpes que vienen de DELANTE hacen la mitad de daño.
 * (Los rayos láser no llegan aquí: el propio rayo se da la vuelta, ver PGLaserBoltEntity.)
 */
public final class PGLaserSwordEvents {

    /**
     * Guardia con la espada: el daño de frente se reduce a la mitad (gasta energía).
     * Devuelve el daño que queda. Lo llaman Forge (LivingHurtEvent) y Fabric (mixin del jugador).
     */
    public static float onHurt(LivingEntity entity, DamageSource source, float amount) {
        Vec3 from = source.getSourcePosition();
        if (entity.level().isClientSide || from == null || source.is(DamageTypeTags.BYPASSES_SHIELD)
                || !PGLaserSwordItem.isGuarding(entity) || !PGLaserSwordItem.isFacing(entity, from)) {
            return amount;
        }
        if (PGLaserSwordItem.payGuard(entity)) {
            PGLaserSwordItem.sparks(entity.level(), entity.getEyePosition().add(entity.getViewVector(1.0f).scale(0.6)),
                    ((PGLaserSwordItem) entity.getUseItem().getItem()).getLaserTier().getColor(), 6);
            return amount * 0.5f;
        }
        return amount;
    }

    private PGLaserSwordEvents() {
    }
}
