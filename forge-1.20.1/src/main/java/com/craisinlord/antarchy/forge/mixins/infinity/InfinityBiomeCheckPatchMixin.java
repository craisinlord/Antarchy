package com.craisinlord.antarchy.forge.mixins.infinity;

import net.lerariemann.infinity.util.InfinityMethods;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = InfinityMethods.class, remap = false)
public interface InfinityBiomeCheckPatchMixin {
}
