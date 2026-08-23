package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunRollClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererPortalMixin {
    @Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true)
    private static void antarchy$ignorePortalViewBlock(Player player, CallbackInfoReturnable<net.minecraft.world.level.block.state.BlockState> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player != null && PortalGunRollClientState.isEntityInsidePortal(minecraft, player)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
