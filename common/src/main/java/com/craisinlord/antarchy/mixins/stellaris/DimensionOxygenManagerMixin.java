package com.craisinlord.antarchy.mixins.stellaris;

import com.st0x0ef.stellaris.common.oxygen.DimensionOxygenManager;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DimensionOxygenManager.class, priority = 2000)
/*
 * Tweaks Stellaris oxygen checks for Antarchy dimensions.
 */
public abstract class DimensionOxygenManagerMixin {
    @Unique
    private static final TagKey<Item> ANTARCHY_MARK_39_ARMOR = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("marvel", "iron_man_mark_39_armor")
    );

    @Unique
    private static final ResourceLocation ANTARCHY_HELMET_OPEN_ID =
            ResourceLocation.fromNamespaceAndPath("marvel", "helmet_open");

    @Inject(method = "breath", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowClosedMark39SuitToBreathe(
            LivingEntity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (antarchy$hasArmor(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean antarchy$hasArmor(LivingEntity entity) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        boolean hasHelmet = helmet.is(ANTARCHY_MARK_39_ARMOR) && !antarchy$isHelmetOpen(helmet);
        boolean hasChestplate = entity.getItemBySlot(EquipmentSlot.CHEST).is(ANTARCHY_MARK_39_ARMOR);
        boolean hasLeggings = entity.getItemBySlot(EquipmentSlot.LEGS).is(ANTARCHY_MARK_39_ARMOR);
        boolean hasBoots = entity.getItemBySlot(EquipmentSlot.FEET).is(ANTARCHY_MARK_39_ARMOR);
        return hasHelmet && hasChestplate && hasLeggings && hasBoots;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static boolean antarchy$isHelmetOpen(ItemStack helmet) {
        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ANTARCHY_HELMET_OPEN_ID);
        if (componentType == null) {
            return false;
        }
        return Boolean.TRUE.equals(helmet.get((DataComponentType<Boolean>) componentType));
    }
}
