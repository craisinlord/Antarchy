package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
/*
 * Keeps path debug shapes lined up with gravity-aware nav.
 */
public abstract class PathNavigationDebugMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Antarchy/Pathfinding");

    @Unique
    private int antarchy$debugTickCooldown;

    @Inject(method = "moveTo(DDDD)Z", at = @At("HEAD"))
    private void antarchy$logMoveToPosition(double x, double y, double z, double speed, CallbackInfoReturnable<Boolean> cir) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        LOGGER.debug(
                "[Path] moveTo(pos) inverted mob={} target=({}, {}, {}) speed={}",
                mob.getClass().getSimpleName(),
                x,
                y,
                z,
                speed
        );
    }

    @Inject(method = "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", at = @At("HEAD"))
    private void antarchy$logMoveToPath(Path path, double speed, CallbackInfoReturnable<Boolean> cir) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        LOGGER.debug(
                "[Path] moveTo(path) inverted mob={} nodes={} nextIndex={} speed={}",
                mob.getClass().getSimpleName(),
                path == null ? -1 : path.getNodeCount(),
                path == null ? -1 : path.getNextNodeIndex(),
                speed
        );
    }

    @Inject(method = "createPath(DDDI)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
    private void antarchy$logCreatePath(double x, double y, double z, int accuracy, CallbackInfoReturnable<Path> cir) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        Path path = cir.getReturnValue();
        LOGGER.debug(
                "[Path] createPath(pos) inverted mob={} target=({}, {}, {}) accuracy={} result={}",
                mob.getClass().getSimpleName(),
                x,
                y,
                z,
                accuracy,
                path == null ? "null" : ("nodes=" + path.getNodeCount() + " nextIndex=" + path.getNextNodeIndex())
        );
    }

    @Inject(method = "createPath(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
    private void antarchy$logCreatePath(Entity target, int accuracy, CallbackInfoReturnable<Path> cir) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        Path path = cir.getReturnValue();
        LOGGER.debug(
                "[Path] createPath(entity) inverted mob={} target={} accuracy={} result={}",
                mob.getClass().getSimpleName(),
                target == null ? "null" : target.getClass().getSimpleName(),
                accuracy,
                path == null ? "null" : ("nodes=" + path.getNodeCount() + " nextIndex=" + path.getNextNodeIndex())
        );
    }

    @Inject(method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
    private void antarchy$logCreatePath(BlockPos target, int accuracy, CallbackInfoReturnable<Path> cir) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        Path path = cir.getReturnValue();
        LOGGER.debug(
                "[Path] createPath(blockpos) inverted mob={} target={} accuracy={} result={}",
                mob.getClass().getSimpleName(),
                target,
                accuracy,
                path == null ? "null" : ("nodes=" + path.getNodeCount() + " nextIndex=" + path.getNextNodeIndex())
        );
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$logTick(CallbackInfo ci) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        if (this.antarchy$debugTickCooldown-- > 0) {
            return;
        }

        this.antarchy$debugTickCooldown = 20;
        Path path = ((PathNavigationAccessor) (Object) this).antarchy$getPath();
        LOGGER.debug(
                "[Path] tick inverted mob={} pathPresent={} pathDone={}",
                mob.getClass().getSimpleName(),
                path != null,
                path != null && path.isDone()
        );
    }
}
