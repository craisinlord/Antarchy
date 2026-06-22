package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SplashManager.class)
/*
 * Adds Antarchy splash text without replacing vanilla handling.
 */
public abstract class SplashManagerMixin {
    @Shadow
    @Final
    private List<String> splashes;

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void antarchy$addCustomSplashes(List<String> splashes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        this.splashes.add("Virtual Antsanity!");
        this.splashes.add("Ants! Ants! Ants!");
        this.splashes.add("What is this a center for ants??");
        this.splashes.add("F-ANT-tastic!");
        this.splashes.add("If you squint hard enough, everything looks the same!");
        this.splashes.add("You are very import-ant!");
        this.splashes.add("There are ants in your pants!");
        this.splashes.add("Don't let the bed bugs bite!");
        this.splashes.add("Ants Ants Revolution!");
        this.splashes.add("Rainbow sugar yummy");
        this.splashes.add("Now with inverted gravity!");
        this.splashes.add("Everything we need is already here");
    }
}
