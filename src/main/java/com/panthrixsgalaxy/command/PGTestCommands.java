package com.panthrixsgalaxy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.panthrixsgalaxy.entity.ship.ShipTier;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGBatteryItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.item.PGOxygenTankItem;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.energy.ItemEnergyStorage;
import com.panthrixsgalaxy.weapon.PGLaserItem;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Comandos para PROBAR el mod rápido (Fase 24). Necesitan trucos/operador.
 *
 *   /pgtp <planeta>      → te lleva a la superficie de un planeta (moon, mars, asteroids, venus, xenoria, nyx...)
 *   /pgkit <kit>         → te da un equipo: starter (Tierra→Luna), explorer (Marte y nave), endgame (todo lo mejor)
 *   /pgrefill            → llena de oxígeno, energía y combustible todo lo que llevas
 */
public final class PGTestCommands {

    private static final List<String> KITS = List.of("starter", "explorer", "endgame");

    /** Registra los comandos (lo llaman Forge y Fabric al arrancar el servidor). */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pgtp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("planet", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                PGPlanets.ALL.stream().filter(PGPlanet::isLandable).map(PGPlanet::id), builder))
                        .executes(PGTestCommands::teleport)));
        dispatcher.register(Commands.literal("pgkit")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("kit", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(KITS, builder))
                        .executes(PGTestCommands::kit)));
        dispatcher.register(Commands.literal("pgrefill")
                .requires(source -> source.hasPermission(2))
                .executes(PGTestCommands::refill));
    }

    private static int teleport(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(context, "planet");
        PGPlanet planet = PGPlanets.ALL.stream().filter(p -> p.id().equals(id) && p.isLandable()).findFirst().orElse(null);
        ServerLevel level = planet == null ? null : player.server.getLevel(planet.dimension());
        if (level == null) {
            context.getSource().sendFailure(Component.translatable("command.panthrixsgalaxy.unknown_planet", id));
            return 0;
        }
        int x = player.getBlockX();
        int z = player.getBlockZ();
        level.getChunk(x >> 4, z >> 4); // cargar el trozo para saber la altura del suelo
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (y <= level.getMinBuildHeight() + 1) {
            y = 150; // sin suelo (asteroides): se aparece flotando y se cae muy despacio
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0));
        }
        player.teleportTo(level, x + 0.5, y + 1, z + 0.5, player.getYRot(), player.getXRot());
        context.getSource().sendSuccess(() -> Component.translatable("command.panthrixsgalaxy.teleported",
                Component.translatable(planet.getTranslationKey())), false);
        return 1;
    }

    private static int kit(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String kit = StringArgumentType.getString(context, "kit");
        switch (kit) {
            case "starter" -> {
                give(player, ModBlocks.PG_ENGINEERING_BENCH.get(), 1);
                suit(player);
                give(player, full(ModItems.PG_SPACE_OXYGEN_TANK.get()), 2);
                give(player, full(ModItems.PG_SPACE_BACKPACK.get()), 1);
                give(player, full(ModItems.PG_BASIC_BATTERY.get()), 1);
                give(player, full(ModItems.PG_FUEL_CANISTER.get()), 3);
                give(player, ModItems.PG_BASIC_ROCKET.get(), 1);
                give(player, ModBlocks.PG_LAUNCH_PAD.get(), 9);
            }
            case "explorer" -> {
                suit(player);
                give(player, full(ModItems.PG_ADVANCED_SPACE_BACKPACK.get()), 1);
                give(player, ModItems.PG_ADVANCED_ROCKET.get(), 1);
                give(player, ship(ShipTier.SHIP), 1);
                give(player, full(ModItems.PG_FUEL_CANISTER.get()), 4);
                give(player, ModItems.PG_HABITAT_MODULE.get(), 2);
                give(player, full(ModItems.PG_BLUE_LASER_PISTOL.get()), 1);
                give(player, full(ModItems.PG_RED_LASER_SWORD.get()), 1);
            }
            case "endgame" -> {
                suit(player);
                give(player, full(ModItems.PG_EXPERIMENTAL_SPACE_BACKPACK.get()), 1);
                give(player, ship(ShipTier.ADVANCED), 1);
                give(player, ModItems.PG_HABITAT_MODULE.get(), 4);
                give(player, full(ModItems.PG_WHITE_LASER_PISTOL.get()), 1);
                give(player, full(ModItems.PG_BLACK_LASER_SWORD.get()), 1);
                give(player, ModItems.PG_ALIEN_BEACON.get(), 1);
            }
            default -> {
                context.getSource().sendFailure(Component.translatable("command.panthrixsgalaxy.unknown_kit", kit));
                return 0;
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.panthrixsgalaxy.kit_given", kit), false);
        return 1;
    }

    private static int refill(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        for (ItemStack stack : player.getInventory().items) {
            fill(stack);
        }
        fill(PGBackpackSlot.getEquipped(player));
        context.getSource().sendSuccess(() -> Component.translatable("command.panthrixsgalaxy.refilled"), false);
        return 1;
    }

    // ===== Ayudantes =====

    private static void suit(ServerPlayer player) {
        give(player, ModItems.PG_SPACE_HELMET.get(), 1);
        give(player, ModItems.PG_SPACE_CHESTPLATE.get(), 1);
        give(player, ModItems.PG_SPACE_LEGGINGS.get(), 1);
        give(player, ModItems.PG_SPACE_BOOTS.get(), 1);
    }

    private static ItemStack ship(ShipTier tier) {
        ItemStack stack = new ItemStack(tier.getItem());
        stack.getOrCreateTag().putInt("Fuel", tier.getFuelCapacity());
        stack.getOrCreateTag().putInt("Energy", tier.getEnergyCapacity());
        return stack;
    }

    private static ItemStack full(Item item) {
        ItemStack stack = new ItemStack(item);
        fill(stack);
        return stack;
    }

    /** Llena un objeto del mod: oxígeno, combustible, energía o los 4 depósitos de una mochila. */
    private static void fill(ItemStack stack) {
        if (stack.getItem() instanceof PGOxygenTankItem tank) {
            PGOxygenTankItem.setOxygen(stack, tank.getCapacity());
        } else if (stack.getItem() instanceof PGFuelCanisterItem canister) {
            PGFuelCanisterItem.setFuel(stack, canister.getCapacity());
        } else if (stack.getItem() instanceof PGBatteryItem battery) {
            stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, battery.getCapacity());
        } else if (stack.getItem() instanceof PGLaserItem laser) {
            stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, laser.getTier().getCapacity());
        } else if (stack.getItem() instanceof PGLaserSwordItem sword) {
            stack.getOrCreateTag().putInt(ItemEnergyStorage.ENERGY_TAG, sword.getLaserTier().getCapacity());
        } else if (stack.getItem() instanceof PGBackpackItem backpack) {
            for (PGBackpackItem.Tank tank : PGBackpackItem.Tank.values()) {
                PGBackpackItem.setTank(stack, tank, backpack.getCapacity(tank));
            }
        }
    }

    private static void give(ServerPlayer player, ItemLike item, int count) {
        give(player, new ItemStack(item, count), 1);
    }

    private static void give(ServerPlayer player, ItemStack stack, int copies) {
        for (int i = 0; i < copies; i++) {
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
    }

    private PGTestCommands() {
    }
}
