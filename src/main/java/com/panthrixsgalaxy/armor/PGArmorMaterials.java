package com.panthrixsgalaxy.armor;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.init.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * Materiales de armadura del mod.
 *
 * Un "material de armadura" define cuánto protege, cuánto dura y qué textura usa.
 * El nombre (por ejemplo "panthrixsgalaxy:space_suit") indica las texturas:
 *   assets/panthrixsgalaxy/textures/models/armor/space_suit_layer_1.png  (casco, pechera, botas)
 *   assets/panthrixsgalaxy/textures/models/armor/space_suit_layer_2.png  (pantalones)
 *
 * Para crear un traje mejor en el futuro, basta con añadir otra línea a esta lista.
 */
public enum PGArmorMaterials implements ArmorMaterial {

    /**
     * Traje espacial básico. Protege como el hierro (2/6/5/2 = 15 puntos),
     * dura un poco más y se repara con placas reforzadas.
     */
    SPACE_SUIT("space_suit", 20, new int[]{2, 5, 6, 2}, 12,
            SoundEvents.ARMOR_EQUIP_IRON, 1.0f, 0.0f,
            () -> Ingredient.of(ModItems.PG_REINFORCED_PLATE.get()));

    /** Durabilidad base de cada pieza (igual que Minecraft): botas, pantalones, pechera, casco. */
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    /** Protección de cada pieza en este orden: botas, pantalones, pechera, casco. */
    private final int[] protection;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    PGArmorMaterials(String name, int durabilityMultiplier, int[] protection, int enchantmentValue,
                     SoundEvent equipSound, float toughness, float knockbackResistance,
                     Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protection = protection;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    /** Convierte el tipo de pieza en una posición de las listas (0 botas ... 3 casco). */
    private static int index(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 0;
            case LEGGINGS -> 1;
            case CHESTPLATE -> 2;
            case HELMET -> 3;
        };
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[index(type)] * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return protection[index(type)];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return PanthrixsGalaxy.MOD_ID + ":" + name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
