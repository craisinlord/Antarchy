package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class MinersDreamItem extends Item {
    public MinersDreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!AntarchySettings.minersDreamEnabled()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.antarchy.miners_dream_disabled"), true);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Direction travelDirection = clickedFace.getAxis().isHorizontal() ? clickedFace : player.getDirection();
        BlockPos startPos = clickedPos.relative(travelDirection);

        if (!serverLevel.isInWorldBounds(startPos) || !MinersDreamCaveGenerator.isChunkLoaded(serverLevel, startPos)) {
            return InteractionResult.FAIL;
        }

        if (MinersDreamExcavationManager.hasActiveExcavation(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.antarchy.miners_dream_busy"), true);
            return InteractionResult.FAIL;
        }

        MinersDreamExcavation excavation = MinersDreamExcavationManager.tryStart(serverLevel, player.getUUID(), startPos, travelDirection);
        if (excavation == null) {
            return InteractionResult.FAIL;
        }

        serverLevel.playSound(null, clickedPos, SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.8F, 1.1F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.miners_dream.line1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.miners_dream.line2").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
