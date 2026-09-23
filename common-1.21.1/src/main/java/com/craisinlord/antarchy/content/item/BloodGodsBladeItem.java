package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public final class BloodGodsBladeItem extends SwordItem {
    private static final int BLEEDING_TICKS = 100;
    private static final int BLOOD_MOON_TICKS = 200;
    private static final int BLOOD_MOON_MAX_TICKS = 300;
    private static final int BLOOD_MOON_COOLDOWN_TICKS = 1200;
    private static final double BLOOD_MOON_RADIUS = 12.0D;
    private static final DustParticleOptions BLOOD_PARTICLES = new DustParticleOptions(new Vector3f(0.65F, 0.02F, 0.05F), 1.6F);

    private final Tier tier;
    private final int attackDamage;
    private final float attackSpeed;

    public BloodGodsBladeItem(Tier tier, Item.Properties properties, int attackDamage, float attackSpeed) {
        super(tier, properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(this.tier, this.attackDamage, this.attackSpeed);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(AntarchyObjects.BLEEDING_EFFECT.get(), BLEEDING_TICKS, 0, false, true, true));
            if (attacker instanceof Player player && player.hasEffect(AntarchyObjects.BLOOD_MOON_EFFECT.get())) {
                player.heal(1.0F);
                if (target.isDeadOrDying()) {
                    MobEffectInstance bloodMoon = player.getEffect(AntarchyObjects.BLOOD_MOON_EFFECT.get());
                    if (bloodMoon != null) {
                        player.addEffect(new MobEffectInstance(AntarchyObjects.BLOOD_MOON_EFFECT.get(), Math.min(BLOOD_MOON_MAX_TICKS, bloodMoon.getDuration() + 20), bloodMoon.getAmplifier(), false, true, true));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            player.getCooldowns().addCooldown(this, BLOOD_MOON_COOLDOWN_TICKS);
            player.addEffect(new MobEffectInstance(AntarchyObjects.BLOOD_MOON_EFFECT.get(), BLOOD_MOON_TICKS, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BLOOD_MOON_TICKS, 0, false, false, true));
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BLOOD_MOON_RADIUS), entity -> entity != player && entity.isAlive() && !player.isAlliedTo(entity))) {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, BLOOD_MOON_TICKS, 0, false, true, true));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BLOOD_MOON_TICKS, 0, false, true, true));
            }
            serverLevel.sendParticles(BLOOD_PARTICLES, player.getX(), player.getY() + 1.0D, player.getZ(), 160, 2.5D, 1.2D, 2.5D, 0.08D);
            serverLevel.sendParticles(BLOOD_PARTICLES, player.getX(), player.getY() + 8.0D, player.getZ(), 80, 5.0D, 0.2D, 5.0D, 0.02D);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.blood_gods_blade.bleeding").withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.blood_gods_blade.blood_moon").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
