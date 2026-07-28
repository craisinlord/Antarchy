package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CampfireBlockEntity.class)
public abstract class DreamCampfireBlockEntityMixin {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            ),
            index = 0
    )
    private static BlockEntityType<? extends BlockEntity> antarchy$useDreamCampfireType(
            BlockEntityType<? extends BlockEntity> original
    ) {
        return DreamCampfireBlockEntity.isConstructingDreamCampfire()
                ? AntarchyObjects.DREAM_CAMPFIRE_BLOCK_ENTITY.get()
                : original;
    }
}
