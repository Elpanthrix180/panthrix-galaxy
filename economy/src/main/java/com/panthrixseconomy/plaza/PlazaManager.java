package com.panthrixseconomy.plaza;

import com.panthrixseconomy.PanthrixsEconomy;
import com.panthrixseconomy.block.BankBlock;
import com.panthrixseconomy.block.TownCoreBlockEntity;
import com.panthrixseconomy.command.Menus;
import com.panthrixseconomy.config.PEConfig;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.data.SettlementType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * La Gran Plaza: se construye UNA sola vez por mundo (la posición queda guardada en EconomyData)
 * y es el punto de bienvenida de cada jugador nuevo. No es un spawn forzado: nadie vuelve a ser
 * teletransportado al morir o al reconectar.
 */
public final class PlazaManager {

    public static final String WELCOMED_TAG = PanthrixsEconomy.MODID + ":welcomed";
    /** Quien ya ha jugado más de 2 minutos en este mundo ya "empezó su aventura": no se le mueve. */
    public static final int VETERAN_TICKS = 20 * 60 * 2;

    public enum Welcome { TELEPORT, MARK_ONLY, NONE }

    // ===================== Generación =====================

    public static void onServerStarted(MinecraftServer server, ServerLevel overworld) {
        EconomyData data = EconomyData.get(server);
        data.maxHistory = PEConfig.MAX_HISTORY.get();
        if (data.plazaCenter != null || data.plazaAttempted || !PEConfig.GENERATE_PLAZA.get()) {
            return;
        }
        data.plazaAttempted = true;
        data.setDirty();
        boolean fresh = overworld.getGameTime() < 200;
        if (!fresh && !PEConfig.GENERATE_PLAZA_IN_EXISTING_WORLDS.get()) {
            PanthrixsEconomy.LOGGER.info("[Panthrix's Economy] Mundo existente: no se construye la Gran Plaza (un admin puede usar /pe plaza generate).");
            return;
        }
        BlockPos spawn = overworld.getSharedSpawnPos();
        int radius = PEConfig.PLAZA_SEARCH_RADIUS.get();
        List<int[]> candidates = new ArrayList<>();
        for (int ox = -radius; ox <= radius; ox += 32) {
            for (int oz = -radius; oz <= radius; oz += 32) {
                if (ox * ox + oz * oz <= radius * radius) {
                    candidates.add(new int[]{spawn.getX() + ox, spawn.getZ() + oz});
                }
            }
        }
        candidates.sort(Comparator.comparingInt(p -> (p[0] - spawn.getX()) * (p[0] - spawn.getX()) + (p[1] - spawn.getZ()) * (p[1] - spawn.getZ())));
        String last = null;
        java.util.Map<String, Integer> reasons = new java.util.TreeMap<>();
        for (int step : new int[]{PlazaSite.STRICT_STEP, PlazaSite.RELAXED_STEP}) {
            for (int[] p : candidates) {
                PlazaSite.Result r = PlazaSite.check(overworld, p[0], p[1], step);
                if (r.ok()) {
                    build(overworld, data, r.center());
                    return;
                }
                last = r.problem();
                reasons.merge(last.replaceAll(" en -?\\d.*| cerca de .*| \\(.*", ""), 1, Integer::sum);
            }
        }
        PanthrixsEconomy.LOGGER.warn("[Panthrix's Economy] Sitios descartados: {}", reasons);
        PanthrixsEconomy.LOGGER.warn("[Panthrix's Economy] No se encontró un sitio seguro para la Gran Plaza (último motivo: {}). "
                + "Un administrador puede construirla con /pe plaza generate.", last);
    }

    /** Construye y registra la plaza. Solo se llama con un sitio ya validado. */
    public static void build(ServerLevel level, EconomyData data, BlockPos center) {
        PlazaBuilder b = new PlazaBuilder(level, center);
        b.build();
        net.minecraft.resources.ResourceLocation dim = level.dimension().location();
        Loc coreLoc = EconomyData.loc(dim, b.corePos);
        Settlement s = data.createSettlement("Gran Plaza", SettlementType.TOWN, EconomyData.loc(dim, center), PEConfig.TOWN_RADIUS.get());
        s.core = coreLoc;
        if (level.getBlockEntity(b.corePos) instanceof TownCoreBlockEntity core) {
            core.setSettlementId(s.id);
        }
        BankBlock.register(level, b.bankPos);
        data.createShop("Mercado de la Gran Plaza", EconomyData.loc(dim, b.marketPos));
        data.plazaCenter = center.immutable();
        data.plazaSpawn = b.spawnPos.immutable();
        data.setDirty();
        if (PEConfig.SET_WORLD_SPAWN_TO_PLAZA.get()) {
            level.setDefaultSpawnPos(b.spawnPos, 180.0F);
        }
        PanthrixsEconomy.LOGGER.info("[Panthrix's Economy] Gran Plaza construida en {}", center.toShortString());
    }

    // ===================== Bienvenida =====================

    /** Decide qué hacer con un jugador que entra (lógica pura, probada en la prueba de arranque). */
    public static Welcome decide(boolean plazaExists, boolean teleportEnabled, int playTimeTicks, boolean alreadyWelcomed) {
        if (alreadyWelcomed || !plazaExists) {
            return Welcome.NONE;
        }
        if (playTimeTicks > VETERAN_TICKS || !teleportEnabled) {
            return Welcome.MARK_ONLY;
        }
        return Welcome.TELEPORT;
    }

    public static void welcome(ServerPlayer player) {
        EconomyData data = EconomyData.get(player.server);
        boolean already = data.welcomed.contains(player.getUUID()) || player.getPersistentData().getBoolean(WELCOMED_TAG);
        int playTime = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        Welcome w = decide(data.plazaSpawn != null, PEConfig.TELEPORT_NEW_PLAYERS.get(), playTime, already);
        if (w == Welcome.NONE) {
            return;
        }
        data.welcomed.add(player.getUUID());
        data.setDirty();
        player.getPersistentData().putBoolean(WELCOMED_TAG, true);
        if (w == Welcome.TELEPORT) {
            BlockPos p = data.plazaSpawn;
            player.teleportTo(player.server.overworld(), p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 180.0F, 0.0F);
            player.sendSystemMessage(Component.literal("¡Bienvenido a la Gran Plaza! Aquí empieza tu aventura en Panthrix's Economy.")
                    .withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("El banco está al este, el mercado al oeste y el Ayuntamiento al norte. "
                    + "Eres libre de explorar el mundo cuando quieras.").withStyle(ChatFormatting.GRAY));
        }
    }

    // ===================== Comandos de admin =====================

    public static int info(CommandSourceStack src) {
        EconomyData data = EconomyData.get(src.getServer());
        if (data.plazaCenter == null) {
            src.sendSystemMessage(Component.literal("Este mundo aún no tiene Gran Plaza. ").withStyle(ChatFormatting.YELLOW)
                    .append(Menus.run("Construir aquí", "/pe plaza generate", "Comprueba que el sitio es seguro y la construye centrada en ti")));
            return 0;
        }
        src.sendSystemMessage(Menus.line("Gran Plaza", data.plazaCenter.toShortString() + " (llegada: " + data.plazaSpawn.toShortString() + ")"));
        src.sendSystemMessage(Menus.run("Ir", "/pe plaza tp", "Teletransportarte a la plaza"));
        return 1;
    }

    public static int teleport(CommandSourceStack src) {
        EconomyData data = EconomyData.get(src.getServer());
        if (data.plazaSpawn == null || !(src.getEntity() instanceof ServerPlayer p)) {
            src.sendFailure(Component.literal("No hay Gran Plaza o no eres un jugador."));
            return 0;
        }
        p.teleportTo(src.getServer().overworld(), data.plazaSpawn.getX() + 0.5, data.plazaSpawn.getY(), data.plazaSpawn.getZ() + 0.5, 180.0F, 0.0F);
        return 1;
    }

    public static int generateHere(CommandSourceStack src) {
        EconomyData data = EconomyData.get(src.getServer());
        if (data.plazaCenter != null) {
            src.sendFailure(Component.literal("Este mundo ya tiene su Gran Plaza en " + data.plazaCenter.toShortString() + " (solo puede haber una)."));
            return 0;
        }
        if (src.getLevel() != src.getServer().overworld()) {
            src.sendFailure(Component.literal("La Gran Plaza se construye en el mundo normal (Overworld)."));
            return 0;
        }
        BlockPos at = BlockPos.containing(src.getPosition());
        PlazaSite.Result r = PlazaSite.check(src.getLevel(), at.getX(), at.getZ());
        if (!r.ok()) {
            src.sendFailure(Component.literal("Sitio no válido: " + r.problem() + ". No se ha tocado nada."));
            return 0;
        }
        build(src.getLevel(), data, r.center());
        src.sendSuccess(() -> Component.literal("Gran Plaza construida en " + r.center().toShortString()).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private PlazaManager() {
    }
}
