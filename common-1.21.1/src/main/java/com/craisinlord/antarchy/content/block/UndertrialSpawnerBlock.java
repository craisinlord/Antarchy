package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.Supplier;

public final class UndertrialSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<UndertrialSpawnerBlock> CODEC = MapCodec.unit(() -> new UndertrialSpawnerBlock(null, BlockBehaviour.Properties.of()));
    public static final EnumProperty<TrialSpawnerState> STATE = BlockStateProperties.TRIAL_SPAWNER_STATE;
    public static final BooleanProperty OMINOUS = BlockStateProperties.OMINOUS;
    private final Supplier<? extends BlockEntityType<UndertrialSpawnerBlockEntity>> blockEntityType;

    public UndertrialSpawnerBlock(Supplier<? extends BlockEntityType<UndertrialSpawnerBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STATE, TrialSpawnerState.INACTIVE)
                .setValue(OMINOUS, false));
    }

    @Override
    public MapCodec<UndertrialSpawnerBlock> codec() {
        return CODEC;
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UndertrialSpawnerBlockEntity(pos, state, blockEntityType);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (this.blockEntityType == null || blockEntityType != this.blockEntityType.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<UndertrialSpawnerBlockEntity>) UndertrialSpawnerBlockEntity::clientTick;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<UndertrialSpawnerBlockEntity>) ((tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                UndertrialSpawnerBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
            }
        });
    }
}
