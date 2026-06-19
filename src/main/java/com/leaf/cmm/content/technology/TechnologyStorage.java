package com.leaf.cmm.content.technology;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TechnologyStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "technology.json";

    private final Map<String, Boolean> technologies = new HashMap<>();
    private final Path filePath;

    private static TechnologyStorage INSTANCE;

    private TechnologyStorage(Path worldDir) {
        this.filePath = worldDir.resolve(FILE_NAME);
        load();
    }

    public static TechnologyStorage getOrCreateInstance(MinecraftServer server) {
        if (INSTANCE == null) {
            // 用主世界的存档目录
            ServerLevel overworld = server.overworld();
            Path worldDir = overworld.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            INSTANCE = new TechnologyStorage(worldDir);
        }
        return INSTANCE;
    }

    public static TechnologyStorage getInstance() {
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ========== 读写 ==========

    public void load() {
        technologies.clear();
        if (!Files.exists(filePath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            Map<String, Boolean> data = GSON.fromJson(reader, new TypeToken<Map<String, Boolean>>() {}.getType());
            if (data != null) {
                technologies.putAll(data);
            }
        } catch (IOException e) {
            System.err.println("[TechnologyStorage] 加载失败: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(technologies, writer);
            }
        } catch (IOException e) {
            System.err.println("[TechnologyStorage] 保存失败: " + e.getMessage());
        }
    }

    // ========== 操作 ==========

    public boolean isActive(String techId) {
        return technologies.getOrDefault(techId, false);
    }

    public void setActive(String techId, boolean active) {
        technologies.put(techId, active);
        save(); // 立即保存
    }

    public Map<String, Boolean> getAll() {
        return new HashMap<>(technologies);
    }

    // 获取文件路径
    public Path getFilePath() {
        return filePath;
    }
}