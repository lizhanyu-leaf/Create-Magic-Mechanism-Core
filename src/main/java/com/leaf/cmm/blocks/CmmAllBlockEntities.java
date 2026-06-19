package com.leaf.cmm.blocks;

import com.leaf.cmm.CreateMagicMechanismCore;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CmmAllBlockEntities {
    public static final BlockEntityEntry<BlockEntity> UPLOAD_TASK_BLOCK_ENTITY =
            CreateMagicMechanismCore.REGISTRATE.blockEntity("upload_task_block", UploadTaskBlockEntity::new)
                    .register();
}
