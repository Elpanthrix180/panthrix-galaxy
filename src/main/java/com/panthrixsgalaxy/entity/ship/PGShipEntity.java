package com.panthrixsgalaxy.entity.ship;

import com.panthrixsgalaxy.config.PGConfig;
import com.panthrixsgalaxy.entity.PGSpaceVehicle;
import com.panthrixsgalaxy.init.ModEntities;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.item.PGFuelCanisterItem;
import com.panthrixsgalaxy.item.PGShipItem;
import com.panthrixsgalaxy.menu.PGShipMenu;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import com.panthrixsgalaxy.system.gravity.PGGravity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Nave espacial: un vehículo que se pilota libremente, en un planeta y en el Espacio.
 *
 * Datos de la nave:
 *   - Combustible: lo gastan los motores (empujar y mantenerse en el aire).
 *   - Energía: soporte vital. Con energía la cabina tiene aire; sin ella necesitas el traje.
 *     Se recarga con baterías o, con los motores apagados, con su panel solar.
 *   - Integridad: los choques y aterrizajes bruscos la dañan. A 0, la nave explota.
 *   - Bodega: 27 o 54 huecos de carga (se abre con Mayús + clic derecho).
 *
 * Controles (dentro):
 *   ESPACIO = encender / apagar motores   W = empujar hacia donde miras   S = frenar
 *   Con motores encendidos y sin pulsar nada, la nave flota. Apagados, cae con la gravedad.
 *
 * Viajes: al subir por encima de la altura de salida del planeta pasa al Espacio; en el Espacio,
 * al bajar de Y 40 vuelve a la Tierra y al acercarse a un planeta entra en él (si tiene alcance).
 */
public class PGShipEntity extends Entity implements PGSpaceVehicle {

    /** Velocidad a partir de la cual un choque daña el casco. */
    private static final double SAFE_IMPACT_SPEED = 0.45;
    /** Energía que gasta el soporte vital por tick con los motores encendidos. */
    private static final int LIFE_SUPPORT_PER_TICK = 1;
    /** Energía que recoge el panel solar de la nave por tick (motores apagados, con sol). */
    private static final int SOLAR_PER_TICK = 5;
    private static final int REBOARD_DELAY = 5;
    /** Huecos máximos de la bodega (la nave avanzada usa todos). */
    private static final int MAX_CARGO = 54;

    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(PGShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FUEL =
            SynchedEntityData.defineId(PGShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENERGY =
            SynchedEntityData.defineId(PGShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTEGRITY =
            SynchedEntityData.defineId(PGShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENGINES =
            SynchedEntityData.defineId(PGShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> SPACE_ORIGIN =
            SynchedEntityData.defineId(PGShipEntity.class, EntityDataSerializers.BLOCK_POS);

    private final SimpleContainer cargo = new SimpleContainer(MAX_CARGO);
    @Nullable
    private UUID pendingPilot;
    private int reboardTicks;
    /** Combustible "a medias" pendiente de gastar (por el multiplicador de la configuración). */
    private double fuelDebt;
    @Nullable
    private PGPlanet lastWarnedPlanet;

    public PGShipEntity(EntityType<? extends PGShipEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TIER, ShipTier.SHIP.ordinal());
        entityData.define(FUEL, 0);
        entityData.define(ENERGY, 0);
        entityData.define(INTEGRITY, ShipTier.SHIP.getMaxIntegrity());
        entityData.define(ENGINES, false);
        entityData.define(SPACE_ORIGIN, BlockPos.ZERO);
    }

    // ===== Datos =====

    public ShipTier getTier() {
        return ShipTier.byId(entityData.get(TIER));
    }

    public void setTier(ShipTier tier) {
        entityData.set(TIER, tier.ordinal());
    }

    public int getFuel() {
        return entityData.get(FUEL);
    }

    public void setFuel(int fuel) {
        entityData.set(FUEL, Math.max(0, Math.min(fuel, getTier().getFuelCapacity())));
    }

    public int getEnergy() {
        return entityData.get(ENERGY);
    }

    public void setEnergy(int energy) {
        entityData.set(ENERGY, Math.max(0, Math.min(energy, getTier().getEnergyCapacity())));
    }

    public int getIntegrity() {
        return entityData.get(INTEGRITY);
    }

    public void setIntegrity(int integrity) {
        entityData.set(INTEGRITY, Math.max(0, Math.min(integrity, getTier().getMaxIntegrity())));
    }

    public boolean areEnginesOn() {
        return entityData.get(ENGINES);
    }

    private void setEngines(boolean on) {
        entityData.set(ENGINES, on);
    }

    public SimpleContainer getCargo() {
        return cargo;
    }

    public Vec3 getPlanetPosition(PGPlanet planet) {
        return Vec3.atCenterOf(getSpaceOrigin()).add(planet.spaceOffset());
    }

    public boolean isInSpace() {
        return level().dimension().equals(PGPlanets.SPACE);
    }

    // ===== PGSpaceVehicle =====

    @Override
    public BlockPos getSpaceOrigin() {
        return entityData.get(SPACE_ORIGIN);
    }

    /** La cabina solo tiene aire si queda energía para el soporte vital. */
    @Override
    public boolean isPressurized() {
        return getEnergy() > 0;
    }

    /** En vuelo = en el Espacio, o en el aire con los motores encendidos. */
    @Override
    public boolean isInFlight() {
        return isInSpace() || (areEnginesOn() && !onGround());
    }

    // ===== Motores (tecla ESPACIO) =====

    public void toggleEngines(Player pilot) {
        if (!areEnginesOn() && getFuel() <= 0) {
            message(pilot, "message.panthrixsgalaxy.ship_no_fuel", ChatFormatting.RED);
            return;
        }
        setEngines(!areEnginesOn());
        level().playSound(null, blockPosition(), areEnginesOn() ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.NEUTRAL, 1.0f, 1.2f);
        message(pilot, areEnginesOn() ? "message.panthrixsgalaxy.ship_engines_on" : "message.panthrixsgalaxy.ship_engines_off",
                ChatFormatting.AQUA);
    }

    private void message(Player player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    private void messagePilot(String key, ChatFormatting color) {
        if (getFirstPassenger() instanceof Player pilot) {
            pilot.displayClientMessage(Component.translatable(key).withStyle(color), false);
        }
    }

    // ===== Cada tick =====

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            move(MoverType.SELF, getDeltaMovement()); // la pantalla adelanta el movimiento
            if (areEnginesOn()) {
                spawnEngineParticles();
            }
            return;
        }
        if (pendingPilot != null) {
            tickReboard();
        }
        tickFlight();
    }

    private void tickFlight() {
        boolean inSpace = isInSpace();
        double gravity = inSpace ? 0.0 : PGGravity.getGravity(level());
        ShipTier tier = getTier();
        Player pilot = getFirstPassenger() instanceof Player player ? player : null;
        Vec3 velocity = getDeltaMovement();

        if (areEnginesOn() && getFuel() <= 0) {
            setEngines(false);
            messagePilot("message.panthrixsgalaxy.ship_out_of_fuel", ChatFormatting.RED);
        }
        // Si el piloto se baja en tierra, los motores se apagan solos (no gastan combustible)
        if (areEnginesOn() && pilot == null && pendingPilot == null && onGround()) {
            setEngines(false);
        }

        if (areEnginesOn()) {
            velocity = velocity.scale(inSpace ? 0.995 : 0.9); // rozamiento (casi nada en el Espacio)
            if (pilot != null) {
                setYRot(pilot.getYRot());
                if (pilot.zza > 0.0f) {
                    velocity = velocity.add(pilot.getLookAngle().scale(tier.getAcceleration()));
                    burnFuel(1.0);
                } else if (pilot.zza < 0.0f) {
                    velocity = velocity.scale(0.8);
                }
            }
            if (!inSpace && tickCount % 2 == 0) {
                burnFuel(1.0); // mantenerse en el aire cuesta combustible
            }
            setEnergy(getEnergy() - LIFE_SUPPORT_PER_TICK);
        } else {
            velocity = velocity.add(0.0, -0.08 * gravity, 0.0);
            if (inSpace) {
                velocity = velocity.scale(0.999).add(0.0, getFuel() <= 0 ? -0.01 : 0.0, 0.0);
            } else {
                velocity = velocity.multiply(onGround() ? 0.5 : 0.98, 0.98, onGround() ? 0.5 : 0.98);
            }
            // Panel solar de la nave
            if (inSpace || level().canSeeSky(blockPosition().above())) {
                setEnergy(getEnergy() + SOLAR_PER_TICK);
            }
        }

        double maxSpeed = inSpace ? tier.getSpaceSpeed() : tier.getAtmosphereSpeed();
        if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }
        if (inSpace && getY() >= PGPlanets.SPACE_CEILING_Y && velocity.y > 0.0) {
            velocity = new Vec3(velocity.x, 0.0, velocity.z);
        }
        double speedBefore = velocity.length();
        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);

        // Choques
        if ((horizontalCollision || verticalCollision) && speedBefore > SAFE_IMPACT_SPEED) {
            damageHull((int) Math.ceil((speedBefore - SAFE_IMPACT_SPEED) * 40.0));
            if (isRemoved()) {
                return;
            }
        }
        getPassengers().forEach(passenger -> passenger.fallDistance = 0.0f);
        checkTravel(inSpace);
    }

    /** Gasta combustible (multiplicado por la configuración; los decimales se van acumulando). */
    private void burnFuel(double amount) {
        fuelDebt += amount * PGConfig.shipFuelMultiplier.get();
        int whole = (int) fuelDebt;
        if (whole > 0) {
            fuelDebt -= whole;
            setFuel(getFuel() - whole);
        }
    }

    private void damageHull(int amount) {
        setIntegrity(getIntegrity() - amount);
        level().playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 0.8f, 0.8f);
        if (getIntegrity() <= 0) {
            explode();
        } else if (getIntegrity() < getTier().getMaxIntegrity() / 4) {
            messagePilot("message.panthrixsgalaxy.ship_hull_critical", ChatFormatting.RED);
        }
    }

    private void explode() {
        setEngines(false);
        ejectPassengers();
        Containers.dropContents(level(), blockPosition(), cargo);
        level().explode(this, getX(), getY() + 0.5, getZ(), 3.5f, Level.ExplosionInteraction.TNT);
        discard();
    }

    private void spawnEngineParticles() {
        Vec3 back = Vec3.directionFromRotation(0.0f, getYRot()).scale(-1.4);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 sideways = Vec3.directionFromRotation(0.0f, getYRot() + 90.0f).scale(0.4 * side);
            double x = getX() + back.x + sideways.x;
            double z = getZ() + back.z + sideways.z;
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, getY() + 0.45, z, back.x * 0.05, 0.0, back.z * 0.05);
        }
        if (!isInSpace() && !onGround() && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.CLOUD, getX(), getY() - 0.1, getZ(), 0.0, -0.15, 0.0);
        }
    }

    // ===== Viajes entre dimensiones =====

    private void checkTravel(boolean inSpace) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!inSpace) {
            PGPlanet planet = PGPlanets.fromDimension(level().dimension());
            if (planet == null || getY() < planet.exitHeight()) {
                return;
            }
            ServerLevel space = serverLevel.getServer().getLevel(PGPlanets.SPACE);
            if (space == null) {
                return;
            }
            if (planet == PGPlanets.EARTH) {
                BlockPos arrival = new BlockPos(getBlockX(), PGPlanets.SPACE_ARRIVAL_Y, getBlockZ());
                PGShipEntity copy = travelTo(space, Vec3.atBottomCenterOf(arrival));
                copy.entityData.set(SPACE_ORIGIN, arrival);
            } else {
                travelTo(space, getPlanetPosition(planet).add(0.0, 0.0, planet.entryDistance() + 40.0));
            }
            return;
        }
        // En el Espacio: ¿volvemos a la Tierra?
        if (getY() < PGPlanets.EARTH_REENTRY_Y) {
            enterPlanet(serverLevel, PGPlanets.EARTH);
            return;
        }
        for (PGPlanet planet : PGPlanets.ALL) {
            if (planet != PGPlanets.EARTH && position().distanceTo(getPlanetPosition(planet)) < planet.entryDistance()) {
                if (getTier().getReach() < planet.requiredReach() || !planet.isLandable()) {
                    Vec3 away = position().subtract(getPlanetPosition(planet)).normalize();
                    setDeltaMovement(away.scale(0.3));
                    if (lastWarnedPlanet != planet && getFirstPassenger() instanceof Player pilot) {
                        pilot.displayClientMessage(Component.translatable(planet.isLandable()
                                        ? "message.panthrixsgalaxy.ship_out_of_reach" : "message.panthrixsgalaxy.planet_not_landable",
                                Component.translatable(planet.getTranslationKey())).withStyle(ChatFormatting.YELLOW), false);
                    }
                    lastWarnedPlanet = planet;
                } else {
                    enterPlanet(serverLevel, planet);
                }
                return;
            }
        }
        lastWarnedPlanet = null;
    }

    private void enterPlanet(ServerLevel serverLevel, PGPlanet planet) {
        if (planet.dimension() == null) {
            return;
        }
        ServerLevel target = serverLevel.getServer().getLevel(planet.dimension());
        if (target != null) {
            PGShipEntity copy = travelTo(target, new Vec3(getX(), planet.exitHeight() - 30.0, getZ()));
            copy.setEngines(true); // llega flotando: el piloto aterriza donde quiera
        }
    }

    /** Cambia la nave (con su piloto y su carga) a otra dimensión en pleno vuelo. */
    private PGShipEntity travelTo(ServerLevel target, Vec3 position) {
        ServerPlayer pilot = getFirstPassenger() instanceof ServerPlayer player ? player : null;
        boolean engines = areEnginesOn();
        setEngines(false);
        if (pilot != null) {
            pilot.stopRiding();
        }
        target.getChunk(SectionPos.blockToSectionCoord(position.x), SectionPos.blockToSectionCoord(position.z));

        PGShipEntity copy = ModEntities.SHIP.get().create(target);
        copy.setTier(getTier());
        copy.setFuel(getFuel());
        copy.setEnergy(getEnergy());
        copy.setIntegrity(getIntegrity());
        copy.setEngines(engines);
        copy.entityData.set(SPACE_ORIGIN, getSpaceOrigin());
        for (int i = 0; i < cargo.getContainerSize(); i++) {
            copy.cargo.setItem(i, cargo.getItem(i).copy());
        }
        cargo.clearContent();
        copy.moveTo(position.x, position.y, position.z, getYRot(), 0.0f);
        copy.setDeltaMovement(getDeltaMovement().scale(0.3));
        target.addFreshEntity(copy);

        if (pilot != null) {
            pilot.teleportTo(target, position.x, position.y, position.z, pilot.getYRot(), pilot.getXRot());
            pilot.fallDistance = 0.0f;
            copy.pendingPilot = pilot.getUUID();
            copy.reboardTicks = REBOARD_DELAY;
        }
        discard();
        return copy;
    }

    private void tickReboard() {
        if (!(level() instanceof ServerLevel serverLevel)
                || !(serverLevel.getPlayerByUUID(pendingPilot) instanceof ServerPlayer pilot)) {
            if (--reboardTicks < -100) {
                pendingPilot = null;
            }
            return;
        }
        pilot.teleportTo(getX(), getY(), getZ());
        pilot.fallDistance = 0.0f;
        if (--reboardTicks <= 0) {
            pilot.startRiding(this, true);
            pendingPilot = null;
        }
    }

    // ===== Interacción =====

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (isInFlight()) {
            return InteractionResult.PASS;
        }
        // Combustible
        if (held.getItem() instanceof PGFuelCanisterItem) {
            int moved = Math.min(PGFuelCanisterItem.getFuel(held), getTier().getFuelCapacity() - getFuel());
            PGFuelCanisterItem.setFuel(held, PGFuelCanisterItem.getFuel(held) - moved);
            setFuel(getFuel() + moved);
            showStatus(player);
            return InteractionResult.CONSUME;
        }
        // Reparar el casco
        int repair = held.is(ModItems.PG_ASTEROID_METAL_INGOT.get()) ? 30 : held.is(ModItems.PG_REINFORCED_PLATE.get()) ? 10 : 0;
        if (repair > 0) {
            if (getIntegrity() < getTier().getMaxIntegrity()) {
                setIntegrity(getIntegrity() + repair);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level().playSound(null, blockPosition(), SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 0.7f, 1.3f);
            }
            showStatus(player);
            return InteractionResult.CONSUME;
        }
        // Batería (u otro objeto con energía): descargarla en la nave
        IEnergyStorage itemEnergy = held.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (itemEnergy != null) {
            int space = getTier().getEnergyCapacity() - getEnergy();
            int moved = itemEnergy.extractEnergy(space, false);
            setEnergy(getEnergy() + moved);
            showStatus(player);
            return InteractionResult.CONSUME;
        }
        // Mayús + mano vacía: panel de control y bodega
        if (player.isShiftKeyDown() && held.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            int rows = getTier().getCargoRows();
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                            (id, inventory, p) -> new PGShipMenu(id, inventory, this, rows),
                            Component.translatable("container.panthrixsgalaxy.ship_" + getTier().getSerializedName())),
                    buf -> buf.writeVarInt(rows));
            return InteractionResult.CONSUME;
        }
        // Subirse (la mochila pasa su combustible a la nave)
        if (!isVehicle()) {
            ItemStack backpack = PGBackpackSlot.getEquipped(player);
            if (backpack.getItem() instanceof PGBackpackItem) {
                int inBackpack = PGBackpackItem.getTank(backpack, PGBackpackItem.Tank.FUEL);
                int moved = Math.min(inBackpack, getTier().getFuelCapacity() - getFuel());
                PGBackpackItem.setTank(backpack, PGBackpackItem.Tank.FUEL, inBackpack - moved);
                setFuel(getFuel() + moved);
            }
            player.startRiding(this);
            message(player, "message.panthrixsgalaxy.ship_boarded", ChatFormatting.AQUA);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private void showStatus(Player player) {
        player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.ship_status",
                getFuel(), getTier().getFuelCapacity(), getEnergy(), getTier().getEnergyCapacity(),
                getIntegrity(), getTier().getMaxIntegrity()).withStyle(ChatFormatting.AQUA), true);
    }

    /** Recoger la nave como objeto (con su combustible, energía, integridad y carga). Lo pide el panel de control. */
    public void pickUp(Player player) {
        if (isVehicle() || isInFlight() || isRemoved()) {
            return;
        }
        ItemStack item = new ItemStack(getTier().getItem());
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt("Fuel", getFuel());
        tag.putInt("Energy", getEnergy());
        tag.putInt("Integrity", getIntegrity());
        NonNullList<ItemStack> items = NonNullList.withSize(MAX_CARGO, ItemStack.EMPTY);
        for (int i = 0; i < MAX_CARGO; i++) {
            items.set(i, cargo.getItem(i));
        }
        tag.put("Cargo", ContainerHelper.saveAllItems(new CompoundTag(), items));
        cargo.clearContent();
        if (!player.getInventory().add(item)) {
            spawnAtLocation(item);
        }
        discard();
    }

    /** Lee los datos guardados en el objeto al colocar la nave. */
    public void loadFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            setIntegrity(getTier().getMaxIntegrity());
            return;
        }
        setFuel(tag.getInt("Fuel"));
        setEnergy(tag.getInt("Energy"));
        setIntegrity(tag.contains("Integrity") ? tag.getInt("Integrity") : getTier().getMaxIntegrity());
        NonNullList<ItemStack> items = NonNullList.withSize(MAX_CARGO, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Cargo"), items);
        for (int i = 0; i < MAX_CARGO; i++) {
            cargo.setItem(i, items.get(i));
        }
    }

    /** Los ataques dañan el casco. Un jugador en creativo la quita de un golpe. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) {
            return false;
        }
        if (source.getEntity() instanceof Player player && player.isCreative()) {
            Containers.dropContents(level(), blockPosition(), cargo);
            discard();
            return true;
        }
        damageHull(Math.max(1, Math.round(amount)));
        return true;
    }

    // ===== Pasajero y física =====

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.35;
    }

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
        setTier(ShipTier.byId(tag.getInt("Tier")));
        setFuel(tag.getInt("Fuel"));
        setEnergy(tag.getInt("Energy"));
        setIntegrity(tag.contains("Integrity") ? tag.getInt("Integrity") : getTier().getMaxIntegrity());
        setEngines(tag.getBoolean("Engines"));
        entityData.set(SPACE_ORIGIN, NbtUtils.readBlockPos(tag.getCompound("SpaceOrigin")));
        NonNullList<ItemStack> items = NonNullList.withSize(MAX_CARGO, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Cargo"), items);
        for (int i = 0; i < MAX_CARGO; i++) {
            cargo.setItem(i, items.get(i));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Tier", getTier().ordinal());
        tag.putInt("Fuel", getFuel());
        tag.putInt("Energy", getEnergy());
        tag.putInt("Integrity", getIntegrity());
        tag.putBoolean("Engines", areEnginesOn());
        tag.put("SpaceOrigin", NbtUtils.writeBlockPos(getSpaceOrigin()));
        NonNullList<ItemStack> items = NonNullList.withSize(MAX_CARGO, ItemStack.EMPTY);
        for (int i = 0; i < MAX_CARGO; i++) {
            items.set(i, cargo.getItem(i));
        }
        tag.put("Cargo", ContainerHelper.saveAllItems(new CompoundTag(), items));
    }

    /** Para que el objeto sepa qué nave crear. */
    public static PGShipEntity create(Level level, PGShipItem item) {
        PGShipEntity ship = ModEntities.SHIP.get().create(level);
        ship.setTier(item.getTier());
        return ship;
    }
}
