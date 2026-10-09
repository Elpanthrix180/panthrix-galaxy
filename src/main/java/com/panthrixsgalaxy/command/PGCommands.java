package com.panthrixsgalaxy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.panthrixsgalaxy.platform.PGPlatform;
import com.panthrixsgalaxy.system.oxygen.PGAtmosphere;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comandos del mod para pruebas.
 *
 *   /pgvacuum true   -> simula que no hay aire (como en el espacio)
 *   /pgvacuum false  -> vuelve a la normalidad
 *
 * Necesita permisos de operador (trucos activados en un mundo de un jugador).
 */
public final class PGCommands {

    /** Registra los comandos (lo llaman Forge y Fabric al arrancar el servidor). */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pgvacuum")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            PGPlatform.getPersistentData(player).putBoolean(PGAtmosphere.VACUUM_TEST_TAG, enabled);
                            context.getSource().sendSuccess(() -> Component.translatable(enabled
                                    ? "command.panthrixsgalaxy.vacuum_on"
                                    : "command.panthrixsgalaxy.vacuum_off"), false);
                            return 1;
                        })));
    }

    private PGCommands() {
    }
}
