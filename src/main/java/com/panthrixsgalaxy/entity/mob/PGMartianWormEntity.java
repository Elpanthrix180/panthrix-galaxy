package com.panthrixsgalaxy.entity.mob;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Gusano marciano: vive ENTERRADO. Cuando te acercas (menos de 10 bloques) sale del suelo
 * de golpe, con un temblor y una nube de tierra, y te muerde muy fuerte. Si te alejas
 * durante 10 segundos vuelve a enterrarse.
 *
 * Mientras está enterrado no se ve, no se mueve y no se le puede golpear.
 */
public class PGMartianWormEntity extends Monster {

    private static final EntityDataAccessor<Boolean> EMERGED =
            SynchedEntityData.defineId(PGMartianWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double EMERGE_DISTANCE = 10.0;
    private static final int BURROW_AFTER_TICKS = 200;

    private int calmTicks;
    /** Solo en la pantalla: cuánto ha salido (0 = enterrado, 1 = fuera). Lo usa el dibujo. */
    private float emergeProgress;
    private float emergeProgressOld;

    public PGMartianWormEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EMERGED, false);
    }

    public boolean isEmerged() {
        return entityData.get(EMERGED);
    }

    public float getEmergeProgress(float partialTick) {
        return emergeProgressOld + (emergeProgress - emergeProgressOld) * partialTick;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return isEmerged() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return isEmerged() && super.canContinueToUse();
            }
        });
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            emergeProgressOld = emergeProgress;
            emergeProgress = isEmerged() ? Math.min(1.0f, emergeProgress + 0.12f) : Math.max(0.0f, emergeProgress - 0.06f);
            return;
        }
        LivingEntity target = getTarget();
        if (!isEmerged()) {
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO.add(0.0, getDeltaMovement().y, 0.0));
            if (target != null && target.isAlive() && distanceTo(target) < EMERGE_DISTANCE) {
                setEmerged(true);
            }
            return;
        }
        boolean close = target != null && target.isAlive() && distanceTo(target) < EMERGE_DISTANCE * 1.6;
        calmTicks = close ? 0 : calmTicks + 1;
        if (calmTicks > BURROW_AFTER_TICKS) {
            setEmerged(false);
        }
    }

    private void setEmerged(boolean emerged) {
        entityData.set(EMERGED, emerged);
        calmTicks = 0;
        playSound(emerged ? SoundEvents.RAVAGER_ROAR : SoundEvents.ROOTED_DIRT_BREAK, 1.0f, emerged ? 1.6f : 0.7f);
        if (level() instanceof ServerLevel server) {
            BlockState ground = level().getBlockState(blockPosition().below());
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.2, getZ(),
                    40, 0.6, 0.3, 0.6, 0.15);
        }
    }

    /** Enterrado no se le puede hacer daño (salvo con /kill o el vacío). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isEmerged() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return isEmerged() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return isEmerged() && super.isPushable();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Emerged", isEmerged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(EMERGED, tag.getBoolean("Emerged"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isEmerged() ? SoundEvents.SILVERFISH_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.4f;
    }

    /** La luz ya la comprueba PGSpawnRules (sol sí, antorchas no). Sin esto, de día no aparecerían. */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return true;
    }
}
