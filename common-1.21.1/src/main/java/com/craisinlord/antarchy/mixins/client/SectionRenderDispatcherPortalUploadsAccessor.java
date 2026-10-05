package com.craisinlord.antarchy.mixins.client;

import java.util.Queue;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionRenderDispatcher.class)
public interface SectionRenderDispatcherPortalUploadsAccessor {
    @Accessor("toUpload")
    Queue<Runnable> antarchy$getToUpload();
}
