package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class UltimateCrossbowItem extends CrossbowItem {
    private static final int MAX_ARROWS = 16;
    private static final int FIRE_COOLDOWN_TICKS = 5;
    private static final float SHOOTING_POWER = 3.15F;
    private static final double VANILLA_CROSSBOW_ARROW_DAMAGE = 7.0D;
    private static final String RESERVE_PROJECTILES_TAG = "UltimateCrossbowProjectiles";
    private static final String CHARGED_PROJECTILES_TAG = "ChargedProjectiles";
    private static final String PROJECTILE_ITEM_TAG = "Item";

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
        saveLoadedProjectiles(stack, projectiles);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void fireOneShot(Level level, Player player, InteractionHand hand, ItemStack stack) {
        List<ItemStack> all = getLoadedProjectiles(stack);
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

        saveLoadedProjectiles(stack, List.of());
        this.performShooting(level, player, hand, stack, toFire, SHOOTING_POWER, 1.0F);

        saveLoadedProjectiles(stack, all);
    }

    private void performShooting(Level level, Player player, InteractionHand hand, ItemStack weapon,
                                 List<ItemStack> projectiles, float velocity, float inaccuracy) {
        float spread = projectiles.size() == 1 ? 0.0F : 20.0F / (float) (projectiles.size() - 1);
        float baseAngle = (float) ((projectiles.size() - 1) % 2) * spread / 2.0F;
        float direction = 1.0F;
        for (int i = 0; i < projectiles.size(); i++) {
            ItemStack ammo = projectiles.get(i);
            if (ammo.isEmpty()) {
                continue;
            }
            float angle = baseAngle + direction * (float) ((i + 1) / 2) * spread;
            direction = -direction;
            Projectile projectile = createProjectile(level, player, weapon, ammo);
            shootProjectile(player, projectile, i, velocity, inaccuracy, angle);
            weapon.hurtAndBreak(ammo.is(Items.FIREWORK_ROCKET) ? 3 : 1, player, entity -> entity.broadcastBreakEvent(hand));
            level.addFreshEntity(projectile);
            float pitch = i == 0 ? 1.0F : getRandomShotPitch((i & 1) == 1, player.getRandom());
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, player.getSoundSource(), 1.0F, pitch);
            if (weapon.isEmpty()) {
                break;
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, weapon);
            serverPlayer.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
        }
    }

    private static Projectile createProjectile(Level level, Player shooter, ItemStack weapon, ItemStack ammo) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - 0.15F, shooter.getZ(), true);
        }
        ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
        AbstractArrow arrow = arrowItem.createArrow(level, ammo, shooter);
        arrow.setCritArrow(true);
        arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        arrow.setShotFromCrossbow(true);
        int piercing = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, weapon);
        if (piercing > 0) {
            arrow.setPierceLevel((byte) piercing);
        }
        return arrow;
    }

    private void shootProjectile(LivingEntity shooter, Projectile projectile, int projectileIndex,
                                 float velocity, float inaccuracy, float angle) {
        Vec3 upVector = shooter.getUpVector(1.0F);
        Quaternionf rotation = new Quaternionf().setAngleAxis(angle * ((float) Math.PI / 180.0F), upVector.x, upVector.y, upVector.z);
        Vector3f direction = shooter.getViewVector(1.0F).toVector3f().rotate(rotation);
        projectile.shoot(direction.x(), direction.y(), direction.z(), velocity, inaccuracy);
        if (projectile instanceof AbstractArrow arrow) {
            scaleArrowDamage(arrow);
            if (projectileIndex > 0) {
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            }
            UltimateGearHelper.tagUltimateCrossbowProjectile(arrow);
        }
    }

    private static float getRandomShotPitch(boolean highPitch, RandomSource random) {
        float base = highPitch ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + base;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
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
        CompoundTag tag = crossbow.getTag();
        if (tag == null) {
            return projectiles;
        }
        ListTag charged = tag.getList(CHARGED_PROJECTILES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < charged.size(); i++) {
            ItemStack projectile = ItemStack.of(charged.getCompound(i));
            if (!projectile.isEmpty() && projectiles.size() < MAX_ARROWS) {
                projectiles.add(projectile.copyWithCount(1));
            }
        }

        ListTag reserve = tag.getList(RESERVE_PROJECTILES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < reserve.size() && projectiles.size() < MAX_ARROWS; i++) {
            ItemStack projectile = ItemStack.of(reserve.getCompound(i).getCompound(PROJECTILE_ITEM_TAG));
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
            CompoundTag tag = crossbow.getTag();
            if (tag != null) {
                tag.remove(CHARGED_PROJECTILES_TAG);
            }
            CrossbowItem.setCharged(crossbow, false);
            clearReserveProjectiles(crossbow);
            return;
        }

        ListTag charged = new ListTag();
        charged.add(sanitizedProjectiles.get(0).save(new CompoundTag()));
        crossbow.getOrCreateTag().put(CHARGED_PROJECTILES_TAG, charged);
        CrossbowItem.setCharged(crossbow, true);
        ListTag reserve = new ListTag();
        for (int i = 1; i < sanitizedProjectiles.size(); i++) {
            CompoundTag entry = new CompoundTag();
            entry.put(PROJECTILE_ITEM_TAG, sanitizedProjectiles.get(i).save(new CompoundTag()));
            reserve.add(entry);
        }

        if (reserve.isEmpty()) {
            crossbow.getOrCreateTag().remove(RESERVE_PROJECTILES_TAG);
        } else {
            crossbow.getOrCreateTag().put(RESERVE_PROJECTILES_TAG, reserve);
        }
    }

    private static void clearReserveProjectiles(ItemStack crossbow) {
        CompoundTag tag = crossbow.getTag();
        if (tag != null) {
            tag.remove(RESERVE_PROJECTILES_TAG);
        }
    }
}
