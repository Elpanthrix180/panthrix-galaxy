package com.panthrixsgalaxy.fabric.mixin;

import com.panthrixsgalaxy.fabric.PGBackpackHolder;
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
 * Aquí le damos a cada jugador un hueco de mochila y lo guardamos con sus datos.
 * (En Forge se hace con una "capability": system/backpack/PGBackpackSlot.)
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements PGBackpackHolder {

    @Unique
    private static final String BACKPACK_TAG = "PanthrixsGalaxyBackpack";

    @Unique
    private ItemStack panthrixsgalaxy$backpack = ItemStack.EMPTY;

    @Override
    public ItemStack panthrixsgalaxy$getBackpack() {
        return panthrixsgalaxy$backpack;
    }

    @Override
    public void panthrixsgalaxy$setBackpack(ItemStack backpack) {
        panthrixsgalaxy$backpack = backpack;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void panthrixsgalaxy$saveBackpack(CompoundTag tag, CallbackInfo info) {
        if (!panthrixsgalaxy$backpack.isEmpty()) {
            tag.put(BACKPACK_TAG, panthrixsgalaxy$backpack.save(new CompoundTag()));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void panthrixsgalaxy$loadBackpack(CompoundTag tag, CallbackInfo info) {
        panthrixsgalaxy$backpack = tag.contains(BACKPACK_TAG) ? ItemStack.of(tag.getCompound(BACKPACK_TAG)) : ItemStack.EMPTY;
    }
}
