package com.leaf.cmm.recipe;

import com.leaf.cmm.CreateMagicMechanismCore;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum CmmAllRecipeTypes implements IRecipeTypeInfo {
    ITEM_COMBINE(() -> new ProcessingRecipeSerializer<>(ItemCombineRecipe::new));

    private final ResourceLocation id;
    private final RegistryObject<RecipeSerializer<?>> serializerObject;
    private final RegistryObject<RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;

    CmmAllRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(this.name());
        this.id = CreateMagicMechanismCore.asResource(name);
        this.serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        this.typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(this.id));
        this.type = this.typeObject;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) this.serializerObject.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeType<?>> T getType() {
        return (T) this.type.get();
    }

    @SuppressWarnings("unchecked")
    public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
        return world.getRecipeManager().getRecipeFor((RecipeType<T>) this.getType(), inv, world);
    }

    private static class Registers {
        static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER =
                DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "cmm");
        static final DeferredRegister<RecipeType<?>> TYPE_REGISTER =
                DeferredRegister.create(Registries.RECIPE_TYPE, "cmm");
    }

    public static final Predicate<Recipe<?>> CAN_BE_AUTOMATED =
            r -> !r.getId().getPath().endsWith("_manual_only");

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZER_REGISTER.register(modEventBus);
        Registers.TYPE_REGISTER.register(modEventBus);
    }

    public static boolean shouldIgnoreInAutomation(Recipe<?> recipe) {
        RecipeSerializer<?> serializer = recipe.getSerializer();
        if (AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.matches(serializer)) {
            return true;
        }
        return !CAN_BE_AUTOMATED.test(recipe);
    }
}
