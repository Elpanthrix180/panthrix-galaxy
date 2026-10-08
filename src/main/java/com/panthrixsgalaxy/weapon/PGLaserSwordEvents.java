package com.panthrixsgalaxy.weapon;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Guardia con la espada láser: los golpes que vienen de DELANTE hacen la mitad de daño.
 * (Los rayos láser no llegan aquí: el propio rayo se da la vuelta, ver PGLaserBoltEntity.)
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGLaserSwordEvents {

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Vec3 from = source.getSourcePosition();
        if (entity.level().isClientSide || from == null || source.is(DamageTypeTags.BYPASSES_SHIELD)
                || !PGLaserSwordItem.isGuarding(entity) || !PGLaserSwordItem.isFacing(entity, from)) {
            return;
        }
        if (PGLaserSwordItem.payGuard(entity)) {
            event.setAmount(event.getAmount() * 0.5f);
            PGLaserSwordItem.sparks(entity.level(), entity.getEyePosition().add(entity.getViewVector(1.0f).scale(0.6)),
                    ((PGLaserSwordItem) entity.getUseItem().getItem()).getLaserTier().getColor(), 6);
        }
    }

    private PGLaserSwordEvents() {
    }
}
