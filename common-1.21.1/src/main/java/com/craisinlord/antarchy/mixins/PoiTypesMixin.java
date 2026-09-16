package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PoiTypes.class)
public abstract class PoiTypesMixin {
    @Inject(method = "forState", at = @At("HEAD"), cancellable = true)
    private static void antarchy$computerPoi(BlockState state,
                                              CallbackInfoReturnable<Optional<Holder<PoiType>>> callback) {
        try {
            if (!state.is(AntarchyObjects.COMPUTER.get())) {
                return;
            }
            ResourceKey<PoiType> key = ResourceKey.create(
                    Registries.POINT_OF_INTEREST_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "computer"));
            BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(key)
                    .ifPresent(holder -> callback.setReturnValue(Optional.of(holder)));
        } catch (IllegalStateException ignored) {
            // Registration can be queried during bootstrap before loader bindings finish.
        }
    }
}
