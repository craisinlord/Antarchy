package com.craisinlord.antarchy.content.fluid;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;

public final class AntarchyFluidChecks {
    private static final ResourceLocation ANTIWATER_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antiwater");
    private static final ResourceLocation FLOWING_ANTIWATER_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flowing_antiwater");
    private static final ResourceLocation ICHOR_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ichor");
    private static final ResourceLocation FLOWING_ICHOR_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flowing_ichor");
    private static final ResourceLocation LUMEN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lumen");
    private static final ResourceLocation FLOWING_LUMEN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flowing_lumen");
    private static final ResourceLocation BILE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bile");
    private static final ResourceLocation FLOWING_BILE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flowing_bile");

    private AntarchyFluidChecks() {
    }

    public static boolean isAntiwater(FluidState fluidState) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return ANTIWATER_ID.equals(fluidId) || FLOWING_ANTIWATER_ID.equals(fluidId);
    }

    public static boolean isIchor(FluidState fluidState) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return ICHOR_ID.equals(fluidId) || FLOWING_ICHOR_ID.equals(fluidId);
    }

    public static boolean isLumen(FluidState fluidState) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return LUMEN_ID.equals(fluidId) || FLOWING_LUMEN_ID.equals(fluidId);
    }

    public static boolean isBile(FluidState fluidState) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return BILE_ID.equals(fluidId) || FLOWING_BILE_ID.equals(fluidId);
    }

    public static boolean usesWaterLikePhysics(FluidState fluidState) {
        return isIchor(fluidState) || isLumen(fluidState) || isBile(fluidState);
    }
}
