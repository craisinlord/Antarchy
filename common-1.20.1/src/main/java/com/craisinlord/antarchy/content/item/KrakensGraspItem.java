package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.google.common.collect.ImmutableMultimap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public class KrakensGraspItem extends TridentItem {
    private static final String ATTACK_DAMAGE_MODIFIER_ID = "krakens_grasp_attack_damage";
    private static final String ATTACK_SPEED_MODIFIER_ID = "krakens_grasp_attack_speed";

    public KrakensGraspItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                UUID.nameUUIDFromBytes(ATTACK_DAMAGE_MODIFIER_ID.getBytes()),
                ATTACK_DAMAGE_MODIFIER_ID,
                AntarchySettings.krakensGraspAttackDamage(),
                AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                UUID.nameUUIDFromBytes(ATTACK_SPEED_MODIFIER_ID.getBytes()),
                ATTACK_SPEED_MODIFIER_ID,
                AntarchySettings.krakensGraspAttackSpeed(),
                AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide) {
            ensureInnateLoyalty(stack, level.registryAccess());
        }
    }

    private static void ensureInnateLoyalty(ItemStack stack, HolderLookup.Provider registries) {
        if (!AntarchySettings.krakensGraspInnateLoyalty()) {
            return;
        }

        Enchantment loyalty = Enchantments.LOYALTY;
        int desiredLevel = net.minecraft.util.Mth.clamp(AntarchySettings.krakensGraspInnateLoyaltyLevel(), 1, 3);
        if (EnchantmentHelper.getItemEnchantmentLevel(loyalty, stack) >= desiredLevel) {
            return;
        }

        java.util.Map<Enchantment, Integer> enchantments = new java.util.HashMap<>(EnchantmentHelper.getEnchantments(stack));
        enchantments.put(loyalty, desiredLevel);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean didHurt = super.hurtEnemy(stack, target, attacker);
        if (didHurt && attacker.level() instanceof ServerLevel serverLevel) {
            strikeLightning(target, serverLevel);
            com.craisinlord.antarchy.content.entity.kraken.TentacleEntity.spawnAt(serverLevel,
                    new net.minecraft.world.phys.Vec3(target.getX(), target.getY(), target.getZ()), attacker);
        }
        return didHurt;
    }

    public static void strikeLightning(LivingEntity target, ServerLevel level) {
        strikeLightningAt(level, target.getX(), target.getY(), target.getZ());
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    public static void strikeLightningAt(ServerLevel level, double x, double y, double z) {
        LightningBolt lightningBolt = new LightningBolt(net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, level);
        lightningBolt.moveTo(x, y, z);
        level.addFreshEntity(lightningBolt);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.2D, z, 10, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!(user instanceof Player player)) {
            return;
        }

        int useTime = this.getUseDuration(stack) - timeLeft;
        if (useTime < 10) {
            return;
        }

        int riptideLevel = EnchantmentHelper.getRiptide(stack);
        if (riptideLevel > 0 && player.isInWaterOrRain()) {
            super.releaseUsing(stack, level, user, timeLeft);
            return;
        }

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return;
        }

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(user.getUsedItemHand()));
            com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident trident =
                    new com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident(AntarchyObjects.KRAKENS_GRASP_TRIDENT.get(), player, level, stack.copy());
            trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.getAbilities().instabuild) {
                trident.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(trident);
            level.playSound(null, trident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
