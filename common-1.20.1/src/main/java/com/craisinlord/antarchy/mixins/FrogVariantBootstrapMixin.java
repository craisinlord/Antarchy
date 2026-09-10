package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.ElythiaFrog;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.FrogVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrogVariant.class)
public abstract class FrogVariantBootstrapMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void antarchy$registerElythiaVariant(Registry<FrogVariant> registry, CallbackInfoReturnable<FrogVariant> cir) {
        ResourceLocation id = ElythiaFrog.VARIANT.location();
        if (registry.containsKey(id)) {
            return;
        }

        Registry.register(registry, id, new FrogVariant(ElythiaFrog.TEXTURE));
    }
}
