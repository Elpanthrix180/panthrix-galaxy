package com.panthrixsgalaxy.client.screen;

import com.panthrixsgalaxy.menu.PGShipMenu;
import com.panthrixsgalaxy.network.PGNetwork;
import com.panthrixsgalaxy.network.ShipActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/**
 * Panel de control de la nave: la bodega (ventana de cofre) + un panel a la derecha
 * con combustible, energía e integridad, y el botón "Recoger nave".
 */
public class PGShipScreen extends ContainerScreen {

    private static final int PANEL_WIDTH = 110;
    private static final int ROW_HEIGHT = 22;
    private static final int BAR_WIDTH = 98;
    private static final int COLOR_PANEL = 0xE0161B26;
    private static final int COLOR_BORDER = 0xFF55CCFF;
    private static final int COLOR_BAR_BACKGROUND = 0xFF2A3040;
    private static final int COLOR_FUEL = 0xFFFF9922;
    private static final int COLOR_ENERGY = 0xFFFFDD33;
    private static final int COLOR_INTEGRITY = 0xFF55DD66;

    private int panelX;

    public PGShipScreen(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // A la derecha de la ventana; si no cabe, a la izquierda
        panelX = leftPos + imageWidth + 4;
        if (panelX + PANEL_WIDTH > width) {
            panelX = leftPos - PANEL_WIDTH - 4;
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.panthrixsgalaxy.ship_pickup"),
                        button -> PGNetwork.sendToServer(new ShipActionPacket(ShipActionPacket.Action.PICKUP)))
                .bounds(panelX + 6, topPos + 18 + 3 * ROW_HEIGHT + 4, BAR_WIDTH, 20)
                .build());
    }

    /** Se dibuja con el fondo: así el botón queda por encima del panel. */
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        if (menu instanceof PGShipMenu shipMenu) {
            renderStatusPanel(graphics, shipMenu);
        }
    }

    private void renderStatusPanel(GuiGraphics graphics, PGShipMenu ship) {
        int x = panelX;
        int y = topPos;
        int height = 18 + 3 * ROW_HEIGHT + 30;
        graphics.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + height + 1, COLOR_BORDER);
        graphics.fill(x, y, x + PANEL_WIDTH, y + height, COLOR_PANEL);
        graphics.drawString(font, Component.translatable("gui.panthrixsgalaxy.ship_status"), x + 6, y + 5, 0xFFFFFF);

        int rowY = y + 18;
        rowY = bar(graphics, x, rowY, "gui.panthrixsgalaxy.ship_fuel", ship.getFuel(), ship.getMaxFuel(), COLOR_FUEL);
        rowY = bar(graphics, x, rowY, "gui.panthrixsgalaxy.ship_energy", ship.getEnergy(), ship.getMaxEnergy(), COLOR_ENERGY);
        bar(graphics, x, rowY, "gui.panthrixsgalaxy.ship_integrity", ship.getIntegrity(), ship.getMaxIntegrity(), COLOR_INTEGRITY);
    }

    private int bar(GuiGraphics graphics, int x, int rowY, String key, int amount, int capacity, int color) {
        graphics.drawString(font, Component.translatable(key, amount, capacity), x + 6, rowY, color, false);
        int barY = rowY + 10;
        graphics.fill(x + 6, barY, x + 6 + BAR_WIDTH, barY + 5, COLOR_BAR_BACKGROUND);
        if (capacity > 0) {
            graphics.fill(x + 6, barY, x + 6 + (int) ((long) BAR_WIDTH * amount / capacity), barY + 5, color);
        }
        return rowY + ROW_HEIGHT;
    }
}
