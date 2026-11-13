package com.craisinlord.antarchy.neoforge.mixins.compat;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.plaaasma.timevortex.config.ServerSettings", remap = false)
public abstract class TimeVortexSettingsCheckAsyncMixin {
    @Unique
    private static final AtomicBoolean antarchy$checking = new AtomicBoolean();
    @Unique
    private static volatile boolean antarchy$firstDone;
    @Unique
    private static volatile Method antarchy$check;

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/plaaasma/timevortex/config/SettingsFile;check()V"), require = 0, remap = false)
    private static void antarchy$checkOffThread(@Coerce Object file) {
        if (!antarchy$firstDone) {
            antarchy$invoke(file);
            antarchy$firstDone = true;
            return;
        }
        if (!antarchy$checking.compareAndSet(false, true)) {
            return;
        }
        Util.ioPool().execute(() -> {
            try {
                antarchy$invoke(file);
            } finally {
                antarchy$checking.set(false);
            }
        });
    }

    @Unique
    private static void antarchy$invoke(Object file) {
        try {
            Method check = antarchy$check;
            if (check == null) {
                check = file.getClass().getMethod("check");
                antarchy$check = check;
            }
            check.invoke(file);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Time Vortex settings check failed", e);
        }
    }
}
