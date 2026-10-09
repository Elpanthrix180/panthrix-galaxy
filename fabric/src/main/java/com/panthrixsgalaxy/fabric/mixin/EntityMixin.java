package com.panthrixsgalaxy.fabric.mixin;

import com.panthrixsgalaxy.entity.rocket.PGRocketEvents;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Solo Fabric: no se puede bajar de un cohete o nave en vuelo (en Forge: EntityMountEvent). */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    private void panthrixsgalaxy$stayInVehicle(CallbackInfo info) {
        Entity self = (Entity) (Object) this;
        Entity vehicle = self.getVehicle();
        if (vehicle != null && !PGRocketEvents.canDismount(self, vehicle)) {
            info.cancel();
        }
    }
}
