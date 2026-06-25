package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CreepingHorrorEggBlock extends Block {
    public static final MapCodec<CreepingHorrorEggBlock> CODEC = Block.simpleCodec(CreepingHorrorEggBlock::new);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;
    private static final int MAX_HATCH = 2;
    private static final int MAX_EGGS = 4;
    private static final VoxelShape SINGLE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 7.0D, 13.0D);
    private static final VoxelShape MULTI_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);

    public CreepingHorrorEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
    }

    @Override
    public MapCodec<CreepingHorrorEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(EGGS) > 1 ? MULTI_SHAPE : SINGLE_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive()
                && context.getItemInHand().is(this.asItem())
                && state.getValue(EGGS) < MAX_EGGS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existingState = context.getLevel().getBlockState(context.getClickedPos());
        if (existingState.is(this)) {
            return existingState.setValue(EGGS, Math.min(MAX_EGGS, existingState.getValue(EGGS) + 1));
        }
        return this.defaultBlockState();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.shouldUpdateHatchLevel(level)) {
            return;
        }

        int hatch = state.getValue(HATCH);
        if (hatch < MAX_HATCH) {
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.95F + random.nextFloat() * 0.1F);
            return;
        }

        this.hatchEggs(level, pos, state, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully()) {
            this.destroyEgg(level, state, pos, entity, 100);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        this.destroyEgg(level, state, pos, entity, 3);
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (hasHatchSupport(level, pos) && !level.isClientSide) {
            level.levelEvent(2012, pos, 15);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, net.minecraft.world.item.ItemStack stack) {
        super.playerDestroy(level, player, pos, state, blockEntity, stack);
        this.decreaseEggs(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    public static boolean hasHatchSupport(BlockGetter level, BlockPos pos) {
        return isSupportedGround(level, pos.below());
    }

    public static boolean isSupportedGround(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.SAND) || level.getBlockState(pos).blocksMotion();
    }

    private void destroyEgg(Level level, BlockState state, BlockPos pos, Entity entity, int chance) {
        if (level.isClientSide || !this.canDestroyEgg(level, entity) || level.random.nextInt(chance) != 0) {
            return;
        }
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
        this.decreaseEggs(level, pos, state);
    }

    private void hatchEggs(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.75F, 0.95F + random.nextFloat() * 0.1F);

        for (int i = 0; i < state.getValue(EGGS); i++) {
            CreepingHorrorEntity entity = AntarchyObjects.CREEPING_HORROR.get().create(level);
            if (entity == null) {
                continue;
            }
            double spawnX = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double spawnZ = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            entity.moveTo(spawnX, pos.getY() + 0.1D, spawnZ, random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(entity);
        }
    }

    private void decreaseEggs(Level level, BlockPos pos, BlockState state) {
        int eggs = state.getValue(EGGS);
        if (eggs <= 1) {
            level.destroyBlock(pos, false);
            return;
        }
        level.setBlock(pos, state.setValue(EGGS, eggs - 1), 2);
    }

    private boolean shouldUpdateHatchLevel(Level level) {
        return level.random.nextInt(500) == 0;
    }

    private boolean canDestroyEgg(Level level, Entity entity) {
        if (entity instanceof CreepingHorrorEntity || entity instanceof Bat) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.LivingEntity) {
            return entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        return false;
    }
}
