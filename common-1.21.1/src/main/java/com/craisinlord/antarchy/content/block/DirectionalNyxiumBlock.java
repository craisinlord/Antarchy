package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class DirectionalNyxiumBlock extends Block implements BonemealableBlock {
    public static final MapCodec<DirectionalNyxiumBlock> CODEC = simpleCodec(properties -> new DirectionalNyxiumBlock(properties, Type.NADIR));
    public static final DirectionProperty VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    private static final ResourceLocation NYXITE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nyxite");
    private static final ResourceLocation NADIR_FERN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nadir_fern");
    private static final ResourceLocation DUSKBELL_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "duskbell");
    private static final ResourceLocation NADIR_SAPLING_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nadir_sapling");
    private static final ResourceLocation WHIRLFLOWER_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "whirlflower");
    private static final ResourceLocation SPIRALING_VINES_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "spiraling_vines");
    private final Type type;

    public DirectionalNyxiumBlock(BlockBehaviour.Properties properties, Type type) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(VERTICAL_DIRECTION, Direction.UP));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(
                VERTICAL_DIRECTION,
                context.getClickedFace() == Direction.DOWN ? Direction.DOWN : Direction.UP
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return hasAdjacentNyxite(level, pos) || level.getBlockState(pos.relative(state.getValue(VERTICAL_DIRECTION))).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        spreadToNearbyNyxite(level, random, pos, state);
        BlockPos growthPos = pos.relative(state.getValue(VERTICAL_DIRECTION));
        if (level.getBlockState(growthPos).isAir()) {
            placeVegetation(level, random, growthPos, state.getValue(VERTICAL_DIRECTION));
        }
    }

    private void spreadToNearbyNyxite(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        for (int i = 0; i < 24; i++) {
            BlockPos targetPos = pos.offset(
                    random.nextInt(5) - 2,
                    random.nextInt(3) - 1,
                    random.nextInt(5) - 2
            );
            if (isNyxite(level.getBlockState(targetPos))) {
                level.setBlock(targetPos, state, Block.UPDATE_ALL);
            }
        }
    }

    private void placeVegetation(ServerLevel level, RandomSource random, BlockPos pos, Direction direction) {
        ResourceLocation id = switch (this.type) {
            case NADIR -> {
                if (direction == Direction.DOWN && random.nextInt(7) == 0) {
                    yield NADIR_SAPLING_ID;
                }
                yield random.nextInt(4) == 0 ? DUSKBELL_ID : NADIR_FERN_ID;
            }
            case VERDANT -> random.nextInt(3) == 0 ? SPIRALING_VINES_ID : WHIRLFLOWER_ID;
        };
        Block block = BuiltInRegistries.BLOCK.get(id);
        BlockState growthState = block.defaultBlockState();
        if (growthState.hasProperty(SimpleDirectionalVineBlock.GROWTH_DIRECTION)) {
            growthState = growthState.setValue(SimpleDirectionalVineBlock.GROWTH_DIRECTION, direction);
        }
        if (growthState.hasProperty(VERTICAL_DIRECTION)) {
            growthState = growthState.setValue(VERTICAL_DIRECTION, direction);
        }
        if (growthState.canSurvive(level, pos)) {
            level.setBlock(pos, growthState, Block.UPDATE_ALL);
        }
    }

    private static boolean hasAdjacentNyxite(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (isNyxite(level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNyxite(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(NYXITE_ID);
    }

    public enum Type {
        NADIR,
        VERDANT
    }
}
