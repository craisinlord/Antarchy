package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class DimensionMusicMixin {
    private static final ResourceLocation CAVARYN_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("antarchy", "cavaryn");
    private static final ResourceLocation THORAXIS_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("antarchy", "thoraxis");
    private static final float THROWBACK_MUSIC_THRESHOLD = 0.25F;
    private static Music antarchy$throwbackMusic;
    private static Music antarchy$continuingMusic;

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void antarchy$useDimensionBiomeMusic(CallbackInfoReturnable<Music> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        if (level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            SoundInstance current = ((MusicManagerAccessor) minecraft.getMusicManager()).antarchy$getCurrentMusic();
            if (current != null && minecraft.getSoundManager().isActive(current)) {
                cir.setReturnValue(antarchy$continue(current.getLocation()));
                return;
            }
            if (AntarchySettings.throwbackClassicMusic() && ThrowbackClientState.zone(1.0F) > THROWBACK_MUSIC_THRESHOLD) {
                if (antarchy$throwbackMusic == null) {
                    antarchy$throwbackMusic = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(AntarchySoundEvents.THROWBACK_MUSIC.get()), 0, 40, false);
                }
                cir.setReturnValue(antarchy$throwbackMusic);
                return;
            }
        }

        ResourceLocation dimensionId = level.dimension().location();
        if (!dimensionId.equals(CAVARYN_DIMENSION) && !dimensionId.equals(THORAXIS_DIMENSION)) {
            return;
        }

        BlockPos musicPos = minecraft.player != null ? minecraft.player.blockPosition() : BlockPos.containing(minecraft.gameRenderer.getMainCamera().getPosition());
        Biome biome = level.getBiome(musicPos).value();
        biome.getBackgroundMusic().ifPresent(cir::setReturnValue);
    }

    private static Music antarchy$continue(ResourceLocation playing) {
        if (antarchy$continuingMusic == null || !antarchy$continuingMusic.getEvent().value().getLocation().equals(playing)) {
            antarchy$continuingMusic = new Music(Holder.direct(SoundEvent.createVariableRangeEvent(playing)), 20, 600, false);
        }
        return antarchy$continuingMusic;
    }
}
