package com.craisinlord.antarchy.mixins.gravity.client;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
/*
 * Keeps client gravity state in sync from entity updates.
 */
public abstract class ClientPacketListenerGravityMixin {

    @WrapOperation(method = "handleExplosion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0))
    private Vec3 antarchy$fixExplosionKnockback(Vec3 current,
                                                  double x, double y, double z,
                                                  Operation<Vec3> original) {
        if (Minecraft.getInstance().player == null) {
            return original.call(current, x, y, z);
        }
        AntarchyGravityDirection gravity =
                AntarchyGravityApi.getGravityDirection(Minecraft.getInstance().player);
        if (!gravity.isInverted()) {
            return original.call(current, x, y, z);
        }
        Vec3 local = AntarchyGravityRotationUtil.vecWorldToPlayer(new Vec3(x, y, z), gravity);
        return original.call(current, local.x, local.y, local.z);
    }
}
