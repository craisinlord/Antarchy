package com.craisinlord.antarchy.fabric.content.fluid;

import java.util.Optional;
import java.util.function.Supplier;
import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class SimpleFluid extends FlowingFluid {
    private final Supplier<? extends FlowingFluid> source;
    private final Supplier<? extends FlowingFluid> flowing;
    private final Supplier<? extends Item> bucket;
    private final String blockId;
    private final int slopeFindDistance;
    private final int dropOff;
    private final int tickDelay;

    protected SimpleFluid(
            Supplier<? extends FlowingFluid> source,
            Supplier<? extends FlowingFluid> flowing,
            Supplier<? extends Item> bucket,
            String blockId,
            int slopeFindDistance,
            int dropOff,
            int tickDelay
    ) {
        this.source = source;
        this.flowing = flowing;
        this.bucket = bucket;
        this.blockId = blockId;
        this.slopeFindDistance = slopeFindDistance;
        this.dropOff = dropOff;
        this.tickDelay = tickDelay;
    }

    @Override
    public Fluid getFlowing() {
        return this.flowing.get();
    }

    @Override
    public Fluid getSource() {
        return this.source.get();
    }

    @Override
    public Item getBucket() {
        return this.bucket.get();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        net.minecraft.world.level.block.Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return this.slopeFindDistance;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return this.dropOff;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return this.tickDelay;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == this.source.get() || fluid == this.flowing.get();
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        LiquidBlock block = (LiquidBlock) BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, this.blockId));
        return block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    public static final class Flowing extends SimpleFluid {
        public Flowing(
                Supplier<? extends FlowingFluid> source,
                Supplier<? extends FlowingFluid> flowing,
                Supplier<? extends Item> bucket,
                String blockId,
                int slopeFindDistance,
                int dropOff,
                int tickDelay
        ) {
            super(source, flowing, bucket, blockId, slopeFindDistance, dropOff, tickDelay);
        }

        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static final class Source extends SimpleFluid {
        public Source(
                Supplier<? extends FlowingFluid> source,
                Supplier<? extends FlowingFluid> flowing,
                Supplier<? extends Item> bucket,
                String blockId,
                int slopeFindDistance,
                int dropOff,
                int tickDelay
        ) {
            super(source, flowing, bucket, blockId, slopeFindDistance, dropOff, tickDelay);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
