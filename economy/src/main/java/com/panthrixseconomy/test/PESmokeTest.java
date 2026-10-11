package com.panthrixseconomy.test;

import com.panthrixseconomy.PanthrixsEconomy;
import com.panthrixseconomy.block.TownCoreBlockEntity;
import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.Country;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.data.SettlementType;
import com.panthrixseconomy.permission.PEPermissions;
import com.panthrixseconomy.plaza.PlazaManager;
import com.panthrixseconomy.plaza.PlazaSite;
import com.panthrixseconomy.registry.PERegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Prueba de arranque (GitHub la ejecuta en cada subida con ./gradlew -p economy runServer -Psmoke).
 * Comprueba las "pruebas obligatorias" de la actualización en un servidor real recién creado
 * y escribe "ECONOMY SMOKE TEST OK" o la lista de fallos. Jugando normalmente no hace nada.
 */
public final class PESmokeTest {

    private static final List<String> failures = new ArrayList<>();

    public static boolean isEnabled() {
        return Boolean.getBoolean("panthrixseconomy.smokeTest");
    }

    public static void onServerStarted(MinecraftServer server) {
        if (!isEnabled()) {
            return;
        }
        failures.clear();
        try {
            bankNames();
            renameKeepsAccounts();
            saveAndLoad();
            permissions(server);
            townCore(server);
            plaza(server);
            welcome();
            shopTrade();
            jobs();
            screens();
        } catch (Throwable t) {
            failures.add("excepción: " + t);
            PanthrixsEconomy.LOGGER.error("Smoke test", t);
        }
        if (failures.isEmpty()) {
            PanthrixsEconomy.LOGGER.info("[Panthrix's Economy] ECONOMY SMOKE TEST OK");
        } else {
            for (String f : failures) {
                PanthrixsEconomy.LOGGER.error("[Panthrix's Economy] FALLO: {}", f);
            }
            PanthrixsEconomy.LOGGER.error("[Panthrix's Economy] ECONOMY SMOKE TEST FAILED ({} fallos)", failures.size());
        }
        server.halt(false);
    }

    private static void check(boolean ok, String what) {
        if (ok) {
            PanthrixsEconomy.LOGGER.info("[Panthrix's Economy] OK: {}", what);
        } else {
            failures.add(what);
        }
    }

    private static void eq(Object expected, Object actual, String what) {
        check(expected.equals(actual), what + " (esperado «" + expected + "», obtenido «" + actual + "»)");
    }

    private static final ResourceLocation OW = new ResourceLocation("minecraft", "overworld");

    private static Loc at(int x, int z) {
        return new Loc(OW, new BlockPos(x, 64, z));
    }

    /** Mundo de prueba aislado (no toca los datos del mundo real). */
    private static EconomyData sample() {
        EconomyData d = new EconomyData();
        Country a = d.createCountry("Panthria");
        Country b = d.createCountry("Reino de Aurelia");
        Settlement sa = d.createSettlement("Capital", SettlementType.CITY, at(0, 0), 100);
        sa.countryId = a.id;
        Settlement sb = d.createSettlement("Aurelia", SettlementType.TOWN, at(5000, 0), 100);
        sb.countryId = b.id;
        d.createBank(at(10, 0));
        d.createBank(at(20, 0));
        d.createBank(at(5010, 0));
        d.createBank(at(-9000, 0));
        return d;
    }

    private static List<Bank> banks(EconomyData d) {
        return new ArrayList<>(d.banks.values());
    }

    // 1. Dos países distintos generan nombres bancarios distintos
    private static void bankNames() {
        EconomyData d = sample();
        List<Bank> b = banks(d);
        eq("Banco Nacional de Panthria", d.bankName(b.get(0)), "nombre del banco de Panthria");
        eq("Banco Nacional de Panthria — Sucursal 2", d.bankName(b.get(1)), "sucursal 2");
        eq("Banco Nacional del Reino de Aurelia", d.bankName(b.get(2)), "nombre con artículo (del Reino)");
        eq("Banco Independiente", d.bankName(b.get(3)), "banco fuera de cualquier país");
        check(!d.bankName(b.get(0)).equals(d.bankName(b.get(2))), "países distintos -> nombres distintos");
        Country rep = d.createCountry("República de Sol");
        b.get(3).countryOverride = rep.id;
        eq("Banco Nacional de la República de Sol", d.bankName(b.get(3)), "asignación manual de país");
    }

    // 2. Cambiar el nombre de un país actualiza sus bancos sin borrar cuentas
    private static void renameKeepsAccounts() {
        EconomyData d = sample();
        Bank b = banks(d).get(0);
        UUID player = UUID.randomUUID();
        String id = d.identityOf(b);
        d.setBalance(id, player, 750);
        d.log("DEPOSITO", null, player, 750, id, "prueba");
        int hist = d.history.size();
        Country c = d.countryOf(b);
        d.renameCountry(c, "Gran Panthria");
        eq("Banco Nacional de Gran Panthria", d.bankName(b), "nombre tras renombrar el país");
        eq(750L, d.balance(d.identityOf(b), player), "saldo tras renombrar");
        eq(hist, d.history.size(), "historial tras renombrar");
        d.deleteCountry(c);
        eq(750L, d.balance(EconomyData.INDEPENDENT, player), "borrar país traspasa las cuentas (no se pierde dinero)");
    }

    // 3. Todo sobrevive a guardar y cargar
    private static void saveAndLoad() {
        EconomyData d = sample();
        UUID player = UUID.randomUUID();
        Bank b = banks(d).get(1);
        d.setBalance(d.identityOf(b), player, 1234);
        Settlement s = d.settlements.values().iterator().next();
        s.core = at(1, 1);
        s.treasury = 99;
        d.welcomed.add(player);
        d.plazaCenter = new BlockPos(1, 70, 2);
        d.plazaSpawn = new BlockPos(1, 71, 19);
        EconomyData r = EconomyData.load(d.save(new CompoundTag()));
        Bank rb = r.banks.get(b.id);
        check(rb != null, "banco tras cargar");
        if (rb != null) {
            eq(d.bankName(b), r.bankName(rb), "nombre del banco tras cargar");
            eq(1234L, r.balance(r.identityOf(rb), player), "saldo tras cargar");
        }
        Settlement rs = r.settlements.get(s.id);
        check(rs != null && at(1, 1).equals(rs.core) && rs.treasury == 99 && rs.name.equals(s.name), "pueblo y su núcleo tras cargar");
        check(r.welcomed.contains(player), "jugadores ya recibidos tras cargar");
        check(d.plazaCenter.equals(r.plazaCenter) && d.plazaSpawn.equals(r.plazaSpawn), "posición de la plaza tras cargar");
        // Una segunda vuelta no cambia nada
        CompoundTag t1 = r.save(new CompoundTag());
        CompoundTag t2 = EconomyData.load(t1).save(new CompoundTag());
        eq(t1, t2, "guardar/cargar es estable");
    }

    // 4. Jugador normal no puede crear entidades oficiales; administrador sí (con el despachador real)
    private static void permissions(MinecraftServer server) {
        EconomyData d = EconomyData.get(server);
        CommandSourceStack normal = server.createCommandSourceStack().withPermission(0).withSuppressedOutput();
        CommandSourceStack admin = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        check(!PEPermissions.isAdmin(normal), "un jugador normal no es admin");
        check(PEPermissions.isAdmin(admin), "un operador es admin");
        int countries = d.countries.size();
        int settlements = d.settlements.size();
        int shops = d.shops.size();
        String[] cmds = {"pe country create Pais Ilegal", "pe city create Ciudad Ilegal", "pe town create Pueblo Ilegal", "pe shop create Tienda Ilegal"};
        for (String c : cmds) {
            server.getCommands().performPrefixedCommand(normal, c);
        }
        check(d.countries.size() == countries && d.settlements.size() == settlements && d.shops.size() == shops,
                "un jugador normal NO puede crear países, ciudades, pueblos ni comercios");
        for (String c : cmds) {
            server.getCommands().performPrefixedCommand(admin, c.replace("Ilegal", "Oficial"));
        }
        check(d.countries.size() == countries + 1 && d.settlements.size() == settlements + 2 && d.shops.size() == shops + 1,
                "un administrador SÍ puede crearlos");
        Country c = d.findCountry("Pais Oficial");
        Settlement s = d.findSettlement("Pueblo Oficial");
        if (c != null && s != null) {
            server.getCommands().performPrefixedCommand(normal, "pe country rename " + c.id + " Hackeado");
            eq("Pais Oficial", c.name, "un jugador normal no puede renombrar países");
            server.getCommands().performPrefixedCommand(admin, "pe settlement country " + s.id + " " + c.id);
            eq(c.id, s.countryId, "un admin asigna un pueblo a un país");
            server.getCommands().performPrefixedCommand(admin, "pe country rename " + c.id + " Pais Renombrado");
            eq("Pais Renombrado", c.name, "un admin renombra un país");
            server.getCommands().performPrefixedCommand(admin, "pe country delete " + c.id);
            check(d.countries.containsKey(c.id), "borrar sin confirmar solo avisa");
            server.getCommands().performPrefixedCommand(admin, "pe country delete " + c.id + " confirm");
            check(!d.countries.containsKey(c.id), "borrar con confirmación elimina");
        } else {
            failures.add("no se encontraron las entidades creadas por el admin");
        }
        // Dejar el mundo como estaba
        d.settlements.values().removeIf(x -> x.name.endsWith("Oficial"));
        d.shops.values().removeIf(x -> x.name.endsWith("Oficial"));
        d.setDirty();
    }

    // 5. El Núcleo del Pueblo conserva sus datos (bloque + datos del mundo)
    private static void townCore(MinecraftServer server) {
        UUID id = UUID.randomUUID();
        TownCoreBlockEntity be = new TownCoreBlockEntity(BlockPos.ZERO, PERegistry.TOWN_CORE.get().defaultBlockState());
        be.setSettlementId(id);
        CompoundTag tag = be.saveWithoutMetadata();
        TownCoreBlockEntity loaded = new TownCoreBlockEntity(BlockPos.ZERO, PERegistry.TOWN_CORE.get().defaultBlockState());
        loaded.load(tag);
        eq(id, loaded.getSettlementId(), "el Núcleo guarda su pueblo");
    }

    // 6. Una sola Gran Plaza, sin sobrescribir construcciones
    private static void plaza(MinecraftServer server) {
        EconomyData d = EconomyData.get(server);
        ServerLevel ow = server.overworld();
        check(d.plazaCenter != null, "el mundo nuevo tiene Gran Plaza");
        if (d.plazaCenter == null) {
            return;
        }
        BlockPos center = d.plazaCenter;
        check(ow.getBlockState(center.offset(0, 6, 0)).getFluidState().isSource(), "la fuente tiene agua en lo alto");
        Settlement s = d.findSettlement("Gran Plaza");
        check(s != null && s.core != null && ow.getBlockState(s.core.pos()).is(PERegistry.TOWN_CORE.get()), "Núcleo del Pueblo en la plaza");
        if (s != null && s.core != null && ow.getBlockEntity(s.core.pos()) instanceof TownCoreBlockEntity core) {
            eq(s.id, core.getSettlementId(), "el Núcleo de la plaza está enlazado");
        }
        Bank b = d.banks.values().stream().filter(x -> x.loc.pos().closerThan(center, 30)).findFirst().orElse(null);
        check(b != null && ow.getBlockState(b.loc.pos()).is(PERegistry.BANK.get()), "banco en la plaza");
        if (b != null) {
            eq("Banco Independiente", d.bankName(b), "el banco de la plaza sin país es independiente");
        }
        check(PlazaSite.isNaturalAbove(ow.getBlockState(d.plazaSpawn)) && ow.getBlockState(d.plazaSpawn.below()).isSolidRender(ow, d.plazaSpawn.below()),
                "punto de llegada seguro (suelo firme y aire)");
        PlazaSite.Result again = PlazaSite.check(ow, center.getX(), center.getZ());
        check(!again.ok(), "no se puede construir encima de una construcción (" + again.problem() + ")");
        int before = d.settlements.size();
        CommandSourceStack admin = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(admin, "pe plaza generate");
        check(d.plazaCenter.equals(center) && d.settlements.size() == before, "no se generan plazas duplicadas");
        PlazaManager.onServerStarted(server, ow);
        check(d.plazaCenter.equals(center) && d.settlements.size() == before, "reiniciar no genera otra plaza");
        Set<BlockPos> unique = new HashSet<>();
        d.banks.values().forEach(x -> unique.add(x.loc.pos()));
        eq(d.banks.size(), unique.size(), "sin bancos duplicados");
        check(d.shops.values().stream().anyMatch(x -> x.name.equals("Mercado de la Gran Plaza") && x.offers.size() == 4), "el mercado de la plaza tiene ofertas");
    }

    // 7. Jugadores nuevos van a la plaza una vez; los veteranos nunca
    private static void welcome() {
        eq(PlazaManager.Welcome.TELEPORT, PlazaManager.decide(true, true, 0, false), "jugador nuevo -> plaza");
        eq(PlazaManager.Welcome.NONE, PlazaManager.decide(true, true, 0, true), "reconectar no teletransporta");
        eq(PlazaManager.Welcome.MARK_ONLY, PlazaManager.decide(true, true, 20 * 60 * 60, false), "jugador que ya empezó no es teletransportado");
        eq(PlazaManager.Welcome.MARK_ONLY, PlazaManager.decide(true, false, 0, false), "desactivado en la configuración");
        eq(PlazaManager.Welcome.NONE, PlazaManager.decide(false, true, 0, false), "sin plaza no pasa nada");
    }

    // 8. Comercios: comprar, vender, tesorería y guardado de ofertas
    private static void shopTrade() {
        EconomyData d = sample();
        Settlement capital = d.findSettlement("Capital");
        com.panthrixseconomy.data.Shop shop = d.createShop("Tienda", at(5, 5));
        com.panthrixseconomy.data.Shop.Offer sell = new com.panthrixseconomy.data.Shop.Offer(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD, 4), 30, true);
        com.panthrixseconomy.data.Shop.Offer buy = new com.panthrixseconomy.data.Shop.Offer(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT, 16), 50, false);
        shop.offers.add(sell);
        shop.offers.add(buy);
        UUID player = UUID.randomUUID();
        String id = d.shopIdentity(shop);
        check(id.startsWith("country:"), "el comercio cobra con el banco de su país");
        check(d.trade(shop, sell, player) != null, "sin saldo no se puede comprar");
        check(d.trade(shop, buy, player) != null, "la tesorería sin fondos no paga");
        capital.treasury = 100;
        check(d.trade(shop, buy, player) == null && d.balance(id, player) == 50 && capital.treasury == 50, "vender cobra de la tesorería");
        check(d.trade(shop, sell, player) == null && d.balance(id, player) == 20 && capital.treasury == 80, "comprar paga a la tesorería");
        EconomyData r = EconomyData.load(d.save(new CompoundTag()));
        com.panthrixseconomy.data.Shop rs = r.shops.get(shop.id);
        check(rs != null && rs.offers.size() == 2 && rs.offers.get(0).item().getCount() == 4 && rs.offers.get(1).price() == 50
                && !rs.offers.get(1).sells(), "las ofertas sobreviven a guardar y cargar");
    }

    // 9. Empleos y salarios
    private static void jobs() {
        EconomyData d = sample();
        Settlement capital = d.findSettlement("Capital");
        Settlement.Job baker = new Settlement.Job("Panadero", 20, 1);
        capital.jobs.add(baker);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        check(d.hire(a, capital, baker) == null, "un jugador se apunta a un empleo");
        check(d.hire(b, capital, baker) != null, "no se supera el número de puestos");
        String id = EconomyData.identity(d.countries.get(capital.countryId));
        eq(0, d.payroll(java.util.List.of(a)), "sin fondos no se paga");
        capital.treasury = 50;
        eq(1, d.payroll(java.util.List.of(a, b)), "solo cobran los empleados");
        check(d.balance(id, a) == 20 && capital.treasury == 30, "el salario va de la tesorería a la cuenta del país");
        EconomyData r = EconomyData.load(d.save(new CompoundTag()));
        check(r.employment.containsKey(a) && r.settlements.get(capital.id).jobs.size() == 1, "empleos y trabajadores sobreviven a guardar y cargar");
        d.deleteSettlement(capital);
        check(!d.employment.containsKey(a), "borrar el pueblo libera a sus trabajadores");
    }

    // 10. Pantallas: los paquetes se codifican y decodifican sin perder datos
    private static void screens() throws Exception {
        java.util.List<com.panthrixseconomy.network.ShopView.Offer> offers = java.util.List.of(
                new com.panthrixseconomy.network.ShopView.Offer(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD, 4), 3, true));
        com.panthrixseconomy.network.ShopView shop = new com.panthrixseconomy.network.ShopView("Mercado", "Banco Independiente", 42, offers,
                net.minecraft.network.chat.Component.literal("ok"));
        com.panthrixseconomy.network.BankView bank = new com.panthrixseconomy.network.BankView("Banco Nacional de Panthria", 99, 5, 7,
                java.util.List.of("linea"), net.minecraft.network.chat.Component.literal("hecho"));
        com.panthrixseconomy.network.ShopView s2 = roundTrip(shop, "encode", "decode");
        com.panthrixseconomy.network.BankView b2 = roundTrip(bank, "encode", "decode");
        check(s2.name().equals("Mercado") && s2.balance() == 42 && s2.offers().size() == 1 && s2.offers().get(0).item().getCount() == 4
                && s2.offers().get(0).price() == 3 && s2.offers().get(0).sells(), "el paquete de la tienda llega completo");
        check(b2.name().equals(bank.name()) && b2.balance() == 99 && b2.tax() == 5 && b2.coins() == 7 && b2.history().equals(bank.history())
                && b2.status().getString().equals("hecho"), "el paquete del banco llega completo");
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T msg, String enc, String dec) throws Exception {
        net.minecraft.network.FriendlyByteBuf buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        java.lang.reflect.Method e = msg.getClass().getDeclaredMethod(enc, net.minecraft.network.FriendlyByteBuf.class);
        e.setAccessible(true);
        e.invoke(msg, buf);
        java.lang.reflect.Method d = msg.getClass().getDeclaredMethod(dec, net.minecraft.network.FriendlyByteBuf.class);
        d.setAccessible(true);
        return (T) d.invoke(null, buf);
    }

    private PESmokeTest() {
    }
}
