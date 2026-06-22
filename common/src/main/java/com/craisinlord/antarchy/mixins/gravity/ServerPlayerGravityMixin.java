package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
/*
 * Fixes server-side fall damage for inverted players.
 */
public abstract class ServerPlayerGravityMixin {
    @Unique
    private float antarchy$invertedFallDistance = 0.0F;

    @Inject(method = "doCheckFallDamage", at = @At("HEAD"), cancellable = true)
    private void antarchy$cancelFallDamageInAntiwater(double x, double y, double z, boolean onGround, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(self);
        if (!gravity.isInverted()) {
            return;
        }
        if (!antarchy$isTouchingAntiwater(self)) {
            return;
        }

        self.fallDistance = 0.0F;
        this.antarchy$invertedFallDistance = 0.0F;
        ci.cancel();
    }

    @WrapOperation(method = "doCheckFallDamage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private void antarchy$fixFallDamage(ServerPlayer instance,
                                         double worldY, boolean onGround,
                                         BlockState state, BlockPos pos,
                                         Operation<Void> original,
                                         @Local(ordinal = 0) double dx,
                                         @Local(ordinal = 1) double dy,
                                         @Local(ordinal = 2) double dz) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(self);
        if (!gravity.isInverted()) {
            this.antarchy$invertedFallDistance = 0.0F;
            original.call(instance, worldY, onGround, state, pos);
            return;
        }
        if (antarchy$isTouchingAntiwater(self) || antarchy$passedThroughAntiwater(self, dx, dy, dz)) {
            self.fallDistance = 0.0F;
            this.antarchy$invertedFallDistance = 0.0F;
            original.call(instance, 0.0D, onGround, state, pos);
            return;
        }
        Vec3 localVec = AntarchyGravityRotationUtil.vecWorldToPlayer(new Vec3(dx, dy, dz), gravity);
        if (!onGround) {
            if (localVec.y < 0.0) {
                this.antarchy$invertedFallDistance += (float)(-localVec.y);
            } else {
                this.antarchy$invertedFallDistance = 0.0F;
            }
        } else if (this.antarchy$invertedFallDistance > self.fallDistance) {
            // Vanilla damage uses fallDistance on landing; keep our own inverted accumulator
            // in sync because the server pipeline is clearing the field before that point.
            self.fallDistance = this.antarchy$invertedFallDistance;
        }
        original.call(instance, localVec.y, onGround, state, pos);
        if (onGround) {
            this.antarchy$invertedFallDistance = 0.0F;
        }
    }

    @Unique
    private boolean antarchy$isTouchingAntiwater(ServerPlayer player) {
        return antarchy$intersectsAntiwater(player, player.getBoundingBox().inflate(0.05D));
    }

    @Unique
    private boolean antarchy$passedThroughAntiwater(ServerPlayer player, double dx, double dy, double dz) {
        AABB sweptBox = player.getBoundingBox().expandTowards(-dx, -dy, -dz).inflate(0.05D);
        return antarchy$intersectsAntiwater(player, sweptBox);
    }

    @Unique
    private boolean antarchy$intersectsAntiwater(ServerPlayer player, AABB box) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = player.level().getFluidState(cursor);
                    if (PotentNyxiteBlock.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
