package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.network.BackpackActionPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos que ocurren en la pantalla del jugador durante la partida (teclas pulsadas...). */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, value = Dist.CLIENT)
public final class PGClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        while (PGKeyBindings.BACKPACK.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                BackpackActionPacket.Action action = Screen.hasShiftDown()
                        ? BackpackActionPacket.Action.UNEQUIP
                        : BackpackActionPacket.Action.OPEN;
                PGNetwork.CHANNEL.sendToServer(new BackpackActionPacket(action));
            }
        }
    }

    private PGClientEvents() {
    }
}
