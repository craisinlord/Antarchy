package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererGrimMixin {
    @Unique
    private static final Identifier ANTARCHY$GRIM_WILD_TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/grim/grim_spotted.png");

    @Unique
    private static final Identifier ANTARCHY$GRIM_TAME_TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/grim/grim_spotted_tame.png");

    @Unique
    private static final Identifier ANTARCHY$GRIM_ANGRY_TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/grim/grim_spotted_angry.png");

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/animal/wolf/Wolf;Lnet/minecraft/client/renderer/entity/state/WolfRenderState;F)V",
            at = @At("TAIL")
    )
    private void antarchy$useGrimTexture(Wolf wolf, WolfRenderState state, float partialTicks, CallbackInfo ci) {
        if (!antarchy$isGrim(wolf)) {
            return;
        }

        if (wolf.isAngry()) {
            state.texture = ANTARCHY$GRIM_ANGRY_TEXTURE;
            return;
        }

        state.texture = wolf.isTame() ? ANTARCHY$GRIM_TAME_TEXTURE : ANTARCHY$GRIM_WILD_TEXTURE;
    }

    @Unique
    private static boolean antarchy$isGrim(Wolf wolf) {
        return wolf.hasCustomName() && "grim".equalsIgnoreCase(wolf.getName().getString().trim());
    }
}
