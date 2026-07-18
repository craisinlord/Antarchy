package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowcapMushroomBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<GlowcapMushroomBlock> CODEC = Block.simpleCodec(GlowcapMushroomBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.9D, 0.0D, 3.9D, 12.1D, 12.0D, 12.1D);
    private static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_GLOWCAP_MUSHROOM = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "huge_glowcap_mushroom")
    );
    private static final int SPREAD_ATTEMPTS = 4;
    private static final int MAX_LIGHT_LEVEL = 13;

    public GlowcapMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<GlowcapMushroomBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.MUSHROOM_GROW_BLOCK) || state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return this.mayPlaceOn(below, level, belowPos) && (below.is(BlockTags.MUSHROOM_GROW_BLOCK) || level.getRawBrightness(pos, 0) < MAX_LIGHT_LEVEL);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(25) != 0) {
            return;
        }

        int spreadRange = 4;
        BlockPos targetPos = pos;
        for (int i = 0; i < SPREAD_ATTEMPTS; i++) {
            targetPos = pos.offset(
                    random.nextInt(spreadRange * 2 + 1) - spreadRange,
                    random.nextInt(3) - 1,
                    random.nextInt(spreadRange * 2 + 1) - spreadRange
            );
            if (level.isEmptyBlock(targetPos) && canSpreadTo(level, targetPos)) {
                level.setBlock(targetPos, this.defaultBlockState(), 2);
                break;
            }
        }
    }

    private boolean canSpreadTo(ServerLevel level, BlockPos pos) {
        if (level.getRawBrightness(pos, 0) > MAX_LIGHT_LEVEL) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return this.mayPlaceOn(below, level, pos.below());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.4F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Holder.Reference<ConfiguredFeature<?, ?>> feature = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(HUGE_GLOWCAP_MUSHROOM);
        level.removeBlock(pos, false);
        if (!feature.value().place(level, level.getChunkSource().getGenerator(), random, pos)) {
            level.setBlock(pos, state, 3);
        }
    }
}
