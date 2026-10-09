package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.rocket.LaunchState;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Panel del cohete (solo cuando vas dentro):
 *
 *   COHETE BÁSICO · Destino: Luna
 *   Combustible [███████░░░] 1400 / 2000 mB
 *   Altitud 238 · Velocidad 20 m/s · Atravesando las nubes
 *
 *   y en el centro, en grande: T-5 ... ¡DESPEGUE!
 */
public final class RocketHudOverlay {

    private static final int BAR_WIDTH = 120;
    private static final int COLOR_FUEL = 0xFFFF9922;
    private static final int COLOR_INFO = 0xFF55CCFF;
    private static final int COLOR_HINT = 0xFFAAAAAA;
    private static final int COLOR_ALERT = 0xFFFF4444;
    private static final int FADE_TICKS = 30;

    /** Ticks que quedan del fundido a negro tras cambiar de dimensión. */
    private static int fadeTicks;

    public static void startFade() {
        fadeTicks = FADE_TICKS;
    }

    public static void tickFade() {
        if (fadeTicks > 0) {
            fadeTicks--;
        }
    }

    /** Dibuja el indicador (lo llaman Forge y Fabric cada fotograma). */
    public static void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        // Fundido a negro (también mientras el piloto vuelve a sentarse)
        if (fadeTicks > 0) {
            int alpha = (int) (255.0f * fadeTicks / FADE_TICKS);
            graphics.fill(0, 0, screenWidth, screenHeight, alpha << 24);
        }
        if (minecraft.player == null || minecraft.options.hideGui || !PGConfig.showVehicleHud.get()
                || !(minecraft.player.getVehicle() instanceof PGRocketEntity rocket)) {
            return;
        }
        Font font = minecraft.font;
        int centerX = screenWidth / 2;
        int y = 8;
        LaunchState state = rocket.getLaunchState();

        // ----- Panel superior -----
        if (state == LaunchState.IN_SPACE) {
            renderSpacePanel(graphics, font, rocket, centerX, y, minecraft.player.getYRot(), minecraft.player.getXRot());
            return;
        }
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
    }

    /**
     * Panel de navegación en el Espacio:
     *   ESPACIO · Destino: Luna (ESPACIO para cambiar)
     *   Distancia 412 bloques · ◄ gira a la izquierda · ▲ sube
     *   Combustible [████░░] · 34 m/s
     *   W acelerar · S frenar · Mira hacia donde quieres ir
     */
    private static void renderSpacePanel(GuiGraphics graphics, Font font, PGRocketEntity rocket, int centerX, int y,
                                         float playerYaw, float playerPitch) {
        graphics.fill(centerX - 125, y - 4, centerX + 125, y + 56, 0xA0000000);
        PGPlanet destination = rocket.getDestination();
        graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.space_destination",
                Component.translatable(destination.getTranslationKey())), centerX, y, COLOR_INFO);

        // Distancia y dirección hacia el destino
        Component guidance;
        if (destination == PGPlanets.EARTH) {
            int height = (int) (rocket.getY() - PGPlanets.EARTH_REENTRY_Y);
            guidance = Component.translatable("hud.panthrixsgalaxy.space_to_earth", Math.max(0, height));
        } else {
            Vec3 toTarget = rocket.getPlanetPosition(destination).subtract(rocket.position());
            int distance = (int) Math.max(0, toTarget.length() - destination.entryDistance());
            double targetYaw = Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
            float turn = Mth.wrapDegrees((float) targetYaw - playerYaw);
            double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            float targetPitch = (float) -Math.toDegrees(Math.atan2(toTarget.y, horizontal));
            float tilt = targetPitch - playerPitch;
            String turnKey = Math.abs(turn) < 12.0f ? "ahead" : turn < 0 ? "left" : "right";
            String tiltKey = Math.abs(tilt) < 12.0f ? "level" : tilt < 0 ? "up" : "down";
            guidance = Component.translatable("hud.panthrixsgalaxy.space_guidance", distance,
                    Component.translatable("hud.panthrixsgalaxy.turn." + turnKey),
                    Component.translatable("hud.panthrixsgalaxy.tilt." + tiltKey));
        }
        graphics.drawCenteredString(font, guidance, centerX, y + 12, 0xFFFFFFFF);

        // Combustible y velocidad
        int barX = centerX - BAR_WIDTH / 2;
        int barY = y + 25;
        int capacity = rocket.getFuelCapacity();
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 5, 0xFF2A3040);
        if (capacity > 0) {
            graphics.fill(barX, barY, barX + (int) ((long) BAR_WIDTH * rocket.getFuel() / capacity), barY + 5, COLOR_FUEL);
        }
        int speed = (int) Math.round(rocket.getDeltaMovement().length() * 20.0);
        graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.space_fuel_speed",
                rocket.getFuel(), capacity, speed), centerX, barY + 8,
                rocket.getFuel() > 0 ? COLOR_FUEL : COLOR_ALERT);
        graphics.drawCenteredString(font, Component.translatable(rocket.getFuel() > 0
                ? "hud.panthrixsgalaxy.space_controls" : "hud.panthrixsgalaxy.space_no_fuel"),
                centerX, barY + 20, COLOR_HINT);
    }

    /** Tercera línea del panel según la etapa del vuelo. */
    private static Component statusLine(PGRocketEntity rocket, LaunchState state) {
        return switch (state) {
            case IDLE -> {
                if (rocket.needsLaunchPad() && !rocket.isOnLaunchPad()) {
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

    private RocketHudOverlay() {
    }
}
