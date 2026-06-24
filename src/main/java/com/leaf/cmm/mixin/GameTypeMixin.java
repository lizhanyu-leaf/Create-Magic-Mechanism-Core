package com.leaf.cmm.mixin;

import com.leaf.cmm.CmmAllConfig;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameType.class)
public class GameTypeMixin {

    @Inject(method = "updatePlayerAbilities", at = @At("TAIL"))
    void alwaysAllowFly(Abilities p_46399_, CallbackInfo ci) {
        if (CmmAllConfig.alwaysFly)
            p_46399_.mayfly = true;
    }
}
