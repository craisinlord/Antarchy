package com.craisinlord.antarchy.content.item;

import net.minecraft.world.entity.EntityTypes;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class KrakensGraspItem extends TridentItem {
    private static final Identifier ATTACK_DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "krakens_grasp_attack_damage");
    private static final Identifier ATTACK_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "krakens_grasp_attack_speed");

    public KrakensGraspItem(Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.enchantable(1).component(DataComponents.WEAPON, new Weapon(1)),
                KrakensGraspItem::createConfiguredAttributes
        ));
    }

    private static ItemAttributeModifiers createConfiguredAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, AntarchySettings.krakensGraspAttackDamage(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, AntarchySettings.krakensGraspAttackSpeed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        ensureInnateLoyalty(stack, level.registryAccess());
    }

    private static void ensureInnateLoyalty(ItemStack stack, HolderLookup.Provider registries) {
        if (!AntarchySettings.krakensGraspInnateLoyalty()) {
            return;
        }

        var loyalty = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOYALTY);
        int desiredLevel = net.minecraft.util.Mth.clamp(AntarchySettings.krakensGraspInnateLoyaltyLevel(), 1, 3);
        if (EnchantmentHelper.getItemEnchantmentLevel(loyalty, stack) >= desiredLevel) {
            return;
        }

        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getEnchantments());
        enchantments.set(loyalty, desiredLevel);
        EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable());
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            strikeLightning(target, serverLevel);
            com.craisinlord.antarchy.content.entity.kraken.TentacleEntity.spawnAt(serverLevel,
                    new net.minecraft.world.phys.Vec3(target.getX(), target.getY(), target.getZ()), attacker);
        }
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return !(user instanceof Player player && player.isCreative());
    }

    public static void strikeLightning(LivingEntity target, ServerLevel level) {
        strikeLightningAt(level, target.getX(), target.getY(), target.getZ());
        target.hurtServer(level, level.damageSources().lightningBolt(), (float) AntarchySettings.krakensGraspLightningDamage());
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    public static void strikeLightningAt(ServerLevel level, double x, double y, double z) {
        LightningBolt lightningBolt = new LightningBolt(net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT, level);
        lightningBolt.snapTo(x, y, z);
        lightningBolt.setVisualOnly(true);
        level.addFreshEntity(lightningBolt);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.2D, z, 10, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!(user instanceof Player player)) {
            return false;
        }

        int useTime = this.getUseDuration(stack, user) - timeLeft;
        if (useTime < 10) {
            return false;
        }

        float spinStrength = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
        if (spinStrength > 0.0F && player.isInWaterOrRain()) {
            return super.releaseUsing(stack, level, user, timeLeft);
        }

        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return false;
        }

        if (!level.isClientSide()) {
            stack.hurtAndBreak(1, player, user.getUsedItemHand().asEquipmentSlot());
            com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident trident =
                    new com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident(AntarchyObjects.KRAKENS_GRASP_TRIDENT.get(), player, level, stack.copy());
            trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.hasInfiniteMaterials()) {
                trident.pickup = net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(trident);
            level.playSound(null, trident, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.hasInfiniteMaterials()) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }
}
