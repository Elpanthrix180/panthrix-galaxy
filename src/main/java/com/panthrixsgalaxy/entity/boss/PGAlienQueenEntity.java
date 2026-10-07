package com.panthrixsgalaxy.entity.boss;

import com.panthrixsgalaxy.entity.laser.PGLaserBoltEntity;
import com.panthrixsgalaxy.entity.mob.PGAliens;
import com.panthrixsgalaxy.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 👑 REINA ALIENÍGENA — el primer jefe.
 *
 * 300 de vida (150 ❤) y 3 FASES según la vida que le queda:
 *
 *   FASE 1 (100-66 %)  Cuerpo a cuerpo + escupe ÁCIDO (rayos verdes; ¡se pueden devolver con la espada láser!).
 *                      Cada 20 s llama a 2 criaturas alienígenas (sus crías).
 *   FASE 2 (66-33 %)   FURIA: más rápida, escupe más a menudo y da SALTOS DE IMPACTO: salta y al caer
 *                      lanza una onda que daña y empuja a todos en 6 bloques. Llama a crías y soldados.
 *   FASE 3 (<33 %)     DESESPERACIÓN: más fuerza, LLUVIA DE ÁCIDO en círculo y llama a un depredador.
 *
 * Al cambiar de fase ruge y es INVULNERABLE 2 segundos (un escudo morado).
 * Tiene barra de jefe y nunca desaparece sola. Suelta NECRONITA (botín exclusivo).
 */
public class PGAlienQueenEntity extends Monster {

    private static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(PGAlienQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHIELDED =
            SynchedEntityData.defineId(PGAlienQueenEntity.class, EntityDataSerializers.BOOLEAN);

    private static final UUID FURY_SPEED_ID = UUID.fromString("3c2d5a90-6f2e-4b7a-9e61-7a0f4c1b8d21");
    private static final UUID DESPAIR_DAMAGE_ID = UUID.fromString("8e4b1f27-2c9d-4e3a-b5f6-1d7c9a0e3b44");
    private static final int ACID_COLOR = 0x9CFF3A;
    private static final int MAX_MINIONS = 8;

    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.panthrixsgalaxy.alien_queen"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    private int shieldTicks;
    private int acidCooldown = 60;
    private int summonCooldown = 200;
    private int slamCooldown = 120;
    private int barrageCooldown = 100;
    private boolean slamming;
    private boolean predatorCalled;

    public PGAlienQueenEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 200;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PHASE, 1);
        entityData.define(SHIELDED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    /** ¿Tiene el escudo de cambio de fase? (lo usa el dibujo) */
    public boolean isShielded() {
        return entityData.get(SHIELDED);
    }

    // ===== Cada tick (servidor) =====

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(getHealth() / getMaxHealth());
        updatePhase();

        if (shieldTicks > 0 && --shieldTicks == 0) {
            entityData.set(SHIELDED, false);
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || isShielded()) {
            return;
        }
        int phase = getPhase();
        double distance = distanceTo(target);

        // Ácido: a distancia, cada 2 s (fase 1) o 1,25 s (fases 2-3)
        if (--acidCooldown <= 0 && distance > 3.5 && hasLineOfSight(target)) {
            spitAcid(target);
            acidCooldown = phase == 1 ? 40 : 25;
        }
        // Crías
        if (--summonCooldown <= 0) {
            summonBrood(phase, target);
            summonCooldown = phase == 1 ? 400 : 320;
        }
        // Salto de impacto (fase 2+)
        if (phase >= 2 && --slamCooldown <= 0 && distance < 10.0 && onGround()) {
            Vec3 toTarget = target.position().subtract(position()).normalize().scale(0.6);
            setDeltaMovement(toTarget.x, 1.0, toTarget.z);
            slamming = true;
            playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.7f);
            slamCooldown = 160;
        }
        if (slamming && onGround() && getDeltaMovement().y <= 0.0) {
            slam();
        }
        // Lluvia de ácido (fase 3)
        if (phase == 3 && --barrageCooldown <= 0) {
            acidRing();
            barrageCooldown = 120;
        }
    }

    /** Cambia de fase al bajar del 66 % y del 33 % de vida. */
    private void updatePhase() {
        float health = getHealth() / getMaxHealth();
        int newPhase = health > 0.66f ? 1 : health > 0.33f ? 2 : 3;
        if (newPhase <= getPhase()) {
            return;
        }
        entityData.set(PHASE, newPhase);
        entityData.set(SHIELDED, true);
        shieldTicks = 40;
        playSound(SoundEvents.ENDER_DRAGON_GROWL, 3.0f, 1.2f);
        applyPhaseEffects(newPhase);
        for (ServerPlayer player : bossBar.getPlayers()) {
            player.displayClientMessage(Component.translatable("message.panthrixsgalaxy.queen_phase_" + newPhase)
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        }
    }

    /** Fase 2: +25 % de velocidad y barra roja. Fase 3: +50 % de daño, barra blanca y pantalla oscura. */
    private void applyPhaseEffects(int phase) {
        if (phase >= 2) {
            bossBar.setColor(BossEvent.BossBarColor.RED);
            if (getAttribute(Attributes.MOVEMENT_SPEED).getModifier(FURY_SPEED_ID) == null) {
                getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(FURY_SPEED_ID,
                        "Furia de la Reina", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
        if (phase >= 3) {
            bossBar.setColor(BossEvent.BossBarColor.WHITE);
            bossBar.setDarkenScreen(true);
            if (getAttribute(Attributes.ATTACK_DAMAGE).getModifier(DESPAIR_DAMAGE_ID) == null) {
                getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(DESPAIR_DAMAGE_ID,
                        "Desesperación de la Reina", 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }

    // ===== Ataques especiales =====

    private void spitAcid(LivingEntity target) {
        PGLaserBoltEntity acid = new PGLaserBoltEntity(level(), this, 8.0f, 30, 0, 0.3, false, ACID_COLOR);
        acid.setPos(getX(), getEyeY() - 0.3, getZ());
        double dx = target.getX() - getX();
        double dy = target.getY(0.5) - acid.getY();
        double dz = target.getZ() - getZ();
        acid.shoot(dx, dy, dz, 2.2f, 2.0f);
        level().addFreshEntity(acid);
        playSound(SoundEvents.LLAMA_SPIT, 2.0f, 0.5f);
    }

    /** Fase 3: 12 escupitajos de ácido en círculo. */
    private void acidRing() {
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2.0 * i / 12.0;
            PGLaserBoltEntity acid = new PGLaserBoltEntity(level(), this, 7.0f, 20, 0, 0.3, false, ACID_COLOR);
            acid.setPos(getX(), getY() + 1.5, getZ());
            acid.shoot(Math.cos(angle), 0.05, Math.sin(angle), 1.6f, 0.0f);
            level().addFreshEntity(acid);
        }
        playSound(SoundEvents.LLAMA_SPIT, 3.0f, 0.4f);
    }

    /** Onda de choque al caer de un salto: daña y empuja a todos los que no son aliens. */
    private void slam() {
        slamming = false;
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 12, 3.0, 0.2, 3.0, 0.0);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0f, 0.6f);
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0, 2.0, 6.0))) {
            if (victim == this || PGAliens.isAlien(victim)) {
                continue;
            }
            victim.hurt(damageSources().mobAttack(this), 10.0f);
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.knockback(1.5, -away.x, -away.z);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
        }
    }

    /** Llama a sus crías (sin pasar de 8 aliens a su alrededor). */
    private void summonBrood(int phase, LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        int nearby = level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(32.0),
                monster -> monster != this && PGAliens.isAlien(monster)).size();
        if (nearby >= MAX_MINIONS) {
            return;
        }
        spawnMinion(server, ModEntities.ALIEN_CREATURE.get(), target);
        spawnMinion(server, ModEntities.ALIEN_CREATURE.get(), target);
        if (phase >= 2) {
            spawnMinion(server, ModEntities.ALIEN_SOLDIER.get(), target);
        }
        if (phase == 3 && !predatorCalled) {
            spawnMinion(server, ModEntities.ALIEN_PREDATOR.get(), target);
            predatorCalled = true;
        }
        playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0f, 0.6f);
    }

    private void spawnMinion(ServerLevel server, EntityType<? extends Mob> type, LivingEntity target) {
        Mob minion = type.create(server);
        if (minion == null) {
            return;
        }
        double angle = random.nextDouble() * Math.PI * 2.0;
        minion.moveTo(getX() + Math.cos(angle) * 3.0, getY(), getZ() + Math.sin(angle) * 3.0, random.nextFloat() * 360.0f, 0.0f);
        minion.finalizeSpawn(server, server.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        minion.setTarget(target);
        server.addFreshEntity(minion);
        server.sendParticles(ParticleTypes.PORTAL, minion.getX(), minion.getY() + 1.0, minion.getZ(), 20, 0.5, 1.0, 0.5, 0.2);
    }

    // ===== Daño =====

    /** Con el escudo de cambio de fase no recibe daño (salvo /kill). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isShielded() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 0.6f);
            return false;
        }
        return super.hurt(source, amount);
    }

    /** No sufre daño de caída (sus saltos de impacto). */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ===== Barra de jefe =====

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    // ===== Efectos en pantalla =====

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && isShielded()) {
            for (int i = 0; i < 4; i++) {
                double angle = random.nextDouble() * Mth.TWO_PI;
                level().addParticle(ParticleTypes.WITCH, getX() + Math.cos(angle) * 1.2, getY() + random.nextDouble() * 3.0,
                        getZ() + Math.sin(angle) * 1.2, 0.0, 0.05, 0.0);
            }
        }
    }

    // ===== Guardar =====

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        tag.putBoolean("PredatorCalled", predatorCalled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(PHASE, Math.max(1, tag.getInt("Phase")));
        predatorCalled = tag.getBoolean("PredatorCalled");
        applyPhaseEffects(getPhase());
        if (hasCustomName()) {
            bossBar.setName(getDisplayName());
        }
    }

    // ===== Sonidos =====

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.8f;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }
}
