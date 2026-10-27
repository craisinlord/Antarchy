package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AttitudeAdjusterItem extends Item {
    private static final float ATTACK_SPEED = -3.6F;
    private static final int USE_DURATION = 12;
    private final ToolMaterial material;

    public AttitudeAdjusterItem(ToolMaterial material, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material, (float) Math.round(AntarchySettings.attitudeAdjusterBaseDamage()), ATTACK_SPEED),
                () -> AntarchyItemProperties.toolAttributes(
                        material,
                        (int) Math.round(AntarchySettings.attitudeAdjusterBaseDamage()),
                        ATTACK_SPEED
                )
        ));
        this.material = material;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.attitude_adjuster.grounded").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.attitude_adjuster.slam").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!(attacker instanceof Player player) || attacker.level().isClientSide() || player.getMainHandItem() != stack || !(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!AttitudeAdjusterSlamManager.consumeSpecialHit(player)) {
            return;
        }
        if (target.onGround()) {
            AttitudeAdjusterSlamManager.onGroundedHit(serverLevel, player, target);
        } else {
            AttitudeAdjusterSlamManager.onAirborneHit(serverLevel, player, target);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        if (!AttitudeAdjusterSlamManager.tryStartPlayerSlam(serverPlayer, hand)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return !(user instanceof Player miner && miner.isCreative());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }
}
