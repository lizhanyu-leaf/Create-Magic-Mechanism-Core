package com.leaf.cmm.data;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CmmDatagen {

    @SubscribeEvent
    public static void datagen(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        generator.addProvider(
                event.includeServer(), new CmmRecipeProvider(packOutput)
        );
    }
}
