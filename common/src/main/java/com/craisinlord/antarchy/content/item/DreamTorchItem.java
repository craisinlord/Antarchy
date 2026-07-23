package com.craisinlord.antarchy.content.item;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DreamTorchItem extends BlockItem {
    private final Block standingBlock;
    private final Block wallBlock;
    private final Block ceilingBlock;

    public DreamTorchItem(Block standingBlock, Block wallBlock, Block ceilingBlock, Item.Properties properties) {
        super(standingBlock, properties);
        this.standingBlock = standingBlock;
        this.wallBlock = wallBlock;
        this.ceilingBlock = ceilingBlock;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (clickedFace == Direction.DOWN) {
            BlockState ceilingState = this.ceilingBlock.defaultBlockState();
            if (ceilingState.canSurvive(level, pos)) {
                return ceilingState;
            }
        }

        if (clickedFace == Direction.UP) {
            BlockState standingState = this.standingBlock.defaultBlockState();
            if (standingState.canSurvive(level, pos)) {
                return standingState;
            }
        }

        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        if (wallState != null && wallState.canSurvive(level, pos)) {
            return wallState;
        }

        BlockState standingState = this.standingBlock.defaultBlockState();
        if (standingState.canSurvive(level, pos)) {
            return standingState;
        }

        BlockState ceilingState = this.ceilingBlock.defaultBlockState();
        if (ceilingState.canSurvive(level, pos)) {
            return ceilingState;
        }

        return null;
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        blockToItemMap.put(this.wallBlock, item);
        blockToItemMap.put(this.ceilingBlock, item);
    }
}
