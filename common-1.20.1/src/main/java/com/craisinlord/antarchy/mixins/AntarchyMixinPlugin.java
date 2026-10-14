package com.craisinlord.antarchy.mixins;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class AntarchyMixinPlugin implements IMixinConfigPlugin {
    private static final AntarchyRangedAttackPatcher RANGED_ATTACK_PATCHER = new AntarchyRangedAttackPatcher();

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
        if (!classPresent(targetClassName)) {
            return false;
        }
        if (mixinClassName.startsWith("com.craisinlord.antarchy.mixins.compat.scguns.")) {
            return classPresent("top.ribs.scguns.entity.projectile.ProjectileEntity");
        }
        if (mixinClassName.startsWith("com.craisinlord.antarchy.mixins.compat.irons_spellbooks.")) {
            return classPresent("io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile");
        }
        if (mixinClassName.startsWith("com.craisinlord.antarchy.mixins.compat.ars_nouveau.")) {
            return classPresent("com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell");
        }
        if (mixinClassName.startsWith("com.craisinlord.antarchy.mixins.compat.biolith.")) {
            return classPresent("com.terraformersmc.biolith.api.biome.BiolithFittestNodes");
        }
        if (mixinClassName.startsWith("com.craisinlord.antarchy.mixins.compat.alexscaves.")) {
            return true;
        }
        if (mixinClassName.endsWith(".VillagerTradesMixin")) {
            return false;
        }
        return true;
    }

    private static boolean classPresent(String className) {
        String resourcePath = className.replace('.', '/') + ".class";
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null && contextClassLoader.getResource(resourcePath) != null) {
            return true;
        }
        ClassLoader mixinClassLoader = AntarchyMixinPlugin.class.getClassLoader();
        return mixinClassLoader != null && mixinClassLoader.getResource(resourcePath) != null;
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
        RANGED_ATTACK_PATCHER.patch(targetClass);
    }
}
