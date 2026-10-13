package com.panthrixseconomy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.Country;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.data.Shop;
import com.panthrixseconomy.data.Transaction;
import com.panthrixseconomy.registry.PERegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Comandos para todos los jugadores: usar el banco y consultar información pública.
 * No permiten crear ninguna entidad oficial.
 */
public final class PlayerCommands {

    /** Hay que estar cerca de un mostrador de banco para operar. */
    public static final double BANK_REACH = 6.0;

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(literal("banco")
                .executes(ctx -> withBank(ctx, PlayerCommands::openBank))
                .then(literal("saldo").executes(ctx -> withBank(ctx, PlayerCommands::openBank)))
                .then(literal("tarjeta").executes(ctx -> withBank(ctx, PlayerCommands::giveCard)))
                .then(literal("chat").executes(ctx -> withBank(ctx, (p, data, b) -> Menus.bank(p, data, b))))
                .then(literal("depositar").then(argument("cantidad", IntegerArgumentType.integer(1)).executes(ctx ->
                        withBank(ctx, (p, data, b) -> deposit(p, data, b, IntegerArgumentType.getInteger(ctx, "cantidad"))))))
                .then(literal("retirar").then(argument("cantidad", IntegerArgumentType.integer(1, 100000)).executes(ctx ->
                        withBank(ctx, (p, data, b) -> withdraw(p, data, b, IntegerArgumentType.getInteger(ctx, "cantidad"))))))
                .then(literal("transferir").then(argument("jugador", EntityArgument.player())
                        .then(argument("cantidad", IntegerArgumentType.integer(1)).executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "jugador");
                            int amount = IntegerArgumentType.getInteger(ctx, "cantidad");
                            return withBank(ctx, (p, data, b) -> transfer(p, data, b, target, amount));
                        }))))
                .then(literal("historial").executes(ctx -> withBank(ctx, PlayerCommands::history))));

        d.register(literal("tienda")
                .executes(ctx -> withShop(ctx, (p, data, shop) -> com.panthrixseconomy.network.PENetwork.toPlayer(p,
                        com.panthrixseconomy.network.ShopView.of(p, data, shop, Component.empty()))))
                .then(literal("lista").executes(ctx -> withShop(ctx, (p, data, shop) -> showOffers(ctx.getSource(), data, shop, false))))
                .then(literal("comprar").then(argument("oferta", IntegerArgumentType.integer(1))
                        .executes(ctx -> withShop(ctx, (p, data, shop) -> trade(p, data, shop, IntegerArgumentType.getInteger(ctx, "oferta"), true)))))
                .then(literal("vender").then(argument("oferta", IntegerArgumentType.integer(1))
                        .executes(ctx -> withShop(ctx, (p, data, shop) -> trade(p, data, shop, IntegerArgumentType.getInteger(ctx, "oferta"), false))))));

        d.register(literal("trabajo")
                .executes(ctx -> jobs(ctx.getSource()))
                .then(literal("unirse").then(argument("empleo", IntegerArgumentType.integer(1)).executes(ctx ->
                        joinJob(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "empleo")))))
                .then(literal("dejar").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    EconomyData data = EconomyData.get(p.server);
                    if (!data.employment.containsKey(p.getUUID())) {
                        ctx.getSource().sendFailure(Component.literal("No tienes empleo."));
                        return 0;
                    }
                    data.quit(p.getUUID());
                    p.sendSystemMessage(Component.literal("Has dejado tu empleo.").withStyle(ChatFormatting.YELLOW));
                    return 1;
                })));

        d.register(literal("economia")
                .executes(ctx -> where(ctx.getSource()))
                .then(literal("aqui").executes(ctx -> where(ctx.getSource())))
                .then(literal("paises").executes(ctx -> countries(ctx.getSource()))));
    }

    interface BankAction {
        void run(ServerPlayer player, EconomyData data, Bank bank) throws CommandSyntaxException;
    }

    private static int withBank(CommandContext<CommandSourceStack> ctx, BankAction action) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        EconomyData data = EconomyData.get(p.server);
        Bank b = reachableBank(p);
        if (b == null) {
            ctx.getSource().sendFailure(Component.literal("Tienes que estar junto a un mostrador de banco oficial (o a un cajero, con tu tarjeta en la mano)."));
            return 0;
        }
        action.run(p, data, b);
        return 1;
    }

    /** Abre la pantalla gráfica del banco. */
    private static void openBank(ServerPlayer p, EconomyData data, Bank b) {
        com.panthrixseconomy.network.PENetwork.toPlayer(p, com.panthrixseconomy.network.BankView.of(p, data, b, Component.empty()));
    }

    // ===================== Empleos =====================

    private static int jobs(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        EconomyData data = EconomyData.get(p.server);
        EconomyData.Employment mine = data.employment.get(p.getUUID());
        if (mine != null) {
            Settlement where = data.settlements.get(mine.settlementId());
            src.sendSystemMessage(Menus.row(Menus.line("Tu empleo", mine.job() + (where == null ? "" : " en " + where.name)),
                    Menus.danger("Dejarlo", "/trabajo dejar", "Dejar tu empleo")));
        }
        Settlement s = data.settlementAt(AdminCommands.here(src));
        if (s == null) {
            src.sendSystemMessage(Component.literal("Ve a un pueblo o ciudad oficial para buscar trabajo.").withStyle(ChatFormatting.GRAY));
            return 1;
        }
        src.sendSystemMessage(Menus.title("Empleos en " + s.name));
        if (s.jobs.isEmpty()) {
            src.sendSystemMessage(Component.literal("Este pueblo no ofrece empleos todavía.").withStyle(ChatFormatting.GRAY));
        }
        for (int i = 0; i < s.jobs.size(); i++) {
            Settlement.Job j = s.jobs.get(i);
            long w = data.workers(s, j.name());
            MutableComponent row = Component.literal((i + 1) + ". " + j.name() + " — " + j.salary() + " ₱ por día (" + w + "/" + j.slots() + ") ");
            if (w < j.slots()) {
                row.append(Menus.run("Unirse", "/trabajo unirse " + (i + 1), "Cobras cada día de Minecraft mientras estés conectado"));
            }
            src.sendSystemMessage(row);
        }
        src.sendSystemMessage(Component.literal("El salario se ingresa cada día de Minecraft en tu cuenta del banco del país, "
                + "si estás conectado y el pueblo tiene fondos.").withStyle(ChatFormatting.DARK_GRAY));
        return 1;
    }

    private static int joinJob(CommandSourceStack src, int n) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        EconomyData data = EconomyData.get(p.server);
        Settlement s = data.settlementAt(AdminCommands.here(src));
        if (s == null || n > s.jobs.size()) {
            src.sendFailure(Component.literal("Ese empleo no existe aquí."));
            return 0;
        }
        Settlement.Job j = s.jobs.get(n - 1);
        EconomyData.Employment mine = data.employment.get(p.getUUID());
        if (mine != null && mine.settlementId().equals(s.id) && mine.job().equalsIgnoreCase(j.name())) {
            src.sendFailure(Component.literal("Ya trabajas de " + j.name() + "."));
            return 0;
        }
        String error = data.hire(p.getUUID(), s, j);
        if (error != null) {
            src.sendFailure(Component.literal(error));
            return 0;
        }
        p.sendSystemMessage(Component.literal("¡Ahora trabajas de " + j.name() + " en " + s.name + "! Cobrarás " + j.salary() + " ₱ cada día.")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    // ===================== Comercios =====================

    /** Hay que estar cerca de un comercio oficial para comprar o vender. */
    public static final double SHOP_REACH = 8.0;

    interface ShopAction {
        void run(ServerPlayer player, EconomyData data, Shop shop) throws CommandSyntaxException;
    }

    private static int withShop(CommandContext<CommandSourceStack> ctx, ShopAction action) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        EconomyData data = EconomyData.get(p.server);
        Shop shop = data.nearestShop(EconomyData.loc(p.level().dimension().location(), p.blockPosition()), SHOP_REACH);
        if (shop == null) {
            ctx.getSource().sendFailure(Component.literal("No hay ningún comercio oficial cerca."));
            return 0;
        }
        action.run(p, data, shop);
        return 1;
    }

    public static void showOffers(CommandSourceStack src, EconomyData data, Shop shop, boolean admin) {
        src.sendSystemMessage(Menus.title(shop.name));
        src.sendSystemMessage(Menus.line("Se paga con", data.identityName(data.shopIdentity(shop))));
        if (shop.offers.isEmpty()) {
            src.sendSystemMessage(Component.literal("Este comercio aún no tiene ofertas.").withStyle(ChatFormatting.GRAY));
        }
        for (int i = 0; i < shop.offers.size(); i++) {
            Shop.Offer o = shop.offers.get(i);
            int n = i + 1;
            String what = o.item().getCount() + "× " + o.item().getHoverName().getString();
            MutableComponent row = Component.literal(n + ". " + (o.sells() ? "Vende " : "Compra ") + what + " — " + o.price() + " ₱ ")
                    .withStyle(o.sells() ? ChatFormatting.WHITE : ChatFormatting.YELLOW);
            row.append(o.sells() ? Menus.run("Comprar", "/tienda comprar " + n, "Pagar " + o.price() + " ₱")
                    : Menus.run("Vender", "/tienda vender " + n, "Recibir " + o.price() + " ₱"));
            if (admin) {
                row.append(" ").append(Menus.danger("Quitar", "/pe shop offer " + shop.id + " remove " + n, "Eliminar la oferta"));
            }
            src.sendSystemMessage(row);
        }
        if (admin) {
            src.sendSystemMessage(Menus.row(
                    Menus.suggest("Añadir venta", "/pe shop offer " + shop.id + " sell ", "Con el objeto en la mano: precio"),
                    Menus.suggest("Añadir compra", "/pe shop offer " + shop.id + " buy ", "Con el objeto en la mano: precio")));
        }
    }

    public static void trade(ServerPlayer p, EconomyData data, Shop shop, int n, boolean buying) {
        if (n > shop.offers.size() || shop.offers.get(n - 1).sells() != buying) {
            reply(p, Component.literal("Esa oferta no existe.").withStyle(ChatFormatting.RED));
            return;
        }
        Shop.Offer o = shop.offers.get(n - 1);
        ItemStack want = o.item();
        if (!buying && countMatching(p, want) < want.getCount()) {
            reply(p, Component.literal("Necesitas " + want.getCount() + "× " + want.getHoverName().getString() + ".").withStyle(ChatFormatting.RED));
            return;
        }
        String error = data.trade(shop, o, p.getUUID());
        if (error != null) {
            reply(p, Component.literal(error).withStyle(ChatFormatting.RED));
            return;
        }
        if (buying) {
            ItemStack give = want.copy();
            if (!p.getInventory().add(give)) {
                p.drop(give, false);
            }
        } else {
            removeMatching(p, want, want.getCount());
        }
        String id = data.shopIdentity(shop);
        reply(p, Component.literal((buying ? "Compraste " : "Vendiste ") + want.getCount() + "× " + want.getHoverName().getString()
                + " por " + o.price() + " ₱. Saldo: " + data.balance(id, p.getUUID()) + " ₱").withStyle(ChatFormatting.GREEN));
    }

    private static int countMatching(ServerPlayer p, ItemStack want) {
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(st, want)) {
                n += st.getCount();
            }
        }
        return n;
    }

    private static void removeMatching(ServerPlayer p, ItemStack want, int amount) {
        for (int i = 0; i < p.getInventory().getContainerSize() && amount > 0; i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(st, want)) {
                int take = Math.min(amount, st.getCount());
                st.shrink(take);
                amount -= take;
            }
        }
    }

    // ===================== Operaciones =====================

    public static void deposit(ServerPlayer p, EconomyData data, Bank b, int amount) {
        int have = p.getInventory().countItem(PERegistry.COIN.get());
        if (have < amount) {
            reply(p, Component.literal("Solo llevas " + have + " monedas.").withStyle(ChatFormatting.RED));
            return;
        }
        int left = amount;
        for (int i = 0; i < p.getInventory().getContainerSize() && left > 0; i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (st.is(PERegistry.COIN.get())) {
                int take = Math.min(left, st.getCount());
                st.shrink(take);
                left -= take;
            }
        }
        String id = data.identityOf(b);
        data.setBalance(id, p.getUUID(), data.balance(id, p.getUUID()) + amount);
        data.log("DEPOSITO", null, p.getUUID(), amount, id, data.bankName(b));
        reply(p, Component.literal("Depositadas " + amount + " ₱. Saldo: " + data.balance(id, p.getUUID()) + " ₱").withStyle(ChatFormatting.GREEN));
    }

    public static void withdraw(ServerPlayer p, EconomyData data, Bank b, int amount) {
        String id = data.identityOf(b);
        long bal = data.balance(id, p.getUUID());
        if (bal < amount) {
            reply(p, Component.literal("Saldo insuficiente (" + bal + " ₱).").withStyle(ChatFormatting.RED));
            return;
        }
        data.setBalance(id, p.getUUID(), bal - amount);
        data.log("RETIRO", p.getUUID(), null, amount, id, data.bankName(b));
        giveCoins(p, amount);
        reply(p, Component.literal("Retiradas " + amount + " ₱. Saldo: " + (bal - amount) + " ₱").withStyle(ChatFormatting.GREEN));
    }

    public static void transfer(ServerPlayer p, EconomyData data, Bank b, ServerPlayer target, int amount) {
        if (target.getUUID().equals(p.getUUID())) {
            reply(p, Component.literal("No puedes transferirte dinero a ti mismo.").withStyle(ChatFormatting.RED));
            return;
        }
        String id = data.identityOf(b);
        long bal = data.balance(id, p.getUUID());
        if (bal < amount) {
            reply(p, Component.literal("Saldo insuficiente (" + bal + " ₱).").withStyle(ChatFormatting.RED));
            return;
        }
        Country c = data.countryOf(b);
        long tax = c == null ? 0 : (long) amount * c.taxPercent / 100;
        data.setBalance(id, p.getUUID(), bal - amount);
        data.setBalance(id, target.getUUID(), data.balance(id, target.getUUID()) + amount - tax);
        data.log("TRANSFERENCIA", p.getUUID(), target.getUUID(), amount - tax, id, data.bankName(b));
        if (tax > 0) {
            c.treasury += tax;
            data.log("IMPUESTO", p.getUUID(), null, tax, id, "Tesorería de " + c.name);
        }
        reply(p, Component.literal("Transferidas " + amount + " ₱ a " + target.getGameProfile().getName()
                + (tax > 0 ? " (impuesto " + tax + " ₱)" : "") + ".").withStyle(ChatFormatting.GREEN));
        target.sendSystemMessage(Component.literal(p.getGameProfile().getName() + " te ha transferido " + (amount - tax) + " ₱ en "
                + data.identityName(id) + ".").withStyle(ChatFormatting.GOLD));
    }

    private static void history(ServerPlayer p, EconomyData data, Bank b) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm");
        List<Transaction> mine = data.history.stream().filter(t -> t.involves(p.getUUID())).toList();
        p.sendSystemMessage(Menus.title("Tus movimientos"));
        if (mine.isEmpty()) {
            p.sendSystemMessage(Component.literal("Aún no hay movimientos.").withStyle(ChatFormatting.GRAY));
        }
        for (Transaction t : mine.subList(Math.max(0, mine.size() - 10), mine.size())) {
            String sign = p.getUUID().equals(t.to()) ? "+" : "−";
            p.sendSystemMessage(Component.literal(fmt.format(new Date(t.time())) + "  " + t.type() + "  " + sign + t.amount() + " ₱  "
                    + data.identityName(t.identity())).withStyle(ChatFormatting.GRAY));
        }
    }

    /** Último mensaje de cada jugador (lo muestran las pantallas gráficas). */
    private static final java.util.Map<java.util.UUID, Component> LAST = new java.util.concurrent.ConcurrentHashMap<>();

    private static void reply(ServerPlayer p, Component msg) {
        LAST.put(p.getUUID(), msg);
        p.sendSystemMessage(msg);
    }

    public static Component takeLastMessage(ServerPlayer p) {
        Component c = LAST.remove(p.getUUID());
        return c == null ? Component.empty() : c;
    }

    /** Banco al alcance del jugador (o null). Lo usan los comandos y las pantallas. */
    public static Bank reachableBank(ServerPlayer p) {
        EconomyData data = EconomyData.get(p.server);
        Bank b = data.nearestBank(EconomyData.loc(p.level().dimension().location(), p.blockPosition()), BANK_REACH);
        if (b == null) {
            return null;
        }
        net.minecraft.world.level.block.state.BlockState st = p.level().getBlockState(b.loc.pos());
        if (st.is(PERegistry.BANK.get())) {
            return b;
        }
        // Un cajero solo sirve con la tarjeta del propio jugador en la mano
        return st.is(PERegistry.ATM.get()) && holdsOwnCard(p) ? b : null;
    }

    public static boolean holdsOwnCard(ServerPlayer p) {
        return com.panthrixseconomy.item.BankCardItem.belongsTo(p.getMainHandItem(), p)
                || com.panthrixseconomy.item.BankCardItem.belongsTo(p.getOffhandItem(), p);
    }

    /** Da (o repone) la tarjeta bancaria del jugador. Solo en un mostrador de banco, no en un cajero. */
    private static void giveCard(ServerPlayer p, EconomyData data, Bank b) {
        if (b.atm) {
            p.sendSystemMessage(Component.literal("La tarjeta se pide en un mostrador de banco, no en un cajero.").withStyle(ChatFormatting.RED));
            return;
        }
        ItemStack card = com.panthrixseconomy.item.BankCardItem.forPlayer(PERegistry.BANK_CARD.get(), p);
        if (!p.getInventory().add(card)) {
            p.drop(card, false);
        }
        p.sendSystemMessage(Component.literal("Aquí tienes tu tarjeta bancaria. Úsala en cualquier cajero automático.").withStyle(ChatFormatting.GREEN));
    }

    public static Shop reachableShop(ServerPlayer p) {
        return EconomyData.get(p.server).nearestShop(EconomyData.loc(p.level().dimension().location(), p.blockPosition()), SHOP_REACH);
    }

    public static void giveCoins(ServerPlayer p, int amount) {
        int left = amount;
        while (left > 0) {
            int n = Math.min(64, left);
            ItemStack st = new ItemStack(PERegistry.COIN.get(), n);
            if (!p.getInventory().add(st)) {
                p.drop(st, false);
            }
            left -= n;
        }
    }

    // ===================== Información pública =====================

    private static int where(CommandSourceStack src) {
        EconomyData data = EconomyData.get(src.getServer());
        Loc here = AdminCommands.here(src);
        Settlement s = data.settlementAt(here);
        Country c = data.countryAt(here);
        src.sendSystemMessage(Menus.title("Dónde estás"));
        src.sendSystemMessage(Menus.line("País", c == null ? "Ninguno (territorio libre)" : c.name));
        src.sendSystemMessage(Menus.line("Ciudad o pueblo", s == null ? "Ninguno" : s.type.label + " " + s.name));
        Bank b = data.nearestBank(here, 64);
        if (b != null) {
            src.sendSystemMessage(Menus.line("Banco cercano", data.bankName(b) + " (" + b.loc.shortText() + ")"));
        }
        return 1;
    }

    private static int countries(CommandSourceStack src) {
        EconomyData data = EconomyData.get(src.getServer());
        src.sendSystemMessage(Menus.title("Países del mundo"));
        if (data.countries.isEmpty()) {
            src.sendSystemMessage(Component.literal("Todavía no hay países oficiales.").withStyle(ChatFormatting.GRAY));
        }
        for (Country c : data.countries.values()) {
            long n = data.settlements.values().stream().filter(s -> c.id.equals(s.countryId)).count();
            src.sendSystemMessage(Component.literal("• " + c.name + " — " + n + " ciudad(es)/pueblo(s)"));
        }
        return 1;
    }

    private PlayerCommands() {
    }
}
