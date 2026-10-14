package com.craisinlord.antarchy.content.item;

import com.google.common.collect.ImmutableMultimap;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BrutalflyElytraItem extends ElytraItem {
    private static final ResourceLocation REPAIR_MATERIAL_ID = new ResourceLocation("antarchy", "brutalfly_wing");
    private static final ResourceLocation ARMOR_MODIFIER_ID = new ResourceLocation("antarchy", "brutalfly_elytra_armor");
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = new ResourceLocation("antarchy", "brutalfly_elytra_toughness");

    public static Supplier<Component> FLAP_KEY_NAME = () -> Component.literal("Shift");

    public BrutalflyElytraItem(Properties properties) {
        super(properties);
    }

    public static boolean isWearingBrutalflyElytra(LivingEntity entity) {
        return entity != null && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BrutalflyElytraItem;
    }

    @Override
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.CHEST) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ARMOR,
                new AttributeModifier(UUID.nameUUIDFromBytes(ARMOR_MODIFIER_ID.toString().getBytes(StandardCharsets.UTF_8)),
                        ARMOR_MODIFIER_ID.toString(), 3.0D, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        Optional<Item> repairMaterial = BuiltInRegistries.ITEM.getOptional(REPAIR_MATERIAL_ID);
        return repairMaterial.isPresent() && repair.is(repairMaterial.get());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.brutalfly_elytra.flap", FLAP_KEY_NAME.get()).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
