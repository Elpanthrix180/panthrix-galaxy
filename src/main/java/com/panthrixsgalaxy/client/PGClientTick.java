package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import com.panthrixsgalaxy.network.BackpackActionPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import com.panthrixsgalaxy.network.RocketLaunchPacket;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Lo que hace la pantalla del jugador en cada tick: tecla B (mochila), ESPACIO en el cohete,
 * fundido a negro al cambiar de dimensión y polvo de las tormentas de Marte.
 * Es común: Forge (PGClientEvents) y Fabric (PGFabricClient) lo llaman.
 */
public final class PGClientTick {

    /** ¿Estaba pulsado ESPACIO en el tick anterior? (para detectar solo el momento de pulsar) */
    private static boolean jumpWasDown;
    /** Dimensión del tick anterior (para detectar el cambio y hacer el fundido a negro). */
    private static ResourceKey<Level> lastDimension;
    /** ¿Había tormenta de polvo en el tick anterior? (para avisar al empezar y acabar) */
    private static boolean lastStorm;
    /** Color del polvo marciano. */
    private static final DustParticleOptions MARS_DUST = new DustParticleOptions(new Vector3f(0.8f, 0.42f, 0.22f), 1.6f);

    /** Al final de cada tick de la pantalla (lo llaman Forge y Fabric). */
    public static void onClientTickEnd(Minecraft minecraft) {
        // Fundido a negro al cambiar de dimensión (Tierra <-> Espacio)
        if (minecraft.level != null) {
            ResourceKey<Level> dimension = minecraft.level.dimension();
            if (lastDimension != null && !lastDimension.equals(dimension)) {
                RocketHudOverlay.startFade();
            }
            lastDimension = dimension;
        }
        RocketHudOverlay.tickFade();
        tickMarsStorm(minecraft);

        // ESPACIO dentro del cohete: iniciar / cancelar la cuenta atrás
        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (jumpDown && !jumpWasDown && minecraft.screen == null && minecraft.player != null
                && minecraft.player.getVehicle() instanceof PGSpaceVehicle) {
            PGNetwork.sendToServer(new RocketLaunchPacket());
        }
        jumpWasDown = jumpDown;

        while (PGKeyBindings.BACKPACK.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                BackpackActionPacket.Action action = Screen.hasShiftDown()
                        ? BackpackActionPacket.Action.UNEQUIP
                        : BackpackActionPacket.Action.OPEN;
                PGNetwork.sendToServer(new BackpackActionPacket(action));
            }
        }
    }

    /** Tormenta de polvo: polvo rojo volando con el viento y avisos al empezar y acabar. */
    private static void tickMarsStorm(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        float intensity = PGMarsWeather.getStormIntensity(minecraft.level);
        boolean storm = intensity > 0.0f;
        if (storm != lastStorm && minecraft.level.dimension().equals(PGPlanets.MARS_LEVEL)) {
            minecraft.player.displayClientMessage(Component.translatable(storm
                    ? "message.panthrixsgalaxy.dust_storm_start"
                    : "message.panthrixsgalaxy.dust_storm_end").withStyle(storm ? ChatFormatting.GOLD : ChatFormatting.GREEN), true);
        }
        lastStorm = storm;
        if (!storm) {
            return;
        }
        double angle = PGMarsWeather.getWindAngle(minecraft.level);
        double windX = Math.cos(angle) * 0.6;
        double windZ = Math.sin(angle) * 0.6;
        int count = (int) (intensity * PGConfig.stormParticles.get());
        var random = minecraft.level.random;
        for (int i = 0; i < count; i++) {
            double x = minecraft.player.getX() + (random.nextDouble() - 0.5) * 24.0;
            double y = minecraft.player.getY() + random.nextDouble() * 8.0 - 1.0;
            double z = minecraft.player.getZ() + (random.nextDouble() - 0.5) * 24.0;
            minecraft.level.addParticle(MARS_DUST, x, y, z, windX, 0.0, windZ);
        }
    }

    private PGClientTick() {
    }
}
