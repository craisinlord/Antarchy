package com.craisinlord.antarchy.content.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.Nullable;

public final class ManticoreWingsItem extends Item {
    private static final Identifier REPAIR_MATERIAL_ID = Identifier.fromNamespaceAndPath("antarchy", "manticore_wing");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "manticore_wings_armor");
    private static final Identifier TOUGHNESS_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "manticore_wings_toughness");
    private static final int DAMAGE_COOLDOWN_TICKS = 10;
    private static final double MIN_COLLISION_SPEED = 0.2D;
    private static final float COLLISION_DAMAGE_PER_BLOCK_PER_TICK = 8.0F;
    private static final float MAX_COLLISION_DAMAGE = 20.0F;
    private static final Map<UUID, Long> LAST_COLLISION_DAMAGE = new ConcurrentHashMap<>();

    public ManticoreWingsItem(Item.Properties properties) {
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
                                        new AttributeModifier(ARMOR_MODIFIER_ID, 5.0D, AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.CHEST)
                                .add(Attributes.ARMOR_TOUGHNESS,
                                        new AttributeModifier(TOUGHNESS_MODIFIER_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.CHEST)
                                .build()),
                REPAIR_MATERIAL_ID
        ));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player) || !player.isFallFlying()
                || player.getItemBySlot(EquipmentSlot.CHEST) != stack) {
            return;
        }
        long gameTime = level.getGameTime();
        double speed = player.getDeltaMovement().length();
        if (speed < MIN_COLLISION_SPEED) {
            return;
        }
        float collisionDamage = Math.min(MAX_COLLISION_DAMAGE,
                Math.max(1.0F, (float) speed * COLLISION_DAMAGE_PER_BLOCK_PER_TICK));
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(0.45D), living ->
                living != player && living.isAlive() && !player.isAlliedTo(living) && !living.isAlliedTo(player))) {
            long availableAt = LAST_COLLISION_DAMAGE.getOrDefault(target.getUUID(), 0L);
            if (availableAt <= gameTime && target.hurtServer(level, level.damageSources().playerAttack(player), collisionDamage)) {
                LAST_COLLISION_DAMAGE.put(target.getUUID(), gameTime + DAMAGE_COOLDOWN_TICKS);
            }
        }
    }

    public static boolean isWearingManticoreWings(LivingEntity entity) {
        return entity != null && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ManticoreWingsItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.manticore_wings").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
