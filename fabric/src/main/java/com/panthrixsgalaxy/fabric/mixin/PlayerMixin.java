package com.panthrixsgalaxy.fabric.mixin;

import com.panthrixsgalaxy.fabric.PGFabricEvents;
import com.panthrixsgalaxy.fabric.PGPlayerData;
import com.panthrixsgalaxy.system.backpack.PGBackpackEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Solo Fabric: un "mixin" añade código a una clase de Minecraft.
 * Aquí le damos a cada jugador un hueco de mochila y datos extra, y los guardamos con él.
 * (En Forge se hace con una "capability": system/backpack/PGBackpackSlot.)
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements PGPlayerData {

    @Unique
    private static final String BACKPACK_TAG = "PanthrixsGalaxyBackpack";

    @Unique
    private static final String DATA_TAG = "PanthrixsGalaxyData";

    @Unique
    private ItemStack panthrixsgalaxy$backpack = ItemStack.EMPTY;

    @Unique
    private CompoundTag panthrixsgalaxy$data = new CompoundTag();

    @Override
    public CompoundTag panthrixsgalaxy$getData() {
        return panthrixsgalaxy$data;
    }

    @Override
    public ItemStack panthrixsgalaxy$getBackpack() {
        return panthrixsgalaxy$backpack;
    }

    @Override
    public void panthrixsgalaxy$setBackpack(ItemStack backpack) {
        panthrixsgalaxy$backpack = backpack;
    }

    /** Al final de cada tick del jugador (en Forge: PlayerTickEvent). */
    @Inject(method = "tick", at = @At("TAIL"))
    private void panthrixsgalaxy$afterTick(CallbackInfo info) {
        PGFabricEvents.onPlayerTickEnd((Player) (Object) this);
    }

    /** Al morir: sin keepInventory la mochila cae al suelo (en Forge: LivingDropsEvent). */
    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void panthrixsgalaxy$dropBackpack(CallbackInfo info) {
        Player player = (Player) (Object) this;
        ItemStack backpack = PGBackpackEvents.takeDeathDrop(player);
        if (!backpack.isEmpty()) {
            player.drop(backpack, true, false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void panthrixsgalaxy$saveBackpack(CompoundTag tag, CallbackInfo info) {
        if (!panthrixsgalaxy$backpack.isEmpty()) {
            tag.put(BACKPACK_TAG, panthrixsgalaxy$backpack.save(new CompoundTag()));
        }
        if (!panthrixsgalaxy$data.isEmpty()) {
            tag.put(DATA_TAG, panthrixsgalaxy$data);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void panthrixsgalaxy$loadBackpack(CompoundTag tag, CallbackInfo info) {
        panthrixsgalaxy$backpack = tag.contains(BACKPACK_TAG) ? ItemStack.of(tag.getCompound(BACKPACK_TAG)) : ItemStack.EMPTY;
        panthrixsgalaxy$data = tag.getCompound(DATA_TAG);
    }
}
