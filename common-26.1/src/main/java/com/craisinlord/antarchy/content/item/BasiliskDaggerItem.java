package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class BasiliskDaggerItem extends Item {
    private final ToolMaterial material;
    private final int attackDamage;
    private final float attackSpeed;

    public BasiliskDaggerItem(ToolMaterial material, Item.Properties properties, int attackDamage, float attackSpeed) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material, attackDamage, attackSpeed),
                () -> AntarchyItemProperties.toolAttributes(
                        material,
                        (int) Math.round(AntarchySettings.basiliskDaggerAttackDamage()),
                        (float) AntarchySettings.basiliskDaggerAttackSpeed()
                )
        ));
        this.material = material;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    AntarchySettings.basiliskDaggerPoisonDurationTicks(),
                    AntarchySettings.basiliskDaggerPoisonAmplifier()
            ));
        }
    }
}
