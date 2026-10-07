package com.panthrixsgalaxy.entity.mob;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;

/**
 * "Si me atacan, me defiendo y aviso a los demás aliens": como el de Minecraft, pero avisa a
 * TODOS los alienígenas cercanos (exploradores, soldados, criaturas...), no solo a los de su especie.
 */
public class PGAlienHurtByTargetGoal extends HurtByTargetGoal {

    public PGAlienHurtByTargetGoal(PathfinderMob mob) {
        super(mob);
    }

    @Override
    protected void alertOthers() {
        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker == null) {
            return;
        }
        double range = getFollowDistance();
        AABB area = AABB.unitCubeFromLowerCorner(mob.position()).inflate(range, 10.0, range);
        for (Mob other : mob.level().getEntitiesOfClass(Mob.class, area, PGAliens::isAlien)) {
            if (other != mob && other.getTarget() == null && !PGAliens.isAlien(attacker)) {
                other.setTarget(attacker);
            }
        }
    }
}
