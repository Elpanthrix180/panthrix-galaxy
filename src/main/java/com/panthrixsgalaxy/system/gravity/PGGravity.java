package com.panthrixsgalaxy.system.gravity;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Gravedad de cada planeta (dato "gravity" de PGPlanets).
 *
 * Forge tiene un "atributo" de gravedad para las criaturas. En un planeta con gravedad 0,17
 * le ponemos un modificador que la multiplica por 0,17: se salta más alto y se cae despacio.
 * El daño por caída también se reduce en la misma proporción.
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGGravity {

    /** Identificador fijo del modificador (para poder quitarlo y cambiarlo). */
    private static final UUID MODIFIER_ID = UUID.fromString("6b1f2d64-7c3a-4e0b-9a8e-2f6c5d3b1a90");
    private static final String MODIFIER_NAME = "panthrixsgalaxy:planet_gravity";

    /** Gravedad de la dimensión (1.0 si no es un planeta del mod). */
    public static double getGravity(Level level) {
        PGPlanet planet = PGPlanets.fromDimension(level.dimension());
        return planet == null ? 1.0 : planet.gravity();
    }

    /** Pone (o quita) el modificador de gravedad según el planeta donde está la criatura. */
    public static void apply(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (attribute == null) {
            return;
        }
        double gravity = getGravity(entity.level());
        AttributeModifier current = attribute.getModifier(MODIFIER_ID);
        double wanted = gravity - 1.0;
        if (current != null && Math.abs(current.getAmount() - wanted) < 1.0e-6) {
            return; // ya está bien
        }
        if (current != null) {
            attribute.removeModifier(MODIFIER_ID);
        }
        if (gravity != 1.0) {
            attribute.addTransientModifier(new AttributeModifier(MODIFIER_ID, MODIFIER_NAME, wanted,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    /** Criaturas que aparecen o llegan a una dimensión. */
    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            apply(living);
        }
    }

    /** Jugadores: se comprueba cada segundo (por si cambian de dimensión). */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.tickCount % 20 == 0) {
            apply(event.player);
        }
    }

    /** Menos gravedad = caídas más suaves. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        double gravity = getGravity(event.getEntity().level());
        if (gravity < 1.0) {
            event.setDistance((float) (event.getDistance() * gravity));
        }
    }

    private PGGravity() {
    }
}
