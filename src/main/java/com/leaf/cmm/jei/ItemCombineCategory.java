package com.leaf.cmm.jei;

import com.leaf.cmm.CmmLang;
import com.leaf.cmm.recipe.CmmAllRecipeTypes;
import com.leaf.cmm.recipe.ItemCombineRecipe;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ItemCombineCategory extends CreateRecipeCategory<ItemCombineRecipe> {

    public ItemCombineCategory() {
        super(new RecipeInfo().createCategoryInfo());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemCombineRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> inputs = recipe.getIngredients();
        var outputs = recipe.getRollableResults();

        int slotSize = 18;
        int slotSpacing = 2;
        int slotTotalSize = slotSize + slotSpacing;
        int arrowWidth = 44;
        int arrowSpacing = 2;

        int maxPerRow = 3;
        int inputCount = inputs.size();
        int outputCount = outputs.size();

        int inputRows = (int) Math.ceil(inputCount / (double) maxPerRow);
        int inputCols = Math.min(inputCount, maxPerRow);

        int outputRows = (int) Math.ceil(outputCount / (double) maxPerRow);
        int outputCols = Math.min(outputCount, maxPerRow);

        int inputWidth = inputCols * slotTotalSize - slotSpacing;
        int outputWidth = outputCols * slotTotalSize - slotSpacing;
        int totalWidth = inputWidth + arrowWidth + arrowSpacing * 2 + outputWidth;

        int inputStartX = (178 - totalWidth) / 2;
        int arrowStartX = inputStartX + inputWidth + arrowSpacing;
        int outputStartX = arrowStartX + arrowWidth + arrowSpacing;

        int inputHeight = inputRows * slotTotalSize - slotSpacing;
        int outputHeight = outputRows * slotTotalSize - slotSpacing;

        int inputStartY = (120 - inputHeight) / 2;
        int outputStartY = (120 - outputHeight) / 2;

        for (int i = 0; i < inputCount; i++) {
            Ingredient item = inputs.get(i);
            int row = i / maxPerRow;
            int col = i % maxPerRow;

            int slotX = inputStartX + col * slotTotalSize;
            int slotY = inputStartY + row * slotTotalSize;

            builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(item);
        }

        for (int i = 0; i < outputCount; i++) {
            var output = outputs.get(i);
            int row = i / maxPerRow;
            int col = i % maxPerRow;
            int slotX = outputStartX + col * slotTotalSize;
            int slotY = outputStartY + row * slotTotalSize;

            builder.addSlot(RecipeIngredientRole.OUTPUT, slotX, slotY)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

    @Override
    public void draw(
            ItemCombineRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        int slotSize = 18;
        int slotSpacing = 2;
        int slotTotalSize = slotSize + slotSpacing;
        int arrowWidth = 44;
        int arrowSpacing = 2;
        int arrowHeight = 4;

        int maxPerRow = 3;

        int inputCount = recipe.getIngredients().size();
        int outputCount = recipe.getRollableResults().size();

        int inputCols = Math.min(inputCount, maxPerRow);
        int outputCols = Math.min(outputCount, maxPerRow);

        int inputWidth = inputCols * slotTotalSize - slotSpacing;
        int outputWidth = outputCols * slotTotalSize - slotSpacing;
        int totalWidth = inputWidth + arrowWidth + arrowSpacing * 2 + outputWidth;

        int inputStartX = (178 - totalWidth) / 2;
        int arrowStartX = inputStartX + inputWidth + arrowSpacing;

        int arrowStartY = (120 - arrowHeight) / 2;

        AllGuiTextures.JEI_ARROW.render(guiGraphics, arrowStartX, arrowStartY);
    }

    public static class RecipeInfo implements RecipeCategoryHelper.RecipeCategoryInfo<ItemCombineRecipe> {

        private static final List<Supplier<? extends ItemStack>> CATALYSTS =
                RecipeCategoryHelper.getCatalysts(Items.GRASS_BLOCK);

        @Override
        public List<Supplier<? extends ItemStack>> getCatalysts() {
            return CATALYSTS;
        }

        @Override
        public Class<? extends ItemCombineRecipe> getRecipeClass() {
            return ItemCombineRecipe.class;
        }

        @Override
        public IRecipeTypeInfo getRecipeTypeInfo() {
            return CmmAllRecipeTypes.ITEM_COMBINE;
        }

        @Override
        public IDrawable getIcon() {
            return new DoubleItemIcon(
                    () -> Items.GRASS_BLOCK.getDefaultInstance(),
                    () -> AllItems.BRASS_HAND.asStack()
            );
        }

        @Override
        public Pair<Integer, Integer> getBackgroundSize() {
            return Pair.of(178, 120);
        }

        @Override
        public BiFunction<String, List<Object>, MutableComponent> getTranslateDirect() {
            return (key, args) -> CmmLang.translateDirect(key, args.toArray());
        }
    }
}
