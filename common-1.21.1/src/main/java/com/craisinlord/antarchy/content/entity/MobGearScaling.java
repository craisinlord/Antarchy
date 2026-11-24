package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public final class MobGearScaling {
    private static final int NO_PLAYERS = -1;
    public static final int NONE = 0;
    public static final int IRON = 1;
    public static final int DIAMOND = 2;
    public static final int NETHERITE = 3;
    public static final int ULTIMATE = 4;

    private static final TagKey<EntityType<?>> NO_SCALED_GEAR = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "no_scaled_gear"));
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private MobGearScaling() {
    }

    public static void equip(Mob mob, ServerLevelAccessor accessor, MobSpawnType spawnType) {
        if (scaledSpawn(spawnType) && accessor instanceof ServerLevel level) {
            fill(mob, level);
        }
    }

    public static void equipTrialSpawn(Entity root) {
        if (!(root.level() instanceof ServerLevel level)) {
            return;
        }
        root.getSelfAndPassengers().forEach(entity -> {
            if (entity instanceof Mob mob) {
                fill(mob, level);
            }
        });
    }

    public static boolean extremeDifficulty() {
        return false;
    }

    public static double chanceMultiplier(ServerLevel level) {
        if (extremeDifficulty()) {
            return AntarchySettings.mobGearExtremeChanceMultiplier();
        }
        return switch (level.getDifficulty()) {
            case HARD -> AntarchySettings.mobGearHardChanceMultiplier();
            case NORMAL -> 1.0D;
            default -> AntarchySettings.mobGearEasyChanceMultiplier();
        };
    }

    public static double higherTierChance(ServerLevel level) {
        if (extremeDifficulty()) {
            return AntarchySettings.mobGearExtremeHigherTierChance();
        }
        return level.getDifficulty() == Difficulty.HARD ? AntarchySettings.mobGearHardHigherTierChance() : 0.0D;
    }

    public static double score(ServerPlayer player) {
        return player.getAttributeValue(Attributes.ARMOR) + player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
    }

    private static void fill(Mob mob, ServerLevel level) {
        if (!(mob instanceof Zombie || mob instanceof AbstractSkeleton) || !AntarchySettings.mobGearScalingEnabled()
                || mob.getType().is(NO_SCALED_GEAR)) {
            return;
        }
        int tier = nearbyTier(level, mob.position());
        if (tier < NONE) {
            return;
        }
        double multiplier = chanceMultiplier(level);
        double higher = higherTierChance(level);
        RandomSource random = mob.getRandom();
        for (EquipmentSlot slot : SLOTS) {
            double chance = slot == EquipmentSlot.HEAD ? AntarchySettings.mobGearHelmetChance() : AntarchySettings.mobGearSlotChance();
            if (!mob.getItemBySlot(slot).isEmpty() || random.nextDouble() >= chance * multiplier) {
                continue;
            }
            int pieceTier = rollTier(tier, higher, random);
            if (pieceTier > NONE) {
                give(mob, slot, armor(pieceTier, slot, level));
            }
        }
        if (mob instanceof Zombie && !(mob instanceof Drowned) && mob.getMainHandItem().isEmpty()
                && random.nextDouble() < AntarchySettings.mobGearWeaponChance() * multiplier) {
            int weaponTier = random.nextDouble() < higher ? tier + 1 : tier;
            if (weaponTier > NONE) {
                give(mob, EquipmentSlot.MAINHAND, weapon(weaponTier));
            }
        }
    }

    private static int rollTier(int tier, double higher, RandomSource random) {
        double roll = random.nextDouble();
        if (roll < higher) {
            return Math.min(ULTIMATE, tier + 1);
        }
        return tier > IRON && roll < higher + AntarchySettings.mobGearLowerTierChance() ? tier - 1 : tier;
    }

    public static int tierFor(double score) {
        if (score >= AntarchySettings.mobGearUltimateScore()) {
            return ULTIMATE;
        }
        if (score >= AntarchySettings.mobGearNetheriteScore()) {
            return NETHERITE;
        }
        if (score >= AntarchySettings.mobGearDiamondScore()) {
            return DIAMOND;
        }
        return score >= AntarchySettings.mobGearIronScore() ? IRON : NONE;
    }

    private static boolean scaledSpawn(MobSpawnType type) {
        return type == MobSpawnType.NATURAL || type == MobSpawnType.SPAWNER
                || type == MobSpawnType.REINFORCEMENT || type == MobSpawnType.JOCKEY;
    }

    public static int nearbyTier(ServerLevel level, Vec3 pos) {
        double range = AntarchySettings.mobGearPlayerRange();
        double rangeSqr = range * range;
        double lowest = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || !player.isAlive() || player.distanceToSqr(pos) > rangeSqr) {
                continue;
            }
            lowest = Math.min(lowest, score(player));
        }
        return lowest == Double.MAX_VALUE ? NO_PLAYERS : tierFor(lowest);
    }

    private static void give(Mob mob, EquipmentSlot slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        mob.setItemSlot(slot, stack);
        mob.setDropChance(slot, 0.0F);
    }

    private static ItemStack armor(int tier, EquipmentSlot slot, ServerLevel level) {
        if (tier == ULTIMATE) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ultimate_" + piece(slot)));
            return AntarchySettings.mobGearUltimateEnchanted()
                    ? UltimateGearHelper.createUltimateArmorStack(item, level.registryAccess())
                    : new ItemStack(item);
        }
        String material = tier == NETHERITE ? "netherite_" : tier == DIAMOND ? "diamond_" : "iron_";
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(material + piece(slot))));
    }

    private static String piece(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            default -> "boots";
        };
    }

    private static ItemStack weapon(int tier) {
        return new ItemStack(tier >= NETHERITE ? Items.NETHERITE_SWORD : tier == DIAMOND ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
    }
}
