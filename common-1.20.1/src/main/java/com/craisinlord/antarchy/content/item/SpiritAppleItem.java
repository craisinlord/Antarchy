package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SpiritAppleItem extends Item {
    public static final int GLIMMERING_DURATION_TICKS = 20 * 8;

    public SpiritAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(AntarchyObjects.GLIMMERING_EFFECT.get(), GLIMMERING_DURATION_TICKS, 0, false, false, true));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.spirit_apple").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.spirit_apple.eaten").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
