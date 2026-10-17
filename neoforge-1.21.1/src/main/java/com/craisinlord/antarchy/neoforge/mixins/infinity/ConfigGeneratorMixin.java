package com.craisinlord.antarchy.neoforge.mixins.infinity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.neoforge.InfinityBlockEntityScanGuard;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.codexarchonic.infinity.util.config.ConfigGenerator", remap = false)
public interface ConfigGeneratorMixin {
    @Redirect(
            method = "generateBEs()V",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V", ordinal = 0),
            remap = false,
            require = 0
    )
    private static void antarchy$skipBrokenBlockEntityFactories(
            Set<ResourceKey<BlockEntityType<?>>> entries,
            Consumer<ResourceKey<BlockEntityType<?>>> scan) {
        InfinityBlockEntityScanGuard.forEach(entries, scan, (entry, error) ->
                Antarchy.LOGGER.warn(
                        "[Antarchy] Skipping block entity {} in Infinite Dimensions' config scan because its factory has a linkage error. Check the owning mod's dependency versions; normal block placement is still affected.",
                        entry.location(), error));
    }
}
