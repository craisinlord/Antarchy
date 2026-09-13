package com.craisinlord.antarchy.neoforge.mixins.infinity;

import com.craisinlord.antarchy.neoforge.InfinityGenerationFailure;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.util.teleport.PortalCreator;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WarpLogic.class, remap = false)
public interface WarpLogicMixin {
    @Redirect(
            method = "requestWarp(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceLocation;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/lerariemann/infinity/util/teleport/PortalCreator;tryAddInfinityDimension(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/resources/ResourceLocation;)Z"),
            remap = false
    )
    private static boolean antarchy$queuedDimensionCountsAsNew(MinecraftServer server, ResourceLocation dimensionId) throws Throwable {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        if (server instanceof MinecraftServerAccess access && access.infinity$hasToAdd(key)) {
            return true;
        }
        try {
            return PortalCreator.tryAddInfinityDimension(server, dimensionId);
        } catch (Throwable throwable) {
            InfinityGenerationFailure.mark(dimensionId);
            throw throwable;
        }
    }
}
