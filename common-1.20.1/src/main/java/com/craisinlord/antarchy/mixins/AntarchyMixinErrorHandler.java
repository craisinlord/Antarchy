package com.craisinlord.antarchy.mixins;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/*
 * Keeps bad mixins from hard-crashing startup.
 */
public final class AntarchyMixinErrorHandler implements IMixinErrorHandler {
    private static final String BROKEN_MARVEL_STELLARIS_MIXIN =
            "net.tintankgames.marvel.mixin.integration.stellaris.DimensionOxygenManagerMixin";

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable throwable, IMixinInfo mixin, ErrorAction action) {
        return shouldIgnore(mixin) ? ErrorAction.NONE : action;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable throwable, IMixinInfo mixin, ErrorAction action) {
        return shouldIgnore(mixin) ? ErrorAction.NONE : action;
    }

    private static boolean shouldIgnore(IMixinInfo mixin) {
        return mixin != null && BROKEN_MARVEL_STELLARIS_MIXIN.equals(mixin.getClassName());
    }
}
