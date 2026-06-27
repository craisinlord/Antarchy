package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enchantments.class)
public abstract class SquidzookaEnchantMixin {
    @Redirect(
            method = "bootstrap",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderGetter;getOrThrow(Lnet/minecraft/tags/TagKey;)Lnet/minecraft/core/HolderSet$Named;"),
            slice = @org.spongepowered.asm.mixin.injection.Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/enchantment/Enchantments;MULTISHOT:Lnet/minecraft/resources/ResourceKey;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/world/item/enchantment/Enchantments;QUICK_CHARGE:Lnet/minecraft/resources/ResourceKey;")
            )
    )
    private static HolderSet.Named<Item> antarchy$allowSquidzookaForMultishot(HolderGetter<Item> holderGetter, TagKey<Item> tag) {
        if (tag.equals(ItemTags.CROSSBOW_ENCHANTABLE)) {
            return holderGetter.getOrThrow(AntarchyTags.Items.MULTISHOT_ENCHANTABLE);
        }

        return holderGetter.getOrThrow(tag);
    }
}
