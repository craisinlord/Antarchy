package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class UltimateCrossbowItem extends CrossbowItem {
    private static final int MAX_ARROWS = 16;
    private static final int FIRE_COOLDOWN_TICKS = 5; // 4 shots/sec
    private static final float SHOOTING_POWER = 3.15F;
    private static final double VANILLA_CROSSBOW_ARROW_DAMAGE = 7.0D;
    private static final String LOADED_PROJECTILES_TAG = "UltimateCrossbowProjectiles";
    private static final String PROJECTILE_ITEM_TAG = "Item";

    public UltimateCrossbowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (CrossbowItem.isCharged(stack)) {
            if (!level.isClientSide) {
                fireOneShot(level, player, stack);
            }
            player.getCooldowns().addCooldown(this, FIRE_COOLDOWN_TICKS);
            return InteractionResultHolder.consume(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_START, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return;
        if (CrossbowItem.isCharged(stack)) return;

        int useTicks = getUseDuration(stack) - timeLeft;
        int maxTicks = getAdjustedChargeDuration(stack);

        int arrowsToLoad = Mth.clamp(
                (int) Math.floor((double) useTicks / maxTicks * MAX_ARROWS), 0, MAX_ARROWS);
        if (arrowsToLoad == 0) return;

        ItemStack ammoType = player.getProjectile(stack);
        if (ammoType.isEmpty()) return;

        int available = player.getAbilities().instabuild ? MAX_ARROWS : countMatchingArrows(player, ammoType);
        int toLoad = Math.min(arrowsToLoad, available);
        if (toLoad == 0) return;

        if (!player.getAbilities().instabuild) {
            consumeArrows(player, ammoType, toLoad);
        }

        List<ItemStack> projectiles = getLoadedProjectiles(stack);
        for (int i = 0; i < toLoad && projectiles.size() < MAX_ARROWS; i++) {
            projectiles.add(ammoType.copyWithCount(1));
        }
        saveLoadedProjectiles(stack, projectiles);
        CrossbowItem.setCharged(stack, true);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void fireOneShot(Level level, Player player, ItemStack stack) {
        List<ItemStack> loaded = getLoadedProjectiles(stack);
        if (loaded.isEmpty()) {
            CrossbowItem.setCharged(stack, false);
            return;
        }

        ItemStack arrowStack = loaded.remove(0);
        List<ItemStack> toFire = new ArrayList<>();
        if (hasMultishot(stack)) {
            toFire.add(arrowStack);
            toFire.add(arrowStack.copy());
            toFire.add(arrowStack.copy());
        } else {
            toFire.add(arrowStack);
        }

        float[] angles = toFire.size() == 3 ? new float[]{-10.0F, 0.0F, 10.0F} : new float[]{0.0F};
        for (int i = 0; i < toFire.size(); i++) {
            shootArrow(level, player, toFire.get(i), angles[i], i > 0);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

        saveLoadedProjectiles(stack, loaded);
        CrossbowItem.setCharged(stack, !loaded.isEmpty());
    }

    private void shootArrow(Level level, Player player, ItemStack arrowStack, float angleOffset, boolean disallowPickup) {
        if (!(arrowStack.getItem() instanceof ArrowItem arrowItem)) {
            return;
        }

        AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angleOffset, 0.0F, SHOOTING_POWER, 1.0F);
        scaleArrowDamage(arrow);
        if (disallowPickup) {
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
        UltimateGearHelper.tagUltimateCrossbowProjectile(arrow);
        level.addFreshEntity(arrow);
    }

    private static void scaleArrowDamage(AbstractArrow arrow) {
        double multiplier = AntarchySettings.ultimateCrossbowAttackDamage() / VANILLA_CROSSBOW_ARROW_DAMAGE;
        arrow.setBaseDamage(arrow.getBaseDamage() * multiplier);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return getAdjustedChargeDuration(stack) + 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<ItemStack> loadedProjectiles = getLoadedProjectiles(stack);
        if (!loadedProjectiles.isEmpty()) {
            int count = loadedProjectiles.size();
            Component arrowName = loadedProjectiles.get(0).getHoverName();
            tooltipComponents.add(Component.translatable("item.antarchy.ultimate_crossbow.arrows_remaining", count, arrowName)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.translatable("item.antarchy.ultimate_crossbow.capacity")
                .withStyle(ChatFormatting.GRAY));
    }

    private int getAdjustedChargeDuration(ItemStack stack) {
        int vanillaChargeDuration = CrossbowItem.getChargeDuration(stack);
        double multiplier = AntarchySettings.ultimateCrossbowChargeSpeedMultiplier();
        return Math.max(1, Mth.floor((float) (vanillaChargeDuration / multiplier)));
    }

    private boolean hasMultishot(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0;
    }

    private int countMatchingArrows(Player player, ItemStack ammoType) {
        int count = 0;
        for (ItemStack slot : player.getInventory().offhand) {
            if (ItemStack.isSameItemSameTags(slot, ammoType)) {
                count += slot.getCount();
            }
        }
        for (ItemStack slot : player.getInventory().items) {
            if (ItemStack.isSameItemSameTags(slot, ammoType)) {
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
            if (ItemStack.isSameItemSameTags(slot, ammoType)) {
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
            if (ItemStack.isSameItemSameTags(slot, ammoType)) {
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

    private static List<ItemStack> getLoadedProjectiles(ItemStack crossbow) {
        List<ItemStack> projectiles = new ArrayList<>();
        CompoundTag tag = crossbow.hasTag() ? crossbow.getTag() : new CompoundTag();
        ListTag loaded = tag.getList(LOADED_PROJECTILES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < loaded.size() && projectiles.size() < MAX_ARROWS; i++) {
            ItemStack projectile = ItemStack.of(loaded.getCompound(i).getCompound(PROJECTILE_ITEM_TAG));
            if (!projectile.isEmpty()) {
                projectiles.add(projectile.copyWithCount(1));
            }
        }
        return projectiles;
    }

    private static void saveLoadedProjectiles(ItemStack crossbow, List<ItemStack> projectiles) {
        List<ItemStack> sanitizedProjectiles = projectiles.stream()
                .filter(projectile -> !projectile.isEmpty())
                .limit(MAX_ARROWS)
                .map(projectile -> projectile.copyWithCount(1))
                .toList();

        if (sanitizedProjectiles.isEmpty()) {
            if (crossbow.hasTag()) {
                crossbow.getTag().remove(LOADED_PROJECTILES_TAG);
            }
            return;
        }

        ListTag loaded = new ListTag();
        for (ItemStack projectile : sanitizedProjectiles) {
            CompoundTag entry = new CompoundTag();
            entry.put(PROJECTILE_ITEM_TAG, projectile.save(new CompoundTag()));
            loaded.add(entry);
        }

        crossbow.getOrCreateTag().put(LOADED_PROJECTILES_TAG, loaded);
    }
}
