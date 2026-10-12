package com.transporteiberico.entity;

import com.transporteiberico.registry.TIRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Autobús Urbano Azul.
 *
 * Cómo funciona la conducción (igual que las barcas de Minecraft):
 *  - Quien lo conduce es el jugador sentado. Su ordenador (el "cliente") lee W/A/S/D
 *    (los valores zza/xxa que Minecraft ya calcula) y mueve el autobús.
 *  - Minecraft envía solo esa posición al servidor (paquete de "mover vehículo"), y el servidor
 *    la comprueba con sus reglas anti-trampas. No hacen falta teclas ni paquetes propios.
 *  - Sin conductor, el servidor se encarga del autobús (gravedad y frenado).
 */
public class BusEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(BusEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_HURT = SynchedEntityData.defineId(BusEntity.class, EntityDataSerializers.INT);

    public static final float MAX_SPEED = 0.55F;     // bloques por tick (unos 40 km/h de Minecraft)
    public static final float MAX_REVERSE = 0.15F;
    public static final float ACCELERATION = 0.015F;
    public static final float BRAKE = 0.04F;
    public static final float ROLLING = 0.97F;       // frenado suave al soltar W
    public static final float TURN_SPEED = 3.0F;     // grados por tick a velocidad normal

    /** Velocidad hacia delante (negativa = marcha atrás). */
    private float speed;

    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;

    public BusEntity(EntityType<? extends BusEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.setMaxUpStep(1.0F);   // sube escalones de 1 bloque
    }

    public float getSpeed() {
        return speed;
    }

    // ===================== Datos y guardado =====================

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DAMAGE, 0.0F);
        this.entityData.define(DATA_HURT, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // El autobús se guarda con su posición y orientación (lo hace Minecraft). Al cargar, está parado.
        this.speed = 0.0F;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_HURT);
    }

    // ===================== Colisiones =====================

    @Override
    public boolean canBeCollidedWith() {
        return true;   // se puede chocar con él y caminar por el techo
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return (other.canBeCollidedWith() || other.isPushable()) && !this.isPassengerOfSameVehicle(other);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    // ===================== Subir y bajar =====================

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();   // de momento, solo el conductor
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.0D;
    }

    private Vec3 forward() {
        float rad = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(rad), 0.0D, Mth.cos(rad));
    }

    private Vec3 left() {
        float rad = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(Mth.cos(rad), 0.0D, Mth.sin(rad));
    }

    /** El conductor se sienta delante, a la izquierda (como en España). Gira con el autobús. */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        Vec3 seat = this.position().add(this.forward().scale(1.9D)).add(this.left().scale(0.7D));
        double y = this.getY() + this.getPassengersRidingOffset() + passenger.getMyRidingOffset();
        move.accept(passenger, seat.x, y, seat.z);
        float turned = this.getYRot() - this.yRotO;
        if (turned != 0.0F) {
            passenger.setYRot(passenger.getYRot() + turned);
            passenger.setYHeadRot(passenger.getYHeadRot() + turned);
        }
    }

    /** Al bajar (Mayús), el jugador sale por el lado de las puertas o por el otro; si no cabe, sobre el autobús. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3[] tries = {
                this.position().add(this.left().scale(-2.3D)),           // lado derecho (puertas)
                this.position().add(this.left().scale(2.3D)),            // lado izquierdo
                this.position().add(this.forward().scale(-3.6D)),        // detrás
                this.position().add(this.forward().scale(3.6D))          // delante
        };
        for (Vec3 t : tries) {
            BlockPos pos = BlockPos.containing(t.x, this.getY(), t.z);
            for (int dy = 0; dy <= 2; dy++) {
                BlockPos p = pos.above(dy - 1);
                double floor = this.level().getBlockFloorHeight(p);
                if (DismountHelper.isBlockFloorValid(floor)) {
                    Vec3 spot = Vec3.upFromBottomCenterOf(p, floor);
                    for (Pose pose : passenger.getDismountPoses()) {
                        AABB box = passenger.getLocalBoundsForPose(pose).move(spot);
                        if (DismountHelper.canDismountTo(this.level(), passenger, box)) {
                            passenger.setPose(pose);
                            return spot;
                        }
                    }
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    // ===================== Daño (golpear para recogerlo) =====================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }
        this.entityData.set(DATA_HURT, 10);
        this.entityData.set(DATA_DAMAGE, this.getDamage() + amount * 10.0F);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
        boolean creative = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
        if (creative || this.getDamage() > 120.0F) {
            if (!creative && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                this.spawnAtLocation(this.getPickResult());
            }
            this.ejectPassengers();
            this.discard();
        }
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TIRegistry.BUS_ITEM.get());
    }

    // ===================== Movimiento =====================

    @Override
    public void tick() {
        if (this.getHurtTime() > 0) {
            this.entityData.set(DATA_HURT, this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0F) {
            this.entityData.set(DATA_DAMAGE, Math.max(0.0F, this.getDamage() - 1.0F));
        }
        super.tick();
        this.tickLerp();
        if (this.isControlledByLocalInstance()) {
            LivingEntity driver = this.getControllingPassenger();
            if (driver != null) {
                // zza: W = +1, S = -1 · xxa: A = +1, D = -1 (Minecraft ya lo calcula con las teclas de moverse)
                this.simulate(driver.zza, driver.xxa);
            } else {
                this.simulate(0.0F, 0.0F);
            }
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.checkInsideBlocks();
    }

    /**
     * Un paso de conducción: acelera, frena, gira, aplica la gravedad y mueve el autobús
     * respetando los bloques (Minecraft calcula las colisiones y sube escalones de 1 bloque).
     */
    public void simulate(float forwardInput, float strafeInput) {
        if (forwardInput > 0.0F) {
            this.speed = Math.min(MAX_SPEED, this.speed + ACCELERATION);
        } else if (forwardInput < 0.0F) {
            if (this.speed > 0.0F) {
                this.speed = Math.max(0.0F, this.speed - BRAKE);            // S frena...
            } else {
                this.speed = Math.max(-MAX_REVERSE, this.speed - ACCELERATION * 0.6F);  // ...y luego marcha atrás
            }
        } else {
            this.speed *= ROLLING;
            if (Math.abs(this.speed) < 0.003F) {
                this.speed = 0.0F;
            }
        }
        if (strafeInput != 0.0F && Math.abs(this.speed) > 0.01F) {
            float grip = Mth.clamp(Math.abs(this.speed) / 0.2F, 0.35F, 1.0F);
            float dir = this.speed > 0.0F ? 1.0F : -1.0F;
            this.setYRot(this.getYRot() - Math.signum(strafeInput) * TURN_SPEED * grip * dir);
        }
        if (this.isInWater()) {
            this.speed *= 0.85F;   // el agua frena mucho
        }
        Vec3 horizontal = this.forward().scale(this.speed);
        double vy = this.getDeltaMovement().y;
        vy = this.onGround() ? -0.04D : Math.max(-2.0D, (vy - 0.08D) * 0.98D);
        this.setDeltaMovement(horizontal.x, vy, horizontal.z);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.horizontalCollision) {
            this.speed *= 0.3F;   // chocar contra una pared lo frena
        }
    }

    // ===================== Suavizado en pantalla para los demás =====================

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpSteps = 10;
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps > 0) {
            double x = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            this.setYRot(this.getYRot() + (float) Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps);
            --this.lerpSteps;
            this.setPos(x, y, z);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }
}
