package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultEffects;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultLogic;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public final class UnderVaultBlock extends BaseEntityBlock {
    public static final EnumProperty<UnderVaultState> STATE = EnumProperty.create("vault_state", UnderVaultState.class);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");
    public static final DirectionProperty UNDER_FACING = DirectionProperty.create("under_facing", Direction.values());
    private final Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> blockEntityType;

    public UnderVaultBlock(Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
        super(properties.lightLevel(UnderVaultBlock::lightLevel));
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, UnderVaultState.INACTIVE)
                .setValue(OMINOUS, false)
                .setValue(UNDER_FACING, Direction.NORTH));
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(STATE).lightLevel();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE, OMINOUS);
        builder.add(UNDER_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.DOWN) {
            return null;
        }
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(UNDER_FACING, Direction.DOWN);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        return rotated.setValue(UNDER_FACING, rotation.rotate(state.getValue(UNDER_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state.rotate(mirror.getRotation(state.getValue(FACING)));
        return mirrored.setValue(UNDER_FACING, mirror.getRotation(state.getValue(UNDER_FACING)).rotate(state.getValue(UNDER_FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UnderVaultBlockEntity(pos, state, blockEntityType);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || state.getValue(STATE) != UnderVaultState.ACTIVE) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof UnderVaultBlockEntity underVault) {
            UnderVaultLogic.Server.tryInsertKey(
                    serverLevel, pos, state, underVault.getVaultData().getConfig(),
                    underVault.getVaultData().getServerData(), underVault.getVaultData().getSharedData(),
                    player, stack);
            return InteractionResult.SUCCESS;
        }
        return level.isClientSide ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        return UnderVaultEffects.handle(level, pos, id, param);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (this.blockEntityType == null || type != this.blockEntityType.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<UnderVaultBlockEntity>) UnderVaultBlockEntity::clientTick;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<UnderVaultBlockEntity>) ((serverLevel, pos, blockState, blockEntity) -> {
            if (serverLevel instanceof ServerLevel levelServer) {
                UnderVaultBlockEntity.serverTick(levelServer, pos, blockState, blockEntity);
            }
        });
    }
}
