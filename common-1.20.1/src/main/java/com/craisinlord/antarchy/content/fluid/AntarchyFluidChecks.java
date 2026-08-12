package com.craisinlord.antarchy.content.fluid;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class AntarchyFluidChecks {
    private static final ResourceLocation ANTIWATER_ID = new ResourceLocation(Antarchy.MODID, "antiwater");
    private static final ResourceLocation FLOWING_ANTIWATER_ID = new ResourceLocation(Antarchy.MODID, "flowing_antiwater");
    private static final ResourceLocation ICHOR_ID = new ResourceLocation(Antarchy.MODID, "ichor");
    private static final ResourceLocation FLOWING_ICHOR_ID = new ResourceLocation(Antarchy.MODID, "flowing_ichor");
    private static final ResourceLocation LUMEN_ID = new ResourceLocation(Antarchy.MODID, "lumen");
    private static final ResourceLocation FLOWING_LUMEN_ID = new ResourceLocation(Antarchy.MODID, "flowing_lumen");
    private static final ResourceLocation BILE_ID = new ResourceLocation(Antarchy.MODID, "bile");
    private static final ResourceLocation FLOWING_BILE_ID = new ResourceLocation(Antarchy.MODID, "flowing_bile");
    private static final ResourceLocation BILE_VEIN_ID = new ResourceLocation(Antarchy.MODID, "bile_vein");

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

    public static boolean isBileVein(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return BILE_VEIN_ID.equals(blockId);
    }

    /**
     * Bile always wins: worldgen foliage must not anchor on or near bile fluid or its solid
     * bile_vein shell, since cyst/vein features can carve into a neighboring chunk after this
     * chunk's vegetation has already been placed.
     */
    public static boolean hasBileNearby(LevelAccessor level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mutable);
                    if (isBile(state.getFluidState()) || isBileVein(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
