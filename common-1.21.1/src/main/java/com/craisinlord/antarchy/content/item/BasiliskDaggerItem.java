package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class BasiliskDaggerItem extends SwordItem {
    private final Tier tier;
    private final int attackDamage;
    private final float attackSpeed;

    public BasiliskDaggerItem(Tier tier, Item.Properties properties, int attackDamage, float attackSpeed) {
        super(tier, properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(
                this.tier,
                (int) Math.round(AntarchySettings.basiliskDaggerAttackDamage()),
                (float) AntarchySettings.basiliskDaggerAttackSpeed()
        );
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
