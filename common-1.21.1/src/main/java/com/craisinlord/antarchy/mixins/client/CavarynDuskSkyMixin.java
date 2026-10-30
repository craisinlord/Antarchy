package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class CavarynDuskSkyMixin {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );
    private static final float DUSK_SKY_DARKEN = 0.45F;

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void antarchy$cavarynDuskSky(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (((ClientLevel) (Object) this).dimension().equals(CAVARYN)) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), DUSK_SKY_DARKEN));
        }
    }
}
