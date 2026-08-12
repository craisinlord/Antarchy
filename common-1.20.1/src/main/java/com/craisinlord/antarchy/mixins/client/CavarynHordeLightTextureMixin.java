package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.HordeClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public abstract class CavarynHordeLightTextureMixin {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(Antarchy.MODID, "cavaryn")
    );

    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private static void antarchy$hordeAmbientBrightness(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        float intensity = HordeClientState.intensity();
        if (intensity <= 0.01F || minecraft.level == null || !minecraft.level.dimension().equals(CAVARYN)) {
            return;
        }

        float original = cir.getReturnValue();
        float hordeAmbient = 0.015F + 0.55F * intensity;
        cir.setReturnValue(Mth.clamp(original + hordeAmbient * (1.0F - original), 0.0F, 1.0F));
    }
}
