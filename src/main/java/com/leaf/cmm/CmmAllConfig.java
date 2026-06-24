package com.leaf.cmm;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("common");
            ALWAYS_FLY = builder.comment("The players can fly in survival").define("always_fly", true);
            builder.pop();
        }
    }

    public static boolean alwaysFly;

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        alwaysFly = COMMON.ALWAYS_FLY.get();
    }
}
