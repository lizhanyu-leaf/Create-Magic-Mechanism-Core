package com.leaf.cmm.mixin.technology;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = RecipeManager.class, remap = false)
public interface RecipeManagerAccessor {
    @Accessor("byName")
    Map<ResourceLocation, Recipe<?>> getByName();

    @Accessor("recipes")
    Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getMapRecipes();

    @Accessor("byName")
    void setByName(Map<ResourceLocation, Recipe<?>> value);

    @Accessor("recipes")
    void setRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> value);

//    // 拦截对 byName 字段的赋值
//    @ModifyVariable(
//            method = "*",
//            at = @At(
//                    value = "FIELD",
//                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;byName:Ljava/util/Map;",
//                    opcode = Opcodes.PUTFIELD
//            ),
//            require = 0,
//            remap = false
//    )
//    private Map<ResourceLocation, Recipe<?>> ensureByNameMutable(Map<ResourceLocation, Recipe<?>> value) {
//        return value instanceof HashMap ? value : new HashMap<>(value);
//    }
//
//    // 拦截对 recipes 字段的赋值
//    @ModifyVariable(
//            method = "*",
//            at = @At(
//                    value = "FIELD",
//                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;recipes:Ljava/util/Map;",
//                    opcode = Opcodes.PUTFIELD
//            ),
//            require = 0,
//            remap = false
//    )
//    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> ensureRecipesMutable(
//            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> value
//    ) {
//        if (value instanceof HashMap) return value;
//
//        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newMap = new HashMap<>();
//        for (Map.Entry<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> entry : value.entrySet()) {
//            newMap.put(entry.getKey(), new HashMap<>(entry.getValue()));
//        }
//        return newMap;
//    }
}