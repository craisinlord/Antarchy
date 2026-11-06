package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.entity.royal.RoyalAssailantBlackHoleEntity;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RoyalAssailantBattleAxeItem extends Item {
    private static final int BLACK_HOLE_COOLDOWN_TICKS = 20 * 20;
    private static final int DILATED_DURATION_TICKS = 100;
    private static final Identifier REACH_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "royal_assailant_battleaxe_reach");
    private static final Identifier KNOCKBACK_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "royal_assailant_battleaxe_knockback");

    public RoyalAssailantBattleAxeItem(ToolMaterial material, Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(
                AntarchyItemProperties.configAttributes(
                        properties.axe(material,
                                (float) AntarchySettings.royalAssailantBattleAxeAttackDamage(),
                                (float) AntarchySettings.royalAssailantBattleAxeAttackSpeed()),
                        () -> createRoyalAttributes(material)),
                AntarchySettings::royalWeaponEnchantability));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return Items.NETHERITE_AXE.useOn(context);
    }

    public static ItemAttributeModifiers createRoyalAttributes(ToolMaterial material) {
        return AntarchyItemProperties.toolAttributes(
                        material,
                        (float) AntarchySettings.royalAssailantBattleAxeAttackDamage(),
                        (float) AntarchySettings.royalAssailantBattleAxeAttackSpeed())
                .withModifierAdded(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, AntarchySettings.royalWeaponAttackReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(
                        Attributes.ATTACK_KNOCKBACK,
                        new AttributeModifier(KNOCKBACK_MODIFIER_ID, AntarchySettings.royalWeaponAttackKnockbackBonus() + 1.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_assailant_battleaxe.black_hole")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_assailant_battleaxe.dilation")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel) {
            RoyalAssailantBlackHoleEntity blackHole = RoyalAssailantBlackHoleEntity.create(
                    serverLevel,
                    player.position().add(player.getLookAngle().normalize().scale(2.0D)).add(0.0D, 2.0D, 0.0D),
                    player.getUUID());
            serverLevel.addFreshEntity(blackHole);
            player.getCooldowns().addCooldown(stack, BLACK_HOLE_COOLDOWN_TICKS);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof Player player && !attacker.level().isClientSide()
                && RoyalEffectHooks.dilatedHolder() != null) {
            target.addEffect(new MobEffectInstance(RoyalEffectHooks.dilatedHolder(), DILATED_DURATION_TICKS), player);
        }
    }
}
