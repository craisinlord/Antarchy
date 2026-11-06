package com.craisinlord.antarchy.forge.mixins.infinity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.lerariemann.infinity.util.config.ConfigGenerator", remap = false)
public interface ConfigGeneratorPatchMixin {
}
