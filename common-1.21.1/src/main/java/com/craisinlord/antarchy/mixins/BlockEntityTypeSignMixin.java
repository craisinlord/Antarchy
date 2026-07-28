package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeSignMixin {
    @Unique
    private static Block antarchy$ouranwoodSign;
    @Unique
    private static Block antarchy$ouranwoodWallSign;
    @Unique
    private static Block antarchy$ouranwoodHangingSign;
    @Unique
    private static Block antarchy$ouranwoodWallHangingSign;
    @Unique
    private static Block antarchy$peachSign;
    @Unique
    private static Block antarchy$peachWallSign;
    @Unique
    private static Block antarchy$peachHangingSign;
    @Unique
    private static Block antarchy$peachWallHangingSign;

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowOuranwoodSigns(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self == BlockEntityType.SIGN) {
            if (state.is(antarchy$sign()) || state.is(antarchy$wallSign())
                    || state.is(antarchy$peachSign()) || state.is(antarchy$peachWallSign())) {
                cir.setReturnValue(true);
            }
        } else if (self == BlockEntityType.HANGING_SIGN) {
            if (state.is(antarchy$hangingSign()) || state.is(antarchy$wallHangingSign())
                    || state.is(antarchy$peachHangingSign()) || state.is(antarchy$peachWallHangingSign())) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private static Block antarchy$sign() {
        if (antarchy$ouranwoodSign == null) {
            antarchy$ouranwoodSign = antarchy$resolve("ouranwood_sign");
        }
        return antarchy$ouranwoodSign;
    }

    @Unique
    private static Block antarchy$wallSign() {
        if (antarchy$ouranwoodWallSign == null) {
            antarchy$ouranwoodWallSign = antarchy$resolve("ouranwood_wall_sign");
        }
        return antarchy$ouranwoodWallSign;
    }

    @Unique
    private static Block antarchy$hangingSign() {
        if (antarchy$ouranwoodHangingSign == null) {
            antarchy$ouranwoodHangingSign = antarchy$resolve("ouranwood_hanging_sign");
        }
        return antarchy$ouranwoodHangingSign;
    }

    @Unique
    private static Block antarchy$wallHangingSign() {
        if (antarchy$ouranwoodWallHangingSign == null) {
            antarchy$ouranwoodWallHangingSign = antarchy$resolve("ouranwood_wall_hanging_sign");
        }
        return antarchy$ouranwoodWallHangingSign;
    }

    @Unique
    private static Block antarchy$peachSign() {
        if (antarchy$peachSign == null) {
            antarchy$peachSign = antarchy$resolve("peach_sign");
        }
        return antarchy$peachSign;
    }

    @Unique
    private static Block antarchy$peachWallSign() {
        if (antarchy$peachWallSign == null) {
            antarchy$peachWallSign = antarchy$resolve("peach_wall_sign");
        }
        return antarchy$peachWallSign;
    }

    @Unique
    private static Block antarchy$peachHangingSign() {
        if (antarchy$peachHangingSign == null) {
            antarchy$peachHangingSign = antarchy$resolve("peach_hanging_sign");
        }
        return antarchy$peachHangingSign;
    }

    @Unique
    private static Block antarchy$peachWallHangingSign() {
        if (antarchy$peachWallHangingSign == null) {
            antarchy$peachWallHangingSign = antarchy$resolve("peach_wall_hanging_sign");
        }
        return antarchy$peachWallHangingSign;
    }

    @Unique
    private static Block antarchy$resolve(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
