package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityLightProbeMixin {
    @Inject(method = "getLightProbePosition", at = @At("RETURN"), cancellable = true)
    private void antarchy$mirrorLightProbe(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this instanceof AntimetalMinecartAccess access && access.antarchy$isOnAntimetalRail()) {
            Entity self = (Entity) (Object) this;
            Vec3 original = cir.getReturnValue();
            double mirroredY = original.y - 2.0D * self.getEyeHeight();
            cir.setReturnValue(new Vec3(original.x, mirroredY, original.z));
        }
    }
}
