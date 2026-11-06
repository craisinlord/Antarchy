package com.craisinlord.antarchy.mixins;

import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public interface SurfaceRulesContextAccessor {
    @Accessor("biome")
    Supplier<Holder<Biome>> antarchy$biome();

    @Invoker("updateXZ")
    void antarchy$updateXZ(int blockX, int blockZ);

    @Invoker("updateY")
    void antarchy$updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int blockX, int blockY, int blockZ);

    @Invoker("getMinSurfaceLevel")
    int antarchy$getMinSurfaceLevel();
}
