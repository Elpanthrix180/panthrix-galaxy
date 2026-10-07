package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.network.BackpackActionPacket;
import com.panthrixsgalaxy.network.RocketLaunchPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos que ocurren en la pantalla del jugador durante la partida (teclas pulsadas...). */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, value = Dist.CLIENT)
public final class PGClientEvents {

    /** ¿Estaba pulsado ESPACIO en el tick anterior? (para detectar solo el momento de pulsar) */
    private static boolean jumpWasDown;
    /** Dimensión del tick anterior (para detectar el cambio y hacer el fundido a negro). */
    private static ResourceKey<Level> lastDimension;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        // Fundido a negro al cambiar de dimensión (Tierra <-> Espacio)
        if (minecraft.level != null) {
            ResourceKey<Level> dimension = minecraft.level.dimension();
            if (lastDimension != null && !lastDimension.equals(dimension)) {
                RocketHudOverlay.startFade();
            }
            lastDimension = dimension;
        }
        RocketHudOverlay.tickFade();

        // ESPACIO dentro del cohete: iniciar / cancelar la cuenta atrás
        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (jumpDown && !jumpWasDown && minecraft.screen == null && minecraft.player != null
                && minecraft.player.getVehicle() instanceof PGRocketEntity) {
            PGNetwork.CHANNEL.sendToServer(new RocketLaunchPacket());
        }
        jumpWasDown = jumpDown;

        while (PGKeyBindings.BACKPACK.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                BackpackActionPacket.Action action = Screen.hasShiftDown()
                        ? BackpackActionPacket.Action.UNEQUIP
                        : BackpackActionPacket.Action.OPEN;
                PGNetwork.CHANNEL.sendToServer(new BackpackActionPacket(action));
            }
        }
    }

    /** El astronauta va DENTRO del cohete: no se dibuja (si no, atravesaría las paredes). */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof PGRocketEntity) {
            event.setCanceled(true);
        }
    }

    /**
     * Al subir con el cohete por encima de las nubes, el horizonte se oscurece poco a poco:
     * cada vez queda menos atmósfera entre nosotros y el espacio negro.
     */
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || !minecraft.level.dimension().equals(Level.OVERWORLD)
                || !(minecraft.player.getVehicle() instanceof PGRocketEntity)) {
            return;
        }
        double height = minecraft.player.getY();
        float darkness = (float) Math.max(0.0, Math.min(1.0,
                (height - PGRocketEntity.CLOUD_HEIGHT) / (PGRocketEntity.ATMOSPHERE_TOP - PGRocketEntity.CLOUD_HEIGHT)));
        float keep = 1.0f - 0.9f * darkness;
        event.setRed(event.getRed() * keep);
        event.setGreen(event.getGreen() * keep);
        event.setBlue(event.getBlue() * keep);
    }

    /** La cámara tiembla con los motores encendidos. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof PGRocketEntity rocket)) {
            return;
        }
        LaunchState state = rocket.getLaunchState();
        float strength;
        if (state == LaunchState.COUNTDOWN && rocket.getTimer() <= PGRocketEntity.IGNITION_TICKS) {
            strength = 0.4f * (1.0f - rocket.getTimer() / (float) PGRocketEntity.IGNITION_TICKS);
        } else if (state == LaunchState.ASCENDING) {
            strength = rocket.getTimer() < 60 ? 1.2f : 0.5f; // más fuerte justo al despegar
        } else if (state == LaunchState.FALLING) {
            strength = 0.8f;
        } else {
            return;
        }
        float time = (float) (minecraft.player.tickCount + event.getPartialTick());
        event.setPitch(event.getPitch() + (float) Math.sin(time * 2.7f) * strength);
        event.setRoll(event.getRoll() + (float) Math.cos(time * 3.1f) * strength);
    }

    private PGClientEvents() {
    }
}
