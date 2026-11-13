package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.compat.item.WeaponAttributes;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.google.common.collect.Multimap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return WeaponAttributes.base(this.tier, (float) Math.round(AntarchySettings.basiliskDaggerAttackDamage()), (float) AntarchySettings.basiliskDaggerAttackSpeed()).build();
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
