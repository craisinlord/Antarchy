package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.TemporalContractionAfterimageLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class TemporalContractionLivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void antarchy$addTemporalContractionLayer(EntityRendererProvider.Context context, M model,
                                                       float shadowRadius, CallbackInfo callbackInfo) {
        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        renderer.addLayer(new TemporalContractionAfterimageLayer<>(renderer));
    }
}
