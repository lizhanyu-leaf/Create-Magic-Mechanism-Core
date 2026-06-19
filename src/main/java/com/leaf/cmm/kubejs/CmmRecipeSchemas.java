package com.leaf.cmm.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public final class CmmRecipeSchemas {
    public static final RecipeKey<OutputItem[]> RESULTS = ItemComponents.OUTPUT.asArray().key("results");
    public static final RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT.asArray().key("ingredients");

    public static final RecipeSchema ITEM_COMBINE = new RecipeSchema(
            RESULTS, INGREDIENTS,
            TimeComponent.TICKS.key("processingTime").optional(1L)
    ).constructor(RESULTS, INGREDIENTS);
}
