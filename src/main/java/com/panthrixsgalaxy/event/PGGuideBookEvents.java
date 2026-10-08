package com.panthrixsgalaxy.event;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.item.PGGuideBook;
import com.panthrixsgalaxy.platform.PGPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El libro guía:
 *   - La PRIMERA vez que un jugador entra a un mundo, recibe el libro.
 *     (Se apunta en sus datos guardados, así que no se repite al volver a entrar ni al morir.)
 *   - /pgguide → da otra copia a quien la pida (no hace falta ser operador).
 *   - Se puede desactivar en config/panthrixsgalaxy-common.toml → giveGuideBook.
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGGuideBookEvents {

    private static final String RECEIVED_KEY = PanthrixsGalaxy.MOD_ID + ":received_guide_book";

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !PGConfig.giveGuideBook.get()) {
            return;
        }
        // Los datos "persistidos" sobreviven a la muerte y al cambio de dimensión
        CompoundTag persisted = PGPlatform.getPersistedData(player);
        if (persisted.getBoolean(RECEIVED_KEY)) {
            return;
        }
        persisted.putBoolean(RECEIVED_KEY, true);

        give(player);
        player.sendSystemMessage(Component.translatable("message.panthrixsgalaxy.guide_book_welcome")
                .withStyle(ChatFormatting.AQUA));
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("pgguide")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    give(player);
                    context.getSource().sendSuccess(() -> Component.translatable("command.panthrixsgalaxy.guide_given"), false);
                    return 1;
                }));
    }

    /** Al inventario; si está lleno, cae al suelo junto al jugador. */
    private static void give(ServerPlayer player) {
        if (!player.getInventory().add(PGGuideBook.create())) {
            player.drop(PGGuideBook.create(), false);
        }
    }

    private PGGuideBookEvents() {
    }
}
