package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
/*
 * Lets dream fire pick the floor or ceiling variant from the same support tag.
 */
public abstract class DreamFireBlockMixin {
    private static final ResourceLocation DREAM_FIRE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_fire");
    private static final ResourceLocation DREAM_CEILING_FIRE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_fire_ceiling");
    private static final ResourceLocation THORAXIS_DIMENSION_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");

    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private static void antarchy$useDreamFire(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        boolean thoraxis = level instanceof Level concreteLevel && concreteLevel.dimension().location().equals(THORAXIS_DIMENSION_ID);

        if (thoraxis) {
            if (level.getBlockState(pos.above()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS)) {
                BuiltInRegistries.BLOCK.getOptional(DREAM_CEILING_FIRE_ID).ifPresent(block -> cir.setReturnValue(block.defaultBlockState()));
                return;
            }

            if (level.getBlockState(pos.below()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS)) {
                BuiltInRegistries.BLOCK.getOptional(DREAM_FIRE_ID).ifPresent(block -> cir.setReturnValue(block.defaultBlockState()));
            }
            return;
        }

        if (level.getBlockState(pos.below()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS)) {
            BuiltInRegistries.BLOCK.getOptional(DREAM_FIRE_ID).ifPresent(block -> cir.setReturnValue(block.defaultBlockState()));
            return;
        }

        if (level.getBlockState(pos.above()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS)) {
            BuiltInRegistries.BLOCK.getOptional(DREAM_CEILING_FIRE_ID).ifPresent(block -> cir.setReturnValue(block.defaultBlockState()));
        }
    }
}
