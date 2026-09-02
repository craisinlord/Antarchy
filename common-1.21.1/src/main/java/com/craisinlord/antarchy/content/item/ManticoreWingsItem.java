package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ManticoreWingsItem extends ElytraItem {
    private static final ResourceLocation REPAIR_MATERIAL_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "manticore_wing");
    private static final int DAMAGE_COOLDOWN_TICKS = 10;
    private static final float COLLISION_DAMAGE = 8.0F;
    private static final Map<UUID, Long> LAST_COLLISION_DAMAGE = new ConcurrentHashMap<>();

    public ManticoreWingsItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return BuiltInRegistries.ITEM.getOptional(REPAIR_MATERIAL_ID).map(repair::is).orElse(false);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        if (!(entity instanceof Player player) || level.isClientSide || !player.isFallFlying()
                || player.getItemBySlot(EquipmentSlot.CHEST) != stack) {
            return;
        }
        long gameTime = level.getGameTime();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(0.45D), living ->
                living != player && living.isAlive() && !player.isAlliedTo(living) && !living.isAlliedTo(player))) {
            long availableAt = LAST_COLLISION_DAMAGE.getOrDefault(target.getUUID(), 0L);
            if (availableAt <= gameTime && target.hurt(level.damageSources().playerAttack(player), COLLISION_DAMAGE)) {
                LAST_COLLISION_DAMAGE.put(target.getUUID(), gameTime + DAMAGE_COOLDOWN_TICKS);
            }
        }
    }

    public static boolean isWearingManticoreWings(LivingEntity entity) {
        return entity != null && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ManticoreWingsItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.manticore_wings").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
