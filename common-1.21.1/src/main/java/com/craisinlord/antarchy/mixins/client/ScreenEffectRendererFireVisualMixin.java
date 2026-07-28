package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.fire.AntarchyFireVisualAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererFireVisualMixin {
    private static final ResourceLocation SOUL_FIRE_1 = ResourceLocation.withDefaultNamespace("block/soul_fire_1");
    private static final ResourceLocation DREAM_FIRE_1 = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/dream_fire_1");

    @ModifyVariable(method = "renderFire", at = @At("STORE"), ordinal = 0)
    private static TextureAtlasSprite antarchy$swapPlayerFireOverlay(
            TextureAtlasSprite original,
            Minecraft minecraft,
            PoseStack poseStack
    ) {
        if (!AntarchySettings.entitySpecificFireOverlayEnabled()
                || minecraft.player == null
                || !(minecraft.player instanceof AntarchyFireVisualAccess access)) {
            return original;
        }

        return switch (access.antarchy$getFireVisualType()) {
            case SOUL -> antarchy$getBlockAtlasSprite(SOUL_FIRE_1);
            case DREAM -> antarchy$getBlockAtlasSprite(DREAM_FIRE_1);
            case NORMAL -> original;
        };
    }

    private static TextureAtlasSprite antarchy$getBlockAtlasSprite(ResourceLocation texture) {
        return Minecraft.getInstance()
                .getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(texture);
    }
}
