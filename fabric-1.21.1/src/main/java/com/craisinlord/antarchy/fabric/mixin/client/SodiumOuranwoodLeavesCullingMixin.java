package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.block.LeafFaceCulling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.fabric.block.FabricBlockAccess", remap = false)
public abstract class SodiumOuranwoodLeavesCullingMixin {

    @Inject(method = "shouldSkipRender", at = @At("HEAD"), cancellable = true, remap = false)
    private void antarchy$cullBuriedLeafFaces(BlockGetter level, BlockState state, BlockState neighbor, BlockPos pos, BlockPos neighborPos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (LeafFaceCulling.appliesTo(neighbor) && LeafFaceCulling.hidesNeighborFace(level, neighborPos, state)) {
            cir.setReturnValue(true);
        }
    }
}
