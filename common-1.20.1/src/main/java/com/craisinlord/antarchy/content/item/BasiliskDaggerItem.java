package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class BasiliskDaggerItem extends SwordItem {
    private final Tier tier;
    private final int attackDamage;
    private final float attackSpeed;

    public BasiliskDaggerItem(Tier tier, Item.Properties properties, int attackDamage, float attackSpeed) {
        super(tier, (int) Math.round(AntarchySettings.basiliskDaggerAttackDamage()), (float) AntarchySettings.basiliskDaggerAttackSpeed(), properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot) {
        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return com.craisinlord.antarchy.compat.item.WeaponAttributes.base(this.tier, (float) Math.round(AntarchySettings.basiliskDaggerAttackDamage()), (float) AntarchySettings.basiliskDaggerAttackSpeed()).build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean didHurt = super.hurtEnemy(stack, target, attacker);
        if (didHurt && !target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    AntarchySettings.basiliskDaggerPoisonDurationTicks(),
                    AntarchySettings.basiliskDaggerPoisonAmplifier()
            ));
        }
        return didHurt;
    }
}
