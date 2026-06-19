package com.leaf.cmm.blocks;

import com.leaf.cmm.CreateMagicMechanismCore;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.MapColor;

public final class CmmAllBlocks {
    public static final BlockEntry<?> TECHNOLOGY_BLOCK = CreateMagicMechanismCore.REGISTRATE
            .block("technology_block", UploadTaskBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.strength(1.5f))
            .tag(BlockTags.NEEDS_STONE_TOOL)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .simpleItem()
            .register();
    public static void register() {}
}
