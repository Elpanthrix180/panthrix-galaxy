package com.panthrixsgalaxy.client.screen;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.init.ModRecipes;
import com.panthrixsgalaxy.menu.PGEngineeringBenchMenu;
import com.panthrixsgalaxy.recipe.EngineeringCategory;
import com.panthrixsgalaxy.recipe.PGEngineeringRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Pantalla del Banco de Ingeniería Espacial.
 *
 * A la izquierda hay una GUÍA DE RECETAS:
 *   - 10 botones de categoría (trajes, mochilas, oxígeno...).
 *   - Las recetas de la categoría elegida.
 *   - Al hacer clic en una receta, sus ingredientes aparecen en gris en la cuadrícula
 *     para saber qué poner en cada hueco.
 */
public class PGEngineeringBenchScreen extends AbstractContainerScreen<PGEngineeringBenchMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/gui/engineering_bench.png");

    private static final int PANEL_WIDTH = 100;
    private static final int COLUMNS = 5;
    /** Filas de botones de categoría (5 por fila) y, debajo, dónde empiezan el título y las recetas. */
    private static final int CATEGORY_ROWS = (EngineeringCategory.values().length + COLUMNS - 1) / COLUMNS;
    private static final int LABEL_Y = 15 + CATEGORY_ROWS * 18 + 3;
    private static final int RECIPES_Y = LABEL_Y + 12;
    private static final int MAX_RECIPES = 25;
    private static final int COLOR_PANEL = 0xE0161B26;
    private static final int COLOR_BORDER = 0xFF55CCFF;
    private static final int COLOR_SELECTED = 0x8055CCFF;

    private EngineeringCategory selectedCategory = EngineeringCategory.SUITS;
    @Nullable
    private PGEngineeringRecipe selectedRecipe;

    public PGEngineeringBenchScreen(PGEngineeringBenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    // ===== Dibujar =====

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderGuide(graphics, mouseX, mouseY);
        renderGhostIngredients(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private int panelX() {
        return Math.max(2, leftPos - PANEL_WIDTH - 4);
    }

    private void renderGuide(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = panelX();
        int y = topPos;
        graphics.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + imageHeight + 1, COLOR_BORDER);
        graphics.fill(x, y, x + PANEL_WIDTH, y + imageHeight, COLOR_PANEL);
        graphics.drawString(font, Component.translatable("gui.panthrixsgalaxy.recipe_guide"), x + 5, y + 4, 0xFFFFFF);

        // Botones de categoría (filas de 5)
        EngineeringCategory[] categories = EngineeringCategory.values();
        for (int i = 0; i < categories.length; i++) {
            int bx = x + 5 + (i % COLUMNS) * 18;
            int by = y + 15 + (i / COLUMNS) * 18;
            if (categories[i] == selectedCategory) {
                graphics.fill(bx - 1, by - 1, bx + 17, by + 17, COLOR_SELECTED);
            }
            graphics.renderItem(categories[i].getIcon(), bx, by);
            if (isInside(mouseX, mouseY, bx, by)) {
                graphics.renderTooltip(font, Component.translatable(categories[i].getTranslationKey()), mouseX, mouseY);
            }
        }
        graphics.drawString(font, Component.translatable(selectedCategory.getTranslationKey()), x + 5, y + LABEL_Y, 0x55CCFF);

        // Recetas de la categoría
        List<PGEngineeringRecipe> recipes = recipesOf(selectedCategory);
        if (recipes.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.panthrixsgalaxy.coming_soon"), x + 5, y + RECIPES_Y + 2, 0x888888);
            return;
        }
        for (int i = 0; i < recipes.size(); i++) {
            int rx = x + 5 + (i % COLUMNS) * 18;
            int ry = y + RECIPES_Y + (i / COLUMNS) * 18;
            PGEngineeringRecipe recipe = recipes.get(i);
            if (recipe == selectedRecipe) {
                graphics.fill(rx - 1, ry - 1, rx + 17, ry + 17, COLOR_SELECTED);
            }
            ItemStack result = recipe.getResultItem(registryAccess());
            graphics.renderItem(result, rx, ry);
            graphics.renderItemDecorations(font, result, rx, ry);
            if (isInside(mouseX, mouseY, rx, ry)) {
                graphics.renderTooltip(font, result, mouseX, mouseY);
            }
        }
        graphics.drawWordWrap(font, Component.translatable("gui.panthrixsgalaxy.recipe_guide_hint"),
                x + 5, y + imageHeight - 22, PANEL_WIDTH - 10, 0x888888);
    }

    /** Dibuja en gris los ingredientes de la receta elegida en los huecos vacíos de la cuadrícula. */
    private void renderGhostIngredients(GuiGraphics graphics) {
        if (selectedRecipe == null) {
            return;
        }
        List<Ingredient> ingredients = selectedRecipe.getIngredients();
        int width = selectedRecipe.getWidth();
        int height = selectedRecipe.getHeight();
        long time = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int index = row * width + column;
                if (index >= ingredients.size() || !menu.getCraftSlots().getItem(column + row * 3).isEmpty()) {
                    continue;
                }
                ItemStack[] options = ingredients.get(index).getItems();
                if (options.length == 0) {
                    continue;
                }
                ItemStack shown = options[(int) ((time / 20) % options.length)];
                int sx = leftPos + 30 + column * 18;
                int sy = topPos + 17 + row * 18;
                graphics.renderFakeItem(shown, sx, sy);
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 300);
                graphics.fill(sx, sy, sx + 16, sy + 16, 0x9A8B8B8B);
                graphics.pose().popPose();
            }
        }
    }

    // ===== Clics =====

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = panelX();
        int y = topPos;
        EngineeringCategory[] categories = EngineeringCategory.values();
        for (int i = 0; i < categories.length; i++) {
            if (isInside(mouseX, mouseY, x + 5 + (i % COLUMNS) * 18, y + 15 + (i / COLUMNS) * 18)) {
                selectedCategory = categories[i];
                selectedRecipe = null;
                return true;
            }
        }
        List<PGEngineeringRecipe> recipes = recipesOf(selectedCategory);
        for (int i = 0; i < recipes.size(); i++) {
            if (isInside(mouseX, mouseY, x + 5 + (i % COLUMNS) * 18, y + RECIPES_Y + (i / COLUMNS) * 18)) {
                PGEngineeringRecipe clicked = recipes.get(i);
                selectedRecipe = clicked == selectedRecipe ? null : clicked;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ===== Ayudantes =====

    private static boolean isInside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private RegistryAccess registryAccess() {
        return Minecraft.getInstance().level == null
                ? RegistryAccess.EMPTY
                : Minecraft.getInstance().level.registryAccess();
    }

    /** Todas las recetas del banco de una categoría, ordenadas por nombre. */
    private List<PGEngineeringRecipe> recipesOf(EngineeringCategory category) {
        if (Minecraft.getInstance().level == null) {
            return List.of();
        }
        return Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.ENGINEERING_TYPE.get()).stream()
                .filter(recipe -> recipe.getCategory() == category)
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .limit(MAX_RECIPES)
                .toList();
    }
}
