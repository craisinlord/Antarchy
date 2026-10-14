package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public final class AntTrapBlock extends AntNestBlock {
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 5.0D, 13.0D);

    public AntTrapBlock(Supplier<List<EntityType<? extends BaseAntEntity>>> antTypesSupplier,
                        Supplier<? extends BlockEntityType<AntNestBlockEntity>> blockEntityTypeSupplier,
                        BlockBehaviour.Properties properties) {
        super(antTypesSupplier, blockEntityTypeSupplier, 10, false, false, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof AntNestBlockEntity trap) {
            trap.releaseAll(serverLevel);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        if (blockEntity instanceof AntNestBlockEntity trap && level instanceof ServerLevel serverLevel) {
            trap.releaseAll(serverLevel);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, stack);
    }
}
