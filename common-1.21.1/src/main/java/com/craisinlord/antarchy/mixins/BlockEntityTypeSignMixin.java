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
    @Unique
    private static Block antarchy$nadirSign;
    @Unique
    private static Block antarchy$nadirWallSign;
    @Unique
    private static Block antarchy$nadirHangingSign;
    @Unique
    private static Block antarchy$nadirWallHangingSign;
    @Unique
    private static Block antarchy$royalSign;
    @Unique
    private static Block antarchy$royalWallSign;
    @Unique
    private static Block antarchy$royalHangingSign;
    @Unique
    private static Block antarchy$royalWallHangingSign;
    @Unique
    private static Block antarchy$truffaloSign;
    @Unique
    private static Block antarchy$truffaloWallSign;
    @Unique
    private static Block antarchy$truffaloHangingSign;
    @Unique
    private static Block antarchy$truffaloWallHangingSign;

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowOuranwoodSigns(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self == BlockEntityType.SIGN) {
            if (state.is(antarchy$sign()) || state.is(antarchy$wallSign())
                    || state.is(antarchy$peachSign()) || state.is(antarchy$peachWallSign())
                    || state.is(antarchy$nadirSign()) || state.is(antarchy$nadirWallSign())
                    || state.is(antarchy$royalSign()) || state.is(antarchy$royalWallSign())
                    || state.is(antarchy$truffaloSign()) || state.is(antarchy$truffaloWallSign())) {
                cir.setReturnValue(true);
            }
        } else if (self == BlockEntityType.HANGING_SIGN) {
            if (state.is(antarchy$hangingSign()) || state.is(antarchy$wallHangingSign())
                    || state.is(antarchy$peachHangingSign()) || state.is(antarchy$peachWallHangingSign())
                    || state.is(antarchy$nadirHangingSign()) || state.is(antarchy$nadirWallHangingSign())
                    || state.is(antarchy$royalHangingSign()) || state.is(antarchy$royalWallHangingSign())
                    || state.is(antarchy$truffaloHangingSign()) || state.is(antarchy$truffaloWallHangingSign())) {
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
    private static Block antarchy$nadirSign() {
        if (antarchy$nadirSign == null) {
            antarchy$nadirSign = antarchy$resolve("nadir_sign");
        }
        return antarchy$nadirSign;
    }

    @Unique
    private static Block antarchy$nadirWallSign() {
        if (antarchy$nadirWallSign == null) {
            antarchy$nadirWallSign = antarchy$resolve("nadir_wall_sign");
        }
        return antarchy$nadirWallSign;
    }

    @Unique
    private static Block antarchy$nadirHangingSign() {
        if (antarchy$nadirHangingSign == null) {
            antarchy$nadirHangingSign = antarchy$resolve("nadir_hanging_sign");
        }
        return antarchy$nadirHangingSign;
    }

    @Unique
    private static Block antarchy$nadirWallHangingSign() {
        if (antarchy$nadirWallHangingSign == null) {
            antarchy$nadirWallHangingSign = antarchy$resolve("nadir_wall_hanging_sign");
        }
        return antarchy$nadirWallHangingSign;
    }

    @Unique
    private static Block antarchy$royalSign() {
        if (antarchy$royalSign == null) {
            antarchy$royalSign = antarchy$resolve("royal_sign");
        }
        return antarchy$royalSign;
    }

    @Unique
    private static Block antarchy$royalWallSign() {
        if (antarchy$royalWallSign == null) {
            antarchy$royalWallSign = antarchy$resolve("royal_wall_sign");
        }
        return antarchy$royalWallSign;
    }

    @Unique
    private static Block antarchy$royalHangingSign() {
        if (antarchy$royalHangingSign == null) {
            antarchy$royalHangingSign = antarchy$resolve("royal_hanging_sign");
        }
        return antarchy$royalHangingSign;
    }

    @Unique
    private static Block antarchy$royalWallHangingSign() {
        if (antarchy$royalWallHangingSign == null) {
            antarchy$royalWallHangingSign = antarchy$resolve("royal_wall_hanging_sign");
        }
        return antarchy$royalWallHangingSign;
    }

    @Unique
    private static Block antarchy$truffaloSign() {
        if (antarchy$truffaloSign == null) {
            antarchy$truffaloSign = antarchy$resolve("truffalo_sign");
        }
        return antarchy$truffaloSign;
    }

    @Unique
    private static Block antarchy$truffaloWallSign() {
        if (antarchy$truffaloWallSign == null) {
            antarchy$truffaloWallSign = antarchy$resolve("truffalo_wall_sign");
        }
        return antarchy$truffaloWallSign;
    }

    @Unique
    private static Block antarchy$truffaloHangingSign() {
        if (antarchy$truffaloHangingSign == null) {
            antarchy$truffaloHangingSign = antarchy$resolve("truffalo_hanging_sign");
        }
        return antarchy$truffaloHangingSign;
    }

    @Unique
    private static Block antarchy$truffaloWallHangingSign() {
        if (antarchy$truffaloWallHangingSign == null) {
            antarchy$truffaloWallHangingSign = antarchy$resolve("truffalo_wall_hanging_sign");
        }
        return antarchy$truffaloWallHangingSign;
    }

    @Unique
    private static Block antarchy$resolve(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
