package com.leaf.cmm.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeJS;

public interface ITechnologyRecipeJS {
    RecipeJS technology(String techId);

    String createMagicMechanismCore$getTechnologyId();

    boolean createMagicMechanismCore$isTechnologyRecipe();
}
