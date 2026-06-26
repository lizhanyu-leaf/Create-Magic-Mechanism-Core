package com.leaf.cmm.mixin.gauge;

import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FactoryPanelBehaviour.class)
public abstract class FactoryPanelBehaviourMixin extends FilteringBehaviour implements MenuProvider {

    @Shadow(remap = false) public abstract FactoryPanelBlockEntity panelBE();

    @Shadow(remap = false) public RequestPromiseQueue restockerPromises;

    @Shadow(remap = false) public boolean satisfied;

    @Shadow(remap = false) public boolean promisedSatisfied;

    @Shadow(remap = false) public boolean waitingForNetwork;

    @Shadow(remap = false) public boolean redstonePowered;

    @Shadow(remap = false) private int timer;

    @Shadow(remap = false) public abstract void resetTimer();

    @Shadow(remap = false) public String recipeAddress;

    @Shadow(remap = false) protected abstract void tryRestock();

    @Shadow(remap = false) protected abstract int getConfigRequestIntervalInTicks();

    // 空构造函数，过编译
    public FactoryPanelBehaviourMixin(SmartBlockEntity be, ValueBoxTransform slot) {super(be, slot);}

    /**
     * @author Leaf
     * @reason 移除工厂仪表的主动供应能力
     */
    @Overwrite(remap = false)
    private void tickRequests() {
        FactoryPanelBlockEntity panelBE = this.panelBE();

        // 只保留补给逻辑
        if (panelBE.restocker) {
            this.restockerPromises.tick();

            if (!this.satisfied && !this.promisedSatisfied && !this.waitingForNetwork && !this.redstonePowered) {
                if (this.timer > 0) {
                    this.timer = Math.min(this.timer, this.getConfigRequestIntervalInTicks());
                    --this.timer;
                } else {
                    this.resetTimer();
                    if (!this.recipeAddress.isBlank()) {
                        this.tryRestock();  // 只调用补给
                    }
                }
            }
        }
    }

    /**
     * 拦截菜单创建
     * @reason 取消工厂仪表的主动供应能力
     */
    @Inject(method = "createMenu", at = @At("RETURN"), cancellable = true, remap = false)
    public void createMenu(int containerId, Inventory playerInventory, Player player, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (!this.panelBE().restocker) cir.setReturnValue(null);
    }

    /**
     * 拦截菜单创建
     * @reason 取消工厂仪表的主动供应能力
     */
    @Inject(method = "displayScreen", at = @At("HEAD"), cancellable = true, remap = false)
    public void createMenu(Player player, CallbackInfo ci) {
        if (!this.panelBE().restocker) ci.cancel();
    }
}
