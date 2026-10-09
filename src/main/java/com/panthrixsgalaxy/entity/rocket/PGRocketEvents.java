package com.panthrixsgalaxy.entity.rocket;

import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Eventos del cohete. */
public final class PGRocketEvents {

    /**
     * ¿Puede bajarse este pasajero del vehículo? No, mientras el cohete o la nave vuela.
     * (Lo llaman Forge con EntityMountEvent y Fabric con un mixin en Entity.stopRiding.)
     */
    public static boolean canDismount(Entity passenger, Entity vehicleEntity) {
        if (!(vehicleEntity instanceof PGSpaceVehicle vehicle)) {
            return true;
        }
        if (vehicle.isInFlight() && !vehicleEntity.isRemoved() && passenger.isAlive()) {
            if (passenger instanceof Player player && !player.level().isClientSide && player.tickCount % 20 == 0) {
                player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.cannot_exit_in_flight")
                        .withStyle(ChatFormatting.RED), true);
            }
            return false;
        }
        return true;
    }

    private PGRocketEvents() {
    }
}
