package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.AntarchyItemProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import com.craisinlord.antarchy.mixins.AccessorAbstractArrow;

public class UltimateCrossbowItem extends CrossbowItem {
    private static final int MAX_ARROWS = 16;
    private static final int FIRE_COOLDOWN_TICKS = 5;
    private static final float SHOOTING_POWER = 3.15F;
    private static final double VANILLA_CROSSBOW_ARROW_DAMAGE = 7.0D;
    private static final String RESERVE_PROJECTILES_TAG = "UltimateCrossbowProjectiles";
    private static final String PROJECTILE_ITEM_TAG = "Item";
    private static final CrossbowItem.ChargingSounds DEFAULT_CHARGING_SOUNDS = new CrossbowItem.ChargingSounds(
            Optional.of(SoundEvents.CROSSBOW_LOADING_START), Optional.of(SoundEvents.CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.CROSSBOW_LOADING_END)
    );

    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;

    public UltimateCrossbowItem(Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(properties, AntarchySettings::ultimateCrossbowEnchantability));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (CrossbowItem.isCharged(stack)) {
            if (!level.isClientSide()) {
                fireOneShot(level, player, hand, stack);
            }
            player.getCooldowns().addCooldown(stack, FIRE_COOLDOWN_TICKS);
            return InteractionResult.CONSUME;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_START.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return false;
        if (CrossbowItem.isCharged(stack)) return false;

        int useTicks = getUseDuration(stack, livingEntity) - timeLeft;
        int maxTicks = getAdjustedChargeDuration(stack, livingEntity);

        int arrowsToLoad = Mth.clamp(
                (int) Math.floor((double) useTicks / maxTicks * MAX_ARROWS), 0, MAX_ARROWS);
        if (arrowsToLoad == 0) return false;

        ItemStack ammoType = player.getProjectile(stack);
        if (ammoType.isEmpty()) return false;

        int available = player.isCreative() ? MAX_ARROWS : countMatchingArrows(player, ammoType);
        int toLoad = Math.min(arrowsToLoad, available);
        if (toLoad == 0) return false;

        if (!player.isCreative()) {
            consumeArrows(player, ammoType, toLoad);
        }

        List<ItemStack> projectiles = new ArrayList<>(toLoad);
        for (int i = 0; i < toLoad; i++) {
            projectiles.add(ammoType.copyWithCount(1));
        }
        saveLoadedProjectiles(stack, player.registryAccess(), projectiles);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private void fireOneShot(Level level, Player player, InteractionHand hand, ItemStack stack) {
        List<ItemStack> all = getLoadedProjectiles(stack, player.registryAccess());
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

        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(toFire));
        this.performShooting(level, player, hand, stack, SHOOTING_POWER, 1.0F, null);

        saveLoadedProjectiles(stack, player.registryAccess(), all);
    }

    @Override
    protected void shootProjectile(
            LivingEntity shooter, Projectile projectile, int projectileIndex,
            float velocity, float inaccuracy, float angle, LivingEntity target) {
        super.shootProjectile(shooter, projectile, projectileIndex, velocity, inaccuracy, angle, target);
        if (projectile instanceof AbstractArrow arrow) {
            scaleArrowDamage(arrow);
            if (projectileIndex > 0) {
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            }
            UltimateGearHelper.tagUltimateCrossbowProjectile(arrow);
        }
    }

    private static void scaleArrowDamage(AbstractArrow arrow) {
        double multiplier = AntarchySettings.ultimateCrossbowAttackDamage() / VANILLA_CROSSBOW_ARROW_DAMAGE;
        arrow.setBaseDamage(((AccessorAbstractArrow) arrow).antarchy$getBaseDamage() * multiplier);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return getAdjustedChargeDuration(stack, livingEntity) + 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<ItemStack> loadedProjectiles = getLoadedProjectiles(stack, context.registries());
        if (!loadedProjectiles.isEmpty()) {
            int count = loadedProjectiles.size();
            Component arrowName = loadedProjectiles.get(0).getHoverName();
            tooltipComponents.accept(Component.translatable("item.antarchy.ultimate_crossbow.arrows_remaining", count, arrowName)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.accept(Component.translatable("item.antarchy.ultimate_crossbow.capacity")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!level.isClientSide()) {
            CrossbowItem.ChargingSounds sounds = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS)
                    .orElse(DEFAULT_CHARGING_SOUNDS);
            float tickPercent = (float) (stack.getUseDuration(entity) - ticksRemaining) / CrossbowItem.getChargeDuration(stack, entity);
            if (tickPercent < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (tickPercent >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                sounds.start().ifPresent(sound -> playChargingSound(level, entity, sound));
            }

            if (tickPercent >= 0.5F && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                sounds.mid().ifPresent(sound -> playChargingSound(level, entity, sound));
            }
        }
    }

    private static void playChargingSound(Level level, LivingEntity entity, Holder<SoundEvent> sound) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound.value(), SoundSource.PLAYERS, 0.5F, 1.0F);
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
        ItemStack offhand = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        if (ItemStack.isSameItemSameComponents(offhand, ammoType)) {
            count += offhand.getCount();
        }
        for (ItemStack slot : player.getInventory().getNonEquipmentItems()) {
            if (ItemStack.isSameItemSameComponents(slot, ammoType)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    private void consumeArrows(Player player, ItemStack ammoType, int amount) {
        int remaining = amount;
        ItemStack offhand = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        if (remaining > 0 && ItemStack.isSameItemSameComponents(offhand, ammoType)) {
            int take = Math.min(remaining, offhand.getCount());
            offhand.shrink(take);
            remaining -= take;
            if (offhand.isEmpty()) {
                player.getInventory().setItem(Inventory.SLOT_OFFHAND, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < player.getInventory().getNonEquipmentItems().size() && remaining > 0; i++) {
            ItemStack slot = player.getInventory().getNonEquipmentItems().get(i);
            if (remaining <= 0) break;
            if (ItemStack.isSameItemSameComponents(slot, ammoType)) {
                int take = Math.min(remaining, slot.getCount());
                slot.shrink(take);
                remaining -= take;
                if (slot.isEmpty()) {
                    player.getInventory().getNonEquipmentItems().set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private static List<ItemStack> getLoadedProjectiles(ItemStack crossbow, HolderLookup.Provider registries) {
        List<ItemStack> projectiles = new ArrayList<>();
        ChargedProjectiles charged = crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        for (ItemStack projectile : charged.itemCopies().toList()) {
            if (!projectile.isEmpty() && projectiles.size() < MAX_ARROWS) {
                projectiles.add(projectile.copyWithCount(1));
            }
        }

        CompoundTag customData = crossbow.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag reserve = customData.getListOrEmpty(RESERVE_PROJECTILES_TAG);
        for (int i = 0; i < reserve.size() && projectiles.size() < MAX_ARROWS; i++) {
            ItemStack projectile = reserve.getCompoundOrEmpty(i).getCompound(PROJECTILE_ITEM_TAG)
                    .flatMap(itemTag -> ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), itemTag).result())
                    .orElse(ItemStack.EMPTY);
            if (!projectile.isEmpty()) {
                projectiles.add(projectile.copyWithCount(1));
            }
        }
        return projectiles;
    }

    private static void saveLoadedProjectiles(ItemStack crossbow, HolderLookup.Provider registries, List<ItemStack> projectiles) {
        List<ItemStack> sanitizedProjectiles = projectiles.stream()
                .filter(projectile -> !projectile.isEmpty())
                .limit(MAX_ARROWS)
                .map(projectile -> projectile.copyWithCount(1))
                .toList();

        if (sanitizedProjectiles.isEmpty()) {
            crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            clearReserveProjectiles(crossbow);
            return;
        }

        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(List.of(sanitizedProjectiles.get(0))));
        ListTag reserve = new ListTag();
        for (int i = 1; i < sanitizedProjectiles.size(); i++) {
            CompoundTag entry = new CompoundTag();
            ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), sanitizedProjectiles.get(i))
                    .result()
                    .ifPresent(itemTag -> entry.put(PROJECTILE_ITEM_TAG, itemTag));
            reserve.add(entry);
        }

        CustomData.update(DataComponents.CUSTOM_DATA, crossbow, tag -> {
            if (reserve.isEmpty()) {
                tag.remove(RESERVE_PROJECTILES_TAG);
            } else {
                tag.put(RESERVE_PROJECTILES_TAG, reserve);
            }
        });
    }

    private static void clearReserveProjectiles(ItemStack crossbow) {
        CustomData.update(DataComponents.CUSTOM_DATA, crossbow, tag -> tag.remove(RESERVE_PROJECTILES_TAG));
    }
}
