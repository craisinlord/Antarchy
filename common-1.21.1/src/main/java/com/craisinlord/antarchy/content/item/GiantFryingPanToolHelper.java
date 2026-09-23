package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.item.ultimate.UltimateToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class GiantFryingPanToolHelper {
    public static final int MINING_RADIUS = 2;
    public static final int FIRE_ASPECT_LEVEL = 2;
    private static final double HORIZONTAL_LAUNCH = 1.8D;
    private static final double MAX_HORIZONTAL_SPEED = 2.6D;
    private static final double VERTICAL_LAUNCH = 0.75D;

    private GiantFryingPanToolHelper() {
    }

    public static float getDestroySpeed(Tier tier, ItemStack stack, BlockState state, float defaultSpeed) {
        return UltimateToolHelper.getDestroySpeed(UltimateToolHelper.ToolKind.SHOVEL, tier, stack, state, defaultSpeed);
    }

    public static boolean isShovelBlock(BlockState state) {
        return UltimateToolHelper.matchesToolKind(UltimateToolHelper.ToolKind.SHOVEL, state);
    }

    public static void mineArea(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (miner instanceof ServerPlayer player) {
            UltimateToolHelper.mineArea(UltimateToolHelper.ToolKind.SHOVEL, stack, level, state, pos, player, MINING_RADIUS);
        }
    }

    public static void launch(LivingEntity target, LivingEntity attacker) {
        Vec3 direction = target.position().subtract(attacker.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = attacker.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        Vec3 motion = target.getDeltaMovement();
        double x = motion.x;
        double z = motion.z;
        if (direction.lengthSqr() >= 1.0E-4D) {
            direction = direction.normalize();
            x = Math.max(-MAX_HORIZONTAL_SPEED, Math.min(MAX_HORIZONTAL_SPEED, x + direction.x * HORIZONTAL_LAUNCH));
            z = Math.max(-MAX_HORIZONTAL_SPEED, Math.min(MAX_HORIZONTAL_SPEED, z + direction.z * HORIZONTAL_LAUNCH));
        }
        target.setDeltaMovement(x, Math.max(motion.y, 0.0D) + VERTICAL_LAUNCH, z);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    public static ItemStack createStack(Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        ensureEnchantments(stack, registries);
        return stack;
    }

    public static void ensureEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        var fireAspect = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
        if (stack.getEnchantments().getLevel(fireAspect) >= FIRE_ASPECT_LEVEL) {
            return;
        }
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getEnchantments());
        enchantments.set(fireAspect, FIRE_ASPECT_LEVEL);
        EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable());
    }
}
