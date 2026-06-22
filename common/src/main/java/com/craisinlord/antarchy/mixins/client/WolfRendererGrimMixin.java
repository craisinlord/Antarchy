package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererGrimMixin {
    @Unique
    private static final ResourceLocation ANTARCHY$GRIM_WILD_TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/grim/grim_spotted.png");

    @Unique
    private static final ResourceLocation ANTARCHY$GRIM_TAME_TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/grim/grim_spotted_tame.png");

    @Unique
    private static final ResourceLocation ANTARCHY$GRIM_ANGRY_TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/grim/grim_spotted_angry.png");

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Wolf;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$useGrimTexture(Wolf wolf, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!antarchy$isGrim(wolf)) {
            return;
        }

        if (wolf.isAngry()) {
            cir.setReturnValue(ANTARCHY$GRIM_ANGRY_TEXTURE);
            return;
        }

        cir.setReturnValue(wolf.isTame() ? ANTARCHY$GRIM_TAME_TEXTURE : ANTARCHY$GRIM_WILD_TEXTURE);
    }

    @Unique
    private static boolean antarchy$isGrim(Wolf wolf) {
        return wolf.hasCustomName() && "grim".equalsIgnoreCase(wolf.getName().getString().trim());
    }
}
