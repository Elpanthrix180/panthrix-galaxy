package com.panthrixsgalaxy.tool;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.init.ModItems;
import com.panthrixsgalaxy.init.ModTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

/**
 * Niveles (tiers) de las herramientas espaciales.
 *
 * Orden de progresión (de peor a mejor):
 *   hierro < LUNARITA < diamante < MARTEÍTA < netherita < OSMIO < XENITA
 *
 * Cada nivel tiene un tag "needs_xxx_tool": los bloques de ese tag solo los puede
 * picar una herramienta de ese nivel o superior.
 *
 * Valores de ForgeTier(nivel, usos, velocidad, daño extra, encantabilidad, tag, material de reparación):
 *   - usos: durabilidad (hierro 250, diamante 1561, netherita 2031)
 *   - velocidad: rapidez al picar (hierro 6, diamante 8, netherita 9)
 *   - daño extra: se suma al daño de cada herramienta (hierro 2, diamante 3, netherita 4)
 *   - encantabilidad: mejores encantamientos cuanto más alto (hierro 14, diamante 10, oro 22)
 */
public final class PGToolTiers {

    public static final Tier LUNARITE = TierSortingRegistry.registerTier(
            new ForgeTier(2, 450, 6.5f, 2.5f, 16, ModTags.Blocks.NEEDS_LUNARITE_TOOL,
                    () -> Ingredient.of(ModItems.PG_LUNARITE_INGOT.get())),
            id("lunarite"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));

    public static final Tier MARTIANITE = TierSortingRegistry.registerTier(
            new ForgeTier(3, 1100, 8.0f, 3.0f, 12, ModTags.Blocks.NEEDS_MARTIANITE_TOOL,
                    () -> Ingredient.of(ModItems.PG_MARTIANITE_INGOT.get())),
            id("martianite"), List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE));

    public static final Tier OSMIUM = TierSortingRegistry.registerTier(
            new ForgeTier(4, 2200, 9.0f, 4.0f, 14, ModTags.Blocks.NEEDS_OSMIUM_TOOL,
                    () -> Ingredient.of(ModItems.PG_OSMIUM_INGOT.get())),
            id("osmium"), List.of(Tiers.NETHERITE), List.of());

    public static final Tier XENITE = TierSortingRegistry.registerTier(
            new ForgeTier(5, 3000, 10.0f, 5.0f, 18, ModTags.Blocks.NEEDS_XENITE_TOOL,
                    () -> Ingredient.of(ModItems.PG_XENITE_INGOT.get())),
            id("xenite"), List.of(OSMIUM), List.of());

    private static ResourceLocation id(String name) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID, name);
    }

    /**
     * No hace nada por sí mismo: llamarlo obliga a Java a cargar esta clase, así los
     * niveles quedan registrados al arrancar el mod (antes de que Forge los ordene).
     */
    public static void register() {
    }

    private PGToolTiers() {
    }
}
