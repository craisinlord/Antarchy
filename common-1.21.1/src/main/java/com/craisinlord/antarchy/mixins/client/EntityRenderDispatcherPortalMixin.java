package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherPortalMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void antarchy$clipPortalViewEntities(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (PortalGunPortalRenderState.renderAll() || entity instanceof PortalGunPortalEntity) {
            return;
        }
        PortalGunWorldPortalShape destinationShape = PortalGunPortalRenderState.getDestinationShape();
        PortalGunWorldPortalShape sourceShape = PortalGunPortalRenderState.getSourceShape();
        if (destinationShape == null) {
            return;
        }
        if (!destinationShape.intersectsFront(entity.getBoundingBox(), 0.1D)) {
            cir.setReturnValue(false);
            return;
        }
        if (sourceShape != null && sourceShape.intersectsFront(entity.getBoundingBox(), -0.02D)) {
            cir.setReturnValue(false);
        }
    }
}
