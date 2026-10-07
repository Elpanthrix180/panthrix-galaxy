package com.panthrixsgalaxy.recipe;

import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Categorías del Banco de Ingeniería Espacial. Cada receta del banco indica la suya en el JSON:
 *   "engineering_category": "suits"
 */
public enum EngineeringCategory {
    SUITS(() -> new ItemStack(ModItems.PG_SPACE_HELMET.get())),
    BACKPACKS(() -> new ItemStack(ModItems.PG_SPACE_BACKPACK.get())),
    OXYGEN(() -> new ItemStack(ModItems.PG_OXYGEN_TANK.get())),
    ENERGY(() -> new ItemStack(ModItems.PG_ADVANCED_BATTERY.get())),
    ROCKETS(() -> new ItemStack(Items.FIREWORK_ROCKET)),
    SHIPS(() -> new ItemStack(ModItems.PG_SPACE_SHIP.get())),
    LASERS(() -> new ItemStack(ModItems.PG_MARTIAN_CRYSTAL.get())),
    LASER_SWORDS(() -> new ItemStack(ModItems.PG_XENITE_SWORD.get())),
    MACHINES(() -> new ItemStack(ModBlocks.PG_GENERATOR.get())),
    ALIEN(() -> new ItemStack(ModItems.PG_COSMIC_CRYSTAL.get()));

    private final Supplier<ItemStack> icon;

    EngineeringCategory(Supplier<ItemStack> icon) {
        this.icon = icon;
    }

    /** Objeto que se dibuja en el botón de la categoría. */
    public ItemStack getIcon() {
        return icon.get();
    }

    /** Nombre usado en el JSON y en las traducciones: "suits", "laser_swords"... */
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "engineering.panthrixsgalaxy.category." + getSerializedName();
    }

    /** Busca la categoría por su nombre del JSON (si no existe, MACHINES). */
    public static EngineeringCategory byName(String name) {
        for (EngineeringCategory category : values()) {
            if (category.getSerializedName().equals(name)) {
                return category;
            }
        }
        return MACHINES;
    }
}
