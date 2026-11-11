package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.client.particle.GroundMistRenderType;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleEngine.class)
public abstract class GroundMistRenderOrderMixin {
    @Unique
    private static final Map<List<ParticleRenderType>, List<ParticleRenderType>> antarchy$renderOrders =
            Collections.synchronizedMap(new IdentityHashMap<>());

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleEngine;RENDER_ORDER:Ljava/util/List;"))
    private List<ParticleRenderType> antarchy$appendGroundMist(List<ParticleRenderType> original) {
        if (original.contains(GroundMistRenderType.INSTANCE)) {
            return original;
        }
        return antarchy$renderOrders.computeIfAbsent(original, source ->
                ImmutableList.<ParticleRenderType>builder().addAll(source).add(GroundMistRenderType.INSTANCE).build());
    }
}
