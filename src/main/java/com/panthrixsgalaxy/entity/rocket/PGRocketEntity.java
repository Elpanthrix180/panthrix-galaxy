package com.panthrixsgalaxy.entity.rocket;

import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.item.PGRocketItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * El cohete en el mundo (una "entidad", como una barca o una vagoneta).
 *
 * Fase 9: subirse, repostar, recogerlo.
 * Fase 10: lanzamiento.
 *
 *   IDLE ──ESPACIO──► COUNTDOWN (10 s) ──► ASCENDING ──Y 450──► DESCENDING ──suelo──► IDLE
 *                         │ ESPACIO / bajarse          │ sin combustible
 *                         ▼                            ▼
 *                       IDLE                        FALLING ──golpe fuerte──► ¡EXPLOSIÓN!
 *
 * (En la Fase 11, al llegar arriba, en lugar de bajar pasará al espacio.)
 */
public class PGRocketEntity extends Entity {

    /** Duración de la cuenta atrás (200 ticks = 10 segundos). */
    public static final int COUNTDOWN_TICKS = 200;
    /** Desde aquí se encienden los motores durante la cuenta atrás (60 ticks = 3 s). */
    public static final int IGNITION_TICKS = 60;
    /** Altura del límite de la atmósfera para el vuelo de prueba. */
    public static final int ATMOSPHERE_TOP = 450;
    /** Altura de las nubes en Minecraft 1.20. */
    public static final int CLOUD_HEIGHT = 192;
    /** Velocidad de caída a partir de la cual el cohete explota al tocar el suelo. */
    private static final double CRASH_SPEED = 0.6;
    /** Por debajo de esta distancia al suelo se encienden los retropropulsores. */
    private static final double LANDING_BURN_HEIGHT = 40.0;

    /** Datos que se envían solos a la pantalla de los jugadores. */
    private static final EntityDataAccessor<Integer> FUEL =
            SynchedEntityData.defineId(PGRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(PGRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(PGRocketEntity.class, EntityDataSerializers.INT);
    /** En la cuenta atrás: ticks que faltan. En vuelo: ticks que lleva volando. */
    private static final EntityDataAccessor<Integer> TIMER =
            SynchedEntityData.defineId(PGRocketEntity.class, EntityDataSerializers.INT);

    public PGRocketEntity(EntityType<? extends PGRocketEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FUEL, 0);
        entityData.define(TIER, RocketTier.BASIC.ordinal());
        entityData.define(STATE, LaunchState.IDLE.ordinal());
        entityData.define(TIMER, 0);
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

    public LaunchState getLaunchState() {
        return LaunchState.byId(entityData.get(STATE));
    }

    private void setLaunchState(LaunchState state) {
        entityData.set(STATE, state.ordinal());
        entityData.set(TIMER, state == LaunchState.COUNTDOWN ? COUNTDOWN_TICKS : 0);
    }

    public int getTimer() {
        return entityData.get(TIMER);
    }

    /** ¿Está apoyado en una plataforma de lanzamiento? */
    public boolean isOnLaunchPad() {
        return level().getBlockState(blockPosition().below()).is(ModBlocks.PG_LAUNCH_PAD.get());
    }

    // ===== Control del lanzamiento (lo llama el paquete de la tecla ESPACIO) =====

    /** ESPACIO: empezar la cuenta atrás, o cancelarla si ya ha empezado. */
    public void toggleLaunch(Player pilot) {
        LaunchState state = getLaunchState();
        if (state == LaunchState.COUNTDOWN) {
            setLaunchState(LaunchState.IDLE);
            tell(pilot, "message.panthrixsgalaxy.launch_aborted", ChatFormatting.YELLOW);
            return;
        }
        if (state != LaunchState.IDLE) {
            return;
        }
        if (!isOnLaunchPad()) {
            tell(pilot, "message.panthrixsgalaxy.launch_needs_pad", ChatFormatting.RED);
            return;
        }
        if (getFuel() < getTier().getMinLaunchFuel()) {
            pilot.displayClientMessage(Component.translatable("message.panthrixsgalaxy.launch_needs_fuel",
                    getTier().getMinLaunchFuel()).withStyle(ChatFormatting.RED), true);
            return;
        }
        setLaunchState(LaunchState.COUNTDOWN);
        tell(pilot, "message.panthrixsgalaxy.launch_countdown_started", ChatFormatting.AQUA);
    }

    private void tell(Player player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    // ===== Cada tick =====

    @Override
    public void tick() {
        super.tick();
        LaunchState state = getLaunchState();
        if (level().isClientSide) {
            spawnEngineParticles(state);
        }
        switch (state) {
            case IDLE -> tickIdle();
            case COUNTDOWN -> tickCountdown();
            case ASCENDING -> tickAscending();
            case DESCENDING -> tickDescending();
            case FALLING -> tickFalling();
        }
        // Los pasajeros no acumulan daño de caída dentro del cohete
        getPassengers().forEach(passenger -> passenger.fallDistance = 0.0f);
    }

    /** En reposo: gravedad sencilla. */
    private void tickIdle() {
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(onGround() ? Vec3.ZERO : getDeltaMovement().multiply(0.5, 0.98, 0.5));
    }

    private void tickCountdown() {
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide) {
            return;
        }
        if (!isVehicle()) {
            setLaunchState(LaunchState.IDLE); // el piloto se ha bajado: cancelar
            return;
        }
        int remaining = getTimer() - 1;
        entityData.set(TIMER, remaining);
        if (remaining > 0 && remaining % 20 == 0) {
            // Un pitido por segundo, cada vez más agudo
            float pitch = 0.6f + (COUNTDOWN_TICKS - remaining) / (float) COUNTDOWN_TICKS;
            level().playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 1.0f, pitch);
        }
        if (remaining == IGNITION_TICKS) {
            level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 2.0f, 0.5f);
        }
        if (remaining <= 0) {
            setLaunchState(LaunchState.ASCENDING);
            level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 2.0f, 0.6f);
            level().playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.NEUTRAL, 3.0f, 0.5f);
        }
    }

    private void tickAscending() {
        RocketTier tier = getTier();
        double speed = Math.min(getDeltaMovement().y + tier.getAcceleration(), tier.getMaxSpeed());
        setDeltaMovement(0.0, speed, 0.0);
        move(MoverType.SELF, getDeltaMovement());

        if (level().isClientSide) {
            return;
        }
        entityData.set(TIMER, getTimer() + 1);
        if (getTimer() % 8 == 0) {
            level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 3.0f, 0.4f);
        }
        // ¿Ha chocado contra algo al subir?
        if (verticalCollision || horizontalCollision) {
            explode();
            return;
        }
        // Gastar combustible
        int fuel = getFuel() - tier.getFuelPerTick();
        setFuel(fuel);
        if (fuel <= 0) {
            setLaunchState(LaunchState.FALLING);
            getPassengers().forEach(p -> {
                if (p instanceof Player player) {
                    tell(player, "message.panthrixsgalaxy.out_of_fuel", ChatFormatting.RED);
                }
            });
            return;
        }
        // Límite de la atmósfera (vuelo de prueba de la Fase 10)
        if (getY() >= ATMOSPHERE_TOP) {
            setLaunchState(LaunchState.DESCENDING);
            getPassengers().forEach(p -> {
                if (p instanceof Player player) {
                    player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.test_flight_complete")
                            .withStyle(ChatFormatting.AQUA), false);
                }
            });
        }
    }

    /** Descenso controlado: rápido arriba, frenando con los retropropulsores cerca del suelo. */
    private void tickDescending() {
        double groundY = level().getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ());
        double height = getY() - groundY;
        double speed = height > LANDING_BURN_HEIGHT ? -0.8 : -Math.max(0.08, 0.8 * height / LANDING_BURN_HEIGHT);
        setDeltaMovement(0.0, speed, 0.0);
        move(MoverType.SELF, getDeltaMovement());
        if (!level().isClientSide && onGround()) {
            setLaunchState(LaunchState.IDLE);
            setDeltaMovement(Vec3.ZERO);
            level().playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 0.6f, 0.6f);
            getPassengers().forEach(p -> {
                if (p instanceof Player player) {
                    tell(player, "message.panthrixsgalaxy.landed", ChatFormatting.GREEN);
                }
            });
        }
    }

    /** Sin combustible: caída libre. Si llega al suelo muy rápido, explota. */
    private void tickFalling() {
        double speedBefore = getDeltaMovement().y;
        setDeltaMovement(0.0, Math.max(speedBefore - 0.06, -3.0), 0.0);
        move(MoverType.SELF, getDeltaMovement());
        if (!level().isClientSide && onGround()) {
            if (speedBefore < -CRASH_SPEED) {
                explode();
            } else {
                setLaunchState(LaunchState.IDLE);
            }
        }
    }

    private void explode() {
        setLaunchState(LaunchState.IDLE); // así se permite expulsar al pasajero
        ejectPassengers();
        level().explode(this, getX(), getY() + 1.0, getZ(), 4.0f, Level.ExplosionInteraction.TNT);
        discard();
    }

    // ===== Efectos visuales (solo en la pantalla) =====

    private void spawnEngineParticles(LaunchState state) {
        double x = getX();
        double y = getY();
        double z = getZ();
        switch (state) {
            case COUNTDOWN -> {
                if (getTimer() <= IGNITION_TICKS) {
                    for (int i = 0; i < 3; i++) {
                        level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + rand(0.8), y + 0.1, z + rand(0.8),
                                rand(0.15), 0.02, rand(0.15));
                    }
                    level().addParticle(ParticleTypes.FLAME, x + rand(0.2), y, z + rand(0.2), 0.0, -0.1, 0.0);
                }
            }
            case ASCENDING -> {
                for (int i = 0; i < 4; i++) {
                    level().addParticle(ParticleTypes.FLAME, x + rand(0.25), y - 0.1, z + rand(0.25),
                            rand(0.05), -0.6, rand(0.05));
                }
                for (int i = 0; i < 2; i++) {
                    level().addParticle(ParticleTypes.LARGE_SMOKE, x + rand(0.4), y - 0.6, z + rand(0.4),
                            rand(0.08), -0.15, rand(0.08));
                }
                if (getTimer() < 40) {
                    // Nube de humo en la plataforma al despegar
                    for (int i = 0; i < 6; i++) {
                        level().addParticle(ParticleTypes.CLOUD, x + rand(2.5), y - 0.5, z + rand(2.5),
                                rand(0.3), 0.02, rand(0.3));
                    }
                }
            }
            case DESCENDING -> {
                if (getY() - level().getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ())
                        < LANDING_BURN_HEIGHT) {
                    level().addParticle(ParticleTypes.FLAME, x + rand(0.2), y - 0.1, z + rand(0.2), 0.0, -0.3, 0.0);
                    level().addParticle(ParticleTypes.SMOKE, x + rand(0.3), y - 0.3, z + rand(0.3), 0.0, -0.1, 0.0);
                }
            }
            case FALLING -> level().addParticle(ParticleTypes.SMOKE, x + rand(0.3), y + 1.5, z + rand(0.3), 0.0, 0.1, 0.0);
            default -> {
            }
        }
    }

    private double rand(double spread) {
        return (random.nextDouble() - 0.5) * 2.0 * spread;
    }

    // ===== Interacción (Fase 9) =====

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (getLaunchState() != LaunchState.IDLE) {
            return InteractionResult.PASS; // durante el lanzamiento no se toca
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

    /** Solo un jugador en creativo puede romperlo a golpes (y solo si está en tierra). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && getLaunchState() == LaunchState.IDLE
                && source.getEntity() instanceof Player player && player.isCreative()) {
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
        // Si se guardó la partida en pleno vuelo, al volver aterriza con seguridad
        LaunchState saved = LaunchState.byId(tag.getInt("State"));
        setLaunchState(saved.isFlying() ? LaunchState.DESCENDING : LaunchState.IDLE);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Tier", getTier().ordinal());
        tag.putInt("Fuel", getFuel());
        tag.putInt("State", getLaunchState().ordinal());
    }
}
