package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaHydrology;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaHydrologyHolder;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures Elythia's hydrology right after the router is wired. C2ME's density function compiler swaps the router for
 * compiled functions at the end of this constructor, which hides the hydrology from {@link ElythiaAquiferMixin}.
 */
@Mixin(RandomState.class)
public abstract class ElythiaRandomStateMixin implements ElythiaHydrologyHolder {
    @Shadow
    @Final
    private NoiseRouter router;

    @Unique
    @Nullable
    private ElythiaHydrology antarchy$hydrology;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/levelgen/RandomState;router:Lnet/minecraft/world/level/levelgen/NoiseRouter;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void antarchy$captureHydrology(CallbackInfo ci) {
        this.antarchy$hydrology = ElythiaHydrology.find(this.router.veinToggle());
    }

    @Override
    @Nullable
    public ElythiaHydrology antarchy$hydrology() {
        return this.antarchy$hydrology;
    }
}
