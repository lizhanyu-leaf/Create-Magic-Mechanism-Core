package com.leaf.cmm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = CreateMagicMechanismCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CmmAllConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        var pair = BUILDER.configure(Common::new);
        SPEC = pair.getRight();
        COMMON = pair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue ALWAYS_FLY;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> REDPRINT_BLACKLIST;
        public final ForgeConfigSpec.BooleanValue REDPRINT_BLOCK_UPDATE;
        public final ForgeConfigSpec.IntValue REDPRINT_MAX_REMOVE_BLOCKS;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("player");
            ALWAYS_FLY = builder.comment("The players can fly in survival")
                    .define("always_fly", true);
            builder.pop();
            builder.push("redprint");
            REDPRINT_BLACKLIST = builder.comment("The redprint blacklist")
                    .defineList("redprint_blacklist", List.of(), obj -> obj instanceof String);
            REDPRINT_BLOCK_UPDATE = builder.comment("The redprint will let block be updated")
                    .define("redprint_block_update", false);
            REDPRINT_MAX_REMOVE_BLOCKS = builder.comment("The max blocks can be removed by redprint")
                    .defineInRange("redprint_max_remove_blocks", 256, 1, 2147483647);
            builder.pop();
        }
    }

    public static boolean alwaysFly;
    public static List<Block> redprintBlacklist = new ArrayList<>();
    public static boolean redprintBlockUpdate;
    public static int redprintMaxRemoveBlocks;

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        alwaysFly = COMMON.ALWAYS_FLY.get();

        redprintBlacklist.clear();
        for (var blockId : COMMON.REDPRINT_BLACKLIST.get()) {
            ResourceLocation id = ResourceLocation.tryParse(blockId);
            if (id != null) {
                Block block = ForgeRegistries.BLOCKS.getValue(id);
                if (block != null) {
                    redprintBlacklist.add(block);
                }
            }
        }

        redprintBlockUpdate = COMMON.REDPRINT_BLOCK_UPDATE.get();
        redprintMaxRemoveBlocks = COMMON.REDPRINT_MAX_REMOVE_BLOCKS.get();
    }
}
