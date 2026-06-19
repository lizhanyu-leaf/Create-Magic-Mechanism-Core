package com.leaf.cmm.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;

public final class CmmKubeJSPlugins extends KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace("cmm")
                .register("item_combine", CmmRecipeSchemas.ITEM_COMBINE);
    }
}
