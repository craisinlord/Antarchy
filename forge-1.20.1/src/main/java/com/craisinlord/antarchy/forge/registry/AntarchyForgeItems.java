package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.craisinlord.antarchy.content.item.*;
import com.craisinlord.antarchy.content.item.BloodCrystalArmorItem;
import com.craisinlord.antarchy.content.item.BloodCrystalAppleItem;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.content.item.BloodCrystalShardItem;
import com.craisinlord.antarchy.content.item.ultimate.*;
import com.craisinlord.antarchy.content.item.NightmareArmorItem;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import com.craisinlord.antarchy.content.item.LucidEyeItem;
import com.craisinlord.antarchy.content.item.LucidPearlItem;
import com.craisinlord.antarchy.content.item.OuranwoodBoatOnlyItem;
import com.craisinlord.antarchy.content.item.OuranwoodChestBoatItem;
import com.craisinlord.antarchy.content.item.RainbowSugarItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.sounds.SoundEvents;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public final class AntarchyForgeItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Antarchy.MODID);

    private static final java.util.Map<ArmorItem.Type, Integer> BASE_DURABILITY = java.util.Map.of(
            ArmorItem.Type.BOOTS, 13,
            ArmorItem.Type.LEGGINGS, 15,
            ArmorItem.Type.CHESTPLATE, 16,
            ArmorItem.Type.HELMET, 11
    );

    private static int armorDurability(ArmorItem.Type type, int multiplier) {
        return BASE_DURABILITY.get(type) * multiplier;
    }

    private static ArmorMaterial armorMaterial(
            int durabilityMultiplier,
            java.util.Map<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            net.minecraft.sounds.SoundEvent equipSound,
            java.util.function.Supplier<Ingredient> repairIngredient,
            float toughness,
            float knockbackResistance,
            String name
    ) {
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return BASE_DURABILITY.get(type) * durabilityMultiplier;
            }

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return defense.getOrDefault(type, 0);
            }

            @Override
            public int getEnchantmentValue() {
                return enchantmentValue;
            }

            @Override
            public net.minecraft.sounds.SoundEvent getEquipSound() {
                return equipSound;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return repairIngredient.get();
            }

            @Override
            public String getName() {
                return new ResourceLocation(Antarchy.MODID, name).toString();
            }

            @Override
            public float getToughness() {
                return toughness;
            }

            @Override
            public float getKnockbackResistance() {
                return knockbackResistance;
            }
        };
    }

    // Tier constants
    public static final Tier ULTIMATE_TIER = new SimpleToolTier(
            3072,
            10.5F,
            0.0F,
            Tiers.NETHERITE.getLevel(),
            25,
            AntarchyForgeItems::ultimateRepairIngredient
    );
    public static final Tier BLOOD_CRYSTAL_KATANA_TIER = new SimpleToolTier(
            1200,
            8.0F,
            0.0F,
            Tiers.DIAMOND.getLevel(),
            18,
            AntarchyForgeItems::bloodCrystalRepairIngredient
    );
    public static final Tier ATTITUDE_ADJUSTER_TIER = new SimpleToolTier(
            3072,
            7.0F,
            0.0F,
            Tiers.NETHERITE.getLevel(),
            18,
            () -> Ingredient.of(AntarchyForgeItems.HERCULES_HORN.get())
    );

    public static final RegistryObject<Item> NIGHTMARE_SCALE = ITEMS.registerSimpleItem("nightmare_scale", new Item.Properties().rarity(Rarity.RARE).fireResistant());

    private static final ArmorMaterial ULTIMATE_ARMOR_MATERIAL = armorMaterial(
            40, createUltimateArmorDefense(), 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            AntarchyForgeItems::ultimateRepairIngredient, 2.0F,
            (float) AntarchySettings.ultimateArmorKnockbackResistance(), "ultimate_armor");
    private static final ArmorMaterial NIGHTMARE_ARMOR_MATERIAL = armorMaterial(
            37, createNightmareArmorDefense(), 10, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(NIGHTMARE_SCALE.get()), 3.0F,
            (float) AntarchySettings.nightmareArmorKnockbackResistance(), "nightmare_armor");
    private static final ArmorMaterial BLOOD_CRYSTAL_ARMOR_MATERIAL = armorMaterial(
            33, createBloodCrystalArmorDefense(), 30, SoundEvents.ARMOR_EQUIP_DIAMOND,
            AntarchyForgeItems::bloodCrystalRepairIngredient, 0.0F, 0.0F, "blood_crystal");
    private static final ArmorMaterial MOGGLES_ARMOR_MATERIAL = armorMaterial(
            25, createMogglesArmorDefense(), 12, SoundEvents.ARMOR_EQUIP_GOLD,
            () -> Ingredient.of(Items.GOLD_INGOT), 0.0F, 0.0F, "moggles");
    public static final RegistryObject<Item> PRIMORDIAL_SCUTE = ITEMS.registerSimpleItem("primordial_scute",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    private static final ArmorMaterial PRIMORDIAL_ARMOR_MATERIAL = armorMaterial(
            33, createPrimordialArmorDefense(), 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(PRIMORDIAL_SCUTE.get()), 3.0F, 0.1F, "primordial");
    private static final ArmorMaterial JUMPY_BOOTS_ARMOR_MATERIAL = armorMaterial(
            37, createJumpyBootsDefense(), 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_BOOTS), 3.0F, 0.1F, "jumpy_boots");
    private static final ArmorMaterial FALLEN_KING_CROWN_ARMOR_MATERIAL = armorMaterial(
            25, createFallenKingCrownDefense(), 25, SoundEvents.ARMOR_EQUIP_GOLD,
            () -> Ingredient.of(Items.GOLD_INGOT), 0.0F, 0.0F, "fallen_king_crown");

    // Block items
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DUPLICATOR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DUPLICATOR_LOG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_LOG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_WOOD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOSSY_OURANWOOD_LOG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOSSY_OURANWOOD_WOOD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.STRIPPED_OURANWOOD_LOG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.STRIPPED_OURANWOOD_WOOD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_PLANKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_FENCE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_FENCE_GATE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_DOOR);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_TRAPDOOR);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_PRESSURE_PLATE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_BUTTON);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_LEAVES);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> OURANWOOD_ACORN = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.OURANWOOD_ACORN_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_LOG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_WOOD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> STRIPPED_PEACH_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.STRIPPED_PEACH_LOG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> STRIPPED_PEACH_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.STRIPPED_PEACH_WOOD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_PLANKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_FENCE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_FENCE_GATE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_DOOR);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_TRAPDOOR);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_PRESSURE_PLATE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_BUTTON);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_LEAVES);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PEACH_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PEACH_SAPLING);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> ORANGE_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.ORANGE_MILKWEED);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PINK_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PINK_MILKWEED);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CAMELLIA_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CAMELLIA);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SPIDER_LILY_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SPIDER_LILY);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> GIANT_LILY_PAD_ITEM = ITEMS.register("giant_lily_pad",
            () -> new com.craisinlord.antarchy.content.item.GiantLilyPadItem(AntarchyForgeBlocks.GIANT_LILY_PAD.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SEASHELL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SEASHELL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> LOTUS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.LOTUS);
    public static final RegistryObject<net.minecraft.world.item.SignItem> OURANWOOD_SIGN_ITEM = ITEMS.register("ouranwood_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), AntarchyForgeBlocks.OURANWOOD_SIGN.get(), AntarchyForgeBlocks.OURANWOOD_WALL_SIGN.get()));
    public static final RegistryObject<net.minecraft.world.item.HangingSignItem> OURANWOOD_HANGING_SIGN_ITEM = ITEMS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(AntarchyForgeBlocks.OURANWOOD_HANGING_SIGN.get(), AntarchyForgeBlocks.OURANWOOD_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> OURANWOOD_BOAT = ITEMS.register("ouranwood_boat",
            () -> new OuranwoodBoatOnlyItem(AntarchyForgeEntites.OURANWOOD_BOAT_ENTITY, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OURANWOOD_CHEST_BOAT = ITEMS.register("ouranwood_chest_boat",
            () -> new OuranwoodChestBoatItem(AntarchyForgeEntites.OURANWOOD_CHEST_BOAT_ENTITY, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<net.minecraft.world.item.SignItem> PEACH_SIGN_ITEM = ITEMS.register("peach_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), AntarchyForgeBlocks.PEACH_SIGN.get(), AntarchyForgeBlocks.PEACH_WALL_SIGN.get()));
    public static final RegistryObject<net.minecraft.world.item.HangingSignItem> PEACH_HANGING_SIGN_ITEM = ITEMS.register("peach_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(AntarchyForgeBlocks.PEACH_HANGING_SIGN.get(), AntarchyForgeBlocks.PEACH_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PEACH_BOAT = ITEMS.register("peach_boat",
            () -> new PeachBoatOnlyItem(AntarchyForgeEntites.PEACH_BOAT_ENTITY, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PEACH_CHEST_BOAT = ITEMS.register("peach_chest_boat",
            () -> new PeachChestBoatItem(AntarchyForgeEntites.PEACH_CHEST_BOAT_ENTITY, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<CritterCageItem> CRITTER_CAGE = ITEMS.register("critter_cage",
            () -> new CritterCageItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DUPLICATOR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DUPLICATOR_SAPLING);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> RED_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.RED_ANT_NEST);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROWN_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROWN_ANT_NEST);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> RAINBOW_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.RAINBOW_ANT_NEST);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TERMITE_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.TERMITE_NEST);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> URANIUM_ORE_ITEM = ITEMS.register("uranium_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.URANIUM_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DEEPSLATE_URANIUM_ORE_ITEM = ITEMS.register("deepslate_uranium_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.DEEPSLATE_URANIUM_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TITANIUM_ORE_ITEM = ITEMS.register("titanium_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.TITANIUM_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DEEPSLATE_TITANIUM_ORE_ITEM = ITEMS.register("deepslate_titanium_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.DEEPSLATE_TITANIUM_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BLUESTONE_DUST = ITEMS.register("bluestone",
            () -> new BluestoneDustItem(AntarchyForgeBlocks.BLUESTONE_WIRE.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BLUESTONE_ORE_ITEM = ITEMS.register("bluestone_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.BLUESTONE_ORE.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BLUESTONE_BLOCK_ITEM = ITEMS.register("bluestone_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.BLUESTONE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> BLUESTONE_REPEATER_ITEM = ITEMS.register("bluestone_repeater",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.BLUESTONE_REPEATER.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> BLUESTONE_COMPARATOR_ITEM = ITEMS.register("bluestone_comparator",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.BLUESTONE_COMPARATOR.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> BLUESTONE_TORCH_ITEM = ITEMS.register("bluestone_torch",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.BLUESTONE_TORCH.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> URANIUM_BLOCK_ITEM = ITEMS.register("uranium_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.URANIUM_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TITANIUM_BLOCK_ITEM = ITEMS.register("titanium_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.TITANIUM_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> RAW_URANIUM_BLOCK_ITEM = ITEMS.register("raw_uranium_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.RAW_URANIUM_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> RAW_TITANIUM_BLOCK_ITEM = ITEMS.register("raw_titanium_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.RAW_TITANIUM_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_URANIUM_ITEM = ITEMS.register("cut_uranium",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CUT_URANIUM.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_TITANIUM_ITEM = ITEMS.register("cut_titanium",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CUT_TITANIUM.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_URANIUM_SLAB_ITEM = ITEMS.register("cut_uranium_slab",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CUT_URANIUM_SLAB.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_TITANIUM_SLAB_ITEM = ITEMS.register("cut_titanium_slab",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CUT_TITANIUM_SLAB.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_URANIUM_STAIRS_ITEM = ITEMS.register("cut_uranium_stairs",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CUT_URANIUM_STAIRS.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_TITANIUM_STAIRS_ITEM = ITEMS.register("cut_titanium_stairs",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CUT_TITANIUM_STAIRS.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHISELED_URANIUM_ITEM = ITEMS.register("chiseled_uranium",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CHISELED_URANIUM.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHISELED_TITANIUM_ITEM = ITEMS.register("chiseled_titanium",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.CHISELED_TITANIUM.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> URANIUM_BULB_ITEM = ITEMS.register("uranium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyForgeBlocks.URANIUM_BULB.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TITANIUM_BULB_ITEM = ITEMS.register("titanium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyForgeBlocks.TITANIUM_BULB.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> URANIUM_DOOR_ITEM = ITEMS.register("uranium_door",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.URANIUM_DOOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TITANIUM_DOOR_ITEM = ITEMS.register("titanium_door",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.TITANIUM_DOOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> URANIUM_TRAPDOOR_ITEM = ITEMS.register("uranium_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.URANIUM_TRAPDOOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TITANIUM_TRAPDOOR_ITEM = ITEMS.register("titanium_trapdoor",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.TITANIUM_TRAPDOOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> URANIUM_BARS_ITEM = ITEMS.register("uranium_bars",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.URANIUM_BARS.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TITANIUM_BARS_ITEM = ITEMS.register("titanium_bars",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.TITANIUM_BARS.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> ANTIMETAL_ITEM = ITEMS.register("antimetal",
            () -> new AntimetalBlockItem(AntarchyForgeBlocks.ANTIMETAL.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_ITEM = ITEMS.register("polished_antimetal",
            () -> new AntimetalBlockItem(AntarchyForgeBlocks.POLISHED_ANTIMETAL.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> ANTIMETAL_SCAFFOLDING_ITEM = ITEMS.register("antimetal_scaffolding",
            () -> new AntimetalScaffoldingItem(AntarchyForgeBlocks.ANTIMETAL_SCAFFOLDING.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> ANTIMETAL_RAIL_ITEM = ITEMS.register("antimetal_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.ANTIMETAL_RAIL.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> ANTIMETAL_POWERED_RAIL_ITEM = ITEMS.register("antimetal_powered_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.ANTIMETAL_POWERED_RAIL.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> ANTIMETAL_DETECTOR_RAIL_ITEM = ITEMS.register("antimetal_detector_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.ANTIMETAL_DETECTOR_RAIL.get(), new Item.Properties()));
    public static final RegistryObject<CeilingPlacementTooltipBlockItem> ANTIMETAL_ACTIVATOR_RAIL_ITEM = ITEMS.register("antimetal_activator_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyForgeBlocks.ANTIMETAL_ACTIVATOR_RAIL.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SMALL_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SMALL_BLOOD_CRYSTAL_BUD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MEDIUM_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> LARGE_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.LARGE_BLOOD_CRYSTAL_BUD);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BUDDING_BLOOD_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BUDDING_BLOOD_CRYSTAL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_ITEM = ITEMS.register("blood_crystal_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.BLOOD_CRYSTAL.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BLOOD_CRYSTAL_CRYSTAL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_SAND_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_SAND);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_SANDSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHISELED_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CHISELED_DREAM_SANDSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CUT_DREAM_SANDSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SMOOTH_DREAM_SANDSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_SANDSTONE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_SANDSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_SANDSTONE_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SMOOTH_DREAM_SANDSTONE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SMOOTH_DREAM_SANDSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CUT_DREAM_SANDSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DEAD_STAR_CORAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DEAD_STAR_CORAL_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DEAD_STAR_CORAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DEAD_STAR_CORAL);
    public static final RegistryObject<StandingAndWallBlockItem> DEAD_STAR_CORAL_FAN_ITEM = ITEMS.register("dead_star_coral_fan",
            () -> new StandingAndWallBlockItem(AntarchyForgeBlocks.DEAD_STAR_CORAL_FAN.get(), AntarchyForgeBlocks.DEAD_STAR_CORAL_WALL_FAN.get(), new Item.Properties(), net.minecraft.core.Direction.UP));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> STAR_CORAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.STAR_CORAL_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> STAR_CORAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.STAR_CORAL);
    public static final RegistryObject<StandingAndWallBlockItem> STAR_CORAL_FAN_ITEM = ITEMS.register("star_coral_fan",
            () -> new StandingAndWallBlockItem(AntarchyForgeBlocks.STAR_CORAL_FAN.get(), AntarchyForgeBlocks.STAR_CORAL_WALL_FAN.get(), new Item.Properties(), net.minecraft.core.Direction.UP));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DUCT_TAPE_ITEM = ITEMS.register("duct_tape",
            () -> new DuctTapeBlockItem(AntarchyForgeBlocks.DUCT_TAPE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> INFESTED_ROOTED_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.INFESTED_ROOTED_DIRT);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> INFESTED_COARSE_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.INFESTED_COARSE_DIRT);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MYRMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MYRMITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BIOMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BIOMITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BIOMITE_TURF_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BIOMITE_TURF);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BIOWART_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BIOWART);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BIOWART_TENDRILS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BIOWART_TENDRILS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_BROODSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHISELED_BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CHISELED_BROODSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_BRICKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_BROODSTONE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_BROODSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_BROODSTONE_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_BRICK_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_BRICK_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BROODSTONE_BRICK_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MYRMITE_COAL_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MYRMITE_COAL_ORE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_URANIUM_ORE_ITEM = ITEMS.register("broodstone_uranium_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.BROODSTONE_URANIUM_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BROODSTONE_TITANIUM_ORE_ITEM = ITEMS.register("broodstone_titanium_ore",
            () -> new net.minecraft.world.item.BlockItem(AntarchyForgeBlocks.BROODSTONE_TITANIUM_ORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_NYXITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHISELED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CHISELED_NYXITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_BRICKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHITEN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CHITEN_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_NYXITE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_NYXITE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_NYXITE_WALL);
    public static final RegistryObject<CeilingCompatiblePlacementTooltipBlockItem> POLISHED_NYXITE_PRESSURE_PLATE_ITEM = ITEMS.register("polished_nyxite_pressure_plate",
            () -> new CeilingCompatiblePlacementTooltipBlockItem(AntarchyForgeBlocks.POLISHED_NYXITE_PRESSURE_PLATE.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_NYXITE_BUTTON);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_BRICK_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_BRICK_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_BRICK_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_SHELLSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_BRICKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHISELED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CHISELED_SHELLSTONE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CRACKED_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CRACKED_SHELLSTONE_BRICKS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICK_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICK_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICK_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_SHELLSTONE_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_SHELLSTONE_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POLISHED_SHELLSTONE_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_BRICK_STAIRS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_BRICK_SLAB);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SHELLSTONE_BRICK_WALL);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> TRIFFID_GOO_BLOCK_ITEM = ITEMS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.item.TriffidGooBlockItem(AntarchyForgeBlocks.TRIFFID_GOO_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> PALE_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.PALE_NYXITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> NYXITE_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.NYXITE_SPIKE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CHITEN_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CHITEN_SPIKE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> POTENT_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.POTENT_NYXITE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.UMBRAL_MOSS_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.UMBRAL_MOSS_CARPET);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> AMBER_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.AMBER_MOSS_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> AMBER_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.AMBER_MOSS_CARPET);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> AMBER_LICHEN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.AMBER_LICHEN);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BILE_VEIN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BILE_VEIN);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CREEPVINE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CREEPVINE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BLUSH_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BLUSH_MOSS_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BLUSH_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BLUSH_MOSS_CARPET);
    public static final RegistryObject<com.craisinlord.antarchy.content.item.DreamTorchItem> DREAM_TORCH_ITEM = ITEMS.register("dream_torch",
            () -> new com.craisinlord.antarchy.content.item.DreamTorchItem(AntarchyForgeBlocks.DREAM_TORCH.get(), AntarchyForgeBlocks.DREAM_WALL_TORCH.get(), AntarchyForgeBlocks.DREAM_CEILING_TORCH.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_LANTERN);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> DREAM_CAMPFIRE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.DREAM_CAMPFIRE);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> LUMEN_FROGLIGHT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.LUMEN_FROGLIGHT);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> ROSEATE_FROGLIGHT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.ROSEATE_FROGLIGHT);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> BED_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.BED_BUG_EGG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> CREEPING_HORROR_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.CREEPING_HORROR_EGG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> JUMPY_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.JUMPY_BUG_EGG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> SPIT_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.SPIT_BUG_EGG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> JERRY_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.JERRY_EGG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> LURKING_TERROR_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.LURKING_TERROR_EGG);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> WASP_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.WASP_NEST);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> HUSHWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.HUSHWEED);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> HANGING_CREEPROOTS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.HANGING_CREEPROOTS);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> GLOWCAP_MUSHROOM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.GLOWCAP_MUSHROOM);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> GLOWCAP_MUSHROOM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.GLOWCAP_MUSHROOM_BLOCK);
    public static final RegistryObject<net.minecraft.world.item.BlockItem> MOLTING_VINES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyForgeBlocks.MOLTING_VINES);
    public static final RegistryObject<BucketItem> BILE_BUCKET = ITEMS.register("bile_bucket",
            () -> new BucketItem(AntarchyForgeMisc.BILE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<BucketItem> ICHOR_BUCKET = ITEMS.register("ichor_bucket",
            () -> new BucketItem(AntarchyForgeMisc.ICHOR.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<BucketItem> ANTIWATER_BUCKET = ITEMS.register("antiwater_bucket",
            () -> new BucketItem(AntarchyForgeMisc.ANTIWATER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<BucketItem> LUMEN_BUCKET = ITEMS.register("lumen_bucket",
            () -> new BucketItem(AntarchyForgeMisc.LUMEN.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<com.craisinlord.antarchy.content.item.CloudBucketItem> CLOUD_BUCKET = ITEMS.register("cloud_bucket",
            () -> new com.craisinlord.antarchy.content.item.CloudBucketItem(AntarchyForgeBlocks.CLOUD_BLOCK.get(), new Item.Properties().craftRemainder(Items.BUCKET)));
    public static final RegistryObject<BloodCrystalShardItem> BLOOD_CRYSTAL_SHARD = ITEMS.register("blood_crystal_shard",
            () -> new BloodCrystalShardItem(new Item.Properties()));
    public static final RegistryObject<Item> BLOOD_CRYSTAL_APPLE = ITEMS.register("blood_crystal_apple",
            () -> new BloodCrystalAppleItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationMod(1.2f)
                            .effect(() -> new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1), 1.0f)
                            .alwaysEat()
                            .build())));
    public static final RegistryObject<BloodCrystalArmorItem> BLOOD_CRYSTAL_HELMET = ITEMS.register("blood_crystal_helmet",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BloodCrystalArmorItem> BLOOD_CRYSTAL_CHESTPLATE = ITEMS.register("blood_crystal_chestplate",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BloodCrystalArmorItem> BLOOD_CRYSTAL_LEGGINGS = ITEMS.register("blood_crystal_leggings",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BloodCrystalArmorItem> BLOOD_CRYSTAL_BOOTS = ITEMS.register("blood_crystal_boots",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BloodCrystalKatanaItem> BLOOD_CRYSTAL_KATANA = ITEMS.register("blood_crystal_katana",
            () -> new BloodCrystalKatanaItem(
                    BLOOD_CRYSTAL_KATANA_TIER,
                    new Item.Properties().stacksTo(1).durability(1200).rarity(Rarity.RARE),
                    AntarchySettings.bloodCrystalKatanaAttackDamage(),
                    -2.2F
            ));
    public static final RegistryObject<MantisClawItem> MANTIS_CLAW = ITEMS.register("mantis_claw",
            () -> new MantisClawItem(Tiers.IRON, new Item.Properties().stacksTo(1).durability(50).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> HERCULES_HORN = ITEMS.registerSimpleItem("hercules_horn", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> KING_SCALE = ITEMS.register("king_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> QUEEN_SCALE = ITEMS.register("queen_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CHITEN = ITEMS.registerSimpleItem("chiten", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> JERRY_NUCLEUS = ITEMS.register("jerry_nucleus",
            () -> new com.craisinlord.antarchy.content.item.JerryNucleusItem(new Item.Properties().rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.1F)
                            .build())));
    public static final RegistryObject<Item> CARAPACE = ITEMS.registerSimpleItem("carapace", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> STINK_BUG = ITEMS.registerSimpleItem("stink_bug", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> JUMPY_BUG_LEG = ITEMS.registerSimpleItem("jumpy_bug_leg", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<JumpyBootsItem> JUMPY_BOOTS = ITEMS.register("jumpy_boots",
            () -> new JumpyBootsItem(JUMPY_BOOTS_ARMOR_MATERIAL, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().durability(armorDurability(ArmorItem.Type.BOOTS, 37))));
    public static final RegistryObject<Item> BRUTALFLY_WING = ITEMS.registerSimpleItem("brutalfly_wing", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<BrutalflyElytraItem> BRUTALFLY_ELYTRA = ITEMS.register("brutalfly_elytra",
            () -> new BrutalflyElytraItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(480)));
    public static final RegistryObject<Item> CORNEA_EAR = ITEMS.register("cornea_ear",
            () -> new CorneaEarItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.4F)
                            .build())));
    public static final RegistryObject<Item> CORN = ITEMS.register("corn",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(3)
                            .saturationMod(0.6F)
                            .build())));
    public static final RegistryObject<ItemNameBlockItem> CORN_SEEDS = ITEMS.register("corn_seeds",
            () -> new ItemNameBlockItem(AntarchyForgeBlocks.CORN_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> RAW_VENISON = ITEMS.register("raw_venison",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.3F)
                            .build())));
    public static final RegistryObject<Item> BROODFRUIT = ITEMS.register("broodfruit",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.1F)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0), 1.0F)
                            .build())));
    public static final RegistryObject<Item> RAW_BUG_MEAT = ITEMS.register("raw_bug_meat",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationMod(0.1F)
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)
                            .build())));
    public static final RegistryObject<Item> COOKED_BUG_MEAT = ITEMS.register("cooked_bug_meat",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.8F)
                            .build())));
    public static final RegistryObject<Item> COOKED_VENISON = ITEMS.register("cooked_venison",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationMod(0.8F)
                            .build())));
    public static final RegistryObject<Item> PEACH = ITEMS.register("peach",
            () -> new PeachItem());
    public static final RegistryObject<Item> PEACH_PIE = ITEMS.register("peach_pie",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.3F)
                            .build())));
    public static final RegistryObject<Item> CORNBREAD = ITEMS.register("cornbread",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.9F)
                            .build())));
    public static final RegistryObject<Item> POPCORN = ITEMS.register("popcorn",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationMod(0.1F)
                            .build())));
    public static final RegistryObject<Item> RAW_CORNDOG = ITEMS.register("raw_corndog",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(0.7F)
                            .build())));
    public static final RegistryObject<Item> COOKED_CORNDOG = ITEMS.register("cooked_corndog",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(14)
                            .saturationMod(1.1F)
                            .build())));
    public static final RegistryObject<Item> HIGH_FRUCTOSE_CORN_SYRUP = ITEMS.register("high_fructose_corn_syrup",
            () -> new HighFructoseCornSyrupItem(new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationMod(0.0F)
                            .alwaysEat()
                            .build())));
    public static final RegistryObject<RootBeerItem> ROOT_BEER = ITEMS.register("root_beer",
            () -> new RootBeerItem(new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationMod(0.4F)
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                            .alwaysEat()
                            .build())));
    public static final RegistryObject<Item> TRIFFID_GOO = ITEMS.registerSimpleItem("triffid_goo",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> VORTEX_EYE = ITEMS.register("vortex_eye",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> RAW_URANIUM_SCRAP = ITEMS.registerSimpleItem("raw_uranium_scrap", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> RAW_TITANIUM_SCRAP = ITEMS.registerSimpleItem("raw_titanium_scrap", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> MUD_PIE = ITEMS.register("mud_pie",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationMod(0.9F)
                            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F)
                            .build())));
    public static final RegistryObject<RainbowSugarItem> RAINBOW_SUGAR = ITEMS.register("rainbow_sugar",
            () -> new RainbowSugarItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.0F).alwaysEat().build())));
    public static final RegistryObject<Item> URANIUM_NUGGET = ITEMS.registerSimpleItem("uranium_nugget", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> TITANIUM_NUGGET = ITEMS.registerSimpleItem("titanium_nugget", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot", new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
    public static final RegistryObject<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot", new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
    public static final RegistryObject<Item> MOLEVORE_NOSE = ITEMS.registerSimpleItem("molevore_nose", new Item.Properties().rarity(Rarity.RARE));
    public static final RegistryObject<Item> MOLEWORM_ITEM = ITEMS.register("moleworm",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationMod(0.0F)
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)
                            .build())));
    public static final RegistryObject<Item> CLOUD_SHARK_FIN = ITEMS.registerSimpleItem("cloud_shark_fin", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<CloudSharkFinSoupItem> CLOUD_SHARK_FIN_SOUP = ITEMS.register("cloud_shark_fin_soup",
            () -> new CloudSharkFinSoupItem(new Item.Properties()
                    .stacksTo(1)
                    .craftRemainder(Items.BOWL)
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationMod(0.8F)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
                            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0), 1.0F)
                            .build())));
    public static final RegistryObject<Item> KRAKEN_TOOTH = ITEMS.register("kraken_tooth",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BASILISK_FANG = ITEMS.registerSimpleItem("basilisk_fang", new Item.Properties().rarity(Rarity.RARE));
    public static final RegistryObject<BasiliskDaggerItem> BASILISK_DAGGER = ITEMS.register("basilisk_dagger",
            () -> new BasiliskDaggerItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE), 4, -1.8F));
    public static final RegistryObject<Item> EMPEROR_SCORPION_STINGER = ITEMS.registerSimpleItem("emperor_scorpion_stinger", new Item.Properties().rarity(Rarity.RARE));
    public static final RegistryObject<ScorpionWhipItem> SCORPION_WHIP = ITEMS.register("scorpion_whip",
            () -> new ScorpionWhipItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE).durability(384)));
    public static final RegistryObject<MogglesItem> MOGGLES = ITEMS.register("moggles",
            () -> new MogglesItem(MOGGLES_ARMOR_MATERIAL, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<com.craisinlord.antarchy.content.item.FallenKingCrownItem> FALLEN_KING_CROWN = ITEMS.register("fallen_king_crown",
            () -> new com.craisinlord.antarchy.content.item.FallenKingCrownItem(
                    FALLEN_KING_CROWN_ARMOR_MATERIAL,
                    new Item.Properties().rarity(Rarity.RARE)
            ));
    public static final RegistryObject<ArmorItem> ULTIMATE_HELMET = ITEMS.register("ultimate_helmet",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.HELMET, 41))));
    public static final RegistryObject<ArmorItem> ULTIMATE_CHESTPLATE = ITEMS.register("ultimate_chestplate",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.CHESTPLATE, 41))));
    public static final RegistryObject<ArmorItem> ULTIMATE_LEGGINGS = ITEMS.register("ultimate_leggings",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.LEGGINGS, 41))));
    public static final RegistryObject<ArmorItem> ULTIMATE_BOOTS = ITEMS.register("ultimate_boots",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.BOOTS, 41))));
    public static final RegistryObject<UltimateSwordItem> ULTIMATE_SWORD = ITEMS.register("ultimate_sword",
            () -> new UltimateSwordItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateSwordAttackDamage, -2.4F));
    public static final RegistryObject<UltimatePickaxeItem> ULTIMATE_PICKAXE = ITEMS.register("ultimate_pickaxe",
            () -> new UltimatePickaxeItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimatePickaxeAttackDamage, -2.8F));
    public static final RegistryObject<UltimateAxeItem> ULTIMATE_AXE = ITEMS.register("ultimate_axe",
            () -> new UltimateAxeItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateAxeAttackDamage, -3.0F));
    public static final RegistryObject<UtlimateShovelItem> ULTIMATE_SHOVEL = ITEMS.register("ultimate_shovel",
            () -> new UtlimateShovelItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateShovelAttackDamage, -3.0F));
    public static final RegistryObject<UltimateHoeItem> ULTIMATE_HOE = ITEMS.register("ultimate_hoe",
            () -> new UltimateHoeItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateHoeAttackDamage, 0.0F));
    public static final RegistryObject<UltimateBowItem> ULTIMATE_BOW = ITEMS.register("ultimate_bow",
            () -> new UltimateBowItem(new Item.Properties().stacksTo(1).durability(768).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<UltimateCrossbowItem> ULTIMATE_CROSSBOW = ITEMS.register("ultimate_crossbow",
            () -> new UltimateCrossbowItem(new Item.Properties().stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<NightmareArmorItem> NIGHTMARE_HELMET = ITEMS.register("nightmare_helmet",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.HELMET, 41))));
    public static final RegistryObject<NightmareArmorItem> NIGHTMARE_CHESTPLATE = ITEMS.register("nightmare_chestplate",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.CHESTPLATE, 41))));
    public static final RegistryObject<NightmareArmorItem> NIGHTMARE_LEGGINGS = ITEMS.register("nightmare_leggings",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.LEGGINGS, 41))));
    public static final RegistryObject<NightmareArmorItem> NIGHTMARE_BOOTS = ITEMS.register("nightmare_boots",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(armorDurability(ArmorItem.Type.BOOTS, 41))));
    public static final RegistryObject<NightmareSwordItem> NIGHTMARE_SWORD = ITEMS.register("nightmare_sword",
            () -> new NightmareSwordItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), -2.4F));
    public static final RegistryObject<SizeRayItem> SHRINK_RAY = ITEMS.register("shrink_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyForgeEntites.SHRINK_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.SHRINK,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/shrink_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/shrink_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/shrink_ray.animation.json"),
                    "shrink_ray_active"
            ) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.sizeRay(this));
                }
            });
    public static final RegistryObject<SizeRayItem> GROWTH_RAY = ITEMS.register("growth_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyForgeEntites.GROWTH_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.GROWTH,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/growth_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/growth_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/growth_ray.animation.json"),
                    "growth_ray_active"
            ) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.sizeRay(this));
                }
            });
    public static final RegistryObject<GravityGunItem> GRAVITY_GUN = ITEMS.register("gravity_gun",
            () -> new GravityGunItem(new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant()) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.crossbowHold(this));
                }
            });
    // Temporarily disabled: keeping the code but not registering the item for now.
    // public static final RegistryObject<MinersDreamItem> MINERS_DREAM = ITEMS.register("miners_dream",
    //         () -> new MinersDreamItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<SquidzookaItem> SQUIDZOOKA = ITEMS.register("squidzooka",
            () -> new SquidzookaItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.crossbowHold(this));
                }
            });
    public static final RegistryObject<RpoLauncherItem> RPO_LAUNCHER = ITEMS.register("rpo_launcher",
            () -> new RpoLauncherItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.crossbowHold(this));
                }
            });
    public static final RegistryObject<BattleAxeItem> BATTLE_AXE = ITEMS.register("battle_axe",
            () -> new BattleAxeItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant(),
                    AntarchySettings::battleAxeAttackDamage, -3.1F) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.plain(this));
                }
            });
    public static final RegistryObject<AttitudeAdjusterItem> ATTITUDE_ADJUSTER = ITEMS.register("attitude_adjuster",
            () -> new AttitudeAdjusterItem(ATTITUDE_ADJUSTER_TIER,
                    new Item.Properties().stacksTo(1).durability(ATTITUDE_ADJUSTER_TIER.getUses()).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<DiamondMinecartItem> DIAMOND_MINECART_ITEM = ITEMS.register("diamond_minecart",
            () -> new DiamondMinecartItem(AntarchyForgeEntites.DIAMOND_MINECART, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<ReverieBottleItem> REVERIE_BOTTLE = ITEMS.register("reverie_bottle",
            () -> new ReverieBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BIG_BERTHA_BLADE = ITEMS.registerSimpleItem("big_bertha_blade",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final RegistryObject<Item> BIG_BERTHA_HANDLE = ITEMS.registerSimpleItem("big_bertha_handle",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final RegistryObject<Item> BIG_BERTHA_HILT = ITEMS.registerSimpleItem("big_bertha_hilt",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final RegistryObject<BigBerthaItem> BIG_BERTHA = ITEMS.register("big_bertha",
            () -> new BigBerthaItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.plain(this));
                }
            });

    public static final RegistryObject<Item> KRAKEN_TENTACLE = ITEMS.registerSimpleItem("kraken_tentacle",
            new Item.Properties().rarity(Rarity.RARE));

    public static final RegistryObject<KrakensGraspItem> KRAKENS_GRASP = ITEMS.register("krakens_grasp",
            () -> new KrakensGraspItem(
                    new Item.Properties().stacksTo(1).durability(250).rarity(Rarity.EPIC).fireResistant()));

    // Spawn eggs
    public static final RegistryObject<ForgeSpawnEggItem> EASTER_BUNNY_SPAWN_EGG = ITEMS.register("easter_bunny_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.EASTER_BUNNY, 0xFFF2B2, 0xFF85B5, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> DORRIE_SPAWN_EGG = ITEMS.register("dorrie_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.DORRIE, 0x6F8CFF, 0xD2F2FF, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> OURANWOOD_DEER_SPAWN_EGG = ITEMS.register("ouranwood_deer_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.OURANWOOD_DEER, 0x8A6D4B, 0xE8D9B5, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> GLIMMER_SPAWN_EGG = ITEMS.register("glimmer_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.GLIMMER, 0x7DFFFF, 0x2AC7D0, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> ELKA_SPAWN_EGG = ITEMS.register("elka_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.ELKA, 0x6B2FA0, 0xFFFFFF, new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.item.Item> SPIRIT_APPLE = ITEMS.register("spirit_apple",
            () -> new com.craisinlord.antarchy.content.item.SpiritAppleItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationMod(0.3F)
                            .build())));
    public static final RegistryObject<com.craisinlord.antarchy.content.item.GlimmerBottleItem> GLIMMER_BOTTLE = ITEMS.register("glimmer_bottle",
            com.craisinlord.antarchy.content.item.GlimmerBottleItem::new);
    public static final RegistryObject<ForgeSpawnEggItem> FLYING_SQUIRREL_SPAWN_EGG = ITEMS.register("flying_squirrel_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.FLYING_SQUIRREL, 0x7D6649, 0xDCC59C, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> CATERPILLAR_SPAWN_EGG = ITEMS.register("caterpillar_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.CATERPILLAR, 0xA8D96A, 0xF4E04D, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.BUTTERFLY, 0x7A4A1E, 0xFF7A00, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> REVERIE_SPAWN_EGG = ITEMS.register("reverie_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.REVERIE, 0xF2F2F2, 0xBFC3C7, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> BRUTALFLY_SPAWN_EGG = ITEMS.register("brutalfly_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.BRUTALFLY, 0x4A2214, 0xFF8A1D, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<ForgeSpawnEggItem> RED_ANT_SPAWN_EGG = ITEMS.register("red_ant_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.RED_ANT, 0xA31818, 0x2B0909, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> BROWN_ANT_SPAWN_EGG = ITEMS.register("brown_ant_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.BROWN_ANT, 0x6A4320, 0x26160A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> RAINBOW_ANT_SPAWN_EGG = ITEMS.register("rainbow_ant_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.RAINBOW_ANT, 0x56D4F0, 0xF66DBB, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> TERMITE_SPAWN_EGG = ITEMS.register("termite_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.TERMITE, 0xD4B96A, 0xFF6B1A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> MOLEWORM_SPAWN_EGG = ITEMS.register("moleworm_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.MOLEWORM, 0xB8B8B8, 0x8A623A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> MANTIS_SPAWN_EGG = ITEMS.register("mantis_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.MANTIS, 0xF8F8F2, 0x63B44A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> ALPHA_MANTIS_SPAWN_EGG = ITEMS.register("alpha_mantis_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.ALPHA_MANTIS, 0x8FDD6C, 0x2F5D22, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ForgeSpawnEggItem> ROLLY_POLLY_SPAWN_EGG = ITEMS.register("rolly_polly_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.ROLLY_POLLY, 0x7284A3, 0xBCC8DB, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> MOLEVORE_SPAWN_EGG = ITEMS.register("molevore_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.MOLEVORE, 0x4A4A4A, 0x6B4A2B, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ForgeSpawnEggItem> TRIFFID_SPAWN_EGG = ITEMS.register("triffid_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.TRIFFID, 0x4C8F3A, 0xFF2FB3, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ForgeSpawnEggItem> APPLE_COW_SPAWN_EGG = ITEMS.register("apple_cow_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.APPLE_COW, 0xFF1A1A, 0x32FF32, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("golden_apple_cow_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.GOLDEN_APPLE_COW, 0xFFE14A, 0x32FF32, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("enchanted_golden_apple_cow_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.ENCHANTED_GOLDEN_APPLE_COW, 0x7040B6, 0xFFE14A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> DR_TRAYAURUS_SPAWN_EGG = ITEMS.register("dr_trayaurus_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.DR_TRAYAURUS, 0xB7A27B, 0x4A3D29, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> WASP_SPAWN_EGG = ITEMS.register("wasp_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.WASP, 0x111111, 0xF1D800, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> BOMBER_SPAWN_EGG = ITEMS.register("bomber_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.BOMBER, 0x7A7A7A, 0xB32020, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> JUMPY_BUG_SPAWN_EGG = ITEMS.register("jumpy_bug_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.JUMPY_BUG, 0x0A1636, 0x8A3E00, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> SPIT_BUG_SPAWN_EGG = ITEMS.register("spit_bug_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.SPIT_BUG, 0x6B4A2B, 0x7ED957, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> STINK_BUG_SPAWN_EGG = ITEMS.register("stink_bug_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.STINK_BUG, 0x111111, 0xFF7A00, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> CLOUD_SHARK_SPAWN_EGG = ITEMS.register("cloud_shark_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.CLOUD_SHARK, 0xDDEAF4, 0x7F96A8, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.KRAKEN, 0x163C53, 0x4F8E99, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<ForgeSpawnEggItem> MISSILE_SQUID_SPAWN_EGG = ITEMS.register("missile_squid_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.MISSILE_SQUID, 0xD88FA7, 0x8D5269, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ForgeSpawnEggItem> OCTOPUS_BOMB_SPAWN_EGG = ITEMS.register("octopus_bomb_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.OCTOPUS_BOMB, 0xC882C8, 0x5C1A7A, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ForgeSpawnEggItem> NIGHTMARE_SPAWN_EGG = ITEMS.register("nightmare_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.NIGHTMARE, 0x22121C, 0xB51B2D, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<ForgeSpawnEggItem> BED_BUG_SPAWN_EGG = ITEMS.register("bed_bug_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.BED_BUG, 0x3B2218, 0x611111, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> LUCID_SPAWN_EGG = ITEMS.register("lucid_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.LUCID, 0xE53935, 0xF4D03F, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<ForgeSpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.SCORPION, 0xA8D8FF, 0xE04B5A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.register("basilisk_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.BASILISK, 0x4A7C40, 0xD4A040, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> EMPEROR_SCORPION_SPAWN_EGG = ITEMS.register("emperor_scorpion_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.EMPEROR_SCORPION, 0x3A3242, 0xD8CDB4, new Item.Properties()));
    public static final RegistryObject<LucidEyeItem> LUCID_EYE = ITEMS.register("lucid_eye",
            () -> new LucidEyeItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<LucidPearlItem> LUCID_PEARL = ITEMS.register("lucid_pearl",
            () -> new LucidPearlItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
                    AntarchyForgeEntites.LUCID_PEARL_PROJECTILE
            ));
    public static final RegistryObject<WaterCannonItem> WATER_CANNON = ITEMS.register("water_cannon",
            () -> new WaterCannonItem(new Item.Properties().stacksTo(1).durability(192).rarity(Rarity.RARE)) {
                @Override
                public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
                    consumer.accept(AntarchyGeoItemExtensions.crossbowHold(this));
                }
            });
    public static final RegistryObject<PrimordialArmorItem> PRIMORDIAL_HELMET = ITEMS.register("primordial_helmet",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<PrimordialArmorItem> PRIMORDIAL_CHESTPLATE = ITEMS.register("primordial_chestplate",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<PrimordialArmorItem> PRIMORDIAL_LEGGINGS = ITEMS.register("primordial_leggings",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<PrimordialArmorItem> PRIMORDIAL_BOOTS = ITEMS.register("primordial_boots",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<ForgeSpawnEggItem> CREEPING_HORROR_SPAWN_EGG = ITEMS.register("creeping_horror_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.CREEPING_HORROR, 0x6B3A1F, 0x6B0000, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> LURKING_TERROR_SPAWN_EGG = ITEMS.register("lurking_terror_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.LURKING_TERROR, 0x2D5A1B, 0x8B0000, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> HERCULES_BEETLE_SPAWN_EGG = ITEMS.register("hercules_beetle_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.HERCULES_BEETLE, 0x6B1F2A, 0xD4AF37, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<ForgeSpawnEggItem> JERRY_SPAWN_EGG = ITEMS.register("jerry_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.JERRY, 0x6D2232, 0x8ED34A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> TORETERROR_SPAWN_EGG = ITEMS.register("toreterror_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.TORETERROR, 0x90EE90, 0x5C4033, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<ForgeSpawnEggItem> CHEEP_SPAWN_EGG = ITEMS.register("cheep_spawn_egg",
            () -> new ForgeSpawnEggItem(AntarchyForgeEntites.CHEEP, 0xAA22FF, 0x22FF44, new Item.Properties()));
    public static final RegistryObject<Item> CHEEP_ITEM = ITEMS.register("cheep",
            () -> new Item(new Item.Properties()
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(2)
                            .saturationMod(0.1F)
                            .build())));
    private AntarchyForgeItems() {}

    public static Collection<RegistryObject<Item>> getItemEntries() {
        return ITEMS.getEntries();
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    static Item cloudBucketItem() {
        return CLOUD_BUCKET.get();
    }

    static Item diamondMinecartItem() {
        return DIAMOND_MINECART_ITEM.get();
    }

    static BlockEntityType<PotentNyxiteBlockEntity> potentNyxiteBlockEntityType() {
        return AntarchyForgeBlocks.POTENT_NYXITE_BLOCK_ENTITY.get();
    }

    static BlockEntityType<AntNestBlockEntity> antNestBlockEntityType() {
        return AntarchyForgeBlocks.ANT_NEST_BLOCK_ENTITY.get();
    }

    static BlockEntityType<WaspNestBlockEntity> waspNestBlockEntityType() {
        return AntarchyForgeBlocks.WASP_NEST_BLOCK_ENTITY.get();
    }

    static Ingredient ultimateRepairIngredient() {
        return Ingredient.of(TITANIUM_INGOT.get());
    }

    static Ingredient bloodCrystalRepairIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal_shard"))
                .orElse(Items.AIR));
    }

    private static EnumMap<ArmorItem.Type, Integer> createUltimateArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 6);
        defense.put(ArmorItem.Type.LEGGINGS, 12);
        defense.put(ArmorItem.Type.CHESTPLATE, 16);
        defense.put(ArmorItem.Type.HELMET, 6);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createNightmareArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 4);
        defense.put(ArmorItem.Type.LEGGINGS, 9);
        defense.put(ArmorItem.Type.CHESTPLATE, 11);
        defense.put(ArmorItem.Type.HELMET, 4);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createBloodCrystalArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 2);
        defense.put(ArmorItem.Type.LEGGINGS, 5);
        defense.put(ArmorItem.Type.CHESTPLATE, 6);
        defense.put(ArmorItem.Type.HELMET, 2);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createMogglesArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 2);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createPrimordialArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createFallenKingCrownDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 2);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createJumpyBootsDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 0);
        return defense;
    }
}
