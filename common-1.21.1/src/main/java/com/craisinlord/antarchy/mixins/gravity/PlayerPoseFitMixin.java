package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
/*
 * Fixes pose checks for ceiling-walking players.
 */
public abstract class PlayerPoseFitMixin {
    
    @Inject(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("HEAD"), cancellable = true)
    private void antarchy$canFitPoseWhenInverted(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(player)) {
            return;
        }

        EntityDimensions dimensions = player.getDimensions(pose);
        Vec3 position = player.position();
        float halfWidth = dimensions.width() / 2.0F;
        float height = dimensions.height();
        AABB invertedBox = new AABB(
                position.x - halfWidth, position.y - height, position.z - halfWidth,
                position.x + halfWidth, position.y,           position.z + halfWidth
        );
        cir.setReturnValue(player.level().noCollision(player, invertedBox));
    }
}
