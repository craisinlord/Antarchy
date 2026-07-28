package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class CloudBucketItem extends BlockItem {
    public CloudBucketItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public String getDescriptionId() {
        return Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 target = player.getEyePosition().add(player.getLookAngle().scale(2.0D));
        BlockPos placePos = BlockPos.containing(target);

        if (!level.getBlockState(placePos).canBeReplaced() || !player.mayUseItemAt(placePos, player.getDirection(), stack)) {
            return InteractionResultHolder.pass(stack);
        }

        BlockState placeState = getBlock().defaultBlockState();
        if (!level.isClientSide()) {
            level.setBlock(placePos, placeState, 11);
            level.playSound(null, placePos, placeState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos placePos = level.getBlockState(clickedPos).canBeReplaced() ? clickedPos : clickedPos.relative(context.getClickedFace());
        Player player = context.getPlayer();

        if (!level.getBlockState(placePos).canBeReplaced() || (player != null && !player.mayUseItemAt(placePos, context.getClickedFace(), context.getItemInHand()))) {
            return InteractionResult.FAIL;
        }

        BlockState placeState = getBlock().defaultBlockState();
        if (!level.isClientSide()) {
            level.setBlock(placePos, placeState, 11);
            level.playSound(null, placePos, placeState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (player != null && !player.getAbilities().instabuild && !level.isClientSide()) {
            player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.cloud_bucket.midair").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
