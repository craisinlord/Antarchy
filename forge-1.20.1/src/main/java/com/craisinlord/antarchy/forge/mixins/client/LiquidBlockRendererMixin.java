package com.craisinlord.antarchy.forge.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.AntiwaterFluidRenderer;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public abstract class LiquidBlockRendererMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderAntiwater(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer buffer,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci
    ) {
        if (!AntarchyFluidChecks.isAntiwater(fluidState)) {
            return;
        }

        TextureAtlasSprite[] sprites = ForgeHooksClient.getFluidSprites(level, pos, fluidState);
        int tint = IClientFluidTypeExtensions.of(fluidState).getTintColor(fluidState, level, pos);
        AntiwaterFluidRenderer.render(level, pos, buffer, blockState, fluidState, sprites[0], sprites[1], sprites[2], tint);
        ci.cancel();
    }
}
