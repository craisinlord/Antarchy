package com.craisinlord.antarchy.content.item;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.WormHookProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class WormHookItem extends Item {
    public WormHookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (WormHookTetherManager.hasTether(serverPlayer)) {
            WormHookTetherManager.detach(serverPlayer);
            return InteractionResult.CONSUME;
        }

        WormHookProjectileEntity hook = new WormHookProjectileEntity(AntarchyObjects.WORM_HOOK_PROJECTILE.get(), player, level);
        hook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.2F, 0.5F);
        level.addFreshEntity(hook);
        WormHookTetherManager.launch(serverPlayer, hook);

        stack.hurtAndBreak(1, serverPlayer, hand.asEquipmentSlot());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.7F, 0.9F);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.worm_hook.throw").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.worm_hook.reel").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipComponents, tooltipFlag);
    }
}
