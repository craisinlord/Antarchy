package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.ElythiaFrog;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.FrogVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public abstract class FrogVariantBootstrapMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;freeze()V"))
    private static void antarchy$registerElythiaVariant(CallbackInfo ci) {
        ResourceLocation id = ElythiaFrog.VARIANT.location();
        if (!BuiltInRegistries.FROG_VARIANT.containsKey(id)) {
            Registry.register(BuiltInRegistries.FROG_VARIANT, id, new FrogVariant(ElythiaFrog.TEXTURE));
        }
    }
}
