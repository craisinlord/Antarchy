package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancements;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DuctTapeBlock extends Block {
    public static final MapCodec<DuctTapeBlock> CODEC = Block.simpleCodec(DuctTapeBlock::new);
    public static final int MAX_USES = 4;
    private static final ResourceLocation DUCT_TAPE_ADVANCEMENT_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "duct_tape");
    public static final IntegerProperty USES = IntegerProperty.create("uses", 1, MAX_USES);
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final int TAPE_HEIGHT = 2;

    public DuctTapeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(USES, MAX_USES)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<DuctTapeBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
            case WALL -> {
                BlockPos supportPos = pos.relative(state.getValue(FACING).getOpposite());
                yield level.getBlockState(supportPos).isFaceSturdy(level, supportPos, state.getValue(FACING));
            }
            default -> false;
        };
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
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState state = this.defaultBlockState();
        if (clickedFace.getAxis().isHorizontal()) {
            state = state.setValue(FACE, AttachFace.WALL).setValue(FACING, clickedFace);
        } else {
            state = state.setValue(FACE, AttachFace.FLOOR).setValue(FACING, context.getHorizontalDirection());
        }

        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (stack.is(AntarchyObjects.DUCT_TAPE.get().asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.isDamageableItem() || !stack.isDamaged() || stack.is(AntarchyTags.Items.DUCT_TAPE_BLACKLIST)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int repairPerUse = Math.max(1, (int) Math.ceil(stack.getMaxDamage() * AntarchySettings.ductTapeRepairPercentPerUse()));
        int currentDamage = stack.getDamageValue();
        int repairedDamage = Math.min(currentDamage, repairPerUse);

        if (repairedDamage <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            stack.setDamageValue(currentDamage - repairedDamage);
            if (!player.getAbilities().instabuild) {
                consumeTapeUse(level, pos, state);
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                AntarchyAdvancements.award(serverPlayer, DUCT_TAPE_ADVANCEMENT_ID);
            }
            level.playSound(null, pos, AntarchySoundEvents.DUCT_TAPE_USE.get(), SoundSource.BLOCKS, 0.9F, 0.95F + level.random.nextFloat() * 0.1F);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(USES, FACE, FACING);
    }

    private static VoxelShape getShapeFor(BlockState state) {
        int depth = switch (state.getValue(USES)) {
            case 1 -> 4;
            case 2 -> 8;
            case 3 -> 12;
            default -> 16;
        };

        return switch (state.getValue(FACE)) {
            case FLOOR -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(0.0D, 0.0D, 16.0D - depth, 16.0D, TAPE_HEIGHT, 16.0D);
                case SOUTH -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, depth);
                case EAST -> Block.box(0.0D, 0.0D, 0.0D, depth, TAPE_HEIGHT, 16.0D);
                case WEST -> Block.box(16.0D - depth, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, 16.0D);
                default -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, 16.0D);
            };
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(0.0D, 16.0D - depth, 16.0D - TAPE_HEIGHT, 16.0D, 16.0D, 16.0D);
                case SOUTH -> Block.box(0.0D, 16.0D - depth, 0.0D, 16.0D, 16.0D, TAPE_HEIGHT);
                case EAST -> Block.box(0.0D, 16.0D - depth, 0.0D, TAPE_HEIGHT, 16.0D, 16.0D);
                case WEST -> Block.box(16.0D - TAPE_HEIGHT, 16.0D - depth, 0.0D, 16.0D, 16.0D, 16.0D);
                default -> Block.box(0.0D, 16.0D - depth, 0.0D, TAPE_HEIGHT, 16.0D, 16.0D);
            };
            default -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, TAPE_HEIGHT, 16.0D);
        };
    }

    private static void consumeTapeUse(Level level, BlockPos pos, BlockState state) {
        int remainingUses = state.getValue(USES) - 1;
        if (remainingUses > 0) {
            level.setBlock(pos, state.setValue(USES, remainingUses), Block.UPDATE_ALL);
            return;
        }

        level.removeBlock(pos, false);
    }
}
