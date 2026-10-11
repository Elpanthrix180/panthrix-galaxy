package com.panthrixseconomy.client;

import com.panthrixseconomy.network.PENetwork;
import com.panthrixseconomy.network.ShopAction;
import com.panthrixseconomy.network.ShopView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pantalla de la tienda: ofertas con su objeto, precio y botón de comprar o vender. */
public class ShopScreen extends Screen {

    private static final int W = 260;
    private static final int H = 220;
    private static final int PER_PAGE = 7;
    private ShopView view;
    private int page;

    public ShopScreen(ShopView view) {
        super(Component.literal(view.name()));
        this.view = view;
    }

    public void update(ShopView v) {
        this.view = v;
        rebuildWidgets();
    }

    private int pages() {
        return Math.max(1, (view.offers().size() + PER_PAGE - 1) / PER_PAGE);
    }

    @Override
    protected void init() {
        page = Math.min(page, pages() - 1);
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index >= view.offers().size()) {
                break;
            }
            ShopView.Offer o = view.offers().get(index);
            boolean buying = o.sells();  // si el comercio vende, el jugador compra
            addRenderableWidget(Button.builder(Component.literal(buying ? "Comprar" : "Vender"),
                    b -> PENetwork.toServer(new ShopAction(index, buying))).bounds(x + W - 76, y + 38 + i * 22, 64, 20).build());
        }
        if (pages() > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                page = (page + pages() - 1) % pages();
                rebuildWidgets();
            }).bounds(x + 12, y + H - 26, 24, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                page = (page + 1) % pages();
                rebuildWidgets();
            }).bounds(x + 40, y + H - 26, 24, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> onClose())
                .bounds(x + W / 2 - 40, y + H - 26, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        g.fill(x, y, x + W, y + H, 0xE0202630);
        g.fill(x, y, x + W, y + 2, 0xFF40B060);
        g.drawCenteredString(font, view.name(), x + W / 2, y + 8, 0xFF60D080);
        g.drawString(font, "Saldo: " + view.balance() + " ₱  (" + view.payWith() + ")", x + 12, y + 22, 0xCCCCCC);
        if (view.offers().isEmpty()) {
            g.drawString(font, "Este comercio aún no tiene ofertas.", x + 12, y + 44, 0x888888);
        }
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index >= view.offers().size()) {
                break;
            }
            ShopView.Offer o = view.offers().get(index);
            int ry = y + 38 + i * 22;
            g.fill(x + 8, ry - 1, x + W - 8, ry + 21, 0x40000000);
            g.renderItem(o.item(), x + 12, ry + 2);
            g.renderItemDecorations(font, o.item(), x + 12, ry + 2);
            String label = (o.sells() ? "Vende " : "Compra ") + o.item().getHoverName().getString();
            g.drawString(font, label, x + 34, ry + 2, o.sells() ? 0xFFFFFF : 0xFFE080);
            g.drawString(font, o.price() + " ₱ por " + o.item().getCount(), x + 34, ry + 11, 0xAAAAAA);
            if (mouseX >= x + 12 && mouseX < x + 28 && mouseY >= ry + 2 && mouseY < ry + 18) {
                g.renderTooltip(font, o.item(), mouseX, mouseY);
            }
        }
        if (pages() > 1) {
            g.drawString(font, (page + 1) + "/" + pages(), x + 70, y + H - 20, 0xAAAAAA);
        }
        g.drawString(font, view.status(), x + 12, y + H - 40, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
