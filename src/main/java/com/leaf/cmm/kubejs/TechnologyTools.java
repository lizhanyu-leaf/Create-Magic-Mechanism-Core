package com.leaf.cmm.kubejs;

import com.leaf.cmm.content.technology.TechnologyStorage;
import com.leaf.cmm.content.technology.TechnologySystem;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.server.MinecraftServer;

public class TechnologyTools extends EventJS {

    public void setActive(String technologyId, boolean active) {
        if (TechnologyStorage.getInstance() == null) return;
        TechnologyStorage.getInstance().setActive(technologyId, active);
    }

    public boolean isActive(String technologyId) {
        if (TechnologyStorage.getInstance() == null) return false;
        return TechnologyStorage.getInstance().isActive(technologyId);
    }

    public void applyTechnology(MinecraftServer server) {
        TechnologySystem.apply(server.getRecipeManager());
    }
}
