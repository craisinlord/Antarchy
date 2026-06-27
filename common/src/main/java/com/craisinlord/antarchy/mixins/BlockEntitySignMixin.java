package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.Antarchy;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.level.block.entity.BlockEntity.class)
public abstract class BlockEntitySignMixin {
    private static final ResourceLocation OURANWOOD_SIGN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ouranwood_sign");
    private static final ResourceLocation OURANWOOD_WALL_SIGN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ouranwood_wall_sign");
    private static final ResourceLocation OURANWOOD_HANGING_SIGN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ouranwood_hanging_sign");
    private static final ResourceLocation OURANWOOD_WALL_HANGING_SIGN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ouranwood_wall_hanging_sign");

    @Inject(method = "validateBlockState", at = @At("HEAD"), cancellable = true)
    private static void antarchy$skipOuranwoodSignValidation(BlockState state, CallbackInfo ci) {
        if (isOuranwoodSign(state)) {
            ci.cancel();
        }
    }

    private static boolean isOuranwoodSign(BlockState state) {
        return matches(state, OURANWOOD_SIGN_ID)
                || matches(state, OURANWOOD_WALL_SIGN_ID)
                || matches(state, OURANWOOD_HANGING_SIGN_ID)
                || matches(state, OURANWOOD_WALL_HANGING_SIGN_ID);
    }

    private static boolean matches(BlockState state, ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id).map(state::is).orElse(false);
    }
}
