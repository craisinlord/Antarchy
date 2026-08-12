package com.craisinlord.antarchy.content.item;

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

public class MantisClawItem extends SwordItem {
    public static final double WALL_CLING_FALL_SPEED = -0.02D;
    public static final double WALL_CLIMB_JUMP_VELOCITY = 0.42D;
    private static final int DEFAULT_ATTACK_DAMAGE = 7;
    private static final float DEFAULT_ATTACK_SPEED = -2.4F;

    private final Tier tier;

    public MantisClawItem(Tier tier, Item.Properties properties) {
        super(tier, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED, properties);
        this.tier = tier;
    }

    public static boolean isDualWielding(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof MantisClawItem
                && entity.getOffhandItem().getItem() instanceof MantisClawItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.mantis_claw.wall_cling").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
