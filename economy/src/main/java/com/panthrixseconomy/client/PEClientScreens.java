package com.panthrixseconomy.client;

import com.panthrixseconomy.network.BankView;
import com.panthrixseconomy.network.ShopView;
import net.minecraft.client.Minecraft;

/** Solo cliente: abre o actualiza las pantallas cuando llegan datos del servidor. */
public final class PEClientScreens {

    public static void showBank(BankView view) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BankScreen screen) {
            screen.update(view);
        } else {
            mc.setScreen(new BankScreen(view));
        }
    }

    public static void showShop(ShopView view) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ShopScreen screen) {
            screen.update(view);
        } else {
            mc.setScreen(new ShopScreen(view));
        }
    }

    private PEClientScreens() {
    }
}
