package com.craisinlord.antarchy.mixins;

import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MaterialRuleContext.class)
public interface SurfaceRulesContextAccessor {
    @Invoker("updateXZ")
    void antarchy$updateXZ(int blockX, int blockZ, int surfaceGradientX, int surfaceGradientZ);

    @Invoker("updateY")
    void antarchy$updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int blockY);
}
