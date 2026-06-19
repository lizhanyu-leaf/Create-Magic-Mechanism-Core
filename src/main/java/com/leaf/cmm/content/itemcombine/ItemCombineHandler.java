package com.leaf.cmm.content.itemcombine;

import com.leaf.cmm.recipe.CmmAllRecipeTypes;
import com.leaf.cmm.recipe.ItemCombineRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.recipe.trie.RecipeTrie;
import com.simibubi.create.foundation.recipe.trie.RecipeTrieFinder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;

public class ItemCombineHandler {
    public static final Object ItemCombineRecipesKey = new Object();
    private static final Set<ItemEntity> processedItems = new HashSet<>();
    public static RecipeType<ProcessingRecipe<?>> ITEM_COMBINE = CmmAllRecipeTypes.ITEM_COMBINE.getType();

    private static <C extends Container> boolean matchStaticFilters(Recipe<C> recipe) {
        return recipe.getType() == ITEM_COMBINE;
    }

    public static void clearProcessedItems() {
        processedItems.clear();
    }

    public static boolean isProcessedItem(ItemEntity item) {
        return processedItems.contains(item);
    }

    public static Optional<ProcessingRecipe<?>> findMatchingRecipe(ItemEntity mainItem, List<ItemEntity> nearbyItems) {
        Level level = mainItem.level();

        List<ProcessingRecipe<?>> recipes = new ArrayList<>();

        NonNullList<ItemStack> availableItems = NonNullList.create();
        availableItems.add(mainItem.getItem());

        for (ItemEntity nearby : nearbyItems) {
            if (nearby != mainItem && !nearby.isRemoved()) {
                availableItems.add(nearby.getItem().copy());
            }
        }

        if (availableItems.isEmpty()) {
            return Optional.empty();
        }

        String itemSequenceId;
        int itemSequenceStep;

        // 检查物品的序列装配标签
        ItemStack item = mainItem.getItem();
        // 装配起始物品可以不带序列装配标签
        if (item.getTag() != null && item.hasTag() && item.getTag().contains("SequencedAssembly")) {
            CompoundTag tag = item.getTag().getCompound("SequencedAssembly");
            itemSequenceId = tag.getString("id");
            itemSequenceStep = tag.getInt("Step") + 1;
        } else {
            itemSequenceId = "";
            itemSequenceStep = 1;
        }

        ItemStackHandler itemHandler = new ItemStackHandler(availableItems);

        Optional<ItemCombineRecipe> assemblyRecipe = SequencedAssemblyRecipe.getRecipes(
                level, item, CmmAllRecipeTypes.ITEM_COMBINE.getType(), ItemCombineRecipe.class)
                .filter((it) -> {
                    String id = ItemCombineRecipe.getSequenceId(it);

                    if (id.isEmpty()) return false;

                    // 匹配序列装配, 允许装配起始物品不匹配
                    if (!itemSequenceId.isEmpty() && !id.equals(itemSequenceId) && itemSequenceStep != 1) return false;

                    return ItemCombineRecipe.match(itemHandler, it, itemSequenceStep);
                }).findFirst();
        if (assemblyRecipe.isPresent()) {
            return Optional.of(assemblyRecipe.orElseThrow());
        }

        try {
            RecipeTrie<?> trie = RecipeTrieFinder.get(
                ItemCombineRecipesKey,
                level,
                    ItemCombineHandler::matchStaticFilters
            );
            var availableVariants = RecipeTrie.getVariants(itemHandler, null);

            for (var r : trie.lookup(availableVariants)) {
                if (r instanceof ProcessingRecipe<?>) {
                    recipes.add((ProcessingRecipe<?>) r);
                }
            }
        } catch (Exception e) {
            recipes.clear();
            for (var r : RecipeFinder.get(
                ItemCombineRecipesKey,
                level,
                    ItemCombineHandler::matchStaticFilters
            )) {
                if (r instanceof ProcessingRecipe<?>) {
                    recipes.add((ProcessingRecipe<?>) r);
                }
            }
        }

        if (recipes.isEmpty()) {
            return Optional.empty();
        }

        recipes.sort((r1, r2) -> r2.getIngredients().size() - r1.getIngredients().size());
        return Optional.of(recipes.get(0));
    }

    public static void performCrafting(
        ItemEntity mainItem,
        List<ItemEntity> nearbyItems,
        ProcessingRecipe<?> recipe,
        Level level
    ) {
        List<Ingredient> ingredients = recipe.getIngredients();

        List<ItemEntity> allParticipants = new ArrayList<>();
        allParticipants.add(mainItem);
        allParticipants.addAll(
            nearbyItems.stream()
                .filter(item -> item != mainItem && !item.isRemoved())
                .toList()
        );

        List<ItemStack> availableItems = new ArrayList<>();
        for (ItemEntity item : allParticipants) {
            availableItems.add(item.getItem().copy());
        }

        if (!canCraftWithItems(recipe, availableItems)) {
            return;
        }

        List<ItemStack> outputs = recipe.rollResults();

        consumeItems(allParticipants, ingredients);

        if (!outputs.isEmpty()) {
            for (ItemStack output : outputs) {
                ItemEntity resultEntity = new ItemEntity(level, mainItem.getX(), mainItem.getY(), mainItem.getZ(), output.copy());
                resultEntity.setDefaultPickUpDelay();
                level.addFreshEntity(resultEntity);
            }

            ((ServerLevel) level).sendParticles(
                ParticleTypes.FIREWORK,
                mainItem.getX(), mainItem.getY() + 0.5, mainItem.getZ(),
                10, 0.0, 0.0, 0.0, 0.05
            );

            level.playSound(
                null, mainItem.getX(), mainItem.getY(), mainItem.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.5f, 1.5f
            );
        }
    }

    private static boolean canCraftWithItems(ProcessingRecipe<?> recipe, List<ItemStack> availableItems) {
        List<Ingredient> ingredients = recipe.getIngredients();

        if (ingredients.size() > availableItems.size()) {
            return false;
        }

        List<ItemStack> remainingItems = new ArrayList<>(availableItems);

        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < remainingItems.size(); i++) {
                if (ingredient.test(remainingItems.get(i))) {
                    remainingItems.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    private static void consumeItems(List<ItemEntity> items, List<Ingredient> ingredients) {
        List<ItemStack> remainingItems = new ArrayList<>();
        for (ItemEntity item : items) {
            remainingItems.add(item.getItem().copy());
        }

        for (Ingredient ingredient : ingredients) {
            for (int i = 0; i < remainingItems.size(); i++) {
                if (ingredient.test(remainingItems.get(i))) {
                    ItemEntity entity = items.get(i);
                    processedItems.add(entity);

                    int finalI = i;
                    ItemStack[] matchingItems = Arrays.stream(ingredient.getItems())
                        .filter(item -> remainingItems.get(finalI).getItem() == item.getItem())
                        .toArray(ItemStack[]::new);

                    if (matchingItems.length > 0) {
                        entity.getItem().shrink(matchingItems[0].getCount());
                    }

                    if (entity.getItem().isEmpty()) {
                        entity.discard();
                    }

                    remainingItems.remove(i);
                    items.remove(i);
                    break;
                }
            }
        }
    }
}
