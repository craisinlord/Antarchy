package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public class AttitudeAdjusterItem extends SwordItem {
    private static final float ATTACK_SPEED = -3.6F;
    private static final int USE_DURATION = 12;
    private final Tier tier;

    public AttitudeAdjusterItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
        this.tier = tier;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(
                this.tier,
                (int) Math.round(AntarchySettings.attitudeAdjusterBaseDamage()),
                ATTACK_SPEED
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.attitude_adjuster.grounded").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.attitude_adjuster.slam").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hurt = super.hurtEnemy(stack, target, attacker);
        if (!hurt || !(attacker instanceof Player player) || attacker.level().isClientSide || player.getMainHandItem() != stack || !(attacker.level() instanceof ServerLevel serverLevel)) {
            return hurt;
        }
        if (!AttitudeAdjusterSlamManager.consumeSpecialHit(player)) {
            return hurt;
        }
        if (target.onGround()) {
            AttitudeAdjusterSlamManager.onGroundedHit(serverLevel, player, target);
        } else {
            AttitudeAdjusterSlamManager.onAirborneHit(serverLevel, player, target);
        }
        return hurt;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!AttitudeAdjusterSlamManager.tryStartPlayerSlam(serverPlayer, hand)) {
            return InteractionResultHolder.pass(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState state, Level level, net.minecraft.core.BlockPos pos, Player miner) {
        return !miner.isCreative();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }
}
