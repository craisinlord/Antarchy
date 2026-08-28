package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UpwardFallingBlockEntity.class)
public abstract class DreamSandFallingSpeedMixin {
    private static final ResourceLocation DREAM_SAND_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_sand");

    @Shadow public abstract BlockState getBlockState();

    @Inject(method = "getRiseAccel", at = @At("RETURN"), cancellable = true, remap = false)
    private void antarchy$slowDreamSandRise(CallbackInfoReturnable<Double> cir) {
        if (BuiltInRegistries.BLOCK.getKey(this.getBlockState().getBlock()).equals(DREAM_SAND_ID)) {
            cir.setReturnValue(cir.getReturnValue() * AntarchySettings.dreamSandFallingBlockGravityMultiplier());
        }
    }
}
