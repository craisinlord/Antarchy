package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.DreamCeilingFireBlock;
import com.craisinlord.antarchy.content.block.DreamFireBlock;
import com.craisinlord.antarchy.content.fire.AntarchyFireVisualAccess;
import com.craisinlord.antarchy.content.fire.AntarchyFireVisualType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public abstract class FireVisualBaseFireBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void antarchy$trackFireVisual(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity.fireImmune() || !(entity instanceof AntarchyFireVisualAccess access)) {
            return;
        }

        if (state.getBlock() instanceof DreamFireBlock || state.getBlock() instanceof DreamCeilingFireBlock) {
            access.antarchy$setFireVisualType(AntarchyFireVisualType.DREAM);
            return;
        }

        if (state.getBlock() instanceof SoulFireBlock) {
            access.antarchy$setFireVisualType(AntarchyFireVisualType.SOUL);
            return;
        }

        access.antarchy$setFireVisualType(AntarchyFireVisualType.NORMAL);
    }
}
