package com.panthrixsgalaxy.entity.rocket;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos del cohete. */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGRocketEvents {

    /** No se puede bajar de un vehículo (cohete o nave) mientras vuela. */
    @SubscribeEvent
    public static void onDismount(EntityMountEvent event) {
        if (!event.isDismounting() || !(event.getEntityBeingMounted() instanceof PGSpaceVehicle vehicle)) {
            return;
        }
        boolean pilotAlive = event.getEntityMounting().isAlive();
        if (vehicle.isInFlight() && !event.getEntityBeingMounted().isRemoved() && pilotAlive) {
            event.setCanceled(true);
            if (event.getEntityMounting() instanceof Player player && !player.level().isClientSide
                    && player.tickCount % 20 == 0) {
                player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.cannot_exit_in_flight")
                        .withStyle(ChatFormatting.RED), true);
            }
        }
    }

    private PGRocketEvents() {
    }
}
