package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UltimateBowItem extends BowItem {
    private static final double VANILLA_FULL_DRAW_ARROW_DAMAGE = 6.0D;

    public UltimateBowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        ItemStack projectileStack = player.getProjectile(stack);
        if (projectileStack.isEmpty()) {
            return;
        }

        int useTicks = this.getUseDuration(stack, livingEntity) - timeLeft;
        int adjustedUseTicks = Math.max(1, (int) Math.round(useTicks * AntarchySettings.ultimateBowDrawSpeedMultiplier()));
        float power = BowItem.getPowerForTime(adjustedUseTicks);
        if (power < 0.1F) {
            return;
        }

        List<ItemStack> projectiles = draw(stack, projectileStack, player);
        if (level instanceof ServerLevel serverLevel && !projectiles.isEmpty()) {
            this.shoot(serverLevel, player, player.getUsedItemHand(), stack, projectiles, power * 3.0F, 1.0F, power == 1.0F, null);
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F
        );
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    protected void shootProjectile(
            LivingEntity shooter,
            Projectile projectile,
            int projectileIndex,
            float velocity,
            float inaccuracy,
            float angle,
            LivingEntity target
    ) {
        super.shootProjectile(shooter, projectile, projectileIndex, velocity, inaccuracy, angle, target);
        if (projectile instanceof AbstractArrow arrow) {
            scaleArrowDamage(arrow);
            UltimateGearHelper.tagUltimateBowArrow(arrow);
        }
    }

    private static void scaleArrowDamage(AbstractArrow arrow) {
        double multiplier = AntarchySettings.ultimateBowAttackDamage() / VANILLA_FULL_DRAW_ARROW_DAMAGE;
        arrow.setBaseDamage(arrow.getBaseDamage() * multiplier);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        ensureConfiguredEnchantments(stack, level.registryAccess());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        ensureConfiguredEnchantments(stack, level.registryAccess());
    }

    private static void ensureConfiguredEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        UltimateGearHelper.ensureUltimateBowEnchantments(stack, registries);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.ultimateBowEnchantability();
    }
}
