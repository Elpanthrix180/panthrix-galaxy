package com.panthrixseconomy.data;

import com.panthrixseconomy.PanthrixsEconomy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Todos los datos de la economía del mundo. Se guardan en
 * world/data/panthrixseconomy.dat y sobreviven a cerrar el juego o reiniciar el servidor.
 *
 * Las cuentas no van por nombre de banco ni por nombre de país, sino por "identidad":
 *   "country:<id del país>"  -> todas las sucursales de ese país comparten cuentas
 *   "independent"            -> bancos fuera de cualquier país
 * Así, cambiar el nombre de un país nunca toca saldos, cuentas ni historial.
 */
public final class EconomyData extends SavedData {

    public static final String FILE = PanthrixsEconomy.MODID;
    public static final String INDEPENDENT = "independent";

    public final Map<UUID, Country> countries = new LinkedHashMap<>();
    public final Map<UUID, Settlement> settlements = new LinkedHashMap<>();
    public final Map<UUID, Shop> shops = new LinkedHashMap<>();
    public final Map<UUID, Bank> banks = new LinkedHashMap<>();
    /** identidad -> (jugador -> saldo) */
    public final Map<String, Map<UUID, Long>> accounts = new HashMap<>();
    public final List<Transaction> history = new ArrayList<>();
    public final Set<UUID> welcomed = new HashSet<>();
    public int maxHistory = 5000;
    /** jugador -> empleo (pueblo + nombre del puesto) */
    public final Map<UUID, Employment> employment = new HashMap<>();
    /** Último día de Minecraft en el que se pagaron salarios. */
    public long lastPayday = -1;

    public record Employment(UUID settlementId, String job) {
    }
    private long nextSerial = 1;

    // ===== Gran Plaza =====
    public BlockPos plazaCenter;
    public BlockPos plazaSpawn;
    public boolean plazaAttempted;

    public static EconomyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(EconomyData::load, EconomyData::new, FILE);
    }

    // ===================== Países =====================

    public Country createCountry(String name) {
        Country c = new Country(UUID.randomUUID(), name.trim(), nextSerial++);
        countries.put(c.id, c);
        setDirty();
        return c;
    }

    public void renameCountry(Country c, String newName) {
        c.name = newName.trim();
        setDirty();
    }

    /** Lo que pasará si se borra el país (para avisar antes). */
    public List<String> countryDeletionConsequences(Country c) {
        List<String> out = new ArrayList<>();
        long towns = settlements.values().stream().filter(s -> c.id.equals(s.countryId)).count();
        long banksOf = banks.values().stream().filter(b -> c.equals(countryOf(b))).count();
        Map<UUID, Long> acc = accounts.getOrDefault(identity(c), Map.of());
        long total = acc.values().stream().mapToLong(Long::longValue).sum();
        out.add(towns + " ciudad(es)/pueblo(s) quedarán sin país.");
        out.add(banksOf + " banco(s) pasarán a llamarse «" + BankNames.INDEPENDENT + "».");
        out.add(acc.size() + " cuenta(s) con " + total + " ₱ se traspasarán al Banco Independiente (no se pierde dinero).");
        if (c.treasury > 0) {
            out.add("La tesorería nacional (" + c.treasury + " ₱) se perderá.");
        }
        out.add("El historial bancario se conserva.");
        return out;
    }

    public void deleteCountry(Country c) {
        Map<UUID, Long> acc = accounts.remove(identity(c));
        if (acc != null) {
            Map<UUID, Long> indep = accounts.computeIfAbsent(INDEPENDENT, k -> new HashMap<>());
            acc.forEach((player, bal) -> indep.merge(player, bal, Long::sum));
            log("FUSION", null, null, acc.values().stream().mapToLong(Long::longValue).sum(), INDEPENDENT,
                    "Cuentas del país eliminado «" + c.name + "»");
        }
        for (Settlement s : settlements.values()) {
            if (c.id.equals(s.countryId)) {
                s.countryId = null;
            }
        }
        for (Bank b : banks.values()) {
            if (c.id.equals(b.countryOverride)) {
                b.countryOverride = null;
            }
        }
        countries.remove(c.id);
        setDirty();
    }

    // ===================== Ciudades y pueblos =====================

    public Settlement createSettlement(String name, SettlementType type, Loc center, int radius) {
        Settlement s = new Settlement(UUID.randomUUID(), name.trim(), type, center, radius, nextSerial++);
        s.countryId = countryIdAt(center);
        settlements.put(s.id, s);
        setDirty();
        return s;
    }

    public List<String> settlementDeletionConsequences(Settlement s) {
        List<String> out = new ArrayList<>();
        long shopsIn = shops.values().stream().filter(sh -> s.contains(sh.loc)).count();
        List<Bank> affected = banks.values().stream().filter(b -> b.countryOverride == null && s.contains(b.loc)).toList();
        out.add(shopsIn + " comercio(s) oficiales dentro de su territorio se eliminarán.");
        out.add(affected.size() + " banco(s) de su territorio pueden cambiar de país o pasar a «" + BankNames.INDEPENDENT + "» (las cuentas se conservan).");
        if (s.core != null) {
            out.add("Su Núcleo del Pueblo quedará sin pueblo asignado.");
        }
        if (s.treasury > 0) {
            out.add("La tesorería local (" + s.treasury + " ₱) se perderá.");
        }
        return out;
    }

    public void deleteSettlement(Settlement s) {
        employment.values().removeIf(e -> e.settlementId().equals(s.id));
        shops.values().removeIf(sh -> s.contains(sh.loc));
        settlements.remove(s.id);
        setDirty();
    }

    public Settlement settlementAt(Loc loc) {
        return settlements.values().stream().filter(s -> s.contains(loc))
                .min(Comparator.comparingDouble(s -> s.center.distSqr(loc))).orElse(null);
    }

    /** País al que pertenece una ubicación: el del pueblo/ciudad con país más cercano que la contiene. */
    public UUID countryIdAt(Loc loc) {
        return settlements.values().stream().filter(s -> s.countryId != null && countries.containsKey(s.countryId) && s.contains(loc))
                .min(Comparator.comparingDouble(s -> s.center.distSqr(loc))).map(s -> s.countryId).orElse(null);
    }

    public Country countryAt(Loc loc) {
        UUID id = countryIdAt(loc);
        return id == null ? null : countries.get(id);
    }

    // ===================== Comercios =====================

    public Shop createShop(String name, Loc loc) {
        Shop s = new Shop(UUID.randomUUID(), name.trim(), loc);
        shops.put(s.id, s);
        setDirty();
        return s;
    }

    // ===================== Bancos =====================

    public Bank createBank(Loc loc) {
        Bank b = new Bank(UUID.randomUUID(), loc, nextSerial++);
        banks.put(b.id, b);
        setDirty();
        return b;
    }

    public Bank bankAt(Loc loc) {
        return banks.values().stream().filter(b -> b.loc.equals(loc)).findFirst().orElse(null);
    }

    public Bank nearestBank(Loc loc, double maxDist) {
        return banks.values().stream().filter(b -> b.loc.distSqr(loc) <= maxDist * maxDist)
                .min(Comparator.comparingDouble(b -> b.loc.distSqr(loc))).orElse(null);
    }

    public void removeBank(Bank b) {
        banks.remove(b.id);
        setDirty();
    }

    /** País del banco: el asignado a mano o, si no, el de su ubicación. Se calcula siempre al momento. */
    public Country countryOf(Bank b) {
        if (b.countryOverride != null) {
            Country c = countries.get(b.countryOverride);
            if (c != null) {
                return c;
            }
        }
        return countryAt(b.loc);
    }

    public static String identity(Country c) {
        return c == null ? INDEPENDENT : "country:" + c.id;
    }

    public String identityOf(Bank b) {
        return identity(countryOf(b));
    }

    /** Número de sucursal: 1 para la más antigua de su país, 2 la siguiente... */
    public int branchNumber(Bank b) {
        String id = identityOf(b);
        int n = 1;
        for (Bank other : banks.values()) {
            if (other.serial < b.serial && identityOf(other).equals(id)) {
                n++;
            }
        }
        return n;
    }

    public String bankName(Bank b) {
        return BankNames.displayName(countryOf(b), branchNumber(b));
    }

    public String identityName(String identity) {
        if (identity.startsWith("country:")) {
            try {
                Country c = countries.get(UUID.fromString(identity.substring(8)));
                if (c != null) {
                    return BankNames.baseName(c);
                }
            } catch (IllegalArgumentException ignored) {
            }
            return "Banco de un país eliminado";
        }
        return BankNames.INDEPENDENT;
    }

    // ===================== Cuentas =====================

    public long balance(String identity, UUID player) {
        return accounts.getOrDefault(identity, Map.of()).getOrDefault(player, 0L);
    }

    public void setBalance(String identity, UUID player, long value) {
        accounts.computeIfAbsent(identity, k -> new HashMap<>()).put(player, value);
        setDirty();
    }

    public void log(String type, UUID from, UUID to, long amount, String identity, String note) {
        history.add(new Transaction(System.currentTimeMillis(), type, from, to, amount, identity, note));
        while (history.size() > maxHistory) {
            history.remove(0);
        }
        setDirty();
    }

    public long nationalDeposits(Country c) {
        return accounts.getOrDefault(identity(c), Map.of()).values().stream().mapToLong(Long::longValue).sum();
    }

    // ===================== Comercio =====================

    /** Cuenta con la que se paga en un comercio: el banco del país donde está (o el independiente). */
    public String shopIdentity(Shop shop) {
        return identity(countryAt(shop.loc));
    }

    public Shop nearestShop(Loc loc, double maxDist) {
        return shops.values().stream().filter(s -> s.loc.distSqr(loc) <= maxDist * maxDist)
                .min(Comparator.comparingDouble(s -> s.loc.distSqr(loc))).orElse(null);
    }

    /**
     * Parte económica de un intercambio (los objetos los mueve quien llama).
     * Comprar: el jugador paga y el dinero va a la tesorería del pueblo del comercio.
     * Vender: lo paga la tesorería del pueblo; un comercio fuera de pueblos paga sin límite (admin).
     * Devuelve null si se hizo o el motivo si no.
     */
    public String trade(Shop shop, Shop.Offer offer, UUID player) {
        String id = shopIdentity(shop);
        Settlement town = settlementAt(shop.loc);
        long bal = balance(id, player);
        if (offer.sells()) {
            if (bal < offer.price()) {
                return "Saldo insuficiente en " + identityName(id) + " (" + bal + " ₱).";
            }
            setBalance(id, player, bal - offer.price());
            if (town != null) {
                town.treasury += offer.price();
            }
            log("COMPRA", player, null, offer.price(), id, shop.name);
        } else {
            if (town != null && town.treasury < offer.price()) {
                return "La tesorería de " + town.name + " no tiene fondos para pagarte.";
            }
            if (town != null) {
                town.treasury -= offer.price();
            }
            setBalance(id, player, bal + offer.price());
            log("VENTA", null, player, offer.price(), id, shop.name);
        }
        setDirty();
        return null;
    }

    // ===================== Empleos =====================

    public long workers(Settlement s, String job) {
        return employment.values().stream().filter(e -> e.settlementId().equals(s.id) && e.job().equalsIgnoreCase(job)).count();
    }

    /** Apunta a un jugador a un empleo. Devuelve null si se pudo o el motivo si no. */
    public String hire(UUID player, Settlement s, Settlement.Job job) {
        if (workers(s, job.name()) >= job.slots()) {
            return "No quedan puestos de " + job.name() + " en " + s.name + ".";
        }
        employment.put(player, new Employment(s.id, job.name()));
        setDirty();
        return null;
    }

    public void quit(UUID player) {
        if (employment.remove(player) != null) {
            setDirty();
        }
    }

    /**
     * Paga un día de salario a los empleados indicados (los que están conectados), desde la tesorería
     * de su pueblo a su cuenta en el banco del país del pueblo. Si la tesorería no alcanza, ese
     * empleado no cobra ese día. Devuelve cuántos cobraron.
     */
    public int payroll(java.util.Collection<UUID> players) {
        int paid = 0;
        for (UUID p : players) {
            Employment e = employment.get(p);
            if (e == null) {
                continue;
            }
            Settlement s = settlements.get(e.settlementId());
            Settlement.Job job = s == null ? null : s.jobs.stream().filter(j -> j.name().equalsIgnoreCase(e.job())).findFirst().orElse(null);
            if (job == null) {
                employment.remove(p);  // el pueblo o el puesto ya no existen
                continue;
            }
            if (s.treasury < job.salary()) {
                continue;
            }
            s.treasury -= job.salary();
            String id = identity(s.countryId == null ? null : countries.get(s.countryId));
            setBalance(id, p, balance(id, p) + job.salary());
            log("SALARIO", null, p, job.salary(), id, job.name() + " en " + s.name);
            paid++;
        }
        setDirty();
        return paid;
    }

    // ===================== Búsqueda por nombre o id =====================

    public Country findCountry(String text) {
        return find(countries.values(), text, c -> c.id, c -> c.name);
    }

    public Settlement findSettlement(String text) {
        return find(settlements.values(), text, s -> s.id, s -> s.name);
    }

    public Shop findShop(String text) {
        return find(shops.values(), text, s -> s.id, s -> s.name);
    }

    private static <T> T find(Iterable<T> all, String text, java.util.function.Function<T, UUID> id,
                              java.util.function.Function<T, String> name) {
        String t = text.trim();
        for (T e : all) {
            if (id.apply(e).toString().equalsIgnoreCase(t)) {
                return e;
            }
        }
        for (T e : all) {
            if (name.apply(e).equalsIgnoreCase(t)) {
                return e;
            }
        }
        return null;
    }

    public boolean countryNameTaken(String name, Country except) {
        return countries.values().stream().anyMatch(c -> c != except && c.name.equalsIgnoreCase(name.trim()));
    }

    public boolean settlementNameTaken(String name, Settlement except) {
        return settlements.values().stream().anyMatch(s -> s != except && s.name.equalsIgnoreCase(name.trim()));
    }

    // ===================== Guardar y cargar =====================

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Version", 1);
        tag.putLong("NextSerial", nextSerial);
        tag.put("Countries", list(countries.values().stream().map(Country::save).toList()));
        tag.put("Settlements", list(settlements.values().stream().map(Settlement::save).toList()));
        tag.put("Shops", list(shops.values().stream().map(Shop::save).toList()));
        tag.put("Banks", list(banks.values().stream().map(Bank::save).toList()));
        ListTag acc = new ListTag();
        accounts.forEach((identity, map) -> map.forEach((player, bal) -> {
            CompoundTag a = new CompoundTag();
            a.putString("Identity", identity);
            a.putUUID("Player", player);
            a.putLong("Balance", bal);
            acc.add(a);
        }));
        tag.put("Accounts", acc);
        tag.put("History", list(history.stream().map(Transaction::save).toList()));
        ListTag w = new ListTag();
        for (UUID u : welcomed) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Id", u);
            w.add(t);
        }
        tag.put("Welcomed", w);
        if (plazaCenter != null) {
            tag.putLong("PlazaCenter", plazaCenter.asLong());
            tag.putLong("PlazaSpawn", plazaSpawn.asLong());
        }
        tag.putBoolean("PlazaAttempted", plazaAttempted);
        ListTag emp = new ListTag();
        employment.forEach((player, e) -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("Player", player);
            t.putUUID("Settlement", e.settlementId());
            t.putString("Job", e.job());
            emp.add(t);
        });
        tag.put("Employment", emp);
        tag.putLong("LastPayday", lastPayday);
        return tag;
    }

    public static EconomyData load(CompoundTag tag) {
        EconomyData d = new EconomyData();
        d.nextSerial = Math.max(1, tag.getLong("NextSerial"));
        for (Tag t : tag.getList("Countries", Tag.TAG_COMPOUND)) {
            Country c = Country.load((CompoundTag) t);
            d.countries.put(c.id, c);
        }
        for (Tag t : tag.getList("Settlements", Tag.TAG_COMPOUND)) {
            Settlement s = Settlement.load((CompoundTag) t);
            d.settlements.put(s.id, s);
        }
        for (Tag t : tag.getList("Shops", Tag.TAG_COMPOUND)) {
            Shop s = Shop.load((CompoundTag) t);
            d.shops.put(s.id, s);
        }
        for (Tag t : tag.getList("Banks", Tag.TAG_COMPOUND)) {
            Bank b = Bank.load((CompoundTag) t);
            d.banks.put(b.id, b);
        }
        for (Tag t : tag.getList("Accounts", Tag.TAG_COMPOUND)) {
            CompoundTag a = (CompoundTag) t;
            d.accounts.computeIfAbsent(a.getString("Identity"), k -> new HashMap<>()).put(a.getUUID("Player"), a.getLong("Balance"));
        }
        for (Tag t : tag.getList("History", Tag.TAG_COMPOUND)) {
            d.history.add(Transaction.load((CompoundTag) t));
        }
        for (Tag t : tag.getList("Welcomed", Tag.TAG_COMPOUND)) {
            d.welcomed.add(((CompoundTag) t).getUUID("Id"));
        }
        if (tag.contains("PlazaCenter")) {
            d.plazaCenter = BlockPos.of(tag.getLong("PlazaCenter"));
            d.plazaSpawn = BlockPos.of(tag.getLong("PlazaSpawn"));
        }
        d.plazaAttempted = tag.getBoolean("PlazaAttempted");
        for (Tag t : tag.getList("Employment", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) t;
            d.employment.put(e.getUUID("Player"), new Employment(e.getUUID("Settlement"), e.getString("Job")));
        }
        d.lastPayday = tag.contains("LastPayday") ? tag.getLong("LastPayday") : -1;
        return d;
    }

    private static ListTag list(List<CompoundTag> tags) {
        ListTag l = new ListTag();
        l.addAll(tags);
        return l;
    }

    public static String norm(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }

    public static Loc loc(ResourceLocation dim, BlockPos pos) {
        return new Loc(dim, pos.immutable());
    }
}
