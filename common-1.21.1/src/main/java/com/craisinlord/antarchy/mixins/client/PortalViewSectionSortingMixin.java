package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalViewAreaManager;
import com.craisinlord.antarchy.content.client.renderer.PortalViewSectionMarker;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Unique;
import java.util.Collection;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class PortalViewSectionSortingMixin implements PortalViewSectionMarker {
    @Unique
    private boolean antarchy$portalViewSection;

    @Override
    public void antarchy$markPortalViewSection() {
        this.antarchy$portalViewSection = true;
    }

    @Override
    public boolean antarchy$isPortalViewSection() {
        return this.antarchy$portalViewSection;
    }

    @ModifyExpressionValue(method = "createVertexSorting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;getCameraPosition()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 antarchy$sortPortalGeometryFromVirtualCamera(Vec3 original) {
        return PortalGunPortalViewAreaManager.cameraForSection((SectionRenderDispatcher.RenderSection) (Object) this, original);
    }

    @Redirect(method = "updateGlobalBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;updateGlobalBlockEntities(Ljava/util/Collection;Ljava/util/Collection;)V"))
    private void antarchy$routePortalGlobalBlockEntities(LevelRenderer renderer, Collection<BlockEntity> removed, Collection<BlockEntity> added) {
        if (!PortalGunPortalViewAreaManager.updateGlobalBlockEntities((SectionRenderDispatcher.RenderSection) (Object) this, removed, added)) {
            renderer.updateGlobalBlockEntities(removed, added);
        }
    }
}
