package com.panthrixsgalaxy.system.backpack;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.network.BackpackSyncPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Todo lo que le pasa al hueco de mochila:
 *   - Se añade a cada jugador.
 *   - Se conserva al cambiar de dimensión y, con keepInventory, al morir.
 *   - Al morir (sin keepInventory) la mochila cae al suelo, como la armadura.
 *   - Se sincroniza con los demás jugadores para que la vean en tu espalda.
 */
@Mod.EventBusSubscriber(modid = PanthrixsGalaxy.MOD_ID)
public final class PGBackpackEvents {

    private static final ResourceLocation SLOT_ID = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "backpack_slot");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(SLOT_ID, new PGBackpackSlotProvider());
        }
    }

    /** Al reaparecer o volver del End, Minecraft crea un jugador nuevo: copiamos la mochila si toca. */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        boolean keep = !event.isWasDeath() || keepInventory(original);
        if (keep) {
            PGBackpackSlot.setEquipped(event.getEntity(), PGBackpackSlot.getEquipped(original));
        }
        original.invalidateCaps();
    }

    /** Al morir sin keepInventory, la mochila (con todo lo que lleva dentro) cae al suelo. */
    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || keepInventory(player)) {
            return;
        }
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        if (!backpack.isEmpty()) {
            event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), backpack));
            PGBackpackSlot.setEquipped(player, ItemStack.EMPTY);
        }
    }

    // ----- Sincronizar con la pantalla de los jugadores -----

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGBackpackSlot.sync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGBackpackSlot.sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PGBackpackSlot.sync(player);
        }
    }

    /** Cuando alguien empieza a ver a otro jugador, le contamos qué mochila lleva. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player target && event.getEntity() instanceof ServerPlayer watcher) {
            PGNetwork.sendToPlayer(watcher, new BackpackSyncPacket(target.getId(), PGBackpackSlot.getEquipped(target)));
        }
    }

    private static boolean keepInventory(Player player) {
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }

    private PGBackpackEvents() {
    }
}
