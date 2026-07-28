package com.craisinlord.antarchy.forge.mixins.level;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public abstract class LevelStemDiagnosticMixin {
    @Inject(method = "createTag", at = @At("HEAD"))
    private void antarchy$logLevelStems(RegistryAccess registryAccess, CompoundTag playerTag, CallbackInfoReturnable<CompoundTag> cir) {
        try {
            var registry = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
            Antarchy.LOGGER.info("[Antarchy/Diagnostic] createTag LEVEL_STEM size={} keys={}", registry.size(), registry.keySet());
        } catch (Throwable t) {
            Antarchy.LOGGER.error("[Antarchy/Diagnostic] createTag LEVEL_STEM lookup failed", t);
        }
    }
}
