package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class AmberLichenBlock extends GlowLichenBlock {
    public static final MapCodec<AmberLichenBlock> CODEC = simpleCodec(AmberLichenBlock::new);

    public AmberLichenBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<GlowLichenBlock> codec() {
        return (MapCodec<GlowLichenBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return fluid == Fluids.WATER || AntarchyFluidChecks.isBile(fluid.defaultFluidState());
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (AntarchyFluidChecks.isBile(fluidState)) {
            level.setBlock(pos, fluidState.createLegacyBlock(), 3);
            return true;
        }
        return super.placeLiquid(level, pos, state, fluidState);
    }
}
