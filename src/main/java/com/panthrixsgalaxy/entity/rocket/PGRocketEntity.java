package com.panthrixsgalaxy.entity.rocket;

import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.item.PGRocketItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * El cohete en el mundo (una "entidad", como una barca o una vagoneta).
 *
 * Lo que sabe hacer en la Fase 9:
 *   - Quedarse de pie sobre la plataforma de lanzamiento.
 *   - Llevar un astronauta (clic derecho para subirse, Mayús para bajarse).
 *   - Guardar combustible (clic derecho con un bidón).
 *   - Recogerse con Mayús + clic derecho (conserva el combustible).
 * En la Fase 10 aprenderá a despegar.
 */
public class PGRocketEntity extends Entity {

    /** Datos que se envían solos a la pantalla de los jugadores. */
    private static final EntityDataAccessor<Integer> FUEL =
            SynchedEntityData.defineId(PGRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(PGRocketEntity.class, EntityDataSerializers.INT);

    public PGRocketEntity(EntityType<? extends PGRocketEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FUEL, 0);
        entityData.define(TIER, RocketTier.BASIC.ordinal());
    }

    // ===== Datos =====

    public RocketTier getTier() {
        return RocketTier.byId(entityData.get(TIER));
    }

    public void setTier(RocketTier tier) {
        entityData.set(TIER, tier.ordinal());
    }

    public int getFuel() {
        return entityData.get(FUEL);
    }

    public void setFuel(int fuel) {
        entityData.set(FUEL, Math.max(0, Math.min(fuel, getFuelCapacity())));
    }

    public int getFuelCapacity() {
        return getTier().getFuelCapacity();
    }

    // ===== Comportamiento =====

    @Override
    public void tick() {
        super.tick();
        // Gravedad sencilla: si no está apoyado, cae
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        move(MoverType.SELF, getDeltaMovement());
        if (onGround()) {
            setDeltaMovement(Vec3.ZERO);
        } else {
            setDeltaMovement(getDeltaMovement().multiply(0.5, 0.98, 0.5));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // 1) Bidón de combustible -> depósito del cohete
        if (held.getItem() instanceof PGFuelCanisterItem) {
            int moved = Math.min(PGFuelCanisterItem.getFuel(held), getFuelCapacity() - getFuel());
            if (moved > 0) {
                PGFuelCanisterItem.setFuel(held, PGFuelCanisterItem.getFuel(held) - moved);
                setFuel(getFuel() + moved);
                level().playSound(null, blockPosition(), SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.NEUTRAL, 0.8f, 1.2f);
            }
            showFuel(player);
            return InteractionResult.CONSUME;
        }

        // 2) Mayús + mano vacía -> recoger el cohete
        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (isVehicle()) {
                return InteractionResult.PASS;
            }
            ItemStack item = new ItemStack(getTier().getItem());
            PGRocketItem.setStoredFuel(item, getFuel());
            if (!player.getInventory().add(item)) {
                spawnAtLocation(item);
            }
            discard();
            return InteractionResult.CONSUME;
        }

        // 3) Subirse. La mochila vuelca su combustible en el cohete.
        if (!isVehicle()) {
            transferBackpackFuel(player);
            player.startRiding(this);
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.rocket_boarded")
                    .withStyle(ChatFormatting.AQUA), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private void transferBackpackFuel(Player player) {
        ItemStack backpack = PGBackpackSlot.getEquipped(player);
        if (!(backpack.getItem() instanceof PGBackpackItem)) {
            return;
        }
        int inBackpack = PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.FUEL);
        int moved = Math.min(inBackpack, getFuelCapacity() - getFuel());
        if (moved > 0) {
            PGBackpackItem.setTank(backpack, PGBackpackItem.Tank.FUEL, inBackpack - moved);
            setFuel(getFuel() + moved);
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.rocket_fuel_from_backpack", moved)
                    .withStyle(ChatFormatting.GOLD), false);
        }
    }

    private void showFuel(Player player) {
        player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.rocket_fuel", getFuel(), getFuelCapacity())
                .withStyle(ChatFormatting.GOLD), true);
    }

    /** Solo un jugador en creativo puede romperlo a golpes; si no, hay que recogerlo con Mayús + clic. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() instanceof Player player && player.isCreative()) {
            discard();
            return true;
        }
        return false;
    }

    // ===== Pasajero =====

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    /** Altura del asiento: los ojos del astronauta quedan a la altura de la ventanilla. */
    @Override
    public double getPassengersRidingOffset() {
        return 0.6;
    }

    // ===== Física e interacción =====

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getTier().getItem());
    }

    // ===== Guardar y cargar =====

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setTier(RocketTier.byId(tag.getInt("Tier")));
        setFuel(tag.getInt("Fuel"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Tier", getTier().ordinal());
        tag.putInt("Fuel", getFuel());
    }
}
