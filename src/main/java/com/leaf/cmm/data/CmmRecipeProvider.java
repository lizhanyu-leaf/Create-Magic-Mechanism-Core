package com.leaf.cmm.data;

import com.leaf.cmm.CreateMagicMechanismCore;
import com.leaf.cmm.recipe.ItemCombineRecipe;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class CmmRecipeProvider extends RecipeProvider {

    public CmmRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
//        createItemCombineRecipe(
//                consumer,
//                "brass_ingot_item_combine",
//                new ItemLike[] {Items.COPPER_INGOT, Items.GOLD_INGOT},
//                new ProcessingOutput(AllItems.BRASS_INGOT.asStack(), 1f)
//        );

        new SequencedAssemblyRecipeBuilder(CreateMagicMechanismCore.modLoc("sequenced_assembly/test"))
                .require(Items.COPPER_INGOT)
                .addStep(ItemCombineRecipe::new,
                        builder -> builder.require(AllItems.ZINC_INGOT))
                .addStep(ItemCombineRecipe::new,
                        builder -> builder.require(Items.GOLD_INGOT))
                .addOutput(AllItems.BRASS_INGOT, 1f)
                .transitionTo(Items.COPPER_INGOT).loops(1)
                .build(consumer);
    }

    private void createItemCombineRecipe(
            Consumer<FinishedRecipe> consumer,
            String recipeId,
            ItemLike[] inputs,
            ProcessingOutput output
    ) {
        ResourceLocation location = CreateMagicMechanismCore.modLoc(recipeId);

        ProcessingRecipeBuilder<ItemCombineRecipe> builder =
                new ProcessingRecipeBuilder<>(ItemCombineRecipe::new, location)
                        .output(output)
                        .duration(1);

        for (ItemLike input : inputs) {
            builder.require(input);
        }

        builder.build(consumer);
    }
}
