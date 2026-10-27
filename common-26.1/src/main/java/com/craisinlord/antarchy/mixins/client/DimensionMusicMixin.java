package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class DimensionMusicMixin {
    private static final Identifier CAVARYN_DIMENSION =
            Identifier.fromNamespaceAndPath("antarchy", "cavaryn");
    private static final Identifier THORAXIS_DIMENSION =
            Identifier.fromNamespaceAndPath("antarchy", "thoraxis");

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void antarchy$useDimensionBiomeMusic(CallbackInfoReturnable<Music> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Identifier dimensionId = level.dimension().identifier();
        if (!dimensionId.equals(CAVARYN_DIMENSION) && !dimensionId.equals(THORAXIS_DIMENSION)) {
            return;
        }

        BlockPos musicPos = minecraft.player != null ? minecraft.player.blockPosition() : BlockPos.containing(minecraft.gameRenderer.getMainCamera().position());
        Biome biome = level.getBiome(musicPos).value();
        BackgroundMusic music = biome.getAttributes().applyModifier(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.EMPTY);
        music.defaultMusic().ifPresent(cir::setReturnValue);
    }
}
