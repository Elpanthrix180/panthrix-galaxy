package com.panthrixseconomy.network;

import com.panthrixseconomy.PanthrixsEconomy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Paquetes de las pantallas del banco y de la tienda.
 * El cliente solo pide acciones; el servidor comprueba distancia, saldo y permisos y responde
 * con los datos actualizados. Un cliente modificado no puede saltarse nada.
 */
public final class PENetwork {

    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PanthrixsEconomy.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(BankView.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BankView::encode).decoder(BankView::decode).consumerMainThread(BankView::handle).add();
        CHANNEL.messageBuilder(BankAction.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BankAction::encode).decoder(BankAction::decode).consumerMainThread(BankAction::handle).add();
        CHANNEL.messageBuilder(ShopView.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ShopView::encode).decoder(ShopView::decode).consumerMainThread(ShopView::handle).add();
        CHANNEL.messageBuilder(ShopAction.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ShopAction::encode).decoder(ShopAction::decode).consumerMainThread(ShopAction::handle).add();
    }

    public static void toPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void toServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    private PENetwork() {
    }
}
