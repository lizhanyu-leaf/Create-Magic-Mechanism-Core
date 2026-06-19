package com.leaf.cmm.mixin;

import com.leaf.cmm.content.itemcombine.ItemCombineHandler;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        
        if (self.level().isClientSide()) return;
        
        if (self.tickCount % 5 != 0) return;
        
        if (self.getItem().isEmpty()) return;

        if (ItemCombineHandler.isProcessedItem(self)) return;
        
        Level level = self.level();
        BlockPos pos = self.blockPosition();

        ItemCombineHandler.clearProcessedItems();
        
        AABB searchArea = new AABB(pos.offset(-1, -1, -1), pos.offset(1, 1, 1));
        
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, searchArea).stream()
                .filter(item -> item != self && !ItemCombineHandler.isProcessedItem(item)).toList();
        
        if (nearbyItems.isEmpty()) return;
        
        Optional<ProcessingRecipe<?>> matchingRecipe = ItemCombineHandler.findMatchingRecipe(self, nearbyItems);

        if (matchingRecipe.isEmpty()) return;

        ItemCombineHandler.performCrafting(self, nearbyItems, matchingRecipe.orElseThrow(), level);
    }
}
