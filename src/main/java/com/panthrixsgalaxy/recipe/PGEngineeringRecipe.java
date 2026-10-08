package com.panthrixsgalaxy.recipe;

import com.google.gson.JsonObject;
import com.panthrixsgalaxy.init.ModBlocks;
import com.panthrixsgalaxy.init.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Receta del Banco de Ingeniería Espacial.
 *
 * Se escribe en JSON igual que una receta de mesa de trabajo con forma ("pattern" + "key" + "result"),
 * pero con "type": "panthrixsgalaxy:engineering" y una categoría:
 *
 *   {
 *     "type": "panthrixsgalaxy:engineering",
 *     "engineering_category": "suits",
 *     "pattern": ["PFP", "PVP"],
 *     "key": { ... },
 *     "result": { "item": "panthrixsgalaxy:pg_space_helmet" }
 *   }
 *
 * Por dentro reutiliza la receta con forma de Minecraft (ShapedRecipe), así no reinventamos nada.
 */
public class PGEngineeringRecipe implements Recipe<CraftingContainer> {

    private final ResourceLocation id;
    private final ShapedRecipe shape;
    private final EngineeringCategory category;

    public PGEngineeringRecipe(ResourceLocation id, ShapedRecipe shape, EngineeringCategory category) {
        this.id = id;
        this.shape = shape;
        this.category = category;
    }

    public EngineeringCategory getCategory() {
        return category;
    }

    public int getWidth() {
        return shape.getWidth();
    }

    public int getHeight() {
        return shape.getHeight();
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return shape.matches(container, level);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return shape.assemble(container, registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return shape.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return shape.getResultItem(registryAccess);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return shape.getIngredients();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.PG_ENGINEERING_BENCH.get());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ENGINEERING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ENGINEERING_TYPE.get();
    }

    /** Lee y escribe la receta (del JSON y por la red hacia los jugadores). */
    public static class Serializer implements RecipeSerializer<PGEngineeringRecipe> {

        @Override
        public PGEngineeringRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe shape = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            EngineeringCategory category = EngineeringCategory.byName(
                    GsonHelper.getAsString(json, "engineering_category", "machines"));
            return new PGEngineeringRecipe(id, shape, category);
        }

        @Override
        public @Nullable PGEngineeringRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ShapedRecipe shape = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            EngineeringCategory category = buf.readEnum(EngineeringCategory.class);
            return shape == null ? null : new PGEngineeringRecipe(id, shape, category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PGEngineeringRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe.shape);
            buf.writeEnum(recipe.category);
        }
    }
}
