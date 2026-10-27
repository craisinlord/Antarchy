package com.craisinlord.antarchy.content.fluid;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;

public class BileLiquidBlock extends LiquidBlock {
    private static final Identifier BILE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "bile");
    private static final Identifier FLOWING_BILE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "flowing_bile");
    private static final Identifier BROODSTONE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "broodstone");
    private static final Identifier MYRMITE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "myrmite");
    private static final int EFFECT_DURATION_TICKS = 100;

    public BileLiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide()) {
            reactWithWater(state, level, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            reactWithWater(state, level, pos);
        }
    }

    private void reactWithWater(BlockState state, Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            FluidState neighbor = level.getFluidState(pos.relative(dir));
            if (neighbor.is(Fluids.WATER) || neighbor.is(Fluids.FLOWING_WATER)) {
                boolean isSource = state.getFluidState().isSource();
                Identifier resultId = isSource ? BROODSTONE_ID : MYRMITE_ID;
                Block resultBlock = BuiltInRegistries.BLOCK.getOptional(resultId).orElse(null);
                if (resultBlock != null) {
                    level.setBlock(pos, resultBlock.defaultBlockState(), 3);
                    level.levelEvent(1501, pos, 0);
                }
                return;
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);

        if (entity instanceof LivingEntity livingEntity) {
            if (!level.isClientSide()) {
                if (livingEntity.is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                    return;
                }
                livingEntity.addEffect(new MobEffectInstance(AntarchyObjects.STINKY_EFFECT.get(), EFFECT_DURATION_TICKS, 0, false, true, true));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0, false, true, true));
            }
        }
    }

    public static boolean isBile(FluidState fluidState) {
        Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
        return BILE_ID.equals(fluidId) || FLOWING_BILE_ID.equals(fluidId);
    }
}
