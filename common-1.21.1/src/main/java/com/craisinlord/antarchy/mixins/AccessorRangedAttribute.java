package com.craisinlord.antarchy.mixins;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RangedAttribute.class)
public interface AccessorRangedAttribute {

    @Accessor("minValue")
    @Mutable
    void antarchyAttributefix$setMinValue(double minValue);

    @Accessor("maxValue")
    @Mutable
    void antarchyAttributefix$setMaxValue(double maxValue);
}