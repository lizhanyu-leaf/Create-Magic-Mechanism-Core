package com.leaf.cmm.mixin.technology;

import com.leaf.cmm.kubejs.ITechnologyRecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RecipeJS.class)
public class RecipeJSMixin implements ITechnologyRecipeJS {

    @Unique
    private String createMagicMechanismCore$technologyId = null;
    @Unique
    private boolean createMagicMechanismCore$isTechnologyRecipe = false;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public RecipeJS technology(String techId) {
        createMagicMechanismCore$isTechnologyRecipe = true;
        createMagicMechanismCore$technologyId = techId;
        return (RecipeJS) (Object) this;
    }

    @HideFromJS
    @Override
    public String createMagicMechanismCore$getTechnologyId() {
        return createMagicMechanismCore$technologyId;
    }

    @HideFromJS
    @Override
    public boolean createMagicMechanismCore$isTechnologyRecipe() {
        return createMagicMechanismCore$isTechnologyRecipe;
    }
}
