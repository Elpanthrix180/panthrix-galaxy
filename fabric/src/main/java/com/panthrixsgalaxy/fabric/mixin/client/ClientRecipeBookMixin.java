package com.panthrixsgalaxy.fabric.mixin.client;

import com.panthrixsgalaxy.init.ModRecipes;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Solo Fabric (pantalla): las recetas del Banco no salen en el libro de recetas de Minecraft
 * (tienen su propia guía). Sin esto, el registro del juego se llena de avisos.
 * (En Forge: RegisterRecipeBookCategoriesEvent en client/PGClientModEvents.)
 */
@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {

    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private static void panthrixsgalaxy$engineeringCategory(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookCategories> info) {
        if (recipe.getType() == ModRecipes.ENGINEERING_TYPE.get()) {
            info.setReturnValue(RecipeBookCategories.UNKNOWN);
        }
    }
}
