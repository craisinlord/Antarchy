package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerEffects;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public final class UndertrialSpawnerBlock extends BaseEntityBlock {
    public static final EnumProperty<UndertrialSpawnerState> STATE = EnumProperty.create("trial_spawner_state", UndertrialSpawnerState.class);
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");
    private final Supplier<? extends BlockEntityType<UndertrialSpawnerBlockEntity>> blockEntityType;

    public UndertrialSpawnerBlock(Supplier<? extends BlockEntityType<UndertrialSpawnerBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
        super(properties.lightLevel(UndertrialSpawnerBlock::lightLevel));
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STATE, UndertrialSpawnerState.INACTIVE)
                .setValue(OMINOUS, false));
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(STATE).lightLevel();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE, OMINOUS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.DOWN ? this.defaultBlockState() : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UndertrialSpawnerBlockEntity(pos, state, blockEntityType);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof SpawnEggItem)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof UndertrialSpawnerBlockEntity spawner) {
            if (!level.isClientSide) {
                spawner.setSpawnEntity(((SpawnEggItem) stack.getItem()).getType(stack.getTag()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        return UndertrialSpawnerEffects.handle(level, pos, id, param);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, this.blockEntityType.get(), UndertrialSpawnerBlockEntity::clientTick);
        }
        return createTickerHelper(blockEntityType, this.blockEntityType.get(), (tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel serverLevel) {
                UndertrialSpawnerBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
            }
        });
    }
}
