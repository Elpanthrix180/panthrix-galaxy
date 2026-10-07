package com.panthrixsgalaxy.entity.mob;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Depredador alienígena: un cazador alto y peligroso con CAMUFLAJE.
 * Mientras te acecha (lejos de ti) es INVISIBLE. Cuando está a menos de 6 bloques
 * se hace visible de golpe, ruge y ataca con mucha fuerza.
 */
public class PGAlienPredatorEntity extends Monster {

    private static final double REVEAL_DISTANCE = 6.0;
    private boolean revealed;

    public PGAlienPredatorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Camuflaje: invisible mientras está lejos de su presa. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        boolean close = target != null && distanceTo(target) < REVEAL_DISTANCE;
        boolean recentlyHurt = hurtTime > 0;
        if (close || recentlyHurt) {
            if (!revealed) {
                revealed = true;
                removeEffect(MobEffects.INVISIBILITY);
                playSound(SoundEvents.RAVAGER_ROAR, 1.2f, 1.3f);
            }
        } else {
            revealed = false;
            MobEffectInstance current = getEffect(MobEffects.INVISIBILITY);
            if (current == null || current.getDuration() < 10) {
                addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return revealed ? SoundEvents.RAVAGER_AMBIENT : null; // acechando no hace ruido
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
        return 1.3f;
    }
}
