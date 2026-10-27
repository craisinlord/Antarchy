package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraModel.class)
public interface ElytraModelAccessor {
    @Accessor("leftWing")
    ModelPart antarchy$getLeftWing();

    @Accessor("rightWing")
    ModelPart antarchy$getRightWing();
}
