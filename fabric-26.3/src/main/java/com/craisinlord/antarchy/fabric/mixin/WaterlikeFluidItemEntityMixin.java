package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.fabric.util.CustomFluidPhysicsChecks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemEntity.class, priority = 900)
public abstract class WaterlikeFluidItemEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$applyWaterlikeFluidItemMovement(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (CustomFluidPhysicsChecks.isTouchingAntiwater(entity) || !CustomFluidPhysicsChecks.isTouchingWaterlikeFluid(entity)) {
            return;
        }

        Vec3 motion = entity.getDeltaMovement();
        Vec3 fluidFlow = CustomFluidPhysicsChecks.getWaterlikeFlow(entity);
        Vec3 push = fluidFlow.lengthSqr() > 1.0E-6D ? fluidFlow.normalize().scale(0.03D) : Vec3.ZERO;
        entity.setDeltaMovement(
                motion.x * 0.99D + push.x,
                motion.y + (motion.y < 0.06D ? 5.0E-4D : 0.0D) + push.y,
                motion.z * 0.99D + push.z
        );
    }
}
