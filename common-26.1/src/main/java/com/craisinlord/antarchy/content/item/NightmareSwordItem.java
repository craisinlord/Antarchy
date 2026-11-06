package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.NightmareSwordTooltipHelper;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;

public class NightmareSwordItem extends Item {
    private final ToolMaterial material;
    private final float attackSpeed;

    public NightmareSwordItem(ToolMaterial material, Item.Properties properties, float attackSpeed) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material, (float) Math.round(AntarchySettings.nightmareSwordBaseDamage()), attackSpeed),
                () -> AntarchyItemProperties.toolAttributes(
                        material,
                        (int) Math.round(AntarchySettings.nightmareSwordBaseDamage()),
                        (float) AntarchySettings.nightmareSwordAttackSpeed()
                )
        ));
        this.material = material;
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

    public static Component damageLine(float damage) {
        float rounded = Math.round(damage * 10.0F) / 10.0F;
        String text = rounded == (int) rounded ? String.valueOf((int) rounded) : String.valueOf(rounded);
        return Component.translatable("tooltip.antarchy.nightmare_sword_damage", text).withStyle(ChatFormatting.RED);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.nightmare_sword").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.accept(Antarchy.physicalClient
                ? NightmareSwordTooltipHelper.damageLine()
                : damageLine((float) AntarchySettings.nightmareSwordBaseDamage()));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
