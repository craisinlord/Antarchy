package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class PrimordialGrassLevelMixin {
    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("RETURN"))
    private void antarchy$grassLightChanged(int sectionX, int sectionY, int sectionZ, CallbackInfo callback) {
        PrimordialGrassRenderer.lightChanged(sectionX, sectionY, sectionZ);
    }

    @Inject(method = "onChunkLoaded", at = @At("RETURN"))
    private void antarchy$grassChunkLoaded(ChunkPos pos, CallbackInfo callback) {
        PrimordialGrassRenderer.chunkLoaded(pos);
    }
}
