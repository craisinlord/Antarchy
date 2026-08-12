package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.NightmareSwordTooltipHelper;
import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

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
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_sword").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(NightmareSwordTooltipHelper.damageLine());
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
