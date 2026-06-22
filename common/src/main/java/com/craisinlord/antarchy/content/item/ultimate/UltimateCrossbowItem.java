package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class UltimateCrossbowItem extends CrossbowItem {
    private static final int MAX_ARROWS = 16;
    private static final int FIRE_COOLDOWN_TICKS = 5; // 4 shots/sec
    private static final float SHOOTING_POWER = 3.15F;
    private static final double VANILLA_CROSSBOW_ARROW_DAMAGE = 7.0D;

    public UltimateCrossbowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (CrossbowItem.isCharged(stack)) {
            if (!level.isClientSide) {
                fireOneShot(level, player, hand, stack);
            }
            player.getCooldowns().addCooldown(this, FIRE_COOLDOWN_TICKS);
            return InteractionResultHolder.consume(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_START.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return;
        if (CrossbowItem.isCharged(stack)) return;

        int useTicks = getUseDuration(stack, livingEntity) - timeLeft;
        int maxTicks = getAdjustedChargeDuration(stack, livingEntity);

        int arrowsToLoad = Mth.clamp(
                (int) Math.floor((double) useTicks / maxTicks * MAX_ARROWS), 0, MAX_ARROWS);
        if (arrowsToLoad == 0) return;

        ItemStack ammoType = player.getProjectile(stack);
        if (ammoType.isEmpty()) return;

        int available = player.isCreative() ? MAX_ARROWS : countMatchingArrows(player, ammoType);
        int toLoad = Math.min(arrowsToLoad, available);
        if (toLoad == 0) return;

        if (!player.isCreative()) {
            consumeArrows(player, ammoType, toLoad);
        }

        List<ItemStack> projectiles = new ArrayList<>(toLoad);
        for (int i = 0; i < toLoad; i++) {
            projectiles.add(ammoType.copyWithCount(1));
        }
        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(projectiles));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void fireOneShot(Level level, Player player, InteractionHand hand, ItemStack stack) {
        ChargedProjectiles charged = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        List<ItemStack> all = new ArrayList<>(charged.getItems().stream().filter(s -> !s.isEmpty()).toList());
        if (all.isEmpty()) return;

        ItemStack arrow = all.remove(0);
        List<ItemStack> toFire = new ArrayList<>();
        if (hasMultishot(stack)) {
            toFire.add(arrow);
            toFire.add(arrow.copy());
            toFire.add(arrow.copy());
        } else {
            toFire.add(arrow);
        }

        // Temporarily set only the arrows for this shot so performShooting fires exactly them
        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(toFire));
        this.performShooting(level, player, hand, stack, SHOOTING_POWER, 1.0F, null);

        // performShooting clears CHARGED_PROJECTILES; restore remaining valid arrows if any
        List<ItemStack> remaining = all.stream().filter(s -> !s.isEmpty()).toList();
        if (!remaining.isEmpty()) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(remaining));
        }
    }

    @Override
    protected void shootProjectile(
            LivingEntity shooter, Projectile projectile, int projectileIndex,
            float velocity, float inaccuracy, float angle, LivingEntity target) {
        super.shootProjectile(shooter, projectile, projectileIndex, velocity, inaccuracy, angle, target);
        if (projectile instanceof AbstractArrow arrow) {
            scaleArrowDamage(arrow);
            UltimateGearHelper.tagUltimateCrossbowProjectile(arrow);
        }
    }

    private static void scaleArrowDamage(AbstractArrow arrow) {
        double multiplier = AntarchySettings.ultimateCrossbowAttackDamage() / VANILLA_CROSSBOW_ARROW_DAMAGE;
        arrow.setBaseDamage(arrow.getBaseDamage() * multiplier);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return getAdjustedChargeDuration(stack, livingEntity) + 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ChargedProjectiles charged = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (!charged.isEmpty()) {
            int count = charged.getItems().size();
            Component arrowName = charged.getItems().get(0).getHoverName();
            tooltipComponents.add(Component.translatable("item.antarchy.ultimate_crossbow.arrows_remaining", count, arrowName)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.translatable("item.antarchy.ultimate_crossbow.capacity")
                .withStyle(ChatFormatting.GRAY));
    }

    private int getAdjustedChargeDuration(ItemStack stack, LivingEntity livingEntity) {
        int vanillaChargeDuration = CrossbowItem.getChargeDuration(stack, livingEntity);
        double multiplier = AntarchySettings.ultimateCrossbowChargeSpeedMultiplier();
        return Math.max(1, Mth.floor((float) (vanillaChargeDuration / multiplier)));
    }

    private boolean hasMultishot(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                .keySet().stream()
                .anyMatch(h -> h.is(Enchantments.MULTISHOT));
    }

    private int countMatchingArrows(Player player, ItemStack ammoType) {
        int count = 0;
        for (ItemStack slot : player.getInventory().offhand) {
            if (ItemStack.isSameItemSameComponents(slot, ammoType)) {
                count += slot.getCount();
            }
        }
        for (ItemStack slot : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(slot, ammoType)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    private void consumeArrows(Player player, ItemStack ammoType, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().offhand.size() && remaining > 0; i++) {
            ItemStack slot = player.getInventory().offhand.get(i);
            if (remaining <= 0) break;
            if (ItemStack.isSameItemSameComponents(slot, ammoType)) {
                int take = Math.min(remaining, slot.getCount());
                slot.shrink(take);
                remaining -= take;
                if (slot.isEmpty()) {
                    player.getInventory().offhand.set(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack slot = player.getInventory().items.get(i);
            if (remaining <= 0) break;
            if (ItemStack.isSameItemSameComponents(slot, ammoType)) {
                int take = Math.min(remaining, slot.getCount());
                slot.shrink(take);
                remaining -= take;
                if (slot.isEmpty()) {
                    player.getInventory().items.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.ultimateCrossbowEnchantability();
    }
}
