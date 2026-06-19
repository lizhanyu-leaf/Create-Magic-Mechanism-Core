package com.leaf.cmm.jei;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * JEI 配方类别辅助工具类
 *
 *
 * 提供用于创建和配置 JEI (Just Enough Items) 配方类别的便捷方法。
 * 主要用于简化 Create 模组的 JEI 集成，包括催化剂配置、配方类型创建和配方信息构建。
 *
 *
 * <h2>使用示例：</h2>
 * <h3>1. 创建自定义配方类别（推荐方式）：</h3>
 * <pre>{@code public class MyRecipeCategory extends CreateRecipeCategory<MyRecipe> {
 *
 * // 定义 RecipeInfo 实现类
 * public static class RecipeInfo implements RecipeCategoryHelper.RecipeCategoryInfo<MyRecipe> {
 * private static final List<Supplier<? extends ItemStack>> CATALYSTS =
 * RecipeCategoryHelper.getCatalysts(Items.CRAFTING_TABLE);
 *
 * @Override
 * public List<Supplier<? extends ItemStack>> getCatalysts() {
 * return CATALYSTS;
 * }
 *
 * @Override
 * public Class<? extends MyRecipe> getRecipeClass() {
 * return MyRecipe.class;
 * }
 *
 * @Override
 * public IRecipeTypeInfo getRecipeTypeInfo() {
 * return MyModAllRecipeTypes.MY_RECIPE;
 * }
 *
 * @Override
 * public Pair<Integer, Integer> getBackgroundSize() {
 * return Pair.of(178, 120);
 * }
 * }
 *
 * public MyRecipeCategory() {
 * super(new RecipeInfo().createCategoryInfo());
 * }
 *
 * @Override
 * public void setRecipe(IRecipeLayoutBuilder builder, MyRecipe recipe, IFocusGroup focuses) {
 * // 配置配方布局
 * }
 *
 * @Override
 * public void draw(MyRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
 * double mouseX, double mouseY) {
 * // 绘制额外内容
 * }
 * }
 * }</pre>
 *
 * <h3>2. 在 JEI 插件中注册配方类别：</h3>
 * <pre>{@code public class MyJeiPlugin implements IModPlugin {
 * private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
 *
 * private void loadCategories() {
 * allCategories.clear();
 * allCategories.add(new MyRecipeCategory());
 * }
 *
 * @Override
 * public void registerCategories(IRecipeCategoryRegistration registration) {
 * loadCategories();
 * registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
 * }
 *
 * @Override
 * public void registerRecipes(IRecipeRegistration registration) {
 * allCategories.forEach(c -> c.registerRecipes(registration));
 * }
 *
 * @Override
 * public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
 * allCategories.forEach(c -> c.registerCatalysts(registration));
 * }
 * }
 * }</pre>
 *
 * <h3>3. 使用 Builder 方式创建配方类别：</h3>
 * <pre>{@code CreateRecipeCategory.Info<MyRecipe> info = RecipeCategoryHelper.RecipeCategoryInfoBuilder.<MyRecipe>create()
 * .setSingleRecipe(MyModAllRecipeTypes.MY_RECIPE, MyRecipe.class)
 * .catalysts(RecipeCategoryHelper.getCatalysts(Items.CRAFTING_TABLE))
 * .emptyBackground(178, 120)
 * .setTitleFromRecipeInfo(CMMLang::translateDirect)
 * .build();
 *
 * CreateRecipeCategory<MyRecipe> category = new CreateRecipeCategory<>(info) {
 * // 实现抽象方法
 * };
 * }</pre>
 *
 * <h3>4. 配置催化剂物品：</h3>
 * <pre>{@code // 使用单个物品
 * List<Supplier<? extends ItemStack>> catalysts =
 * RecipeCategoryHelper.getCatalysts(Items.DIAMOND, Items.GOLD_INGOT);
 *
 * // 使用 ItemStack（可以指定数量和 NBT）
 * List<Supplier<? extends ItemStack>> catalysts =
 * RecipeCategoryHelper.getCatalysts(
 * new ItemStack(Items.DIAMOND, 2),
 * new ItemStack(Items.GOLD_INGOT)
 * );
 * }</pre>
 *
 * @author leaf
 * @see RecipeCategoryInfo
 * @see RecipeCategoryInfoBuilder
 */
public class RecipeCategoryHelper {

    /**
     * 私有构造函数，防止实例化
     */
    private RecipeCategoryHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * JEI 配方类别信息接口
     *
     *
     * 定义了创建 JEI 配方类别所需的基本信息。实现此接口的类可以提供：
     *
     *  * 配方类类型
     *  * 配方类型信息
     *  * 催化剂物品列表
     *  * GUI 背景尺寸
     *
     * 通过 {@link #createCategoryInfo()} 方法可以自动构建完整的 {@link CreateRecipeCategory.Info} 对象。
     *
     *
     * @param <T> 配方类型，必须是实现了 {@link Container} 的 {@link Recipe} 子类
     * @see #createCategoryInfo()
     * @see RecipeCategoryInfoBuilder
     */
    public interface RecipeCategoryInfo<T extends Recipe<? extends Container>> {
        List<Supplier<? extends ItemStack>> getCatalysts();
        Class<? extends T> getRecipeClass();
        IRecipeTypeInfo getRecipeTypeInfo();
        IDrawable getIcon();
        Pair<Integer, Integer> getBackgroundSize();
        BiFunction<String, List<Object>, MutableComponent> getTranslateDirect();

        default void modify(RecipeCategoryInfoBuilder<T> builder) {
        }

        default CreateRecipeCategory.Info<T> createCategoryInfo() {
            RecipeCategoryInfoBuilder<T> builder = RecipeCategoryInfoBuilder.create();
            builder
                    .setSingleRecipe(this.getRecipeTypeInfo(), this.getRecipeClass())
                    .catalysts(this.getCatalysts())
                    .emptyBackground(this.getBackgroundSize().getFirst(), this.getBackgroundSize().getSecond())
                    .icon(this.getIcon())
                    .setTitleFromRecipeInfo(this.getTranslateDirect());
            modify(builder);
            return builder.build();
        }
    }

    /**
     * JEI 配方类别信息构建器
     *
     *
     * 提供流式 API 来手动构建 {@link CreateRecipeCategory.Info} 对象。
     * 支持两种使用模式：
     *
     *  1. **自动模式**：使用 {@link #setSingleRecipe} 自动配置大部分属性
     *  1. **手动模式**：逐个设置所有属性以获得完全控制
     *
     * @param <T> 配方类型，必须是 {@link Recipe} 的子类
     * @see RecipeCategoryInfo
     * @see #create()
     */
    public static class RecipeCategoryInfoBuilder<T extends Recipe<?>> {
        private RecipeType<T> type;
        private Component title;
        private IDrawable background;
        private IDrawable icon;
        private Supplier<List<T>> recipes;
        private List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        private IRecipeTypeInfo recipeTypeInfo;
        private Class<? extends T> recipeClass;

        private RecipeCategoryInfoBuilder() {
        }

        public CreateRecipeCategory.Info<T> build() {
            Objects.requireNonNull(type, "RecipeType must be set");
            Objects.requireNonNull(title, "Title must be set");
            Objects.requireNonNull(icon, "Icon must be set");
            Objects.requireNonNull(background, "Background must be set");
            Objects.requireNonNull(recipes, "Recipes supplier must be set");

            return new CreateRecipeCategory.Info<>(type, title, background, icon, recipes, catalysts);
        }

        public RecipeCategoryInfoBuilder<T> type(RecipeType<T> type) {
            this.type = Objects.requireNonNull(type, "RecipeType cannot be null");
            return this;
        }

        public RecipeCategoryInfoBuilder<T> title(Component title) {
            this.title = Objects.requireNonNull(title, "Title cannot be null");
            return this;
        }

        public RecipeCategoryInfoBuilder<T> background(IDrawable background) {
            this.background = Objects.requireNonNull(background, "Background cannot be null");
            return this;
        }

        public RecipeCategoryInfoBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public RecipeCategoryInfoBuilder<T> recipes(Supplier<List<T>> recipes) {
            this.recipes = Objects.requireNonNull(recipes, "Recipes supplier cannot be null");
            return this;
        }

        public RecipeCategoryInfoBuilder<T> catalysts(List<Supplier<? extends ItemStack>> catalysts) {
            this.catalysts = Objects.requireNonNull(catalysts, "Catalysts cannot be null");
            return this;
        }

        public RecipeCategoryInfoBuilder<T> addCatalyst(Supplier<? extends ItemStack> catalyst) {
            catalysts.add(catalyst);
            return this;
        }

        @SuppressWarnings("unchecked")
        public RecipeCategoryInfoBuilder<T> setSingleRecipe(IRecipeTypeInfo recipeType, Class<? extends T> recipeClass) {
            Objects.requireNonNull(recipeType, "RecipeTypeInfo cannot be null");
            Objects.requireNonNull(recipeClass, "RecipeClass cannot be null");

            this.recipeTypeInfo = recipeType;
            this.recipeClass = recipeClass;
            this.type = getRecipeType(recipeType.getId(), recipeClass);
            this.recipes = () -> {
                if (Minecraft.getInstance().getConnection() == null) return new ArrayList<>();
                var recipes = Minecraft.getInstance().getConnection().getRecipeManager().getRecipes();
                var filtered = recipes.stream()
                        .filter(r -> r != null && r.getType() == recipeType.getType())
                        .map(r -> (T) r)
                        .toList();
                return new ArrayList<>(filtered);
            };
            return this;
        }

        public RecipeCategoryInfoBuilder<T> emptyBackground(int width, int height) {
            this.background = new EmptyBackground(width, height);
            return this;
        }

        public RecipeCategoryInfoBuilder<T> setTitleFromRecipeInfo(BiFunction<String, List<Object>, MutableComponent> translateDirect) {
            Objects.requireNonNull(recipeTypeInfo, "RecipeTypeInfo is null. Call setSingleRecipe() first.");
            Objects.requireNonNull(recipeClass, "RecipeClass is null. Call setSingleRecipe() first.");

            this.title = translateDirect.apply("recipe." + recipeTypeInfo.getId().getPath(), new ArrayList<>());
            return this;
        }

        public RecipeCategoryInfoBuilder<T> addModify(Consumer<CreateRecipeCategory.Info<T>> modify) {
            modify.accept(new CreateRecipeCategory.Info<>(type, title, background, icon, recipes, catalysts));
            return this;
        }

        public static <T extends Recipe<?>> RecipeCategoryInfoBuilder<T> create() {
            return new RecipeCategoryInfoBuilder<>();
        }
    }

    public static <T> RecipeType<T> getRecipeType(ResourceLocation recipeId, Class<? extends T> recipeClass) {
        return RecipeType.create(recipeId.getNamespace(), recipeId.getPath(), recipeClass);
    }

    public static List<Supplier<? extends ItemStack>> getCatalysts(Item... items) {
        List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>(items.length);
        for (Item item : items) {
            Objects.requireNonNull(item, "Item cannot be null");
            catalysts.add(() -> item.getDefaultInstance());
        }
        return Collections.unmodifiableList(catalysts);
    }

    public static List<Supplier<? extends ItemStack>> getCatalysts(ItemStack... items) {
        List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>(items.length);
        for (ItemStack item : items) {
            Objects.requireNonNull(item, "ItemStack cannot be null");
            catalysts.add(() -> item);
        }
        return Collections.unmodifiableList(catalysts);
    }
}
