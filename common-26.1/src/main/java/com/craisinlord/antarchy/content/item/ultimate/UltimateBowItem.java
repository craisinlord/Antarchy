package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.AntarchyItemProperties;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import com.craisinlord.antarchy.mixins.AccessorAbstractArrow;

public class UltimateBowItem extends BowItem {
    private static final double VANILLA_FULL_DRAW_ARROW_DAMAGE = 6.0D;

    public UltimateBowItem(Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(properties, AntarchySettings::ultimateBowEnchantability));
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return false;
        }

        ItemStack projectileStack = player.getProjectile(stack);
        if (projectileStack.isEmpty()) {
            return false;
        }

        int useTicks = this.getUseDuration(stack, livingEntity) - timeLeft;
        int adjustedUseTicks = Math.max(1, (int) Math.round(useTicks * AntarchySettings.ultimateBowDrawSpeedMultiplier()));
        float power = BowItem.getPowerForTime(adjustedUseTicks);
        if (power < 0.1F) {
            return false;
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
        return true;
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
        arrow.setBaseDamage(((AccessorAbstractArrow) arrow).antarchy$getBaseDamage() * multiplier);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        ensureConfiguredEnchantments(stack, level.registryAccess());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        ensureConfiguredEnchantments(stack, player.level().registryAccess());
    }

    private static void ensureConfiguredEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        UltimateGearHelper.ensureUltimateBowEnchantments(stack, registries);
    }
}
