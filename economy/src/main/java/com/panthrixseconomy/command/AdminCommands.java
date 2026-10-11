package com.panthrixseconomy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.panthrixseconomy.config.PEConfig;
import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.BankNames;
import com.panthrixseconomy.data.Country;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.data.SettlementType;
import com.panthrixseconomy.data.Shop;
import com.panthrixseconomy.permission.PEPermissions;
import com.panthrixseconomy.plaza.PlazaManager;
import com.panthrixseconomy.block.TownCoreBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * /pe ... — administración de la economía. TODO el árbol exige el permiso panthrixseconomy.admin,
 * comprobado en el servidor (los jugadores normales ni siquiera ven el comando).
 */
public final class AdminCommands {

    private static final SuggestionProvider<CommandSourceStack> COUNTRIES = (ctx, b) ->
            SharedSuggestionProvider.suggest(EconomyData.get(ctx.getSource().getServer()).countries.values().stream().map(c -> Menus.q(c.name)), b);
    private static final SuggestionProvider<CommandSourceStack> COUNTRIES_OR_NONE = (ctx, b) ->
            SharedSuggestionProvider.suggest(Stream.concat(Stream.of("none"),
                    EconomyData.get(ctx.getSource().getServer()).countries.values().stream().map(c -> Menus.q(c.name))), b);
    private static final SuggestionProvider<CommandSourceStack> SETTLEMENTS = (ctx, b) ->
            SharedSuggestionProvider.suggest(EconomyData.get(ctx.getSource().getServer()).settlements.values().stream().map(s -> Menus.q(s.name)), b);
    private static final SuggestionProvider<CommandSourceStack> SHOPS = (ctx, b) ->
            SharedSuggestionProvider.suggest(EconomyData.get(ctx.getSource().getServer()).shops.values().stream().map(s -> Menus.q(s.name)), b);
    private static final SuggestionProvider<CommandSourceStack> ARTICLES = (ctx, b) ->
            SharedSuggestionProvider.suggest(Stream.concat(Stream.of("auto"), BankNames.ARTICLES.stream()), b);

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(literal("pe").requires(PEPermissions::isAdmin)
                .executes(ctx -> menu(ctx.getSource()))
                .then(literal("menu").executes(ctx -> menu(ctx.getSource())))
                .then(countryTree())
                .then(literal("city").then(literal("create").then(argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> createSettlement(ctx, SettlementType.CITY)))))
                .then(literal("town").then(literal("create").then(argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> createSettlement(ctx, SettlementType.TOWN)))))
                .then(settlementTree())
                .then(shopTree())
                .then(bankTree())
                .then(literal("money").then(literal("give").then(argument("player", EntityArgument.player())
                        .then(argument("amount", IntegerArgumentType.integer(1, 100000)).executes(AdminCommands::giveCoins)))))
                .then(literal("core").then(literal("newtown").then(argument("pos", BlockPosArgument.blockPos())
                        .executes(AdminCommands::coreNewTown))))
                .then(literal("plaza")
                        .then(literal("info").executes(ctx -> PlazaManager.info(ctx.getSource())))
                        .then(literal("tp").executes(ctx -> PlazaManager.teleport(ctx.getSource())))
                        .then(literal("generate").executes(ctx -> PlazaManager.generateHere(ctx.getSource())))));
    }

    // ===================== Países =====================

    private static RequiredArgumentBuilder<CommandSourceStack, String> countryArg() {
        return argument("country", StringArgumentType.string()).suggests(COUNTRIES);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> countryTree() {
        return literal("country")
                .then(literal("list").executes(ctx -> listCountries(ctx.getSource())))
                .then(literal("create").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                    EconomyData data = data(ctx);
                    String name = StringArgumentType.getString(ctx, "name").trim();
                    if (name.isEmpty() || name.length() > 48) {
                        return fail(ctx, "El nombre debe tener entre 1 y 48 caracteres.");
                    }
                    if (data.countryNameTaken(name, null)) {
                        return fail(ctx, "Ya existe un país llamado «" + name + "».");
                    }
                    Country c = data.createCountry(name);
                    ok(ctx, "País creado: " + c.name + " · su banco se llamará «" + BankNames.baseName(c) + "»");
                    return 1;
                })))
                .then(literal("rename").then(countryArg().then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                    EconomyData data = data(ctx);
                    Country c = country(ctx);
                    String name = StringArgumentType.getString(ctx, "name").trim();
                    if (name.isEmpty() || name.length() > 48) {
                        return fail(ctx, "El nombre debe tener entre 1 y 48 caracteres.");
                    }
                    if (data.countryNameTaken(name, c)) {
                        return fail(ctx, "Ya existe un país llamado «" + name + "».");
                    }
                    String old = c.name;
                    data.renameCountry(c, name);
                    ok(ctx, "«" + old + "» ahora se llama «" + c.name + "». Sus bancos: «" + BankNames.baseName(c)
                            + "». Cuentas, saldos e historial intactos.");
                    return 1;
                }))))
                .then(literal("article").then(countryArg().then(argument("article", StringArgumentType.greedyString()).suggests(ARTICLES).executes(ctx -> {
                    Country c = country(ctx);
                    String a = StringArgumentType.getString(ctx, "article").trim().toLowerCase(java.util.Locale.ROOT);
                    if (a.equals("auto")) {
                        c.article = null;
                    } else if (BankNames.ARTICLES.contains(a)) {
                        c.article = a;
                    } else {
                        return fail(ctx, "Usa: auto, de, del, de la, de los o de las.");
                    }
                    data(ctx).setDirty();
                    ok(ctx, "Nombre del banco: «" + BankNames.baseName(c) + "»");
                    return 1;
                }))))
                .then(literal("tax").then(countryArg().then(argument("percent", IntegerArgumentType.integer(0, 50)).executes(ctx -> {
                    Country c = country(ctx);
                    c.taxPercent = IntegerArgumentType.getInteger(ctx, "percent");
                    data(ctx).setDirty();
                    ok(ctx, "Impuesto de " + c.name + ": " + c.taxPercent + "% por transferencia (va a la tesorería nacional).");
                    return 1;
                }))))
                .then(literal("treasury").then(countryArg()
                        .then(literal("add").then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> countryTreasury(ctx, 1))))
                        .then(literal("remove").then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> countryTreasury(ctx, -1))))))
                .then(literal("info").then(countryArg().executes(ctx -> countryInfo(ctx.getSource(), data(ctx), country(ctx)))))
                .then(literal("delete").then(countryArg()
                        .executes(ctx -> {
                            Country c = country(ctx);
                            Menus.deletionWarning(ctx.getSource(), "el país «" + c.name + "»", data(ctx).countryDeletionConsequences(c),
                                    "/pe country delete " + c.id + " confirm");
                            return 1;
                        })
                        .then(literal("confirm").executes(ctx -> {
                            Country c = country(ctx);
                            data(ctx).deleteCountry(c);
                            ok(ctx, "País «" + c.name + "» eliminado.");
                            return 1;
                        }))));
    }

    private static int countryTreasury(CommandContext<CommandSourceStack> ctx, int sign) throws CommandSyntaxException {
        Country c = country(ctx);
        long amount = LongArgumentType.getLong(ctx, "amount");
        if (sign < 0 && c.treasury < amount) {
            return fail(ctx, "La tesorería solo tiene " + c.treasury + " ₱.");
        }
        c.treasury += sign * amount;
        data(ctx).log("TESORERIA", null, null, sign * amount, EconomyData.identity(c), "Ajuste de administrador");
        ok(ctx, "Tesorería de " + c.name + ": " + c.treasury + " ₱");
        return 1;
    }

    private static int listCountries(CommandSourceStack src) {
        EconomyData data = EconomyData.get(src.getServer());
        src.sendSystemMessage(Menus.title("Países"));
        if (data.countries.isEmpty()) {
            src.sendSystemMessage(Component.literal("No hay países. ").withStyle(ChatFormatting.GRAY)
                    .append(Menus.suggest("Crear país", "/pe country create ", "Escribe el nombre oficial")));
        }
        for (Country c : data.countries.values()) {
            src.sendSystemMessage(Menus.row(Component.literal("• " + c.name).withStyle(ChatFormatting.WHITE),
                    Menus.run("Info", "/pe country info " + c.id, BankNames.baseName(c)),
                    Menus.suggest("Renombrar", "/pe country rename " + c.id + " ", "Nuevo nombre oficial"),
                    Menus.danger("Eliminar", "/pe country delete " + c.id, "Muestra las consecuencias antes")));
        }
        return 1;
    }

    private static int countryInfo(CommandSourceStack src, EconomyData data, Country c) {
        src.sendSystemMessage(Menus.title(c.name));
        src.sendSystemMessage(Menus.line("Banco", BankNames.baseName(c)));
        src.sendSystemMessage(Menus.line("Tesorería", c.treasury + " ₱"));
        src.sendSystemMessage(Menus.line("Impuesto", c.taxPercent + "%"));
        src.sendSystemMessage(Menus.line("Depósitos de los ciudadanos", data.nationalDeposits(c) + " ₱"));
        for (Settlement s : data.settlements.values()) {
            if (c.id.equals(s.countryId)) {
                src.sendSystemMessage(Component.literal("  • " + s.type.label + " " + s.name).withStyle(ChatFormatting.GRAY));
            }
        }
        for (Bank b : data.banks.values()) {
            if (c.equals(data.countryOf(b))) {
                src.sendSystemMessage(Component.literal("  • " + data.bankName(b) + " (" + b.loc.shortText() + ")").withStyle(ChatFormatting.GRAY));
            }
        }
        String id = c.id.toString();
        src.sendSystemMessage(Menus.row(
                Menus.suggest("Impuesto", "/pe country tax " + id + " ", "0 a 50 %"),
                Menus.suggest("Tesorería +", "/pe country treasury " + id + " add ", "Añadir fondos"),
                Menus.suggest("Tesorería −", "/pe country treasury " + id + " remove ", "Retirar fondos"),
                Menus.suggest("Artículo", "/pe country article " + id + " ", "auto, de, del, de la...")));
        return 1;
    }

    // ===================== Ciudades y pueblos =====================

    private static int createSettlement(CommandContext<CommandSourceStack> ctx, SettlementType type) {
        EconomyData data = data(ctx);
        String name = StringArgumentType.getString(ctx, "name").trim();
        if (name.isEmpty() || name.length() > 48) {
            return fail(ctx, "El nombre debe tener entre 1 y 48 caracteres.");
        }
        if (data.settlementNameTaken(name, null)) {
            return fail(ctx, "Ya existe una ciudad o pueblo llamado «" + name + "».");
        }
        Loc here = here(ctx.getSource());
        int radius = type == SettlementType.CITY ? PEConfig.CITY_RADIUS.get() : PEConfig.TOWN_RADIUS.get();
        Settlement s = data.createSettlement(name, type, here, radius);
        ok(ctx, type.label + " creado: " + s.name + " en " + here.shortText() + " (país: " + Menus.countryName(data, s.countryId) + ")");
        ctx.getSource().sendSystemMessage(Menus.row(
                Menus.run("Asignar país", "/pe settlement country " + s.id, "Elige el país"),
                Menus.run("Info", "/pe settlement info " + s.id, "Ver datos")));
        return 1;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> settlementArg() {
        return argument("settlement", StringArgumentType.string()).suggests(SETTLEMENTS);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> settlementTree() {
        return literal("settlement")
                .then(literal("list").executes(ctx -> {
                    EconomyData data = data(ctx);
                    ctx.getSource().sendSystemMessage(Menus.title("Ciudades y pueblos"));
                    for (Settlement s : data.settlements.values()) {
                        ctx.getSource().sendSystemMessage(Menus.row(
                                Component.literal("• " + s.type.label + " " + s.name + " (" + Menus.countryName(data, s.countryId) + ")"),
                                Menus.run("Info", "/pe settlement info " + s.id, s.center.shortText())));
                    }
                    return 1;
                }))
                .then(literal("info").then(settlementArg().executes(ctx -> {
                    Settlement s = settlement(ctx);
                    CommandSourceStack src = ctx.getSource();
                    src.sendSystemMessage(Menus.title(s.type.label + " " + s.name));
                    src.sendSystemMessage(Menus.line("País", Menus.countryName(data(ctx), s.countryId)));
                    src.sendSystemMessage(Menus.line("Centro", s.center.shortText() + " · radio " + s.radius));
                    src.sendSystemMessage(Menus.line("Tesorería", s.treasury + " ₱"));
                    src.sendSystemMessage(Menus.line("Responsable", s.managerName == null ? "—" : s.managerName));
                    src.sendSystemMessage(Menus.line("Núcleo", s.core == null ? "sin colocar" : s.core.shortText()));
                    if (src.getEntity() instanceof ServerPlayer p) {
                        Menus.settlementAdminButtons(p, s);
                    }
                    return 1;
                })))
                .then(literal("rename").then(settlementArg().then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                    Settlement s = settlement(ctx);
                    String name = StringArgumentType.getString(ctx, "name").trim();
                    if (name.isEmpty() || name.length() > 48) {
                        return fail(ctx, "El nombre debe tener entre 1 y 48 caracteres.");
                    }
                    if (data(ctx).settlementNameTaken(name, s)) {
                        return fail(ctx, "Ya existe una ciudad o pueblo llamado «" + name + "».");
                    }
                    s.name = name;
                    data(ctx).setDirty();
                    ok(ctx, "Ahora se llama «" + name + "».");
                    return 1;
                }))))
                .then(literal("country").then(settlementArg()
                        .executes(ctx -> {
                            Settlement s = settlement(ctx);
                            ctx.getSource().sendSystemMessage(Component.literal("¿A qué país pertenece " + s.name + "?").withStyle(ChatFormatting.GOLD));
                            for (Country c : data(ctx).countries.values()) {
                                ctx.getSource().sendSystemMessage(Menus.run(c.name, "/pe settlement country " + s.id + " " + c.id, "Asignar a " + c.name));
                            }
                            ctx.getSource().sendSystemMessage(Menus.row(Menus.run("Sin país", "/pe settlement country " + s.id + " none", "Quitar el país"),
                                    Menus.suggest("Crear país", "/pe country create ", "Escribe el nombre oficial")));
                            return 1;
                        })
                        .then(argument("country", StringArgumentType.string()).suggests(COUNTRIES_OR_NONE).executes(ctx -> {
                            Settlement s = settlement(ctx);
                            String t = StringArgumentType.getString(ctx, "country");
                            if (t.equalsIgnoreCase("none")) {
                                s.countryId = null;
                            } else {
                                s.countryId = country(ctx).id;
                            }
                            data(ctx).setDirty();
                            ok(ctx, s.name + " pertenece ahora a: " + Menus.countryName(data(ctx), s.countryId));
                            return 1;
                        }))))
                .then(literal("move").then(settlementArg().executes(ctx -> {
                    Settlement s = settlement(ctx);
                    s.center = here(ctx.getSource());
                    data(ctx).setDirty();
                    ok(ctx, "Centro de " + s.name + " movido a " + s.center.shortText());
                    return 1;
                })))
                .then(literal("radius").then(settlementArg().then(argument("radius", IntegerArgumentType.integer(8, 4096)).executes(ctx -> {
                    Settlement s = settlement(ctx);
                    s.radius = IntegerArgumentType.getInteger(ctx, "radius");
                    data(ctx).setDirty();
                    ok(ctx, "Radio de " + s.name + ": " + s.radius);
                    return 1;
                }))))
                .then(literal("type").then(settlementArg()
                        .then(literal("city").executes(ctx -> setType(ctx, SettlementType.CITY)))
                        .then(literal("town").executes(ctx -> setType(ctx, SettlementType.TOWN)))))
                .then(literal("manager").then(settlementArg().then(argument("player", EntityArgument.player()).executes(ctx -> {
                    Settlement s = settlement(ctx);
                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                    s.managerId = p.getUUID();
                    s.managerName = p.getGameProfile().getName();
                    data(ctx).setDirty();
                    ok(ctx, s.managerName + " es responsable de " + s.name + " (puede consultar sus datos, no crear entidades).");
                    return 1;
                }))))
                .then(literal("treasury").then(settlementArg()
                        .then(literal("add").then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> settlementTreasury(ctx, 1))))
                        .then(literal("remove").then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> settlementTreasury(ctx, -1))))))
                .then(literal("delete").then(settlementArg()
                        .executes(ctx -> {
                            Settlement s = settlement(ctx);
                            Menus.deletionWarning(ctx.getSource(), s.type.label.toLowerCase() + " «" + s.name + "»",
                                    data(ctx).settlementDeletionConsequences(s), "/pe settlement delete " + s.id + " confirm");
                            return 1;
                        })
                        .then(literal("confirm").executes(ctx -> {
                            Settlement s = settlement(ctx);
                            data(ctx).deleteSettlement(s);
                            if (s.core != null) {
                                ServerLevel level = ctx.getSource().getServer().getLevel(net.minecraft.resources.ResourceKey.create(
                                        net.minecraft.core.registries.Registries.DIMENSION, s.core.dim()));
                                if (level != null && level.getBlockEntity(s.core.pos()) instanceof TownCoreBlockEntity be) {
                                    be.setSettlementId(null);
                                }
                            }
                            ok(ctx, "«" + s.name + "» eliminado.");
                            return 1;
                        }))));
    }

    private static int setType(CommandContext<CommandSourceStack> ctx, SettlementType type) throws CommandSyntaxException {
        Settlement s = settlement(ctx);
        s.type = type;
        data(ctx).setDirty();
        ok(ctx, s.name + " ahora es: " + type.label);
        return 1;
    }

    private static int settlementTreasury(CommandContext<CommandSourceStack> ctx, int sign) throws CommandSyntaxException {
        Settlement s = settlement(ctx);
        long amount = LongArgumentType.getLong(ctx, "amount");
        if (sign < 0 && s.treasury < amount) {
            return fail(ctx, "La tesorería solo tiene " + s.treasury + " ₱.");
        }
        s.treasury += sign * amount;
        data(ctx).log("TESORERIA", null, null, sign * amount, "settlement:" + s.id, "Ajuste de administrador");
        ok(ctx, "Tesorería de " + s.name + ": " + s.treasury + " ₱");
        return 1;
    }

    private static int coreNewTown(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        ServerLevel level = ctx.getSource().getLevel();
        if (!(level.getBlockEntity(pos) instanceof TownCoreBlockEntity be)) {
            return fail(ctx, "Ahí no hay un Núcleo del Pueblo.");
        }
        EconomyData data = data(ctx);
        Loc loc = EconomyData.loc(level.dimension().location(), pos);
        int n = data.settlements.size() + 1;
        String name = "Nuevo Pueblo " + n;
        while (data.settlementNameTaken(name, null)) {
            name = "Nuevo Pueblo " + (++n);
        }
        Settlement s = data.createSettlement(name, SettlementType.TOWN, loc, PEConfig.TOWN_RADIUS.get());
        s.core = loc;
        be.setSettlementId(s.id);
        ok(ctx, "Pueblo creado: " + s.name);
        if (ctx.getSource().getEntity() instanceof ServerPlayer p) {
            Menus.settlementAdminButtons(p, s);
        }
        return 1;
    }

    // ===================== Comercios =====================

    private static LiteralArgumentBuilder<CommandSourceStack> shopTree() {
        return literal("shop")
                .then(literal("list").executes(ctx -> {
                    EconomyData data = data(ctx);
                    ctx.getSource().sendSystemMessage(Menus.title("Comercios oficiales"));
                    for (Shop s : data.shops.values()) {
                        Settlement where = data.settlementAt(s.loc);
                        ctx.getSource().sendSystemMessage(Menus.row(Component.literal("• " + s.name + " — "
                                        + (where == null ? "fuera de pueblos" : where.name) + " (" + s.loc.shortText() + ")"),
                                Menus.run("Ofertas", "/pe shop offer " + s.id, "Ver y gestionar ofertas"),
                                Menus.danger("Eliminar", "/pe shop delete " + s.id, "Pide confirmación")));
                    }
                    return 1;
                }))
                .then(literal("create").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name").trim();
                    if (name.isEmpty() || name.length() > 48) {
                        return fail(ctx, "El nombre debe tener entre 1 y 48 caracteres.");
                    }
                    Shop s = data(ctx).createShop(name, here(ctx.getSource()));
                    Settlement where = data(ctx).settlementAt(s.loc);
                    ok(ctx, "Comercio oficial creado: " + s.name + (where == null ? " (fuera de pueblos)" : " en " + where.name));
                    return 1;
                })))
                .then(literal("manager").then(argument("shop", StringArgumentType.string()).suggests(SHOPS)
                        .then(argument("player", EntityArgument.player()).executes(ctx -> {
                            Shop s = shop(ctx);
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            s.managerId = p.getUUID();
                            s.managerName = p.getGameProfile().getName();
                            data(ctx).setDirty();
                            ok(ctx, s.managerName + " es responsable de " + s.name);
                            return 1;
                        }))))
                .then(literal("offer").then(argument("shop", StringArgumentType.string()).suggests(SHOPS)
                        .executes(ctx -> {
                            PlayerCommands.showOffers(ctx.getSource(), data(ctx), shop(ctx), true);
                            return 1;
                        })
                        .then(literal("sell").then(argument("price", LongArgumentType.longArg(1)).executes(ctx -> addOffer(ctx, true))))
                        .then(literal("buy").then(argument("price", LongArgumentType.longArg(1)).executes(ctx -> addOffer(ctx, false))))
                        .then(literal("remove").then(argument("n", IntegerArgumentType.integer(1)).executes(ctx -> {
                            Shop s = shop(ctx);
                            int n = IntegerArgumentType.getInteger(ctx, "n");
                            if (n > s.offers.size()) {
                                return fail(ctx, "Ese comercio tiene " + s.offers.size() + " oferta(s).");
                            }
                            s.offers.remove(n - 1);
                            data(ctx).setDirty();
                            ok(ctx, "Oferta " + n + " eliminada de " + s.name);
                            return 1;
                        })))))
                .then(literal("delete").then(argument("shop", StringArgumentType.string()).suggests(SHOPS)
                        .executes(ctx -> {
                            Shop s = shop(ctx);
                            Menus.deletionWarning(ctx.getSource(), "el comercio «" + s.name + "»",
                                    java.util.List.of("Deja de ser un comercio oficial autorizado.", "Los edificios y bloques no se tocan."),
                                    "/pe shop delete " + s.id + " confirm");
                            return 1;
                        })
                        .then(literal("confirm").executes(ctx -> {
                            Shop s = shop(ctx);
                            data(ctx).shops.remove(s.id);
                            data(ctx).setDirty();
                            ok(ctx, "Comercio «" + s.name + "» eliminado.");
                            return 1;
                        }))));
    }

    /** Añade una oferta con el objeto (y la cantidad) que el admin lleva en la mano. */
    private static int addOffer(CommandContext<CommandSourceStack> ctx, boolean sells) throws CommandSyntaxException {
        Shop s = shop(ctx);
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        net.minecraft.world.item.ItemStack held = p.getMainHandItem();
        if (held.isEmpty()) {
            return fail(ctx, "Lleva en la mano el objeto (y la cantidad) de la oferta.");
        }
        if (s.offers.size() >= 27) {
            return fail(ctx, "Máximo 27 ofertas por comercio.");
        }
        long price = LongArgumentType.getLong(ctx, "price");
        s.offers.add(new Shop.Offer(held.copy(), price, sells));
        data(ctx).setDirty();
        ok(ctx, s.name + (sells ? " vende " : " compra ") + held.getCount() + "× " + held.getHoverName().getString() + " por " + price + " ₱");
        return 1;
    }

    // ===================== Bancos =====================

    private static LiteralArgumentBuilder<CommandSourceStack> bankTree() {
        return literal("bank")
                .then(literal("list").executes(ctx -> {
                    EconomyData data = data(ctx);
                    ctx.getSource().sendSystemMessage(Menus.title("Bancos"));
                    for (Bank b : data.banks.values()) {
                        ctx.getSource().sendSystemMessage(Component.literal("• " + data.bankName(b) + " (" + b.loc.dim() + " " + b.loc.shortText() + ")"));
                    }
                    return 1;
                }))
                .then(literal("auto").executes(ctx -> {
                    Bank b = nearBank(ctx);
                    if (b == null) {
                        return fail(ctx, "No hay ningún banco a menos de 8 bloques.");
                    }
                    b.countryOverride = null;
                    data(ctx).setDirty();
                    ok(ctx, "Ahora usa el país de su ubicación: «" + data(ctx).bankName(b) + "»");
                    return 1;
                }))
                .then(literal("assign")
                        .executes(ctx -> {
                            Bank b = nearBank(ctx);
                            if (b == null) {
                                return fail(ctx, "No hay ningún banco a menos de 8 bloques.");
                            }
                            ctx.getSource().sendSystemMessage(Component.literal("¿A qué país pertenece esta sucursal?").withStyle(ChatFormatting.GOLD));
                            for (Country c : data(ctx).countries.values()) {
                                ctx.getSource().sendSystemMessage(Menus.run(c.name, "/pe bank assign " + c.id, BankNames.baseName(c)));
                            }
                            return 1;
                        })
                        .then(argument("country", StringArgumentType.string()).suggests(COUNTRIES).executes(ctx -> {
                            Bank b = nearBank(ctx);
                            if (b == null) {
                                return fail(ctx, "No hay ningún banco a menos de 8 bloques.");
                            }
                            b.countryOverride = country(ctx).id;
                            data(ctx).setDirty();
                            ok(ctx, "Sucursal asignada: «" + data(ctx).bankName(b) + "»");
                            return 1;
                        })));
    }

    private static Bank nearBank(CommandContext<CommandSourceStack> ctx) {
        return data(ctx).nearestBank(here(ctx.getSource()), 8);
    }

    private static int giveCoins(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        PlayerCommands.giveCoins(p, amount);
        ok(ctx, "Entregadas " + amount + " monedas a " + p.getGameProfile().getName());
        return 1;
    }

    // ===================== Menú general =====================

    private static int menu(CommandSourceStack src) {
        src.sendSystemMessage(Menus.title("Administración de Panthrix's Economy"));
        src.sendSystemMessage(Menus.row(
                Menus.run("Países", "/pe country list", "Ver y gestionar países"),
                Menus.suggest("Nuevo país", "/pe country create ", "Escribe el nombre oficial")));
        src.sendSystemMessage(Menus.row(
                Menus.run("Ciudades y pueblos", "/pe settlement list", "Ver todos"),
                Menus.suggest("Nueva ciudad aquí", "/pe city create ", "Se crea en tu posición"),
                Menus.suggest("Nuevo pueblo aquí", "/pe town create ", "Se crea en tu posición")));
        src.sendSystemMessage(Menus.row(
                Menus.run("Comercios", "/pe shop list", "Ver comercios oficiales"),
                Menus.suggest("Nuevo comercio aquí", "/pe shop create ", "Se crea en tu posición")));
        src.sendSystemMessage(Menus.row(
                Menus.run("Bancos", "/pe bank list", "Ver sucursales"),
                Menus.run("Gran Plaza", "/pe plaza info", "Dónde está la plaza")));
        src.sendSystemMessage(Component.literal("Para crear un banco o un pueblo con bloque, coloca un Mostrador de Banco o un Núcleo del Pueblo "
                + "(pestaña «Utilidades de operador» del modo creativo).").withStyle(ChatFormatting.DARK_GRAY));
        return 1;
    }

    // ===================== Utilidades =====================

    static EconomyData data(CommandContext<CommandSourceStack> ctx) {
        return EconomyData.get(ctx.getSource().getServer());
    }

    static Loc here(CommandSourceStack src) {
        return EconomyData.loc(src.getLevel().dimension().location(), BlockPos.containing(src.getPosition()));
    }

    private static Country country(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String t = StringArgumentType.getString(ctx, "country");
        Country c = data(ctx).findCountry(t);
        if (c == null) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.literal("No existe el país «" + t + "».")).create();
        }
        return c;
    }

    private static Settlement settlement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String t = StringArgumentType.getString(ctx, "settlement");
        Settlement s = data(ctx).findSettlement(t);
        if (s == null) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.literal("No existe la ciudad o pueblo «" + t + "».")).create();
        }
        return s;
    }

    private static Shop shop(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String t = StringArgumentType.getString(ctx, "shop");
        Shop s = data(ctx).findShop(t);
        if (s == null) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.literal("No existe el comercio «" + t + "».")).create();
        }
        return s;
    }

    static int fail(CommandContext<CommandSourceStack> ctx, String msg) {
        ctx.getSource().sendFailure(Component.literal(msg));
        return 0;
    }

    static void ok(CommandContext<CommandSourceStack> ctx, String msg) {
        ctx.getSource().sendSuccess(() -> Component.literal(msg).withStyle(ChatFormatting.GREEN), true);
    }


    private AdminCommands() {
    }
}
