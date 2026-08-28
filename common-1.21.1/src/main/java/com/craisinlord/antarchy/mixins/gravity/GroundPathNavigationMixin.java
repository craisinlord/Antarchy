package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.GravityWalkNodeEvaluator;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GroundPathNavigation.class)
/*
 * Makes ground pathing read gravity-aware floor checks.
 */
public abstract class GroundPathNavigationMixin {
    @Inject(method = "createPathFinder", at = @At("HEAD"), cancellable = true)
    private void antarchy$useGravityEvaluator(int maxVisitedNodes, CallbackInfoReturnable<PathFinder> cir) {
        var accessor = (PathNavigationAccessor) (Object) this;
        var mob = accessor.antarchy$getMob();
        NodeEvaluator evaluator = new GravityWalkNodeEvaluator();
        accessor.antarchy$setNodeEvaluator(evaluator);
        cir.setReturnValue(new PathFinder(evaluator, maxVisitedNodes));
    }
}
