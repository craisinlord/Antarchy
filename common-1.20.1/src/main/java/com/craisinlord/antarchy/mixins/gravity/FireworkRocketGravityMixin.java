package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FireworkRocketEntity.class)
/*
 * Fixes firework boost dir for inverted elytra flight.
 */
public abstract class FireworkRocketGravityMixin {

    @Shadow
    @Nullable
    private LivingEntity attachedToEntity;

    @ModifyVariable(method = "tick", at = @At(value = "STORE"), ordinal = 0)
    private Vec3 antarchy$fixFireworkThrustDirection(Vec3 velocity) {
        if (!(this.attachedToEntity instanceof Player player)) return velocity;
        if (!AntarchyGravityApi.isGravityInverted(player)) return velocity;
        return AntarchyGravityRotationUtil.vecWorldToPlayer(
                velocity,
                AntarchyGravityApi.getGravityDirection(player)
        );
    }
}
