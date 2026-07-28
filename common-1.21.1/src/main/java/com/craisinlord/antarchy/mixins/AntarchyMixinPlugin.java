package com.craisinlord.antarchy.mixins;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/*
 * Mixin plugin for compat-gated mixins and optional deps.
 */
public final class AntarchyMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        Mixins.registerErrorHandlerClass(AntarchyMixinErrorHandler.class.getName());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String resourcePath = targetClassName.replace('.', '/') + ".class";
        if (this.getClass().getClassLoader().getResource(resourcePath) == null) {
            return false;
        }
        if (mixinClassName.endsWith(".VillagerTradesMixin")) {
            // Trade registration is platform-driven on every supported loader now. Keeping the
            // static VillagerTrades class-init mixin around risks touching AntarchyObjects before
            // registration is finished on Fabric and duplicates trades on event-driven loaders.
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
