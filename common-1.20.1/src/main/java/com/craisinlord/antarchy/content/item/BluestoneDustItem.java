package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.block.BluestoneWireBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BluestoneDustItem extends Item {
    private final BluestoneWireBlock wireBlock;

    public BluestoneDustItem(BluestoneWireBlock wireBlock, Properties properties) {
        super(properties);
        this.wireBlock = wireBlock;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.bluestone_placement").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != net.minecraft.core.Direction.DOWN) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        BlockState existing = level.getBlockState(targetPos);
        if (!existing.canBeReplaced() && !existing.isAir()) {
            return InteractionResult.FAIL;
        }

        BlockPlaceContext placeContext = BlockPlaceContext.at(new BlockPlaceContext(context), targetPos, context.getClickedFace());
        BlockState placeState = this.wireBlock.getStateForPlacement(placeContext);
        if (placeState == null) {
            return InteractionResult.FAIL;
        }

        if (!level.setBlock(targetPos, placeState, 11)) {
            return InteractionResult.FAIL;
        }

        SoundType soundType = placeState.getSoundType();
        level.playSound(context.getPlayer(), targetPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
