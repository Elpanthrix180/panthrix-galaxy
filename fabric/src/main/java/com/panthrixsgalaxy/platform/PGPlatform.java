package com.panthrixsgalaxy.platform;

import com.panthrixsgalaxy.fabric.FabricLaserSwordItem;
import com.panthrixsgalaxy.fabric.PGFabricEnergy;
import com.panthrixsgalaxy.fabric.PGPlayerData;
import com.panthrixsgalaxy.system.energy.PGEnergyHandler;
import com.panthrixsgalaxy.weapon.LaserSwordTier;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lo poco que se hace DISTINTO en Forge y en Fabric, reunido en un solo sitio.
 *
 * VERSIÓN FABRIC. La versión Forge (src/main/java/...) tiene exactamente los mismos métodos.
 */
public final class PGPlatform {

    /** "forge" o "fabric" (para los mensajes de la consola). */
    public static String getLoaderName() {
        return "fabric";
    }

    // ===== Registro =====

    /** En Fabric el huevo necesita la entidad ya creada: por eso se registran antes las entidades. */
    public static Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int spots,
                                Item.Properties properties) {
        return new SpawnEggItem(type.get(), background, spots, properties);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> menuType(PGMenuFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create);
    }

    /** Pestaña de creativo. En Fabric el orden de las pestañas es el de registro (after no hace falta). */
    public static CreativeModeTab.Builder creativeTabBuilder(@Nullable PGHolder<CreativeModeTab> after) {
        return FabricItemGroup.builder();
    }

    /**
     * En Fabric no se ordenan los niveles: cada bloque dice el nivel que necesita con los tags
     * fabric:needs_tool_level_N (mira data/fabric/tags/blocks).
     */
    public static Tier registerTier(Tier tier, ResourceLocation name, List<Object> after, List<Object> before) {
        return tier;
    }

    /** Espada láser (en Fabric, con el daño según esté encendida a través de FabricItem). */
    public static PGLaserSwordItem laserSword(LaserSwordTier tier, Item.Properties properties) {
        return new FabricLaserSwordItem(tier, properties);
    }

    // ===== Juego =====

    /** Abre una ventana en la pantalla del jugador; extraData escribe datos extra para ella. */
    public static void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer target, FriendlyByteBuf buf) {
                extraData.accept(buf);
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player target) {
                return provider.createMenu(containerId, inventory, target);
            }
        });
    }

    /** Ticks que arde un objeto en un horno (0 = no es combustible). */
    public static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
        return time == null ? 0 : time;
    }

    /** Datos extra del jugador que el mod guarda con él (los añade el mixin PlayerMixin). */
    public static CompoundTag getPersistentData(Player player) {
        return ((PGPlayerData) player).panthrixsgalaxy$getData();
    }

    /** Datos extra del jugador que se conservan también al morir (logros, libro guía...). */
    public static CompoundTag getPersistedData(Player player) {
        CompoundTag data = getPersistentData(player);
        if (!data.contains(PGPlayerData.PERSISTED_TAG)) {
            data.put(PGPlayerData.PERSISTED_TAG, new CompoundTag());
        }
        return data.getCompound(PGPlayerData.PERSISTED_TAG);
    }

    // ===== Energía de OTROS mods (la del propio mod se mira directamente) =====

    public static @Nullable PGEnergyHandler findBlockEnergy(Level level, BlockPos pos, BlockEntity blockEntity,
                                                            @Nullable Direction side) {
        return PGFabricEnergy.findBlockEnergy(level, pos, blockEntity, side);
    }

    public static @Nullable PGEnergyHandler findItemEnergy(ItemStack stack) {
        return PGFabricEnergy.findItemEnergy(stack);
    }

    private PGPlatform() {
    }
}
