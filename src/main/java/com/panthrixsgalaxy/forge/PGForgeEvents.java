package com.panthrixsgalaxy.forge;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.command.PGCommands;
import com.panthrixsgalaxy.command.PGTestCommands;
import com.panthrixsgalaxy.entity.rocket.PGRocketEvents;
import com.panthrixsgalaxy.event.PGGuideBookEvents;
import com.panthrixsgalaxy.event.PGSuitEvents;
import com.panthrixsgalaxy.system.PGSmokeTest;
import com.panthrixsgalaxy.system.backpack.PGBackpackEvents;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlotProvider;
import com.panthrixsgalaxy.system.oxygen.PGOxygenEvents;
import com.panthrixsgalaxy.system.oxygen.PGSealedRooms;
import com.panthrixsgalaxy.system.weather.PGMarsWeather;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Solo Forge: conecta los eventos de Forge con la lógica común del mod.
 * (En Fabric hace lo mismo fabric/PGFabricEvents con los eventos de Fabric.)
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGForgeEvents {

    private static final ResourceLocation BACKPACK_SLOT_ID = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "backpack_slot");

    // ===== Cada tick del jugador =====

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PGMarsWeather.onPlayerTickEnd(event.player);
            PGOxygenEvents.onPlayerTickEnd(event.player);
        }
    }

    // ===== Servidor =====

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PGSmokeTest.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PGSealedRooms.clearAll();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PGCommands.register(event.getDispatcher());
        PGTestCommands.register(event.getDispatcher());
        PGGuideBookEvents.register(event.getDispatcher());
    }

    // ===== Entrar, salir, reaparecer =====

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGGuideBookEvents.onPlayerLogin(player);
            PGBackpackEvents.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PGOxygenEvents.onLogout(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGBackpackEvents.sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGBackpackEvents.sync(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer watcher) {
            PGBackpackEvents.onStartTracking(event.getTarget(), watcher);
        }
    }

    // ===== Vehículos =====

    /** No se puede bajar de un cohete o nave en vuelo. */
    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.isDismounting() && !PGRocketEvents.canDismount(event.getEntityMounting(), event.getEntityBeingMounted())) {
            event.setCanceled(true);
        }
    }

    // ===== Traje y mochila =====

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        PGSuitEvents.onEquipmentChange(event.getEntity(), event.getSlot(), event.getTo());
    }

    /** El hueco de mochila se "pega" a cada jugador como una capability de Forge. */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(BACKPACK_SLOT_ID, new PGBackpackSlotProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps(); // en Forge hay que "revivir" las capabilities del jugador viejo para leerlas
        PGBackpackEvents.onPlayerClone(original, event.getEntity(), event.isWasDeath());
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack backpack = PGBackpackEvents.takeDeathDrop(player);
            if (!backpack.isEmpty()) {
                event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), backpack));
            }
        }
    }

    private PGForgeEvents() {
    }
}
