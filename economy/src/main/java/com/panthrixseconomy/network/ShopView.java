package com.panthrixseconomy.network;

import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Shop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Servidor -> cliente: datos que muestra la pantalla de la tienda. */
public record ShopView(String name, String payWith, long balance, List<Offer> offers, Component status) {

    public record Offer(ItemStack item, long price, boolean sells) {
    }

    public static ShopView of(ServerPlayer p, EconomyData data, Shop shop, Component status) {
        String id = data.shopIdentity(shop);
        List<Offer> list = new ArrayList<>();
        for (Shop.Offer o : shop.offers) {
            list.add(new Offer(o.item().copy(), o.price(), o.sells()));
        }
        return new ShopView(shop.name, data.identityName(id), data.balance(id, p.getUUID()), list, status);
    }

    void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeUtf(payWith);
        buf.writeLong(balance);
        buf.writeVarInt(offers.size());
        for (Offer o : offers) {
            buf.writeItem(o.item());
            buf.writeLong(o.price());
            buf.writeBoolean(o.sells());
        }
        buf.writeComponent(status);
    }

    static ShopView decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        String pay = buf.readUtf();
        long balance = buf.readLong();
        int n = Math.min(buf.readVarInt(), 64);
        List<Offer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Offer(buf.readItem(), buf.readLong(), buf.readBoolean()));
        }
        return new ShopView(name, pay, balance, list, buf.readComponent());
    }

    void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.panthrixseconomy.client.PEClientScreens.showShop(this));
        ctx.get().setPacketHandled(true);
    }
}
