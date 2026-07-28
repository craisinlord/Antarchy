package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoltingVinesBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<MoltingVinesBlock> CODEC = Block.simpleCodec(MoltingVinesBlock::new);
    public static final EnumProperty<Direction> GROWTH_DIRECTION = EnumProperty.create("growth_direction", Direction.class, Direction.UP, Direction.DOWN);
    public static final BooleanProperty TOP_CAP = BooleanProperty.create("top_cap");
    public static final BooleanProperty BOTTOM_CAP = BooleanProperty.create("bottom_cap");
    public static final BooleanProperty BROODFRUIT = BooleanProperty.create("broodfruit");
    public static final BooleanProperty STUNTED = BooleanProperty.create("stunted");
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 6);
    private static final int MAX_DISTANCE = 6;
    private static final int NATURAL_FRUIT_CHANCE = 40;
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final ResourceLocation BROODFRUIT_ITEM_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "broodfruit");

    public MoltingVinesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(GROWTH_DIRECTION, Direction.DOWN)
                .setValue(TOP_CAP, false)
                .setValue(BOTTOM_CAP, true)
                .setValue(BROODFRUIT, false)
                .setValue(STUNTED, false)
                .setValue(DISTANCE, 0));
    }

    @Override
    public MapCodec<MoltingVinesBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction growthDirection = resolveGrowthDirection(level, pos);
        return computeState(this.defaultBlockState().setValue(GROWTH_DIRECTION, growthDirection).setValue(DISTANCE, 0), level, pos);
    }

    private Direction resolveGrowthDirection(BlockGetter level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.is(this) && belowState.getValue(GROWTH_DIRECTION) == Direction.UP) {
            return Direction.UP;
        }

        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.is(this) && aboveState.getValue(GROWTH_DIRECTION) == Direction.DOWN) {
            return Direction.DOWN;
        }

        return hasSturdyAnchor(level, pos, Direction.UP) ? Direction.DOWN : Direction.UP;
    }

    private static boolean hasSturdyAnchor(BlockGetter level, BlockPos pos, Direction towardAnchor) {
        BlockPos anchorPos = pos.relative(towardAnchor);
        BlockState anchorState = level.getBlockState(anchorPos);
        return anchorState.isFaceSturdy(level, anchorPos, towardAnchor.getOpposite());
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(this) || state.isFaceSturdy(level, pos, Direction.DOWN) || state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction anchorDirection = state.getValue(GROWTH_DIRECTION).getOpposite();
        BlockPos anchorPos = pos.relative(anchorDirection);
        BlockState anchorState = level.getBlockState(anchorPos);
        if (anchorState.is(this)) {
            return true;
        }
        return anchorState.isFaceSturdy(level, anchorPos, anchorDirection.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return computeState(state, level, pos);
        }
        return state;
    }

    private static BlockState computeState(BlockState state, BlockGetter level, BlockPos pos) {
        boolean aboveIsSame = level.getBlockState(pos.above()).is(state.getBlock());
        boolean belowIsSame = level.getBlockState(pos.below()).is(state.getBlock());
        Direction growthDirection = state.getValue(GROWTH_DIRECTION);

        boolean topCap = growthDirection == Direction.UP && !aboveIsSame;
        boolean bottomCap = growthDirection == Direction.DOWN && !belowIsSame;

        state = state.setValue(TOP_CAP, topCap).setValue(BOTTOM_CAP, bottomCap);
        if (topCap || bottomCap) {
            state = state.setValue(BROODFRUIT, false);
        }
        return state;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(STUNTED);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STUNTED)) {
            return;
        }

        boolean isTip = state.getValue(GROWTH_DIRECTION) == Direction.DOWN ? state.getValue(BOTTOM_CAP) : state.getValue(TOP_CAP);
        if (isTip && state.getValue(DISTANCE) < MAX_DISTANCE && random.nextInt(6) == 0) {
            BlockPos growPos = pos.relative(state.getValue(GROWTH_DIRECTION));
            if (level.getBlockState(growPos).isAir()) {
                BlockState grownState = this.defaultBlockState()
                        .setValue(GROWTH_DIRECTION, state.getValue(GROWTH_DIRECTION))
                        .setValue(DISTANCE, state.getValue(DISTANCE) + 1);
                level.setBlock(growPos, grownState, Block.UPDATE_ALL);

                BlockState posState = level.getBlockState(pos);
                if (posState.is(this)) {
                    level.setBlock(pos, computeState(posState, level, pos), Block.UPDATE_CLIENTS);
                }
                BlockState growState = level.getBlockState(growPos);
                if (growState.is(this)) {
                    level.setBlock(growPos, computeState(growState, level, growPos), Block.UPDATE_CLIENTS);
                }
            }
        }

        if (!state.getValue(TOP_CAP) && !state.getValue(BOTTOM_CAP) && !state.getValue(BROODFRUIT) && random.nextInt(NATURAL_FRUIT_CHANCE) == 0) {
            level.setBlock(pos, state.setValue(BROODFRUIT, true), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(TOP_CAP) && !state.getValue(BOTTOM_CAP) && !state.getValue(BROODFRUIT);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(BROODFRUIT, true), Block.UPDATE_ALL);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.SHEARS) && !state.getValue(STUNTED)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(STUNTED, true), Block.UPDATE_ALL);
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!state.getValue(BROODFRUIT)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        if (!level.isClientSide) {
            BuiltInRegistries.ITEM.getOptional(BROODFRUIT_ITEM_ID).ifPresent(item -> harvestFruit(level, pos, item));
            level.setBlock(pos, state.setValue(BROODFRUIT, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void harvestFruit(Level level, BlockPos pos, Item item) {
        Block.popResource(level, pos, new ItemStack(item, 1));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && state.getValue(BROODFRUIT) && !state.is(newState.getBlock())) {
            BuiltInRegistries.ITEM.getOptional(BROODFRUIT_ITEM_ID).ifPresent(item -> harvestFruit(level, pos, item));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GROWTH_DIRECTION, TOP_CAP, BOTTOM_CAP, BROODFRUIT, STUNTED, DISTANCE);
    }
}
