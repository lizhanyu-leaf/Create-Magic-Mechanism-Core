package com.leaf.cmm.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

@JeiPlugin
public final class CmmJEI implements IModPlugin {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("cmm", "jei_plugin");
    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    void loadCategories() {
        allCategories.clear();
        allCategories.add(new ItemCombineCategory());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        allCategories.forEach((c) -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach((c) -> c.registerCatalysts(registration));
    }
}
