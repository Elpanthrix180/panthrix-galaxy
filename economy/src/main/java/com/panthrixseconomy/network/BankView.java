package com.panthrixseconomy.network;

import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.Country;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Transaction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/** Servidor -> cliente: datos que muestra la pantalla del banco. */
public record BankView(String name, long balance, int tax, int coins, List<String> history, Component status) {

    public static BankView of(ServerPlayer p, EconomyData data, Bank b, Component status) {
        String id = data.identityOf(b);
        Country c = data.countryOf(b);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm");
        List<Transaction> mine = data.history.stream().filter(t -> t.involves(p.getUUID())).toList();
        List<String> lines = new ArrayList<>();
        for (Transaction t : mine.subList(Math.max(0, mine.size() - 7), mine.size())) {
            String sign = p.getUUID().equals(t.to()) ? "+" : "-";
            lines.add(0, fmt.format(new Date(t.time())) + "  " + t.type() + "  " + sign + t.amount() + " ₱");
        }
        int coins = p.getInventory().countItem(com.panthrixseconomy.registry.PERegistry.COIN.get());
        return new BankView(data.bankName(b), data.balance(id, p.getUUID()), c == null ? 0 : c.taxPercent, coins, lines, status);
    }

    void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeLong(balance);
        buf.writeVarInt(tax);
        buf.writeVarInt(coins);
        buf.writeVarInt(history.size());
        history.forEach(buf::writeUtf);
        buf.writeComponent(status);
    }

    static BankView decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        long balance = buf.readLong();
        int tax = buf.readVarInt();
        int coins = buf.readVarInt();
        int n = Math.min(buf.readVarInt(), 32);
        List<String> h = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            h.add(buf.readUtf());
        }
        return new BankView(name, balance, tax, coins, h, buf.readComponent());
    }

    void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.panthrixseconomy.client.PEClientScreens.showBank(this));
        ctx.get().setPacketHandled(true);
    }
}
