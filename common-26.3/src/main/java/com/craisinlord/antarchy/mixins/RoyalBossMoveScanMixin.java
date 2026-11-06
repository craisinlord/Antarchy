package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalSectionScan;
import com.craisinlord.anthology.gravity.GravityApi;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class RoyalBossMoveScanMixin {
    @WrapOperation(
            method = "collide",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 antarchy$skipRoyalAirCollision(Entity entity, Vec3 movement, AABB box, Level level, List<VoxelShape> shapes, Operation<Vec3> original) {
        if ((Object) this instanceof RoyalBossEntity && !level.isClientSide() && shapes.isEmpty()
                && !GravityApi.isGravityInverted(entity)) {
            AABB swept = box.expandTowards(movement);
            if (!level.getWorldBorder().isInsideCloseToBorder(entity, swept) && RoyalSectionScan.isAllAir(level, swept)) {
                return movement;
            }
        }
        return original.call(entity, movement, box, level, shapes);
    }
}
