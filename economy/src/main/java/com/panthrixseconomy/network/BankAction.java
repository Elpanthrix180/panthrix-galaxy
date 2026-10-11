package com.panthrixseconomy.network;

import com.panthrixseconomy.command.PlayerCommands;
import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.EconomyData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: depositar (0), retirar (1), transferir (2) o solo refrescar (3). */
public record BankAction(int action, String target, int amount) {

    public static final int DEPOSIT = 0, WITHDRAW = 1, TRANSFER = 2, REFRESH = 3;

    void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(action);
        buf.writeUtf(target, 16);
        buf.writeVarInt(amount);
    }

    static BankAction decode(FriendlyByteBuf buf) {
        return new BankAction(buf.readVarInt(), buf.readUtf(16), buf.readVarInt());
    }

    void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ServerPlayer p = ctx.get().getSender();
        if (p == null) {
            return;
        }
        Bank b = PlayerCommands.reachableBank(p);  // el servidor vuelve a comprobar la distancia
        if (b == null) {
            p.sendSystemMessage(Component.literal("Estás demasiado lejos del banco.").withStyle(ChatFormatting.RED));
            return;
        }
        EconomyData data = EconomyData.get(p.server);
        PlayerCommands.takeLastMessage(p);
        int amt = Math.max(1, Math.min(amount, 100000));
        switch (action) {
            case DEPOSIT -> PlayerCommands.deposit(p, data, b, amt);
            case WITHDRAW -> PlayerCommands.withdraw(p, data, b, amt);
            case TRANSFER -> {
                ServerPlayer to = p.server.getPlayerList().getPlayerByName(target);
                if (to == null) {
                    PENetwork.toPlayer(p, BankView.of(p, data, b,
                            Component.literal("«" + target + "» no está conectado.").withStyle(ChatFormatting.RED)));
                    return;
                }
                PlayerCommands.transfer(p, data, b, to, amt);
            }
            default -> {
            }
        }
        PENetwork.toPlayer(p, BankView.of(p, data, b, PlayerCommands.takeLastMessage(p)));
    }
}
