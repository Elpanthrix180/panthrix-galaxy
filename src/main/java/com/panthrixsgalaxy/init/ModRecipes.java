package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.recipe.PGEngineeringRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Tipos de receta propios del mod. */
public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, PanthrixsGalaxy.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PanthrixsGalaxy.MOD_ID);

    /** Recetas del Banco de Ingeniería Espacial ("type": "panthrixsgalaxy:engineering"). */
    public static final RegistryObject<RecipeType<PGEngineeringRecipe>> ENGINEERING_TYPE =
            RECIPE_TYPES.register("engineering",
                    () -> RecipeType.simple(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "engineering")));

    public static final RegistryObject<RecipeSerializer<PGEngineeringRecipe>> ENGINEERING_SERIALIZER =
            RECIPE_SERIALIZERS.register("engineering", PGEngineeringRecipe.Serializer::new);

    private ModRecipes() {
    }
}
