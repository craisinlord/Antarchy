package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.CorneaStalkBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CorneaEarItem extends Item {
    public CorneaEarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction clickedFace = context.getClickedFace();
        if (clickedFace != Direction.UP && clickedFace != Direction.DOWN) {
            return InteractionResult.PASS;
        }

        BlockPos supportPos = context.getClickedPos();
        BlockPos plantPos = supportPos.relative(clickedFace);
        boolean hanging = clickedFace == Direction.DOWN;
        BlockState supportState = level.getBlockState(supportPos);
        if (!supportState.is(AntarchyTags.Blocks.CORNEA_STALK_PLANTABLE)) {
            return InteractionResult.PASS;
        }

        BlockState stalkState = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("antarchy", "cornea_stalk"))
                .map(block -> block.defaultBlockState().setValue(CorneaStalkBlock.HANGING, hanging))
                .orElse(Blocks.AIR.defaultBlockState());
        if (stalkState.isAir() || !level.getBlockState(plantPos).isAir() || !stalkState.canSurvive(level, plantPos)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        level.setBlock(plantPos, stalkState, Block.UPDATE_ALL);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        int durationTicks = Math.max(0, AntarchySettings.corneaEarNightVisionSeconds()) * 20;
        if (!level.isClientSide && durationTicks > 0) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, durationTicks, 0));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.cornea_ear.vision", AntarchySettings.corneaEarNightVisionSeconds()).withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
