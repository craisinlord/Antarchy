package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.GravityWalkNodeEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
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
    private static final Logger LOGGER = LoggerFactory.getLogger("Antarchy/Pathfinding");

    @Inject(method = "createPathFinder", at = @At("HEAD"), cancellable = true)
    private void antarchy$useGravityEvaluator(int maxVisitedNodes, CallbackInfoReturnable<PathFinder> cir) {
        var mob = ((PathNavigationAccessor) (Object) this).antarchy$getMob();
        if (!AntarchyGravityApi.isGravityInverted(mob)) {
            return;
        }

        LOGGER.debug(
                "[Path] swap evaluator mob={} maxVisitedNodes={}",
                mob.getClass().getSimpleName(),
                maxVisitedNodes
        );

        cir.setReturnValue(new PathFinder(new GravityWalkNodeEvaluator(), maxVisitedNodes));
    }
}
