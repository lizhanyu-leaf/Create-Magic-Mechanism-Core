package com.leaf.cmm.content.redprint;

import com.leaf.cmm.CmmAllItems;
import com.leaf.cmm.CmmAllPackets;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RedprintHandler {
    public static final RedprintHandler INSTANCE = new RedprintHandler();

    private final Object outlineSlot = new Object();

    public BlockPos firstPos;
    public BlockPos secondPos;
    private BlockPos selectedPos;
    private Direction selectedFace;
    private int range = 10;

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (level == null || player == null || !CmmAllItems.REDPRINT.isIn(player.getMainHandItem()))
            return;

        if (AllKeys.ACTIVATE_TOOL.isPressed()) {
            float pt = AnimationTickHolder.getPartialTicks();
            Vec3 targetVec = player.getEyePosition(pt).add(player.getLookAngle().scale(range));
            selectedPos = BlockPos.containing(targetVec);
        } else {
            BlockHitResult trace = RaycastHelper.rayTraceRange(player.level(), player, 75);
            if (trace != null && trace.getType() == HitResult.Type.BLOCK) {
                BlockPos hit = trace.getBlockPos();
                boolean replaceable = level.getBlockState(hit)
                        .canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, trace)));
                if (trace.getDirection().getAxis().isVertical() && !replaceable)
                    hit = hit.relative(trace.getDirection());
                selectedPos = hit;
            } else
                selectedPos = null;
        }

        selectedFace = null;
        if (secondPos != null) {
            AABB bb = new AABB(firstPos, secondPos).expandTowards(1, 1, 1)
                    .inflate(.45f);
            Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
                    .getPosition();
            boolean inside = bb.contains(projectedView);
            RaycastHelper.PredicateTraceResult result =
                    RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));
            selectedFace = result.missed() ? null
                    : inside ? result.getFacing()
                    .getOpposite() : result.getFacing();
        }

        AABB currentSelectionBox = getCurrentSelectionBox();
        if (currentSelectionBox != null)
            Outliner.getInstance().chaseAABB(outlineSlot, currentSelectionBox)
                    .colored(0xFF6464)
                    .withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
                    .lineWidth(1 / 16f)
                    .highlightFace(selectedFace);
    }

    private AABB getCurrentSelectionBox() {
        if (secondPos == null) {
            if (firstPos == null)
                return selectedPos == null ? null : new AABB(selectedPos);
            return selectedPos == null ? new AABB(firstPos) : new AABB(firstPos, selectedPos).expandTowards(1, 1, 1);
        }
        return new AABB(firstPos, secondPos).expandTowards(1, 1, 1);
    }

    public boolean mouseScrolled(double delta) {
        if (!AllKeys.ctrlDown())
            return false;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (level == null || player == null || !CmmAllItems.REDPRINT.isIn(player.getMainHandItem()))
            return false;

        if (secondPos == null)
            range = Mth.clamp(range + (int) delta, 1, 100);
        if (selectedFace == null)
            return true;

        // noinspection DataFlowIssue
        AABB bb = new AABB(firstPos, secondPos);
        Vec3i vec = selectedFace.getNormal();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        if (bb.contains(projectedView))
            delta *= -1;

        int x = (int) (vec.getX() * delta);
        int y = (int) (vec.getY() * delta);
        int z = (int) (vec.getZ() * delta);

        Direction.AxisDirection axisDirection = selectedFace.getAxisDirection();
        if (axisDirection == Direction.AxisDirection.NEGATIVE)
            bb = bb.move(-x, -y, -z);

        double maxX = Math.max(bb.maxX - x * axisDirection.getStep(), bb.minX);
        double maxY = Math.max(bb.maxY - y * axisDirection.getStep(), bb.minY);
        double maxZ = Math.max(bb.maxZ - z * axisDirection.getStep(), bb.minZ);
        bb = new AABB(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);

        firstPos = BlockPos.containing(bb.minX, bb.minY, bb.minZ);
        secondPos = BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);

        player.displayClientMessage(Component.translatable("cmm.redprint.dimensions",
                (int) bb.getXsize() + 1, (int) bb.getYsize() + 1, (int) bb.getZsize() + 1), true);

        return true;
    }

    public boolean onMouseInput(int button, boolean pressed) {
        if (!pressed || button != 1)
            return false;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (level == null || player == null || !CmmAllItems.REDPRINT.isIn(player.getMainHandItem()))
            return false;

        if (player.isShiftKeyDown()) {
            firstPos = null;
            secondPos = null;
            player.displayClientMessage(Component.translatable("cmm.redprint.abort"), true);
            return true;
        }

        if (secondPos != null) {
            BoundingBox bounds = BoundingBox.fromCorners(firstPos, secondPos);
            CmmAllPackets.getChannel().sendToServer(new RedprintPacket(
                    new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ()),
                    new BlockPos(bounds.getLength().offset(1, 1, 1))
            ));
            firstPos = null;
            secondPos = null;
            return true;
        }

        if (selectedPos == null) {
            player.displayClientMessage(Component.translatable("cmm.redprint.noTarget"), true);
            return true;
        }

        if (firstPos != null) {
            secondPos = selectedPos;
            player.displayClientMessage(Component.translatable("cmm.redprint.secondPos"), true);
            return true;
        }

        firstPos = selectedPos;
        player.displayClientMessage(Component.translatable("cmm.redprint.firstPos"), true);
        return true;
    }
}
