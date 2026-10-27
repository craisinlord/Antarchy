package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.fire.AntarchyFireVisualAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererFireVisualMixin {
    private static final Identifier SOUL_FIRE_1 = Identifier.withDefaultNamespace("block/soul_fire_1");
    private static final Identifier DREAM_FIRE_1 = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/dream_fire_1");

    @ModifyVariable(method = "submitFire", at = @At("HEAD"), argsOnly = true)
    private static TextureAtlasSprite antarchy$swapPlayerFireOverlay(TextureAtlasSprite original) {
        Minecraft minecraft = Minecraft.getInstance();
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

    private static TextureAtlasSprite antarchy$getBlockAtlasSprite(Identifier texture) {
        return Minecraft.getInstance()
                .getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, texture));
    }
}
