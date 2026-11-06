package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.AntarchyItemProperties;
import java.util.function.DoubleSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class UltimateSwordItem extends Item {
    private final ToolMaterial material;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;

    public UltimateSwordItem(ToolMaterial material, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(AntarchyItemProperties.configEnchantable(
                AntarchyItemProperties.configAttributes(
                        properties.sword(material, (float) Math.round(attackDamage.getAsDouble()), attackSpeed),
                        () -> AntarchyItemProperties.toolAttributes(
                                material,
                                (int) Math.round(attackDamage.getAsDouble()),
                                (float) AntarchySettings.ultimateSwordAttackSpeed()
                        )
                ),
                AntarchySettings::ultimateToolEnchantability
        ));
        this.material = material;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }
}
