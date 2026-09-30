package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalViewAreaManager;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
public abstract class PortalViewChunkCacheMixin {
    @Inject(method = "drop", at = @At("TAIL"))
    private void antarchy$invalidatePortalChunk(ChunkPos pos, CallbackInfo ci) {
        PortalGunPortalViewAreaManager.onChunkDropped(pos);
    }
}
