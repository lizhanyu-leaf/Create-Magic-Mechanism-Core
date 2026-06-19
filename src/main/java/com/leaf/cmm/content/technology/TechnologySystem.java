package com.leaf.cmm.content.technology;

import com.google.common.collect.Maps;
import com.leaf.cmm.mixin.technology.RecipeManagerAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Map;

public final class TechnologySystem {
    public static final Map<String, List<Recipe<?>>> TECHNOLOGY_RECIPES = Maps.newHashMap();

    public static void apply(RecipeManager recipeManager) {
        var accessor = (RecipeManagerAccessor) recipeManager;
        var originalRecipes = accessor.getMapRecipes();
        var originalByName = accessor.getByName();
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes =
                Maps.newHashMap();
        newRecipes.putAll(originalRecipes);
        Map<ResourceLocation, Recipe<?>> newByName =
                Maps.newHashMap();
        newByName.putAll(originalByName);

        for (var entry : TECHNOLOGY_RECIPES.entrySet()) {
            if (TechnologyStorage.getInstance() == null) break;
            if (!TechnologyStorage.getInstance().isActive(entry.getKey())) continue;
            for (var recipe : entry.getValue()) {
                var type = recipe.getType();
                var id = recipe.getId();
                newRecipes.computeIfAbsent(type, k -> Maps.newHashMap()).put(id, recipe);
                newByName.put(id, recipe);
            }
        }

        accessor.setRecipes(newRecipes);
        accessor.setByName(newByName);
    }
}
