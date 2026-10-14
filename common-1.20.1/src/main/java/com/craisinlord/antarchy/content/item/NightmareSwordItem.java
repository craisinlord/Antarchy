package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.NightmareSwordTooltipHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NightmareSwordItem extends SwordItem {
    private final Tier tier;
    private final float attackSpeed;

    public NightmareSwordItem(Tier tier, Item.Properties properties, float attackSpeed) {
        super(tier, (int) Math.round(AntarchySettings.nightmareSwordBaseDamage()), (float) AntarchySettings.nightmareSwordAttackSpeed(), properties);
        this.tier = tier;
        this.attackSpeed = attackSpeed;
    }

    public static float calculateDamage(LivingEntity attacker) {
        float baseDamage = (float) AntarchySettings.nightmareSwordBaseDamage();
        float maxHealth = attacker.getMaxHealth();
        if (maxHealth <= 0.0F) {
            return baseDamage;
        }

        float missingFraction = (maxHealth - attacker.getHealth()) / maxHealth;
        float scalingFactor = (float) AntarchySettings.nightmareSwordScalingFactor();
        return baseDamage + missingFraction * scalingFactor * baseDamage;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier",
                (float) (int) Math.round(AntarchySettings.nightmareSwordBaseDamage()) + this.tier.getAttackDamageBonus(),
                AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID,
                "Weapon modifier",
                (float) AntarchySettings.nightmareSwordAttackSpeed(),
                AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public static Component damageLine(float damage) {
        float rounded = Math.round(damage * 10.0F) / 10.0F;
        String text = rounded == (int) rounded ? String.valueOf((int) rounded) : String.valueOf(rounded);
        return Component.translatable("tooltip.antarchy.nightmare_sword_damage", text).withStyle(ChatFormatting.RED);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_sword").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Antarchy.physicalClient
                ? NightmareSwordTooltipHelper.damageLine()
                : damageLine((float) AntarchySettings.nightmareSwordBaseDamage()));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
