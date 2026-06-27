package com.leaf.cmm;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

import static com.leaf.cmm.CreateMagicMechanismCore.REGISTRATE;

public final class CmmAllItems {

    public static final ItemEntry<Item> REDPRINT = REGISTRATE
            .item("redprint", Item::new)
            .register();

    public static void register() {}
}
