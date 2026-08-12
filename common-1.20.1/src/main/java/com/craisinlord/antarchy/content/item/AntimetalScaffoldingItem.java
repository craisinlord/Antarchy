package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ScaffoldingBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AntimetalScaffoldingItem extends ScaffoldingBlockItem {
    public AntimetalScaffoldingItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

//    @Override
//    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
//        tooltipComponents.add(Component.translatable("tooltip.antarchy.antimetal_scaffolding").withStyle(ChatFormatting.DARK_AQUA));
//        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
//    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState clickedState = level.getBlockState(clickedPos);
        Block block = this.getBlock();
        boolean inverted = context.getPlayer() != null && AntarchyGravityApi.isGravityInverted(context.getPlayer());

        if (!clickedState.is(block)) {
            return AntimetalScaffoldingBlock.getDistance(level, clickedPos, inverted) == 7 ? null : context;
        }

        Direction awayFromSupport = inverted ? Direction.DOWN : Direction.UP;
        Direction direction;
        if (context.isSecondaryUseActive()) {
            direction = context.isInside() ? context.getClickedFace().getOpposite() : context.getClickedFace();
        } else {
            direction = context.getClickedFace() == awayFromSupport ? context.getHorizontalDirection() : awayFromSupport;
        }

        int steps = 0;
        BlockPos.MutableBlockPos cursor = clickedPos.mutable().move(direction);

        while (steps < 7) {
            if (!level.isClientSide && !level.isInWorldBounds(cursor)) {
                break;
            }

            BlockState state = level.getBlockState(cursor);
            if (!state.is(block)) {
                if (state.canBeReplaced(context)) {
                    return BlockPlaceContext.at(context, cursor, direction);
                }
                break;
            }

            cursor.move(direction);
            if (direction.getAxis().isHorizontal()) {
                steps++;
            }
        }

        return null;
    }
}
