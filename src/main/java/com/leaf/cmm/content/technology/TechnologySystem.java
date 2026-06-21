package com.leaf.cmm.content.technology;

import com.google.common.collect.Maps;
import com.leaf.cmm.CmmAllPackets;
import com.leaf.cmm.mixin.technology.RecipeManagerAccessor;
import com.leaf.cmm.packet.RestartJEIPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TechnologySystem {
    public static final Map<String, List<Recipe<?>>> TECHNOLOGY_RECIPES = Maps.newHashMap();
    public static final Map<String, Boolean> TECHNOLOGY_RECIPES_ADDED = Maps.newHashMap();
    public static final RestartJEIDelay RESTART_JEI_DELAY = new RestartJEIDelay(500);

    private static Boolean dirty = false;

    public static void apply(MinecraftServer server, boolean notifyJEI) {
        apply(server.getRecipeManager());
        if (notifyJEI) {
            RESTART_JEI_DELAY.start(server);
        }
    }

    public static void clear() {
        TECHNOLOGY_RECIPES.clear();
        TECHNOLOGY_RECIPES_ADDED.clear();
    }

    public static void setDirty() {
        TechnologySystem.dirty = true;
    }

    public static void apply(RecipeManager manager) {
        // 检测是否需要更新
        if (!dirty) return;

        var accessor = (RecipeManagerAccessor) manager;
        var originalRecipes = accessor.getMapRecipes();
        var originalByName = accessor.getByName();
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes =
                Maps.newHashMap();
        newRecipes.putAll(originalRecipes);
        Map<ResourceLocation, Recipe<?>> newByName =
                Maps.newHashMap();
        newByName.putAll(originalByName);

        applyTechnologyRecipes(newRecipes, newByName);

        accessor.setRecipes(newRecipes);
        accessor.setByName(newByName);
    }

    public static void applyTechnologyRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes,
                                              Map<ResourceLocation, Recipe<?>> byName) {
        // 移除所有科技配方
        if (!TECHNOLOGY_RECIPES_ADDED.isEmpty()) {
            for (var removeEntry : TECHNOLOGY_RECIPES.entrySet()) {
                var removeRecipes = removeEntry.getValue();
                if (TECHNOLOGY_RECIPES_ADDED.getOrDefault(removeEntry.getKey(), true)) continue;
                for (var recipe : removeRecipes) {
                    var type = recipe.getType();
                    var id = recipe.getId();
                    recipes.computeIfPresent(type, (k, v) -> {
                        v.remove(id);
                        return v;
                    });
                    byName.remove(id);
                }
            }
        }

        // 添加启用的科技配方
        for (var entry : TECHNOLOGY_RECIPES.entrySet()) {
            if (TechnologyStorage.getInstance() == null) break;
            if (!TechnologyStorage.getInstance().isActive(entry.getKey())) continue;
            if (TECHNOLOGY_RECIPES_ADDED.getOrDefault(entry.getKey(), false))
                continue;
            for (var recipe : entry.getValue()) {
                var type = recipe.getType();
                var id = recipe.getId();
                recipes.computeIfAbsent(type, k -> Maps.newHashMap()).put(id, recipe);
                byName.put(id, recipe);
            }
            TECHNOLOGY_RECIPES_ADDED.put(entry.getKey(), true);
        }

        dirty = false;
    }

    /**
     * 延迟重启JEI
     */
    public static class RestartJEIDelay {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture<?> future;
        private final long delayMillis;

        private static void task(MinecraftServer server) {
            var recipePacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes());
            var restartPacket = new RestartJEIPacket();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(recipePacket);
                CmmAllPackets.getChannel().send(PacketDistributor.PLAYER.with(()->player), restartPacket);
            }
        }

        /**
         * 创建一个延迟重启JEI的定时器
         * @param delayMillis 延迟时间，单位毫秒
         */
        public RestartJEIDelay(long delayMillis) {
            this.delayMillis = delayMillis;
        }

        /**
         * 启动定时器
         * @param server 服务器实例
         */
        public synchronized void start(MinecraftServer server) {
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            future = scheduler.schedule(() -> task(server), delayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
