package com.panthrixseconomy;

import com.panthrixseconomy.command.AdminCommands;
import com.panthrixseconomy.command.PlayerCommands;
import com.panthrixseconomy.data.Bank;
import com.panthrixseconomy.data.EconomyData;
import com.panthrixseconomy.data.Loc;
import com.panthrixseconomy.data.Settlement;
import com.panthrixseconomy.permission.PEPermissions;
import com.panthrixseconomy.plaza.PlazaManager;
import com.panthrixseconomy.registry.PERegistry;
import com.panthrixseconomy.test.PESmokeTest;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PanthrixsEconomy.MODID)
public final class PEEvents {

    private static boolean isOfficial(BlockState state) {
        return state.is(PERegistry.TOWN_CORE.get()) || state.is(PERegistry.BANK.get());
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        AdminCommands.register(event.getDispatcher());
        PlayerCommands.register(event.getDispatcher());
    }

    /** Solo un administrador puede colocar Núcleos del Pueblo y bancos oficiales (también bloquea dispensadores, etc.). */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!isOfficial(event.getPlacedBlock())) {
            return;
        }
        if (!PEPermissions.isAdmin(event.getEntity())) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer p) {
                p.sendSystemMessage(Component.literal("Solo los administradores pueden crear pueblos y bancos oficiales.")
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    /** Romperlos: solo administradores, y agachados (antes se avisa de las consecuencias). */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (!isOfficial(state) || !(event.getPlayer() instanceof ServerPlayer p)) {
            return;
        }
        if (!PEPermissions.isAdmin(p)) {
            event.setCanceled(true);
            p.sendSystemMessage(Component.literal("Este bloque oficial está protegido.").withStyle(ChatFormatting.RED));
            return;
        }
        if (p.isShiftKeyDown()) {
            return;  // confirmado
        }
        event.setCanceled(true);
        EconomyData data = EconomyData.get(p.server);
        Loc here = EconomyData.loc(p.level().dimension().location(), event.getPos());
        List<String> lines = new ArrayList<>();
        if (state.is(PERegistry.BANK.get())) {
            Bank b = data.bankAt(here);
            lines.add("La sucursal «" + (b == null ? "?" : data.bankName(b)) + "» dejará de existir.");
            lines.add("Las cuentas y saldos se conservan: pertenecen al banco del país y se usan en cualquier otra sucursal suya.");
            lines.add("Las demás sucursales de ese país pueden cambiar de número.");
        } else {
            Settlement s = data.settlements.values().stream().filter(x -> here.equals(x.core)).findFirst().orElse(null);
            lines.add("El pueblo " + (s == null ? "" : "«" + s.name + "» ") + "se queda sin Núcleo, pero NO se borra (ni su tesorería ni sus datos).");
            lines.add("Para borrar el pueblo usa /pe settlement delete.");
        }
        p.sendSystemMessage(Component.literal("⚠ Vas a quitar un bloque oficial:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        for (String l : lines) {
            p.sendSystemMessage(Component.literal("  • " + l).withStyle(ChatFormatting.YELLOW));
        }
        p.sendSystemMessage(Component.literal("Rómpelo agachado (Shift) para confirmar.").withStyle(ChatFormatting.GRAY));
    }

    /** Las explosiones no destruyen bloques oficiales. */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> isOfficial(event.getLevel().getBlockState(pos)));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            PlazaManager.welcome(p);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        PlazaManager.onServerStarted(event.getServer(), overworld);
        PESmokeTest.onServerStarted(event.getServer());
    }


    private PEEvents() {
    }
}
