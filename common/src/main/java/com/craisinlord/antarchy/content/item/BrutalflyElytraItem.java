package com.craisinlord.antarchy.content.item;

import java.util.Optional;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BrutalflyElytraItem extends ElytraItem {
    private static final ResourceLocation REPAIR_MATERIAL_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "brutalfly_wing");

    // Set by NeoForge client setup to return the actual bound key name
    public static Supplier<Component> FLAP_KEY_NAME = () -> Component.literal("Shift");

    public BrutalflyElytraItem(Properties properties) {
        super(properties);
    }

    public static boolean isWearingBrutalflyElytra(LivingEntity entity) {
        return entity != null && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BrutalflyElytraItem;
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.brutalfly_elytra.flap", FLAP_KEY_NAME.get()).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
