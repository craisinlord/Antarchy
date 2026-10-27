package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsEnchantmentOrderMixin {
    @Redirect(
            method = {"generateEnchantmentBookTypesOnlyMaxLevel", "generateEnchantmentBookTypesAllLevels"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;")
    )
    private static Stream<Holder.Reference<Enchantment>> antarchy$orderEnchantments(HolderLookup<Enchantment> lookup) {
        List<Holder.Reference<Enchantment>> enchantments = lookup.listElements().toList();
        List<Holder.Reference<Enchantment>> antarchyEnchantments = enchantments.stream()
                .filter(CreativeModeTabsEnchantmentOrderMixin::isAntarchyEnchantment)
                .sorted(Comparator.comparingInt(CreativeModeTabsEnchantmentOrderMixin::priority))
                .toList();
        return Stream.concat(
                antarchyEnchantments.stream(),
                enchantments.stream().filter(holder -> !isAntarchyEnchantment(holder))
        );
    }

    private static boolean isAntarchyEnchantment(Holder.Reference<Enchantment> holder) {
        return holder.is(AntarchyEnchantments.FEATHER_RISING) || holder.is(AntarchyEnchantments.CHRONOSPHERE);
    }

    private static int priority(Holder.Reference<Enchantment> holder) {
        return holder.is(AntarchyEnchantments.FEATHER_RISING) ? 0 : 1;
    }
}
