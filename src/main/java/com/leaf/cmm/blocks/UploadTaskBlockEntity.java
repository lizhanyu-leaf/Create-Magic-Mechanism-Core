package com.leaf.cmm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class UploadTaskBlockEntity extends BlockEntity {
    public UploadTaskBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    public UploadTaskBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        this(CmmAllBlockEntities.UPLOAD_TASK_BLOCK_ENTITY.get(), p_155229_, p_155230_);
    }
}
