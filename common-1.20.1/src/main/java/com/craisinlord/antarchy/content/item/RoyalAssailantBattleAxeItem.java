package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.compat.entity.EntityReach;
import com.craisinlord.antarchy.compat.item.WeaponAttributes;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.entity.royal.RoyalAssailantBlackHoleEntity;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RoyalAssailantBattleAxeItem extends AxeItem {
    private static final int BLACK_HOLE_COOLDOWN_TICKS = 20 * 20;
    private static final int DILATED_DURATION_TICKS = 100;
    private static final ResourceLocation REACH_MODIFIER_ID = new ResourceLocation("antarchy", "royal_assailant_battleaxe_reach");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = new ResourceLocation("antarchy", "royal_assailant_battleaxe_knockback");
    private final Tier tier;

    public RoyalAssailantBattleAxeItem(Tier tier, Item.Properties properties) {
        super(tier, (float) AntarchySettings.royalAssailantBattleAxeAttackDamage(), (float) AntarchySettings.royalAssailantBattleAxeAttackSpeed(), properties);
        this.tier = tier;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder =
                WeaponAttributes.base(this.tier, (float) AntarchySettings.royalAssailantBattleAxeAttackDamage(), (float) AntarchySettings.royalAssailantBattleAxeAttackSpeed());
        EntityReach.addModifier(builder, REACH_MODIFIER_ID, AntarchySettings.royalWeaponAttackReachBonus());
        WeaponAttributes.add(builder, Attributes.ATTACK_KNOCKBACK, KNOCKBACK_MODIFIER_ID, AntarchySettings.royalWeaponAttackKnockbackBonus() + 1.0D);
        return builder.build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalWeaponEnchantability();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_assailant_battleaxe.black_hole")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_assailant_battleaxe.dilation")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (level instanceof ServerLevel serverLevel) {
            RoyalAssailantBlackHoleEntity blackHole = RoyalAssailantBlackHoleEntity.create(
                    serverLevel,
                    player.position().add(player.getLookAngle().normalize().scale(2.0D)).add(0.0D, 2.0D, 0.0D),
                    player.getUUID());
            serverLevel.addFreshEntity(blackHole);
            player.getCooldowns().addCooldown(this, BLACK_HOLE_COOLDOWN_TICKS);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hurt = super.hurtEnemy(stack, target, attacker);
        if (hurt && attacker instanceof Player player && !attacker.level().isClientSide
                && RoyalEffectHooks.dilatedHolder() != null) {
            target.addEffect(new MobEffectInstance(RoyalEffectHooks.dilatedHolder(), DILATED_DURATION_TICKS), player);
        }
        return hurt;
    }
}
