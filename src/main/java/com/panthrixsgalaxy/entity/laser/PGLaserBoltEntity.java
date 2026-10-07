package com.panthrixsgalaxy.entity.laser;

import com.panthrixsgalaxy.init.ModDamageTypes;
import com.panthrixsgalaxy.init.ModEntities;
import com.panthrixsgalaxy.weapon.LaserTier;
import com.panthrixsgalaxy.weapon.PGLaserSwordItem;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

/**
 * El rayo que dispara un arma láser. Es un "proyectil" como una flecha, pero:
 *   - no cae (no le afecta la gravedad) y va muy rápido;
 *   - desaparece al cabo de un segundo (alcance limitado);
 *   - el rifle atraviesa varios enemigos;
 *   - no rompe bloques: al chocar solo saltan chispas.
 */
public class PGLaserBoltEntity extends Projectile {

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(PGLaserBoltEntity.class, EntityDataSerializers.INT);

    private float damage = 5.0f;
    private int lifetime = 20;
    private int pierceLeft;
    private double knockback;
    /** Enemigos ya alcanzados (para no dañar dos veces al mismo al atravesarlo). */
    private final Set<Integer> hitEntities = new HashSet<>();

    public PGLaserBoltEntity(EntityType<? extends PGLaserBoltEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    /** Rayo nuevo disparado por alguien (jugador o, más adelante, criaturas). */
    public PGLaserBoltEntity(Level level, LivingEntity shooter, LaserTier tier) {
        this(ModEntities.LASER_BOLT.get(), level);
        setOwner(shooter);
        setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.damage = tier.getDamage();
        this.lifetime = tier.getLifetime();
        this.pierceLeft = tier.getPierce();
        this.knockback = tier.getKnockback();
        entityData.set(COLOR, tier.getColor());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(COLOR, 0xFF3A3A);
    }

    /** Color del rayo (lo usa el dibujo). */
    public int getColor() {
        return entityData.get(COLOR);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > lifetime) {
            discard();
            return;
        }
        // ¿Choca con algo en el camino de este tick?
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !level().isClientSide) {
            onHit(hit);
        }
        if (isRemoved()) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        updateRotation();
    }

    /** No vuelve a dañar al mismo enemigo, ni a la nave en la que va quien dispara. */
    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = getOwner();
        if (owner != null && owner.isPassenger() && owner.getRootVehicle() == target.getRootVehicle()) {
            return false;
        }
        return super.canHitEntity(target) && !hitEntities.contains(target.getId());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        // Espada láser en guardia mirando hacia el rayo: ¡lo devuelve! (Fase 16)
        if (target instanceof LivingEntity guard && PGLaserSwordItem.isGuarding(guard)
                && PGLaserSwordItem.isFacing(guard, position()) && PGLaserSwordItem.payGuard(guard)) {
            deflect(guard);
            return;
        }
        hitEntities.add(target.getId());
        Entity owner = getOwner();
        if (target instanceof LivingEntity living) {
            living.invulnerableTime = 0; // los disparos rápidos no se "pierden"
        }
        boolean hurt = target.hurt(ModDamageTypes.laser(level(), this, owner), damage);
        if (hurt && knockback > 0.0 && target instanceof LivingEntity living) {
            Vec3 push = getDeltaMovement().normalize();
            living.knockback(knockback, -push.x, -push.z);
        }
        sparks(result.getLocation(), 6);
        if (pierceLeft-- <= 0) {
            discard();
        }
    }

    /** Da la vuelta al rayo: ahora es de quien lo ha desviado y puede dar al que disparó. */
    private void deflect(LivingEntity guard) {
        setDeltaMovement(getDeltaMovement().scale(-1.0));
        setOwner(guard);
        hitEntities.clear();
        hitEntities.add(guard.getId());
        tickCount = 0;
        sparks(position(), 8);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        sparks(result.getLocation(), 10);
        level().playSound(null, result.getBlockPos(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 0.6f, 1.8f);
        discard();
    }

    /** Chispas del color del rayo. */
    private void sparks(Vec3 at, int count) {
        if (level() instanceof ServerLevel server) {
            int color = getColor();
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(
                    ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f), 0.8f);
            server.sendParticles(dust, at.x, at.y, at.z, count, 0.15, 0.15, 0.15, 0.0);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, count / 2, 0.1, 0.1, 0.1, 0.2);
        }
    }

    /** Los rayos se ven desde lejos. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0 * 128.0;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    // Los rayos duran 1 segundo: no hace falta guardarlos al salir del mundo.
    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
