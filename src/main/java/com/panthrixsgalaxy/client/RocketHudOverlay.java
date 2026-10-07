package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Panel del cohete en la parte de arriba de la pantalla (solo cuando vas dentro):
 *   COHETE BÁSICO · Destino: Luna
 *   Combustible [███████░░░] 1400 / 2000 mB
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RocketHudOverlay {

    private static final int BAR_WIDTH = 120;
    private static final int COLOR_FUEL = 0xFFFF9922;

    public static final IGuiOverlay ROCKET_HUD = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui
                || !(minecraft.player.getVehicle() instanceof PGRocketEntity rocket)) {
            return;
        }
        int centerX = screenWidth / 2;
        int y = 8;
        String tier = rocket.getTier().getSerializedName();
        graphics.fill(centerX - 90, y - 4, centerX + 90, y + 40, 0xA0000000);
        graphics.drawCenteredString(minecraft.font, Component.translatable("hud.panthrixsgalaxy.rocket_title." + tier),
                centerX, y, 0x55CCFF);

        int fuel = rocket.getFuel();
        int capacity = rocket.getFuelCapacity();
        int barX = centerX - BAR_WIDTH / 2;
        int barY = y + 13;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 6, 0xFF2A3040);
        if (capacity > 0) {
            graphics.fill(barX, barY, barX + (int) ((long) BAR_WIDTH * fuel / capacity), barY + 6, COLOR_FUEL);
        }
        graphics.drawCenteredString(minecraft.font, Component.translatable("hud.panthrixsgalaxy.rocket_fuel", fuel, capacity),
                centerX, barY + 9, COLOR_FUEL);
        graphics.drawCenteredString(minecraft.font, Component.translatable("hud.panthrixsgalaxy.rocket_launch_soon"),
                centerX, barY + 20, 0xAAAAAA);
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("rocket", ROCKET_HUD);
    }

    private RocketHudOverlay() {
    }
}
