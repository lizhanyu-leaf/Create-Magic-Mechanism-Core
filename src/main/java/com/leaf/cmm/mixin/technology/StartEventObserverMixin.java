package com.leaf.cmm.mixin.technology;

import com.leaf.cmm.jei.JEIRestarter;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.startup.StartEventObserver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StartEventObserver.class)
public abstract class StartEventObserverMixin {
    @Shadow(remap = false) protected abstract void restart();

    @Inject(method = "register", at = @At("HEAD"), remap = false)
    public void onRegister(PermanentEventSubscriptions subscriptions, CallbackInfo ci) {
        JEIRestarter.JEI_RESTART = this::restart;
    }
}
