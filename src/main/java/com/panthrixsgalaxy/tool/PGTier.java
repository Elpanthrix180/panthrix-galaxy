package com.panthrixsgalaxy.tool;

import net.minecraft.tags.TagKey;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Un nivel de herramienta (como el hierro o el diamante). Sirve igual en Forge y en Fabric.
 *
 * new PGTier(nivel, usos, velocidad, daño extra, encantabilidad, tag, material de reparación):
 *   - nivel: nivel de minado (hierro 2, diamante 3, netherita 4)
 *   - tag: bloques que solo puede picar este nivel o uno mejor
 */
public final class PGTier implements Tier {

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final TagKey<Block> tag;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    public PGTier(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue,
                  TagKey<Block> tag, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.tag = tag;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    /** Forge lo usa para saber qué bloques pica este nivel (en Fabric no existe, por eso no lleva @Override). */
    public TagKey<Block> getTag() {
        return tag;
    }
}
