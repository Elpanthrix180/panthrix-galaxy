package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Panel del cohete (solo cuando vas dentro):
 *
 *   COHETE BÁSICO · Destino: Luna
 *   Combustible [███████░░░] 1400 / 2000 mB
 *   Altitud 238 · Velocidad 20 m/s · Atravesando las nubes
 *
 *   y en el centro, en grande: T-5 ... ¡DESPEGUE!
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RocketHudOverlay {

    private static final int BAR_WIDTH = 120;
    private static final int COLOR_FUEL = 0xFFFF9922;
    private static final int COLOR_INFO = 0xFF55CCFF;
    private static final int COLOR_HINT = 0xFFAAAAAA;
    private static final int COLOR_ALERT = 0xFFFF4444;

    public static final IGuiOverlay ROCKET_HUD = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui
                || !(minecraft.player.getVehicle() instanceof PGRocketEntity rocket)) {
            return;
        }
        Font font = minecraft.font;
        int centerX = screenWidth / 2;
        int y = 8;
        LaunchState state = rocket.getLaunchState();

        // ----- Panel superior -----
        graphics.fill(centerX - 100, y - 4, centerX + 100, y + 44, 0xA0000000);
        graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.rocket_title."
                + rocket.getTier().getSerializedName()), centerX, y, COLOR_INFO);

        int fuel = rocket.getFuel();
        int capacity = rocket.getFuelCapacity();
        int barX = centerX - BAR_WIDTH / 2;
        int barY = y + 13;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 6, 0xFF2A3040);
        if (capacity > 0) {
            graphics.fill(barX, barY, barX + (int) ((long) BAR_WIDTH * fuel / capacity), barY + 6, COLOR_FUEL);
            // Marca del mínimo para despegar
            int minX = barX + (int) ((long) BAR_WIDTH * rocket.getTier().getMinLaunchFuel() / capacity);
            graphics.fill(minX, barY - 1, minX + 1, barY + 7, 0xFFFFFFFF);
        }
        graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.rocket_fuel", fuel, capacity),
                centerX, barY + 9, COLOR_FUEL);
        graphics.drawCenteredString(font, statusLine(rocket, state), centerX, barY + 20,
                state == LaunchState.FALLING ? COLOR_ALERT : COLOR_HINT);

        // ----- Texto grande en el centro -----
        if (state == LaunchState.COUNTDOWN) {
            int seconds = (rocket.getTimer() + 19) / 20;
            drawBig(graphics, font, Component.literal("T-" + seconds), centerX, screenHeight / 2 - 40,
                    seconds <= 3 ? COLOR_ALERT : 0xFFFFFFFF);
        } else if (state == LaunchState.ASCENDING && rocket.getTimer() < 60) {
            drawBig(graphics, font, Component.translatable("hud.panthrixsgalaxy.liftoff"), centerX,
                    screenHeight / 2 - 40, COLOR_FUEL);
        } else if (state == LaunchState.FALLING && (minecraft.player.tickCount / 8) % 2 == 0) {
            drawBig(graphics, font, Component.translatable("hud.panthrixsgalaxy.no_fuel_alert"), centerX,
                    screenHeight / 2 - 40, COLOR_ALERT);
        }
    };

    /** Tercera línea del panel según la etapa del vuelo. */
    private static Component statusLine(PGRocketEntity rocket, LaunchState state) {
        return switch (state) {
            case IDLE -> {
                if (!rocket.isOnLaunchPad()) {
                    yield Component.translatable("hud.panthrixsgalaxy.rocket_not_on_pad");
                }
                if (rocket.getFuel() < rocket.getTier().getMinLaunchFuel()) {
                    yield Component.translatable("hud.panthrixsgalaxy.rocket_low_fuel", rocket.getTier().getMinLaunchFuel());
                }
                yield Component.translatable("hud.panthrixsgalaxy.rocket_ready");
            }
            case COUNTDOWN -> Component.translatable("hud.panthrixsgalaxy.rocket_abort_hint");
            default -> {
                int altitude = (int) rocket.getY();
                int speed = (int) Math.round(Math.abs(rocket.getDeltaMovement().y) * 20.0);
                yield Component.translatable("hud.panthrixsgalaxy.rocket_flight", altitude, speed, layerName(rocket, state));
            }
        };
    }

    /** Nombre de la capa de la atmósfera según la altura. */
    private static Component layerName(PGRocketEntity rocket, LaunchState state) {
        if (state == LaunchState.DESCENDING) {
            return Component.translatable("hud.panthrixsgalaxy.layer.descent");
        }
        double y = rocket.getY();
        String key;
        if (y < PGRocketEntity.CLOUD_HEIGHT - 10) {
            key = "low";
        } else if (y < PGRocketEntity.CLOUD_HEIGHT + 10) {
            key = "clouds";
        } else if (y < 320) {
            key = "high";
        } else {
            key = "edge";
        }
        return Component.translatable("hud.panthrixsgalaxy.layer." + key);
    }

    /** Dibuja un texto 3 veces más grande, centrado. */
    private static void drawBig(GuiGraphics graphics, Font font, Component text, int centerX, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0);
        graphics.pose().scale(3.0f, 3.0f, 1.0f);
        graphics.drawCenteredString(font, text, 0, 0, color);
        graphics.pose().popPose();
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("rocket", ROCKET_HUD);
    }

    private RocketHudOverlay() {
    }
}
