package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.block.GiantLilyPadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public final class GiantLilyPadItem extends BlockItem {
    public GiantLilyPadItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos fluidPos = hitResult.getBlockPos();
        BlockPos centerPos = fluidPos.above();
        if (!level.mayInteract(player, fluidPos) || !player.mayUseItemAt(centerPos, Direction.UP, stack)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!GiantLilyPadBlock.canPlaceStructure(level, centerPos)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            GiantLilyPadBlock.placeStructure(level, centerPos, GiantLilyPadBlock.rotationForPlacement(centerPos));
            level.playSound(null, centerPos, SoundEvents.LILY_PAD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        return this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }
}
