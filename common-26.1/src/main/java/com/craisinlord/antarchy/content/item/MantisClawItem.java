package com.craisinlord.antarchy.content.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class MantisClawItem extends Item {
    public static final double WALL_CLING_FALL_SPEED = -0.02D;
    public static final double WALL_CLIMB_JUMP_VELOCITY = 0.42D;
    private static final int DEFAULT_ATTACK_DAMAGE = 7;
    private static final float DEFAULT_ATTACK_SPEED = -2.4F;

    private final ToolMaterial material;

    public MantisClawItem(ToolMaterial material, Item.Properties properties) {
        super(properties.sword(material, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED));
        this.material = material;
    }

    public static boolean isDualWielding(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof MantisClawItem
                && entity.getOffhandItem().getItem() instanceof MantisClawItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.mantis_claw.wall_cling").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
