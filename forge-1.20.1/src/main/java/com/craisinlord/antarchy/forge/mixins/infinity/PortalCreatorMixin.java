package com.craisinlord.antarchy.forge.mixins.infinity;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.lerariemann.infinity.util.teleport.PortalCreator;
import net.minecraft.server.level.ServerLevel;

@Mixin(value = PortalCreator.class, remap = false)
/*
 * Blocks Infinity portal creation when the config says no
 */
public class PortalCreatorMixin {
    @Inject(
            method = "tryCreatePortalFromItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/item/ItemEntity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void antarchy$blockInfinityBookActivation(ServerLevel level, BlockPos pos, ItemEntity itemEntity, CallbackInfo ci) {
        if (!AntarchySettings.disableInfinityBookPortalCreation()) {
            return;
        }

        if (itemEntity.isRemoved()) {
            return;
        }

        ItemStack stack = itemEntity.getItem();
        if (!stack.is(Items.WRITABLE_BOOK) && !stack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        ci.cancel();
    }
}
