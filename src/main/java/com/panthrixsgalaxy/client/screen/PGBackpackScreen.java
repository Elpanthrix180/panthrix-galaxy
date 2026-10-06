package com.panthrixsgalaxy.client.screen;

import com.panthrixsgalaxy.item.PGBackpackItem.Tank;
import com.panthrixsgalaxy.menu.PGBackpackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/**
 * Pantalla de la mochila: la ventana de cofre de Minecraft + un panel a la derecha
 * con los 4 depósitos (oxígeno, energía, combustible y agua).
 */
public class PGBackpackScreen extends ContainerScreen {

    private static final int PANEL_WIDTH = 100;
    private static final int ROW_HEIGHT = 22;
    private static final int BAR_WIDTH = 88;
    private static final int COLOR_PANEL = 0xE0161B26;
    private static final int COLOR_BORDER = 0xFF55CCFF;
    private static final int COLOR_BAR_BACKGROUND = 0xFF2A3040;

    public PGBackpackScreen(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu instanceof PGBackpackMenu backpackMenu) {
            renderTankPanel(graphics, backpackMenu);
        }
    }

    private void renderTankPanel(GuiGraphics graphics, PGBackpackMenu backpackMenu) {
        // A la derecha de la ventana; si no cabe, a la izquierda
        int x = leftPos + imageWidth + 4;
        if (x + PANEL_WIDTH > width) {
            x = leftPos - PANEL_WIDTH - 4;
        }
        int y = topPos;
        int height = 16 + Tank.values().length * ROW_HEIGHT;

        graphics.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + height + 1, COLOR_BORDER);
        graphics.fill(x, y, x + PANEL_WIDTH, y + height, COLOR_PANEL);
        graphics.drawString(font, Component.translatable("gui.panthrixsgalaxy.tanks"), x + 6, y + 5, 0xFFFFFF);

        int rowY = y + 18;
        for (Tank tank : Tank.values()) {
            int amount = backpackMenu.getTankAmount(tank);
            int capacity = backpackMenu.getTankCapacity(tank);
            graphics.drawString(font, Component.translatable("gui.panthrixsgalaxy.tank." + tank.key(), amount, capacity),
                    x + 6, rowY, tank.getBarColor(), false);
            int barY = rowY + 10;
            graphics.fill(x + 6, barY, x + 6 + BAR_WIDTH, barY + 5, COLOR_BAR_BACKGROUND);
            if (capacity > 0) {
                int filled = (int) ((long) BAR_WIDTH * amount / capacity);
                graphics.fill(x + 6, barY, x + 6 + filled, barY + 5, tank.getBarColor());
            }
            rowY += ROW_HEIGHT;
        }
    }
}
