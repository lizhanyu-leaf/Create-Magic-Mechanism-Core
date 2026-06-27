package com.leaf.cmm.content.redprint;

import com.leaf.cmm.CmmAllConfig;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public class RedprintPacket extends SimplePacketBase {
    public BlockPos anchor;
    public BlockPos size;

    public RedprintPacket(BlockPos anchor, BlockPos size) {
        this.anchor = anchor;
        this.size = size;
    }

    public RedprintPacket(FriendlyByteBuf buffer) {
        anchor = buffer.readBlockPos();
        size = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeBlockPos(size);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player == null)
            return false;
        Level level = player.level();

        context.enqueueWork(() -> {
            if (exceedsThresholdFast(level, anchor, anchor.offset(size), CmmAllConfig.redprintMaxRemoveBlocks)) {
                player.displayClientMessage(Component.translatable("cmm.redprint.too_many_blocks").withStyle(ChatFormatting.RED), true);
                return;
            }

            for (var i = 0; i < size.getX(); i++) {
                for (var j = 0; j < size.getY(); j++) {
                    for (var k = 0; k < size.getZ(); k++) {
                        var pos = anchor.offset(i, j, k);
                        BlockState state = level.getBlockState(pos);
                        Block block = state.getBlock();

                        // 在黑名单内的方块不回收
                        if (!CmmAllConfig.redprintBlacklist.isEmpty() && CmmAllConfig.redprintBlacklist.contains(block)) continue;

                        ItemStack item = new ItemStack(block);
                        if (!player.addItem(item)) {
                            level.addFreshEntity(
                                    new net.minecraft.world.entity.item.ItemEntity(
                                            level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item
                                    )
                            );
                        }
                        if (!CmmAllConfig.redprintBlockUpdate) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 50);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 51);
                        }
                    }
                }
            }

            player.displayClientMessage(Component.translatable("cmm.redprint.success"), true);
        });
        return true;
    }

    /**
     * 判断区域内非空气方块数量是否超过阈值
     * @param level 世界
     * @param from 区域起点
     * @param to 区域终点
     * @param threshold 阈值
     * @return true = 超过阈值
     */
    public static boolean exceedsThreshold(Level level, BlockPos from, BlockPos to, int threshold) {
        int count = 0;

        for (int x = from.getX(); x <= to.getX(); x++) {
            for (int y = from.getY(); y <= to.getY(); y++) {
                for (int z = from.getZ(); z <= to.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (!state.isAir()) {
                        count++;
                        if (count > threshold) {
                            return true; // 立即退出，不再遍历
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 快速判断区域内非空气方块数量是否超过阈值
     * @param level 世界
     * @param from 区域起点
     * @param to 区域终点
     * @param threshold 阈值
     * @return true = 超过阈值
     */
    public static boolean exceedsThresholdFast(Level level, BlockPos from, BlockPos to, int threshold) {
        // 先算区域总体积，如果体积都不够阈值，直接返回 false
        int totalBlocks = (to.getX() - from.getX() + 1)
                * (to.getY() - from.getY() + 1)
                * (to.getZ() - from.getZ() + 1);

        if (totalBlocks <= threshold) {
            // 即使全是非空气也达不到阈值，不用遍历
            return false;
        }

        // 再遍历
        return exceedsThreshold(level, from, to, threshold);
    }

    /**
     * 采样判断区域内非空气方块数量是否超过阈值
     * @param level 世界
     * @param from 区域起点
     * @param to 区域终点
     * @param threshold 阈值
     * @param sampleStep 采样步长
     * @return true = 超过阈值
     */
    public static boolean exceedsThresholdSampled(Level level, BlockPos from, BlockPos to,
                                                  int threshold, int sampleStep) {
        int count = 0;

        for (int x = from.getX(); x <= to.getX(); x += sampleStep) {
            for (int y = from.getY(); y <= to.getY(); y += sampleStep) {
                for (int z = from.getZ(); z <= to.getZ(); z += sampleStep) {
                    if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                        count++;
                    }
                }
            }
        }

        // 按采样比例估算
        return count * sampleStep * sampleStep * sampleStep > threshold;
    }
}
