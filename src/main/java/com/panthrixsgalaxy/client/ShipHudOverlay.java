package com.panthrixsgalaxy.client;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Panel de la nave (solo cuando la pilotas):
 *
 *   NAVE ESPACIAL · MOTORES ENCENDIDOS
 *   Combustible [██████░░]   Energía [████░░░░]   Casco [███████░]
 *   Altitud 142 · Velocidad 18 m/s · Espacio a Y 450
 *   ESPACIO motores · W avanzar · S frenar
 *
 * En el Espacio, en vez de la altitud, muestra la distancia y dirección a la Tierra y a los 5 planetas más cercanos.
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ShipHudOverlay {

    private static final int BAR_WIDTH = 70;
    private static final int COLOR_FUEL = 0xFFFF9922;
    private static final int COLOR_ENERGY = 0xFFFFDD33;
    private static final int COLOR_INTEGRITY = 0xFF55DD66;
    private static final int COLOR_INFO = 0xFF55CCFF;
    private static final int COLOR_HINT = 0xFFAAAAAA;
    private static final int COLOR_ALERT = 0xFFFF4444;
    private static final int MAX_PLANET_LINES = 5;

    public static final IGuiOverlay SHIP_HUD = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui
                || !(minecraft.player.getVehicle() instanceof PGShipEntity ship)) {
            return;
        }
        Font font = minecraft.font;
        int centerX = screenWidth / 2;
        int y = 8;
        boolean inSpace = ship.isInSpace();
        List<PGPlanet> shown = inSpace ? nearestPlanets(ship) : List.of();
        int lines = inSpace ? shown.size() : 1;
        graphics.fill(centerX - 130, y - 4, centerX + 130, y + 40 + lines * 11, 0xA0000000);

        // Título y estado de los motores
        Component engines = Component.translatable(ship.areEnginesOn()
                ? "hud.panthrixsgalaxy.ship_engines_on" : "hud.panthrixsgalaxy.ship_engines_off");
        graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.ship_title."
                + ship.getTier().getSerializedName(), engines), centerX, y, COLOR_INFO);

        // Tres barras: combustible, energía e integridad
        int barY = y + 13;
        int startX = centerX - (BAR_WIDTH * 3 + 20) / 2;
        bar(graphics, font, startX, barY, ship.getFuel(), ship.getTier().getFuelCapacity(), COLOR_FUEL,
                "hud.panthrixsgalaxy.ship_bar_fuel");
        bar(graphics, font, startX + BAR_WIDTH + 10, barY, ship.getEnergy(), ship.getTier().getEnergyCapacity(),
                COLOR_ENERGY, "hud.panthrixsgalaxy.ship_bar_energy");
        bar(graphics, font, startX + 2 * (BAR_WIDTH + 10), barY, ship.getIntegrity(), ship.getTier().getMaxIntegrity(),
                COLOR_INTEGRITY, "hud.panthrixsgalaxy.ship_bar_integrity");

        // Navegación
        int lineY = barY + 16;
        int speed = (int) Math.round(ship.getDeltaMovement().length() * 20.0);
        if (inSpace) {
            for (PGPlanet planet : shown) {
                graphics.drawCenteredString(font, planetLine(ship, planet, minecraft.player), centerX, lineY,
                        ship.getTier().getReach() >= planet.requiredReach() && planet.isLandable() ? 0xFFFFFFFF : COLOR_HINT);
                lineY += 11;
            }
        } else {
            PGPlanet planet = PGPlanets.fromDimension(minecraft.level.dimension());
            int exit = planet == null ? 0 : planet.exitHeight();
            graphics.drawCenteredString(font, Component.translatable("hud.panthrixsgalaxy.ship_flight",
                    (int) ship.getY(), speed, exit), centerX, lineY, 0xFFFFFFFF);
            lineY += 11;
        }

        // Avisos y controles
        Component hint;
        int hintColor = COLOR_HINT;
        if (ship.getFuel() <= 0) {
            hint = Component.translatable("hud.panthrixsgalaxy.ship_no_fuel");
            hintColor = COLOR_ALERT;
        } else if (ship.getEnergy() <= 0) {
            hint = Component.translatable("hud.panthrixsgalaxy.ship_no_life_support");
            hintColor = COLOR_ALERT;
        } else if (ship.getIntegrity() < ship.getTier().getMaxIntegrity() / 4) {
            hint = Component.translatable("hud.panthrixsgalaxy.ship_hull_critical");
            hintColor = COLOR_ALERT;
        } else if (inSpace) {
            hint = Component.translatable("hud.panthrixsgalaxy.ship_space_speed", speed);
        } else {
            hint = Component.translatable("hud.panthrixsgalaxy.ship_controls");
        }
        if (hintColor != COLOR_ALERT || (minecraft.player.tickCount / 10) % 2 == 0) {
            graphics.drawCenteredString(font, hint, centerX, lineY + 2, hintColor);
        }
    };

    /** La Tierra y los 5 cuerpos celestes más cercanos (con 12 planetas no caben todos en la pantalla). */
    private static List<PGPlanet> nearestPlanets(PGShipEntity ship) {
        List<PGPlanet> others = new ArrayList<>(PGPlanets.ALL);
        others.remove(PGPlanets.EARTH);
        others.sort(Comparator.comparingDouble(planet -> ship.getPlanetPosition(planet).distanceTo(ship.position())));
        List<PGPlanet> result = new ArrayList<>();
        result.add(PGPlanets.EARTH);
        result.addAll(others.subList(0, Math.min(MAX_PLANET_LINES, others.size())));
        return result;
    }

    private static void bar(GuiGraphics graphics, Font font, int x, int y, int amount, int capacity, int color, String key) {
        graphics.drawString(font, Component.translatable(key), x, y, color, false);
        graphics.fill(x, y + 9, x + BAR_WIDTH, y + 13, 0xFF2A3040);
        if (capacity > 0) {
            graphics.fill(x, y + 9, x + (int) ((long) BAR_WIDTH * amount / capacity), y + 13, color);
        }
    }

    /** "Marte: 812 bloques · ◄ gira a la izquierda" (o "fuera de alcance"). */
    private static Component planetLine(PGShipEntity ship, PGPlanet planet, Player player) {
        Component name = Component.translatable(planet.getTranslationKey());
        if (planet == PGPlanets.EARTH) {
            int height = (int) Math.max(0, ship.getY() - PGPlanets.EARTH_REENTRY_Y);
            return Component.translatable("hud.panthrixsgalaxy.ship_planet_earth", name, height);
        }
        if (!planet.isLandable()) {
            return Component.translatable("hud.panthrixsgalaxy.ship_planet_gas_giant", name);
        }
        if (ship.getTier().getReach() < planet.requiredReach()) {
            return Component.translatable("hud.panthrixsgalaxy.ship_planet_out_of_reach", name);
        }
        Vec3 toTarget = ship.getPlanetPosition(planet).subtract(ship.position());
        int distance = (int) Math.max(0, toTarget.length() - planet.entryDistance());
        double targetYaw = Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
        float turn = Mth.wrapDegrees((float) targetYaw - player.getYRot());
        String turnKey = Math.abs(turn) < 12.0f ? "ahead" : turn < 0 ? "left" : "right";
        return Component.translatable("hud.panthrixsgalaxy.ship_planet", name, distance,
                Component.translatable("hud.panthrixsgalaxy.turn." + turnKey));
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ship", SHIP_HUD);
    }

    private ShipHudOverlay() {
    }
}
