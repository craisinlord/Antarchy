package com.craisinlord.antarchy.mixins.compat.sodium;

import com.craisinlord.antarchy.content.client.renderer.sodium.SodiumPortalTerrain;
import java.util.ArrayDeque;
import java.util.Map;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.TaskQueueType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class RenderSectionManagerPortalTasksMixin {
    @Shadow(remap = false)
    private Map<TaskQueueType, ArrayDeque<RenderSection>> taskLists;

    @Inject(method = "update", at = @At("TAIL"), remap = false)
    private void antarchy$addPortalViewTasks(CallbackInfo ci) {
        SodiumPortalTerrain.drainPendingTasks(this.taskLists);
    }
}
