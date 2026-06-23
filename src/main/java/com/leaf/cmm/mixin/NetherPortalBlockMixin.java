package com.leaf.cmm.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    // 非玩家不进入传送门
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof Player)) ci.cancel();
    }
}
