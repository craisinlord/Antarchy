package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalSectionGraphMarker;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionOcclusionGraph.class)
public abstract class SectionOcclusionGraphPortalMixin implements PortalSectionGraphMarker {
    @Unique
    private volatile boolean antarchy$portalGraph;

    @Override
    public void antarchy$markPortalGraph() {
        this.antarchy$portalGraph = true;
    }

    @Inject(method = "getRelativeFrom", at = @At("RETURN"), cancellable = true)
    private void antarchy$rejectWrappedPortalSections(BlockPos cameraSectionPos, SectionRenderDispatcher.RenderSection section,
                                                      Direction direction, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (!this.antarchy$portalGraph) {
            return;
        }
        SectionRenderDispatcher.RenderSection relative = cir.getReturnValue();
        if (relative != null && !relative.getOrigin().equals(section.getRelativeOrigin(direction))) {
            cir.setReturnValue(null);
        }
    }
}
