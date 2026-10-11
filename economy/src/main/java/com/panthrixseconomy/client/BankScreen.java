package com.panthrixseconomy.client;

import com.panthrixseconomy.network.BankAction;
import com.panthrixseconomy.network.BankView;
import com.panthrixseconomy.network.PENetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pantalla del banco: saldo, depositar, retirar, transferir e historial. */
public class BankScreen extends Screen {

    private static final int W = 260;
    private static final int H = 220;
    private BankView view;
    private EditBox amount;
    private EditBox target;

    public BankScreen(BankView view) {
        super(Component.literal(view.name()));
        this.view = view;
    }

    public void update(BankView v) {
        this.view = v;
    }

    @Override
    protected void init() {
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        String oldAmount = amount == null ? "" : amount.getValue();
        String oldTarget = target == null ? "" : target.getValue();
        amount = new EditBox(font, x + 12, y + 62, 70, 18, Component.literal("Cantidad"));
        amount.setFilter(s -> s.matches("\\d{0,6}"));
        amount.setHint(Component.literal("Cantidad"));
        amount.setValue(oldAmount);
        addRenderableWidget(amount);
        addRenderableWidget(Button.builder(Component.literal("Depositar"), b -> send(BankAction.DEPOSIT))
                .bounds(x + 88, y + 61, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Retirar"), b -> send(BankAction.WITHDRAW))
                .bounds(x + 170, y + 61, 76, 20).build());
        target = new EditBox(font, x + 12, y + 88, 152, 18, Component.literal("Jugador"));
        target.setMaxLength(16);
        target.setHint(Component.literal("Jugador (para transferir)"));
        target.setValue(oldTarget);
        addRenderableWidget(target);
        addRenderableWidget(Button.builder(Component.literal("Transferir"), b -> send(BankAction.TRANSFER))
                .bounds(x + 170, y + 87, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> onClose())
                .bounds(x + W / 2 - 40, y + H - 26, 80, 20).build());
    }

    private void send(int action) {
        int n;
        try {
            n = Integer.parseInt(amount.getValue());
        } catch (NumberFormatException e) {
            return;
        }
        if (n <= 0 || (action == BankAction.TRANSFER && target.getValue().isBlank())) {
            return;
        }
        PENetwork.toServer(new BankAction(action, target.getValue().trim(), n));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        g.fill(x, y, x + W, y + H, 0xE0202630);
        g.fill(x, y, x + W, y + 2, 0xFFE0B040);
        g.drawCenteredString(font, view.name(), x + W / 2, y + 8, 0xFFE0B040);
        g.drawString(font, "Saldo: " + view.balance() + " ₱", x + 12, y + 24, 0xFFFFFF);
        g.drawString(font, "Monedas encima: " + view.coins(), x + 12, y + 36, 0xAAAAAA);
        if (view.tax() > 0) {
            g.drawString(font, "Impuesto por transferencia: " + view.tax() + "%", x + 12, y + 48, 0xAAAAAA);
        }
        g.drawString(font, view.status(), x + 12, y + 112, 0xFFFFFF);
        g.drawString(font, "Últimos movimientos", x + 12, y + 126, 0xE0B040);
        int ly = y + 138;
        for (String line : view.history()) {
            if (ly > y + H - 36) {
                break;
            }
            g.drawString(font, line, x + 12, ly, 0xBBBBBB);
            ly += 10;
        }
        if (view.history().isEmpty()) {
            g.drawString(font, "Aún no hay movimientos.", x + 12, ly, 0x888888);
        }
        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
