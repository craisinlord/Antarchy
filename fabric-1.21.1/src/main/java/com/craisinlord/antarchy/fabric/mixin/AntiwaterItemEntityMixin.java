package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.fabric.util.CustomFluidPhysicsChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class AntiwaterItemEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$applyAntiwaterItemMovement(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (!antarchy$isTouchingAntiwater(entity)) {
            return;
        }

        Vec3 motion = entity.getDeltaMovement();
        Vec3 antiwaterFlow = CustomFluidPhysicsChecks.getAntiwaterFlow(entity);
        Vec3 push = antiwaterFlow.lengthSqr() > 1.0E-6D ? antiwaterFlow.normalize().scale(0.02D) : Vec3.ZERO;
        entity.setDeltaMovement(
                motion.x * 0.99D + push.x,
                motion.y * 0.8D + 0.025D + push.y,
                motion.z * 0.99D + push.z
        );
    }

    private boolean antarchy$isTouchingAntiwater(ItemEntity entity) {
        BlockPos min = BlockPos.containing(entity.getBoundingBox().minX, entity.getBoundingBox().minY, entity.getBoundingBox().minZ);
        BlockPos max = BlockPos.containing(entity.getBoundingBox().maxX, entity.getBoundingBox().maxY, entity.getBoundingBox().maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    if (AntarchyFluidChecks.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
