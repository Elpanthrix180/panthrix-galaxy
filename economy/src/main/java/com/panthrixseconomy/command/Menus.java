package com.panthrixseconomy.command;

import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.BankNames;
import com.panthrixseconomy.data.Country;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.data.Shop;
import com.panthrixseconomy.permission.PEPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Interfaz sencilla en el chat: textos con botones [clicables].
 * Un botón solo escribe o ejecuta un comando: el servidor vuelve a comprobar los permisos
 * en cada comando, así que nadie puede saltarse las reglas modificando el cliente.
 */
public final class Menus {

    public static MutableComponent title(String text) {
        return Component.literal("══ " + text + " ══").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static MutableComponent line(String label, String value) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    /** Botón que ejecuta un comando al hacer clic. */
    public static MutableComponent run(String label, String command, String hover) {
        return button(label, command, hover, ClickEvent.Action.RUN_COMMAND, ChatFormatting.GREEN);
    }

    /** Botón que escribe un comando en el chat para completarlo (p. ej. un nombre). */
    public static MutableComponent suggest(String label, String command, String hover) {
        return button(label, command, hover, ClickEvent.Action.SUGGEST_COMMAND, ChatFormatting.AQUA);
    }

    public static MutableComponent danger(String label, String command, String hover) {
        return button(label, command, hover, ClickEvent.Action.RUN_COMMAND, ChatFormatting.RED);
    }

    private static MutableComponent button(String label, String command, String hover, ClickEvent.Action action, ChatFormatting color) {
        return Component.literal("[" + label + "]").withStyle(Style.EMPTY.withColor(color)
                .withClickEvent(new ClickEvent(action, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hover))));
    }

    public static MutableComponent row(MutableComponent... parts) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append(" ");
            }
            out.append(parts[i]);
        }
        return out;
    }

    public static String q(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public static String countryName(EconomyData data, java.util.UUID id) {
        Country c = id == null ? null : data.countries.get(id);
        return c == null ? "Sin país" : c.name;
    }

    // ===================== Núcleo del Pueblo =====================

    public static void townCore(ServerPlayer player, EconomyData data, Settlement s, Loc coreLoc) {
        boolean admin = PEPermissions.isAdmin(player);
        player.sendSystemMessage(title("Núcleo del Pueblo"));
        if (s == null) {
            player.sendSystemMessage(Component.literal("Este núcleo no pertenece a ningún pueblo.").withStyle(ChatFormatting.YELLOW));
            if (admin) {
                player.sendSystemMessage(run("Crear pueblo aquí", "/pe core newtown " + coreLoc.shortText(),
                        "Crea un pueblo oficial con centro en este núcleo"));
            }
            return;
        }
        boolean manager = player.getUUID().equals(s.managerId);
        player.sendSystemMessage(line(s.type.label, s.name));
        player.sendSystemMessage(line("País", countryName(data, s.countryId)));
        player.sendSystemMessage(line("Responsable", s.managerName == null ? "—" : s.managerName));
        player.sendSystemMessage(line("Territorio", "radio " + s.radius + " bloques"));
        List<Bank> banks = data.banks.values().stream().filter(b -> s.contains(b.loc)).toList();
        List<Shop> shops = data.shops.values().stream().filter(sh -> s.contains(sh.loc)).toList();
        player.sendSystemMessage(line("Servicios", banks.size() + " banco(s), " + shops.size() + " comercio(s)"));
        for (Bank b : banks) {
            player.sendSystemMessage(Component.literal("  • " + data.bankName(b) + " (" + b.loc.shortText() + ")").withStyle(ChatFormatting.GRAY));
        }
        for (Shop sh : shops) {
            player.sendSystemMessage(Component.literal("  • Comercio: " + sh.name + " (" + sh.loc.shortText() + ")").withStyle(ChatFormatting.GRAY));
        }
        for (Settlement.Job j : s.jobs) {
            player.sendSystemMessage(Component.literal("  • Empleo: " + j.name() + " — " + j.salary() + " ₱/día (" + data.workers(s, j.name())
                    + "/" + j.slots() + ")").withStyle(ChatFormatting.GRAY));
        }
        if (!s.jobs.isEmpty()) {
            player.sendSystemMessage(run("Buscar trabajo", "/trabajo", "Ver empleos y apuntarte"));
        }
        if (admin || manager) {
            player.sendSystemMessage(line("Tesorería local", s.treasury + " ₱"));
            Country c = s.countryId == null ? null : data.countries.get(s.countryId);
            if (c != null) {
                player.sendSystemMessage(line("Economía nacional", "tesorería " + c.treasury + " ₱ · depósitos " + data.nationalDeposits(c)
                        + " ₱ · impuesto " + c.taxPercent + "%"));
            }
        }
        if (admin) {
            settlementAdminButtons(player, s);
        }
    }

    public static void settlementAdminButtons(ServerPlayer player, Settlement s) {
        String id = s.id.toString();
        player.sendSystemMessage(Component.literal("Administración:").withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(row(
                suggest("Renombrar", "/pe settlement rename " + id + " ", "Escribe el nuevo nombre"),
                run("Asignar país", "/pe settlement country " + id, "Elige el país de la lista"),
                suggest("Responsable", "/pe settlement manager " + id + " ", "Designa a un jugador responsable"),
                suggest("Radio", "/pe settlement radius " + id + " ", "Tamaño del territorio")));
        player.sendSystemMessage(row(
                suggest("Tesorería +", "/pe settlement treasury " + id + " add ", "Añadir fondos"),
                suggest("Tesorería −", "/pe settlement treasury " + id + " remove ", "Retirar fondos"),
                run(s.type == com.panthrixseconomy.data.SettlementType.TOWN ? "Hacer ciudad" : "Hacer pueblo",
                        "/pe settlement type " + id + " " + (s.type == com.panthrixseconomy.data.SettlementType.TOWN ? "city" : "town"), "Cambiar el tipo"),
                danger("Eliminar", "/pe settlement delete " + id, "Muestra las consecuencias antes de confirmar")));
        player.sendSystemMessage(row(
                suggest("Nuevo empleo", "/pe settlement job " + id + " add ", "salario plazas nombre (p. ej. 20 3 Panadero)"),
                suggest("Quitar empleo", "/pe settlement job " + id + " remove ", "nombre del empleo")));
    }

    // ===================== Banco =====================

    public static void bank(ServerPlayer player, EconomyData data, Bank b) {
        if (b == null) {
            player.sendSystemMessage(Component.literal("Este mostrador no está dado de alta. Pide a un administrador que lo active.")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        String identity = data.identityOf(b);
        player.sendSystemMessage(title(data.bankName(b)));
        player.sendSystemMessage(line("Tu saldo", data.balance(identity, player.getUUID()) + " ₱"));
        Country c = data.countryOf(b);
        if (c != null && c.taxPercent > 0) {
            player.sendSystemMessage(line("Impuesto por transferencia", c.taxPercent + "%"));
        }
        player.sendSystemMessage(row(
                suggest("Depositar", "/banco depositar ", "Deposita monedas que llevas encima"),
                suggest("Retirar", "/banco retirar ", "Saca monedas de tu cuenta"),
                suggest("Transferir", "/banco transferir ", "jugador cantidad"),
                run("Historial", "/banco historial", "Tus últimos movimientos")));
        if (PEPermissions.isAdmin(player)) {
            player.sendSystemMessage(row(
                    run("Asignar país", "/pe bank assign", "Elige el país de esta sucursal"),
                    run("Según ubicación", "/pe bank auto", "Quitar la asignación manual")));
        }
    }

    /** Botones de administración de una sucursal (la pantalla del banco es para todos). */
    public static void bankAdmin(ServerPlayer player) {
        player.sendSystemMessage(row(
                run("Asignar país", "/pe bank assign", "Elige el país de esta sucursal"),
                run("Según ubicación", "/pe bank auto", "Quitar la asignación manual")));
    }

    public static void bankCreated(ServerPlayer player, EconomyData data, Bank b) {
        player.sendSystemMessage(Component.literal("Sucursal creada: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(data.bankName(b)).withStyle(ChatFormatting.GOLD)));
        if (data.countryOf(b) == null) {
            player.sendSystemMessage(Component.literal("Está fuera de cualquier país registrado. ").withStyle(ChatFormatting.YELLOW)
                    .append(run("Asignar país", "/pe bank assign", "Elige el país de esta sucursal")));
        }
    }

    // ===================== Avisos de borrado =====================

    public static void deletionWarning(CommandSourceStack src, String what, List<String> consequences, String confirmCommand) {
        src.sendSystemMessage(Component.literal("⚠ Vas a eliminar " + what + ". Consecuencias:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        for (String c : consequences) {
            src.sendSystemMessage(Component.literal("  • " + c).withStyle(ChatFormatting.YELLOW));
        }
        src.sendSystemMessage(row(danger("CONFIRMAR", confirmCommand, "No se puede deshacer"),
                Component.literal("(o escribe " + confirmCommand + ")").withStyle(ChatFormatting.DARK_GRAY)));
    }

    public static String independentName() {
        return BankNames.INDEPENDENT;
    }

    private Menus() {
    }
}
