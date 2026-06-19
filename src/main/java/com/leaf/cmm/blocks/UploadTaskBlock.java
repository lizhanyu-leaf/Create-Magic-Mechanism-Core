package com.leaf.cmm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class UploadTaskBlock extends BaseEntityBlock {
    protected UploadTaskBlock(Properties p_49224_) {
        super(p_49224_);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new UploadTaskBlockEntity(p_153215_, p_153216_);
    }
}
