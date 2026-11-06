package com.craisinlord.antarchy.content.item;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;

public final class BrutalflyElytraItem extends Item {
    private static final Identifier REPAIR_MATERIAL_ID = Identifier.fromNamespaceAndPath("antarchy", "brutalfly_wing");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "brutalfly_elytra_armor");

    public static Supplier<Component> FLAP_KEY_NAME = () -> Component.literal("Shift");

    public BrutalflyElytraItem(Item.Properties properties) {
        super(AntarchyItemProperties.repairableBy(
                properties
                        .component(DataComponents.GLIDER, Unit.INSTANCE)
                        .component(
                                DataComponents.EQUIPPABLE,
                                Equippable.builder(EquipmentSlot.CHEST)
                                        .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                                        .setDamageOnHurt(false)
                                        .build()
                        )
                        .attributes(ItemAttributeModifiers.builder()
                                .add(Attributes.ARMOR,
                                        new AttributeModifier(ARMOR_MODIFIER_ID, 3.0D, AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.CHEST)
                                .build())
                        .enchantable(1),
                REPAIR_MATERIAL_ID
        ));
    }

    public static boolean isWearingBrutalflyElytra(LivingEntity entity) {
        return entity != null && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BrutalflyElytraItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.brutalfly_elytra.flap", FLAP_KEY_NAME.get()).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
