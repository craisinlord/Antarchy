package com.craisinlord.antarchy.neoforge.mixins.infinity;

import net.codexarchonic.infinity.util.teleport.PortalCreator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PortalCreator.class, remap = false)
public interface PortalCreatorInvoker {
    @Invoker(value = "tryAddInfinityDimension", remap = false)
    static boolean antarchy$invokeTryAddInfinityDimension(MinecraftServer server, ResourceLocation dimensionId) {
        throw new AssertionError();
    }
}
