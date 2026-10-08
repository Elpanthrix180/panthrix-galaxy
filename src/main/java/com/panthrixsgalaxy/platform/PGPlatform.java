package com.panthrixsgalaxy.platform;

import com.panthrixsgalaxy.forge.ForgeEnergyAdapter;
import com.panthrixsgalaxy.forge.ForgeLaserSwordItem;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.weapon.LaserSwordTier;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lo poco que se hace DISTINTO en Forge y en Fabric, reunido en un solo sitio.
 *
 * VERSIÓN FORGE. La versión Fabric (fabric/src/...) tiene exactamente los mismos métodos.
 * El resto del mod solo llama a estos métodos y así no depende de ningún cargador.
 */
public final class PGPlatform {

    /** "forge" o "fabric" (para los mensajes de la consola). */
    public static String getLoaderName() {
        return "forge";
    }

    // ===== Registro =====

    public static Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int spots,
                                Item.Properties properties) {
        return new ForgeSpawnEggItem(type, background, spots, properties);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> menuType(PGMenuFactory<T> factory) {
        return IForgeMenuType.create(factory::create);
    }

    /** Pestaña de creativo. after = la pestaña que va justo antes (o null). */
    public static CreativeModeTab.Builder creativeTabBuilder(@Nullable PGHolder<CreativeModeTab> after) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        if (after != null) {
            builder.withTabsBefore(after.getKey());
        }
        return builder;
    }

    /** Nivel de herramienta colocado entre otros niveles (p. ej. la lunarita entre hierro y diamante). */
    public static Tier registerTier(Tier tier, ResourceLocation name, List<Object> after, List<Object> before) {
        return TierSortingRegistry.registerTier(tier, name, after, before);
    }

    /** Espada láser (en Forge con su control de acciones y daño según esté encendida). */
    public static PGLaserSwordItem laserSword(LaserSwordTier tier, Item.Properties properties) {
        return new ForgeLaserSwordItem(tier, properties);
    }

    // ===== Juego =====

    /** Abre una ventana en la pantalla del jugador; extraData escribe datos extra para ella. */
    public static void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        NetworkHooks.openScreen(player, provider, extraData);
    }

    /** Ticks que arde un objeto en un horno (0 = no es combustible). */
    public static int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    /** Datos extra del jugador que el mod guarda con él (se pierden al morir). */
    public static CompoundTag getPersistentData(Player player) {
        return player.getPersistentData();
    }

    /** Datos extra del jugador que se conservan también al morir (logros, libro guía...). */
    public static CompoundTag getPersistedData(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) {
            data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    // ===== Energía de OTROS mods (la del propio mod se mira directamente) =====

    public static @Nullable PGEnergyHandler findBlockEnergy(Level level, BlockPos pos, BlockEntity blockEntity,
                                                            @Nullable Direction side) {
        return blockEntity.getCapability(ForgeCapabilities.ENERGY, side).map(ForgeEnergyAdapter::wrap).orElse(null);
    }

    public static @Nullable PGEnergyHandler findItemEnergy(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(ForgeEnergyAdapter::wrap).orElse(null);
    }

    private PGPlatform() {
    }
}
