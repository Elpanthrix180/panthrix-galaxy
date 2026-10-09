package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.system.oxygen.OxygenState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Indicador de oxígeno en pantalla.
 * Solo aparece cuando estás en un lugar sin aire. Se dibuja encima de la barra de comida:
 *   O₂ [██████████░░░░] 72%
 * y, si hay peligro, un aviso rojo parpadeante en el centro de la pantalla.
 *
 * "value = Dist.CLIENT" significa que esta clase solo existe en el juego del jugador,
 * nunca en un servidor dedicado (los servidores no tienen pantalla).
 */
public final class OxygenHudOverlay {

    private static final int BAR_WIDTH = 60;
    private static final int COLOR_OK = 0xFF55CCFF;
    private static final int COLOR_LOW = 0xFFFFAA00;
    private static final int COLOR_EMPTY = 0xFFFF4444;
    private static final int COLOR_BACKGROUND = 0xAA000000;

    /** Dibuja el indicador (lo llaman Forge y Fabric cada fotograma). */
    public static void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        OxygenState state = ClientOxygenData.getState();
        if (minecraft.player == null || minecraft.options.hideGui || state == OxygenState.BREATHABLE
                || !PGConfig.showOxygenHud.get()) {
            return;
        }
        Font font = minecraft.font;

        // ----- Barra de oxígeno (encima de la comida, a la derecha del centro) -----
        int capacity = ClientOxygenData.getCapacity();
        int oxygen = ClientOxygenData.getOxygen();
        int percent = capacity > 0 ? Math.round(100.0f * oxygen / capacity) : 0;
        int color = switch (state) {
            case LOW, EMERGENCY -> COLOR_LOW;
            case NO_HELMET, NO_OXYGEN -> COLOR_EMPTY;
            default -> COLOR_OK;
        };

        int x = screenWidth / 2 + 10;
        int y = screenHeight - 59;
        graphics.drawString(font, "O₂", x, y, color);
        int barX = x + 14;
        graphics.fill(barX, y + 1, barX + BAR_WIDTH, y + 7, COLOR_BACKGROUND);
        graphics.fill(barX, y + 1, barX + BAR_WIDTH * percent / 100, y + 7, color);
        graphics.drawString(font, percent + "%", barX + BAR_WIDTH + 3, y, color);

        // ----- Avisos en el centro de la pantalla -----
        boolean blink = (minecraft.player.tickCount / 10) % 2 == 0;
        Component warning = switch (state) {
            case NO_HELMET -> Component.translatable("hud.panthrixsgalaxy.no_helmet");
            case NO_OXYGEN -> Component.translatable("hud.panthrixsgalaxy.no_oxygen");
            case LOW -> Component.translatable("hud.panthrixsgalaxy.low_oxygen");
            case EMERGENCY -> Component.translatable("hud.panthrixsgalaxy.emergency_oxygen");
            default -> null;
        };
        if (warning != null && (blink || state == OxygenState.LOW || state == OxygenState.EMERGENCY)) {
            graphics.drawCenteredString(font, warning, screenWidth / 2, screenHeight / 2 + 20, color);
        }
        // Temperatura extrema (Fase 12): falta alguna pieza del traje
        if (ClientOxygenData.isTemperatureDanger()) {
            graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.extreme_temperature"),
                    screenWidth / 2, screenHeight / 2 + 44, blink ? COLOR_LOW : COLOR_EMPTY);
        }
        if (state == OxygenState.NO_HELMET || state == OxygenState.NO_OXYGEN) {
            int grace = ClientOxygenData.getGraceSecondsLeft();
            Component detail = grace > 0
                    ? Component.translatable("hud.panthrixsgalaxy.grace", grace)
                    : Component.translatable("hud.panthrixsgalaxy.suffocating");
            graphics.drawCenteredString(font, detail, screenWidth / 2, screenHeight / 2 + 32, COLOR_EMPTY);
        }
    }

    private OxygenHudOverlay() {
    }
}
