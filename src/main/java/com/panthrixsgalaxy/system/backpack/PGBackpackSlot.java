package com.panthrixsgalaxy.system.backpack;

import com.panthrixsgalaxy.network.BackpackSyncPacket;
import com.panthrixsgalaxy.network.PGNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;

/**
 * El HUECO DE MOCHILA del jugador.
 *
 * Minecraft no tiene un hueco para mochilas, así que se lo añadimos a cada jugador
 * con una "capability" de Forge: un dato extra pegado al jugador que se guarda con él
 * (como su inventario), sin necesitar mods externos.
 */
public class PGBackpackSlot implements INBTSerializable<CompoundTag> {

    /** Identificador del dato extra. Forge lo rellena automáticamente. */
    public static final Capability<PGBackpackSlot> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ItemStack backpack = ItemStack.EMPTY;

    public ItemStack getBackpack() {
        return backpack;
    }

    public void setBackpack(ItemStack backpack) {
        this.backpack = backpack;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Backpack", backpack.save(new CompoundTag()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        backpack = ItemStack.of(tag.getCompound("Backpack"));
    }

    // ===== AYUDANTES PARA EL RESTO DEL MOD =====

    /** Se llama al arrancar el mod para dar a conocer el dato a Forge. */
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(PGBackpackSlot.class);
    }

    /** La mochila que lleva equipada el jugador (vacío si no lleva). */
    public static ItemStack getEquipped(Player player) {
        return player.getCapability(CAPABILITY).map(PGBackpackSlot::getBackpack).orElse(ItemStack.EMPTY);
    }

    /** Cambia la mochila equipada (ItemStack.EMPTY para quitarla). */
    public static void setEquipped(Player player, ItemStack stack) {
        player.getCapability(CAPABILITY).ifPresent(slot -> slot.setBackpack(stack));
    }

    /** Avisa al propio jugador y a los que le ven de qué mochila lleva (para dibujarla en su espalda). */
    public static void sync(ServerPlayer player) {
        PGNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new BackpackSyncPacket(player.getId(), getEquipped(player)));
    }
}
