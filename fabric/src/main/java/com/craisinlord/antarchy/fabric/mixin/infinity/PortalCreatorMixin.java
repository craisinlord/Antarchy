package com.craisinlord.antarchy.fabric.mixin.infinity;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetherPortalBlock.class, priority = 2000)
public abstract class PortalCreatorMixin {
    @Inject(
            method = "entityInside",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$blockInfinityBookActivation(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!AntarchySettings.disableInfinityBookPortalCreation()) {
            return;
        }

        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.isRemoved()) {
            return;
        }

        ItemStack stack = itemEntity.getItem();
        if (!stack.has(DataComponents.WRITABLE_BOOK_CONTENT) && !stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            return;
        }

        ci.cancel();
    }
}
