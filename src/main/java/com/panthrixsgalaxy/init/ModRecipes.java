package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.platform.PGHolder;
import com.panthrixsgalaxy.platform.PGRegistry;
import com.panthrixsgalaxy.recipe.PGEngineeringRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Tipos de receta propios del mod. */
public final class ModRecipes {

    public static final PGRegistry<RecipeType<?>> RECIPE_TYPES = PGRegistry.create(Registries.RECIPE_TYPE);
    public static final PGRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = PGRegistry.create(Registries.RECIPE_SERIALIZER);

    /** Recetas del Banco de Ingeniería Espacial ("type": "panthrixsgalaxy:engineering"). */
    public static final PGHolder<RecipeType<PGEngineeringRecipe>> ENGINEERING_TYPE =
            RECIPE_TYPES.register("engineering",
                    () -> RecipeType.simple(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "engineering")));

    public static final PGHolder<RecipeSerializer<PGEngineeringRecipe>> ENGINEERING_SERIALIZER =
            RECIPE_SERIALIZERS.register("engineering", PGEngineeringRecipe.Serializer::new);

    private ModRecipes() {
    }
}
