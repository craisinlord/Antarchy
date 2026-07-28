package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import com.craisinlord.antarchy.config.AntarchySettings;

public class BloodCrystalKatanaItem extends SwordItem {
    @FunctionalInterface
    public interface TrailCallback {
        void trigger(ServerPlayer player, int durationTicks);
    }

    private static TrailCallback trailCallback = (player, durationTicks) -> {};

    private final Tier tier;
    private final int attackDamage;
    private final float attackSpeed;

    public BloodCrystalKatanaItem(Tier tier, Item.Properties properties, int attackDamage, float attackSpeed) {
        super(tier, properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(this.tier, this.attackDamage, this.attackSpeed);
    }

    public static void setTrailCallback(TrailCallback callback) {
        trailCallback = callback == null ? (player, durationTicks) -> {} : callback;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker instanceof ServerPlayer player)) {
            return result;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
        }

        Vec3 launch = horizontal.normalize().scale(AntarchySettings.bloodCrystalKatanaLaunchStrength())
                .add(0.0D, 0.12D, 0.0D);
        player.setDeltaMovement(player.getDeltaMovement().add(launch));
        player.hasImpulse = true;
        player.hurtMarked = true;
        trailCallback.trigger(player, AntarchySettings.bloodCrystalKatanaTrailDurationTicks());
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.blood_crystal_katana.launch").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.blood_crystal_katana.trail").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
