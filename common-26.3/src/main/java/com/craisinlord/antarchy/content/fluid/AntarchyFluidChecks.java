package com.craisinlord.antarchy.content.fluid;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public final class AntarchyFluidChecks {
    private static final Identifier ANTIWATER_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "antiwater");
    private static final Identifier FLOWING_ANTIWATER_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flowing_antiwater");
    private static final Identifier ICHOR_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "ichor");
    private static final Identifier FLOWING_ICHOR_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flowing_ichor");
    private static final Identifier LUMEN_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "lumen");
    private static final Identifier FLOWING_LUMEN_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flowing_lumen");
    private static final Identifier BILE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "bile");
    private static final Identifier FLOWING_BILE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flowing_bile");
    private static final Identifier BILE_VEIN_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "bile_vein");
    private static final FluidRef ANTIWATER = new FluidRef(ANTIWATER_ID);
    private static final FluidRef FLOWING_ANTIWATER = new FluidRef(FLOWING_ANTIWATER_ID);
    private static final FluidRef ICHOR = new FluidRef(ICHOR_ID);
    private static final FluidRef FLOWING_ICHOR = new FluidRef(FLOWING_ICHOR_ID);
    private static final FluidRef LUMEN = new FluidRef(LUMEN_ID);
    private static final FluidRef FLOWING_LUMEN = new FluidRef(FLOWING_LUMEN_ID);
    private static final FluidRef BILE = new FluidRef(BILE_ID);
    private static final FluidRef FLOWING_BILE = new FluidRef(FLOWING_BILE_ID);

    private AntarchyFluidChecks() {
    }

    public static boolean isAntiwater(FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return false;
        }
        Fluid fluid = fluidState.getType();
        return ANTIWATER.matches(fluid) || FLOWING_ANTIWATER.matches(fluid);
    }

    public static boolean isIchor(FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return false;
        }
        Fluid fluid = fluidState.getType();
        return ICHOR.matches(fluid) || FLOWING_ICHOR.matches(fluid);
    }

    public static boolean isLumen(FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return false;
        }
        Fluid fluid = fluidState.getType();
        return LUMEN.matches(fluid) || FLOWING_LUMEN.matches(fluid);
    }

    public static boolean isBile(FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return false;
        }
        Fluid fluid = fluidState.getType();
        return BILE.matches(fluid) || FLOWING_BILE.matches(fluid);
    }

    public static boolean usesWaterLikePhysics(FluidState fluidState) {
        return !fluidState.isEmpty() && (isIchor(fluidState) || isLumen(fluidState) || isBile(fluidState));
    }

    public static boolean isBileVein(BlockState state) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return BILE_VEIN_ID.equals(blockId);
    }

    public static boolean hasBileNearby(LevelAccessor level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(mutable);
                    Fluid fluid = state.getFluidState().getType();
                    if (fluid == BileRefs.BILE || fluid == BileRefs.FLOWING_BILE || state.getBlock() == BileRefs.BILE_VEIN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static final class FluidRef {
        private final Identifier id;
        private volatile Fluid fluid;

        private FluidRef(Identifier id) {
            this.id = id;
        }

        private boolean matches(Fluid candidate) {
            Fluid resolved = this.fluid;
            if (resolved == null) {
                resolved = BuiltInRegistries.FLUID.getOptional(this.id).orElse(null);
                if (resolved == null) {
                    return false;
                }
                this.fluid = resolved;
            }
            return candidate == resolved;
        }
    }

    private static final class BileRefs {
        static final Fluid BILE = BuiltInRegistries.FLUID.getOptional(BILE_ID).orElse(null);
        static final Fluid FLOWING_BILE = BuiltInRegistries.FLUID.getOptional(FLOWING_BILE_ID).orElse(null);
        static final Block BILE_VEIN = BuiltInRegistries.BLOCK.getOptional(BILE_VEIN_ID).orElse(null);
    }
}
