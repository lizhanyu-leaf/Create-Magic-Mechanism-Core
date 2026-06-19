package com.leaf.cmm.recipe;

import com.leaf.cmm.CmmLang;
import com.leaf.cmm.CreateMagicMechanismCore;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ItemCombineRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

    public ItemCombineRecipe(ProcessingRecipeParams params) {
        super(CmmAllRecipeTypes.ITEM_COMBINE, params);
    }

    @Override
    public int getMaxInputCount() {
        return 9;
    }

    @Override
    public int getMaxOutputCount() {
        return 9;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionForAssembly() {
        return CmmLang.translateDirect(
                "recipe.assembly.item_combine",
                ingredients.get(1).getItems()[0].getDisplayName()
        );
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> set) {
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.addAll(ingredients);
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> AssemblyItemCombine::new;
    }

    @Override
    public boolean matches(RecipeWrapper wrapper, Level level) {
        if (wrapper.getContainerSize() < ingredients.size()) {
            return false;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack stackInSlot = wrapper.getItem(i);
            if (!ingredients.get(i).test(stackInSlot)) {
                return false;
            }
        }
        return true;
    }

    public static String getSequenceId(ItemCombineRecipe recipe) {
        String key = recipe.getId().toString();
        int last = key.lastIndexOf("_step_");
        if (last > -1) return key.substring(0, last);
        return "";
    }

    public static boolean match(IItemHandler handler, ItemCombineRecipe recipe, int step) {
        List<Ingredient> ingredients = recipe.getIngredients();
        
        if (handler.getSlots() < ingredients.size()) {
            return false;
        }
        
        // 收集 handler 中的所有物品
        List<ItemStack> availableItems = new java.util.ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                availableItems.add(stack.copy());
            }
        }
        
        // 检查是否能匹配所有原料（允许有多余物品）
        List<ItemStack> remainingItems = new java.util.ArrayList<>(availableItems);
        
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < remainingItems.size(); i++) {
                if (ingredient.test(remainingItems.get(i))) {
                    remainingItems.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        return true;
    }

    public static class AssemblyItemCombine extends SequencedAssemblySubCategory {

        public AssemblyItemCombine() {
            super(25);
        }

        @Override
        public void setRecipe(
                IRecipeLayoutBuilder builder,
                SequencedRecipe<?> recipe,
                IFocusGroup focuses,
                int x
        ) {
            if (recipe.getAsAssemblyRecipe() instanceof ItemCombineRecipe itemCombineRecipe) {
                int i = 1;
                while (i < itemCombineRecipe.ingredients.size() || i <= 2) {
                    builder.addSlot(RecipeIngredientRole.INPUT, x + 4, 40 - (i-1) * 18)
                            .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                            .addIngredients(itemCombineRecipe.ingredients.get(i));
                    i++;
                }
            }
        }

        @Override
        public void draw(
                SequencedRecipe<?> sequencedRecipe,
                GuiGraphics guiGraphics,
                double v,
                double v1,
                int i
        ) {
            var ms = guiGraphics.pose();
            ms.pushPose();
            ms.translate(-7.0f, 50.0f, 0.0f);
            ms.scale(0.75f, 0.75f, 0.75f);
            ms.translate((float) getWidth() / 2, 0f, 100.0f);
            ms.mulPose(Axis.XP.rotationDegrees(-15.5f));
            ms.mulPose(Axis.YP.rotationDegrees(22.5f));

            int scale = 20;
            AnimatedKinetics.defaultBlockElement(Blocks.GRASS_BLOCK.defaultBlockState())
//                    .rotate(0.0, AnimationTickHolder.getRenderTime() * 2.0f % 360.0f, 0.0)
                    .rotate(0.0, 0.0, 0.0)
                    .atLocal(0.0, 2.0, 0.0)
                    .scale(scale)
                    .render(guiGraphics);

            ms.popPose();
        }
    }
}
