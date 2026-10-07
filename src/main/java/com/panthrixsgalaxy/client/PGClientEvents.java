package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.network.BackpackActionPacket;
import com.panthrixsgalaxy.network.RocketLaunchPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
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
