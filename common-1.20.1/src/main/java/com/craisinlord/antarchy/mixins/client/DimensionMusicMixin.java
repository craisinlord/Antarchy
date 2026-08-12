package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class DimensionMusicMixin {
    private static final ResourceLocation CAVARYN_DIMENSION =
            new ResourceLocation("antarchy", "cavaryn");
    private static final ResourceLocation THORAXIS_DIMENSION =
            new ResourceLocation("antarchy", "thoraxis");

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void antarchy$useDimensionBiomeMusic(CallbackInfoReturnable<Music> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        ResourceLocation dimensionId = level.dimension().location();
        if (!dimensionId.equals(CAVARYN_DIMENSION) && !dimensionId.equals(THORAXIS_DIMENSION)) {
            return;
        }

        BlockPos musicPos = minecraft.player != null ? minecraft.player.blockPosition() : BlockPos.containing(minecraft.gameRenderer.getMainCamera().getPosition());
        Biome biome = level.getBiome(musicPos).value();
        biome.getBackgroundMusic().ifPresent(cir::setReturnValue);
    }
}
