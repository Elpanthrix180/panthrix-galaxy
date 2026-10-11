package com.panthrixseconomy.network;

import com.panthrixseconomy.command.PlayerCommands;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Shop;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: comprar o vender la oferta número "index" (desde 0). */
public record ShopAction(int index, boolean buying) {

    void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeBoolean(buying);
    }

    static ShopAction decode(FriendlyByteBuf buf) {
        return new ShopAction(buf.readVarInt(), buf.readBoolean());
    }

    void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ServerPlayer p = ctx.get().getSender();
        if (p == null) {
            return;
        }
        Shop shop = PlayerCommands.reachableShop(p);
        if (shop == null) {
            p.sendSystemMessage(Component.literal("Estás demasiado lejos del comercio.").withStyle(ChatFormatting.RED));
            return;
        }
        EconomyData data = EconomyData.get(p.server);
        PlayerCommands.takeLastMessage(p);
        if (index >= 0) {
            PlayerCommands.trade(p, data, shop, index + 1, buying);
        }
        PENetwork.toPlayer(p, ShopView.of(p, data, shop, PlayerCommands.takeLastMessage(p)));
    }
}
