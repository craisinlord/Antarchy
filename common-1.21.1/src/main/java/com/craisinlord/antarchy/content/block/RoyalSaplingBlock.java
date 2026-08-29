package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancements;
import com.craisinlord.antarchy.content.worldgen.royal.RoyalTreeGrowers;
import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RoyalSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<RoyalSaplingBlock> CODEC = Block.simpleCodec(RoyalSaplingBlock::new);
    private static final ResourceLocation LET_IT_GROW_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "let_it_grow");
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public RoyalSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<RoyalSaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(BlockTags.DIRT);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return state.canSurvive(level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.growRoyalTree(level, pos, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.45F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.growRoyalTree(level, pos, random);
    }

    private void growRoyalTree(ServerLevel level, BlockPos pos, RandomSource random) {
        Map<BlockPos, BlockState> footprint = this.findFootprint(level, pos);
        if (footprint == null) {
            return;
        }

        BlockPos origin = footprint.keySet().iterator().next();
        ResourceKey<ConfiguredFeature<?, ?>> variant = switch (random.nextInt(3)) {
            case 0 -> RoyalTreeGrowers.ROYAL_TREE_SMALL;
            case 1 -> RoyalTreeGrowers.ROYAL_TREE_MEDIUM;
            default -> RoyalTreeGrowers.ROYAL_TREE_LARGE;
        };

        Holder.Reference<ConfiguredFeature<?, ?>> feature = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(variant);

        for (BlockPos saplingPos : footprint.keySet()) {
            level.removeBlock(saplingPos, false);
        }

        if (feature.value().place(level, level.getChunkSource().getGenerator(), random, origin)) {
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(origin).inflate(8.0D))) {
                AntarchyAdvancements.award(player, LET_IT_GROW_ADVANCEMENT);
            }
            return;
        }

        for (Map.Entry<BlockPos, BlockState> entry : footprint.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_ALL);
        }
    }

    private Map<BlockPos, BlockState> findFootprint(ServerLevel level, BlockPos pos) {
        for (int offsetX = 0; offsetX < 2; offsetX++) {
            for (int offsetZ = 0; offsetZ < 2; offsetZ++) {
                BlockPos origin = pos.offset(-offsetX, 0, -offsetZ);
                Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
                boolean matches = true;

                for (int x = 0; x < 2 && matches; x++) {
                    for (int z = 0; z < 2; z++) {
                        BlockPos checkPos = origin.offset(x, 0, z);
                        BlockState checkState = level.getBlockState(checkPos);
                        if (!checkState.is(this)) {
                            matches = false;
                            break;
                        }
                        blocks.put(checkPos, checkState);
                    }
                }

                if (matches) {
                    return blocks;
                }
            }
        }

        return null;
    }
}
