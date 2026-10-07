package com.panthrixsgalaxy.entity.mob;

import com.panthrixsgalaxy.entity.laser.PGLaserBoltEntity;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.weapon.LaserTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Alien soldado: lleva una PISTOLA LÁSER y dispara desde lejos (hasta 16 bloques).
 * PROTEGE su zona: no se aleja más de 24 bloques del sitio donde apareció
 * (cuando haya estructuras alienígenas, Fases 19-21, vigilará la suya).
 * Aparece en grupos de 2-3. Si te atacan, avisa a los demás aliens.
 *
 * Sus rayos se pueden devolver con la GUARDIA de una espada láser (Fase 16).
 */
public class PGAlienSoldierEntity extends Monster implements RangedAttackMob {

    private static final int GUARD_RADIUS = 24;

    public PGAlienSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 30, 16.0f));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Al aparecer: pistola en la mano y "esta es mi zona". */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        restrictTo(blockPosition(), GUARD_RADIUS);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.PG_LASER_PISTOL.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.05f); // 5 %: ¡una pistola láser gratis! (descargada)
    }

    /** Dispara un rayo láser hacia el objetivo. */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        PGLaserBoltEntity bolt = new PGLaserBoltEntity(level(), this, LaserTier.PISTOL);
        double dx = target.getX() - getX();
        double dy = target.getY(0.4) - bolt.getY();
        double dz = target.getZ() - getZ();
        float inaccuracy = 10.0f - level().getDifficulty().getId() * 3.0f;
        bolt.shoot(dx, dy, dz, (float) LaserTier.PISTOL.getSpeed(), Math.max(1.0f, inaccuracy));
        level().addFreshEntity(bolt);
        level().playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 0.6f, 1.9f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.45f;
    }
}
