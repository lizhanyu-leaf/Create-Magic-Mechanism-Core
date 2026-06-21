package com.leaf.cmm.mixin.technology;

import com.google.gson.JsonElement;
import com.leaf.cmm.content.technology.TechnologyStorage;
import com.leaf.cmm.content.technology.TechnologySystem;
import com.leaf.cmm.kubejs.ITechnologyRecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;

@Mixin(value = RecipesEventJS.class, remap = false)
public class RecipesEventJSMixin {

    @Inject(method = "post", at = @At("HEAD"))
    private void init(RecipeManager recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        TechnologySystem.clear();
    }

    @Inject(method = "post", at = @At("RETURN"))
    private void end(RecipeManager recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        TechnologyStorage.whenCreate(() -> {
            TechnologySystem.setDirty();
            TechnologySystem.apply(recipeManager);
        });
    }

    @Inject(method = "createRecipe", at = @At(value = "RETURN", ordinal = 0), remap = false, cancellable = true)
    private void createRecipeMixin(RecipeJS r, CallbackInfoReturnable<Recipe<?>> cir) {
        var isTechRecipe = ((ITechnologyRecipeJS) r).createMagicMechanismCore$isTechnologyRecipe();
        if (!isTechRecipe) return;
        var techId = ((ITechnologyRecipeJS) r).createMagicMechanismCore$getTechnologyId();
        if (!TechnologySystem.TECHNOLOGY_RECIPES.containsKey(techId))
            TechnologySystem.TECHNOLOGY_RECIPES.put(techId, new ArrayList<>());
        TechnologySystem.TECHNOLOGY_RECIPES.get(techId).add(cir.getReturnValue());
        cir.setReturnValue(null);
    }
}