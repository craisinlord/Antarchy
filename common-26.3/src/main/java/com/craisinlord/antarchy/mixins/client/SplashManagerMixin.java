package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SplashManager.class)
public abstract class SplashManagerMixin {
    private static final List<String> ANTARCHY_SPLASHES = List.of(
            "Virtual Antsanity!",
            "Ants! Ants! Ants!",
            "What is this a center for ants??",
            "F-ANT-tastic!",
            "If you squint hard enough, everything looks the same!",
            "You are very import-ant!",
            "There are ants in your pants!",
            "Don't let the bed bugs bite!",
            "Ants Ants Revolution!",
            "Rainbow sugar yummy",
            "Now with inverted gravity!",
            "Everything we need is already here",
            "Rub some bacon on it!"
    );

    @Shadow
    private List<Component> splashes;

    @Shadow
    private static Component literalSplash(String text) {
        throw new AssertionError();
    }

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void antarchy$addCustomSplashes(List<Component> preparations, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        List<Component> combined = new ArrayList<>(this.splashes);
        for (String splash : ANTARCHY_SPLASHES) {
            combined.add(literalSplash(splash));
        }
        this.splashes = List.copyOf(combined);
    }
}
