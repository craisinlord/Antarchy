package com.craisinlord.antarchy.content.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

public final class MogglesItem extends Item {
    public MogglesItem(Item.Properties properties) {
        this(ArmorMaterials.GOLD, properties);
    }

    public MogglesItem(ArmorMaterial material, Item.Properties properties) {
        super(properties.humanoidArmor(material, ArmorType.HELMET)
                .component(DataComponents.MAX_DAMAGE, null)
                .component(DataComponents.DAMAGE, null));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.getItemBySlot(EquipmentSlot.HEAD) != stack) {
            return;
        }

        if (level.canSeeSky(player.blockPosition())) {
            return;
        }

        MobEffectInstance existing = player.getEffect(MobEffects.NIGHT_VISION);
        if (existing == null || existing.getDuration() <= 220) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, true));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.moggles.reveal").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.moggles.underground").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
