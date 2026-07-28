package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

public class WaspNestBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final Supplier<? extends BlockEntityType<WaspNestBlockEntity>> blockEntityTypeSupplier;
    private final int maxOccupants;
    private final boolean populateInitialWasps;
    private final MapCodec<WaspNestBlock> codec;

    public WaspNestBlock(
            Supplier<? extends BlockEntityType<WaspNestBlockEntity>> blockEntityTypeSupplier,
            BlockBehaviour.Properties properties
    ) {
        this(blockEntityTypeSupplier, 4, true, properties);
    }

    public WaspNestBlock(
            Supplier<? extends BlockEntityType<WaspNestBlockEntity>> blockEntityTypeSupplier,
            int maxOccupants,
            boolean populateInitialWasps,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.maxOccupants = maxOccupants;
        this.populateInitialWasps = populateInitialWasps;
        this.codec = Block.simpleCodec(ignored -> this);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<WaspNestBlock> codec() {
        return this.codec;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaspNestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(),
                (tickLevel, tickPos, tickState, nest) -> {
                    if (tickLevel instanceof ServerLevel serverLevel) {
                        WaspNestBlockEntity.serverTick(serverLevel, tickPos, tickState, nest);
                    }
                });
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && this.populateInitialWasps && level.getBlockEntity(pos) instanceof WaspNestBlockEntity nestBlockEntity) {
            nestBlockEntity.populateInitialWasps((ServerLevel) level);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof WaspNestBlockEntity nestBlockEntity) {
            nestBlockEntity.releaseAll((ServerLevel) level);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    public int maxOccupants() {
        return this.maxOccupants;
    }

    public boolean shouldPopulateInitialWasps() {
        return this.populateInitialWasps;
    }

    public EntityType<? extends WaspEntity> waspType() {
        return com.craisinlord.antarchy.content.AntarchyObjects.WASP.get();
    }
}
