package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractArrow.class)
/*
 * Flips arrow gravity for inverted shooters.
 */
public abstract class AbstractArrowGravityMixin {

    @ModifyVariable(method = "tick", at = @At(value = "STORE"), ordinal = 0)
    private Vec3 antarchy$fixArrowGravityDroop(Vec3 velocity) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (!AntarchySettings.invertProjectilesFromInvertedPlayers()) return velocity;
        Entity owner = self.getOwner();
        if (!(owner instanceof Player player)) return velocity;
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(player);
        if (!direction.isInverted()) return velocity;
        velocity = new Vec3(velocity.x, velocity.y + 0.05D, velocity.z);
        velocity = AntarchyGravityRotationUtil.vecWorldToPlayer(velocity, direction);
        velocity = new Vec3(velocity.x, velocity.y - 0.05D, velocity.z);
        velocity = AntarchyGravityRotationUtil.vecPlayerToWorld(velocity, direction);
        return velocity;
    }
}
