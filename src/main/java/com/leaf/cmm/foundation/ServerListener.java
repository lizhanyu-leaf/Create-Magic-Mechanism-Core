package com.leaf.cmm.foundation;

import com.leaf.cmm.CreateMagicMechanismCore;
import com.leaf.cmm.content.technology.TechnologyStorage;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateMagicMechanismCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerListener {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TechnologyStorage storage = TechnologyStorage.getOrCreateInstance(event.getServer());
        CreateMagicMechanismCore.LOGGER.info("科技数据已加载, 存档路径: " + storage.getFilePath());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // 服务器关闭时保存并重置
        TechnologyStorage storage = TechnologyStorage.getInstance();
        if (storage == null) return;
        storage.save();
        TechnologyStorage.reset();
    }
}
