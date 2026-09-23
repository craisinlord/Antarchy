package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.NightmareSwordTooltipHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.TooltipFlag;

public class NightmareSwordItem extends SwordItem {
    private final Tier tier;
    private final float attackSpeed;

    public NightmareSwordItem(Tier tier, Item.Properties properties, float attackSpeed) {
        super(tier, properties);
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
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(
                this.tier,
                (int) Math.round(AntarchySettings.nightmareSwordBaseDamage()),
                (float) AntarchySettings.nightmareSwordAttackSpeed()
        );
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_sword").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Antarchy.physicalClient
                ? NightmareSwordTooltipHelper.damageLine()
                : damageLine((float) AntarchySettings.nightmareSwordBaseDamage()));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
