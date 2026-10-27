package com.craisinlord.antarchy.neoforge.registry;

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
import com.craisinlord.antarchy.content.item.SpringyBootsItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import com.craisinlord.antarchy.content.item.LucidEyeItem;
import com.craisinlord.antarchy.content.item.LucidPearlItem;
import com.craisinlord.antarchy.content.item.OuranwoodBoatOnlyItem;
import com.craisinlord.antarchy.content.item.OuranwoodChestBoatItem;
import com.craisinlord.antarchy.content.item.RainbowSugarItem;
import com.google.common.base.Suppliers;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.sounds.SoundEvents;

import java.util.Collection;
import java.util.EnumMap;
import java.util.function.Supplier;

public final class AntarchyNeoforgeItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Antarchy.MODID);

    public static final ToolMaterial ULTIMATE_TIER = SimpleToolTier.of(
            3072,
            10.5F,
            0.0F,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            AntarchySettings.ultimateToolEnchantability(),
            AntarchyRepairTags.ULTIMATE
    );
    public static final ToolMaterial BLOOD_CRYSTAL_KATANA_TIER = SimpleToolTier.of(
            1200,
            8.0F,
            0.0F,
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            18,
            AntarchyRepairTags.BLOOD_CRYSTAL
    );
    public static final ToolMaterial ATTITUDE_ADJUSTER_TIER = SimpleToolTier.of(
            3072,
            7.0F,
            0.0F,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            18,
            AntarchyRepairTags.ATTITUDE_ADJUSTER
    );

    public static final ToolMaterial ROYAL_GUARDIAN_TIER = SimpleToolTier.of(
            AntarchySettings.royalWeaponDurability(),
            12.0F,
            0.0F,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            AntarchySettings.royalWeaponEnchantability(),
            AntarchyRepairTags.ROYAL_GUARDIAN
    );

    public static final ToolMaterial ROYAL_ASSAILANT_TIER = SimpleToolTier.of(
            AntarchySettings.royalWeaponDurability(),
            12.0F,
            0.0F,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            AntarchySettings.royalWeaponEnchantability(),
            AntarchyRepairTags.ROYAL_ASSAILANT
    );

    public static final DeferredItem<Item> NIGHTMARE_SCALE = ITEMS.registerSimpleItem("nightmare_scale", props -> props.rarity(Rarity.RARE).fireResistant().trimMaterial(com.craisinlord.antarchy.content.item.AntarchyTrimMaterials.NIGHTMARE_SCALE));

    public static final DeferredItem<Item> MUSIC_DISC_UNDERSIDE = ITEMS.registerSimpleItem("music_disc_underside",
            props -> props.stacksTo(1).rarity(Rarity.RARE)
                    .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG,
                            Identifier.fromNamespaceAndPath(Antarchy.MODID, "underside"))));

    public static final DeferredItem<Item> MUSIC_DISC_THE_QUEEN = ITEMS.registerSimpleItem("music_disc_the_queen",
            props -> props.stacksTo(1).rarity(Rarity.RARE)
                    .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG,
                            Identifier.fromNamespaceAndPath(Antarchy.MODID, "the_queen"))));

    public static final DeferredItem<Item> MUSIC_DISC_VERDANCE = ITEMS.registerSimpleItem("music_disc_verdance",
            props -> props.stacksTo(1).rarity(Rarity.RARE)
                    .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG,
                            Identifier.fromNamespaceAndPath(Antarchy.MODID, "verdance"))));

    public static final DeferredItem<Item> MUSIC_DISC_GROTESQUE = ITEMS.registerSimpleItem("music_disc_grotesque",
            props -> props.stacksTo(1).rarity(Rarity.RARE)
                    .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG,
                            Identifier.fromNamespaceAndPath(Antarchy.MODID, "grotesque"))));

    private static final Supplier<ArmorMaterial> ULTIMATE_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            62,
            createUltimateArmorDefense(),
            10,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F,
            (float) AntarchySettings.ultimateArmorKnockbackResistance(),
            AntarchyRepairTags.ULTIMATE,
            equipmentAsset("ultimate_armor")
    ));
    private static final Supplier<ArmorMaterial> NIGHTMARE_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            33,
            createNightmareArmorDefense(),
            10,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,
            (float) AntarchySettings.nightmareArmorKnockbackResistance(),
            AntarchyRepairTags.NIGHTMARE,
            equipmentAsset("nightmare_armor")
    ));
    private static final Supplier<ArmorMaterial> ROYAL_GUARDIAN_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            AntarchySettings.royalArmorDurabilityMultiplier(),
            new EnumMap<>(ArmorType.class),
            AntarchySettings.royalArmorEnchantability(),
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            0.0F,
            0.0F,
            AntarchyRepairTags.ROYAL_GUARDIAN,
            equipmentAsset("royal_guardian")
    ));
    private static final Supplier<ArmorMaterial> ROYAL_ASSAILANT_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            AntarchySettings.royalArmorDurabilityMultiplier(),
            new EnumMap<>(ArmorType.class),
            AntarchySettings.royalArmorEnchantability(),
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            0.0F,
            0.0F,
            AntarchyRepairTags.ROYAL_ASSAILANT,
            equipmentAsset("royal_assailant")
    ));
    private static final Supplier<ArmorMaterial> BLOOD_CRYSTAL_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            8,
            createBloodCrystalArmorDefense(),
            30,
            AntarchyNeoforgeSounds.BLOOD_CRYSTAL_ARMOR_EQUIP,
            0.0F,
            0.0F,
            AntarchyRepairTags.BLOOD_CRYSTAL,
            equipmentAsset("blood_crystal")
    ));
    private static final Supplier<ArmorMaterial> TIGERS_EYE_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            33,
            createDiamondArmorDefense(),
            18,
            SoundEvents.ARMOR_EQUIP_GOLD,
            2.0F,
            0.0F,
            AntarchyRepairTags.TIGERS_EYE,
            equipmentAsset("tigers_eye")
    ));
    private static final Supplier<ArmorMaterial> MOGGLES_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            0,
            createMogglesArmorDefense(),
            12,
            SoundEvents.ARMOR_EQUIP_GOLD,
            0.0F,
            0.0F,
            AntarchyRepairTags.GOLD,
            equipmentAsset("moggles")
    ));
    public static final DeferredItem<Item> PRIMORDIAL_SCUTE = ITEMS.registerSimpleItem("primordial_scute",
            props -> props.rarity(Rarity.UNCOMMON));
    private static final Supplier<ArmorMaterial> PRIMORDIAL_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            33,
            createPrimordialArmorDefense(),
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.5F,
            0.1F,
            AntarchyRepairTags.PRIMORDIAL,
            equipmentAsset("primordial")
    ));
    private static final Supplier<ArmorMaterial> SPRINGY_BOOTS_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            37,
            createSpringyBootsDefense(),
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,
            0.1F,
            AntarchyRepairTags.SPRINGY_BOOTS,
            equipmentAsset("springy_boots")
    ));
    private static final Supplier<ArmorMaterial> FALLEN_KING_CROWN_ARMOR_MATERIAL = Suppliers.memoize(() -> new ArmorMaterial(
            7,
            createFallenKingCrownDefense(),
            25,
            SoundEvents.ARMOR_EQUIP_GOLD,
            0.0F,
            0.0F,
            AntarchyRepairTags.GOLD,
            equipmentAsset("fallen_king_crown")
    ));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DUPLICATOR_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_OURANWOOD_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_OURANWOOD_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_PLANKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_FENCE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_FENCE_GATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_PRESSURE_PLATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_BUTTON);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_LEAVES);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_ACORN = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.OURANWOOD_ACORN_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_PEACH_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_PEACH_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_PEACH_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_PEACH_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_PLANKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_FENCE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_FENCE_GATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_PRESSURE_PLATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_BUTTON);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_LEAVES);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PEACH_SAPLING);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_NADIR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_NADIR_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_NADIR_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_NADIR_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_PLANKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_FENCE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_FENCE_GATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_PRESSURE_PLATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_BUTTON);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_VEIL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_VEIL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_SAPLING);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ORANGE_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ORANGE_MILKWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PINK_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PINK_MILKWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GLIMMERING_REED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.GLIMMERING_REED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CAMELLIA_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CAMELLIA);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SPIDER_LILY_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SPIDER_LILY);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GIANT_LILY_PAD_ITEM = ITEMS.register("giant_lily_pad",
            id -> new com.craisinlord.antarchy.content.item.GiantLilyPadItem(AntarchyNeoforgeBlocks.GIANT_LILY_PAD.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SEASHELL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SEASHELL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LUCID_ANCHOR_ITEM = ITEMS.register("lucid_anchor",
            id -> new LucidAnchorBlockItem(AntarchyNeoforgeBlocks.LUCID_ANCHOR.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UNDERTRIAL_SPAWNER_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UNDERTRIAL_SPAWNER);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UNDERVAULT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UNDERVAULT);
    public static final DeferredItem<Item> UNDERTRIAL_KEY = ITEMS.register("undertrial_key", id -> new Item(itemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOTUS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LOTUS);
    public static final DeferredItem<net.minecraft.world.item.SignItem> OURANWOOD_SIGN_ITEM = ITEMS.register("ouranwood_sign",
            id -> new SignItem(AntarchyNeoforgeBlocks.OURANWOOD_SIGN.get(), AntarchyNeoforgeBlocks.OURANWOOD_WALL_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> OURANWOOD_HANGING_SIGN_ITEM = ITEMS.register("ouranwood_hanging_sign",
            id -> new HangingSignItem(AntarchyNeoforgeBlocks.OURANWOOD_HANGING_SIGN.get(), AntarchyNeoforgeBlocks.OURANWOOD_WALL_HANGING_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<Item> OURANWOOD_BOAT = ITEMS.register("ouranwood_boat",
            id -> new OuranwoodBoatOnlyItem(AntarchyNeoforgeEntites.OURANWOOD_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> OURANWOOD_CHEST_BOAT = ITEMS.register("ouranwood_chest_boat",
            id -> new OuranwoodChestBoatItem(AntarchyNeoforgeEntites.OURANWOOD_CHEST_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD = ITEMS.register("hoverboard",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), null, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_WHITE = ITEMS.register("hoverboard_white",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.WHITE, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_ORANGE = ITEMS.register("hoverboard_orange",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.ORANGE, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_MAGENTA = ITEMS.register("hoverboard_magenta",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.MAGENTA, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_LIGHT_BLUE = ITEMS.register("hoverboard_light_blue",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.LIGHT_BLUE, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_YELLOW = ITEMS.register("hoverboard_yellow",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.YELLOW, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_LIME = ITEMS.register("hoverboard_lime",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.LIME, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_PINK = ITEMS.register("hoverboard_pink",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.PINK, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_GRAY = ITEMS.register("hoverboard_gray",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.GRAY, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_LIGHT_GRAY = ITEMS.register("hoverboard_light_gray",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.LIGHT_GRAY, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_CYAN = ITEMS.register("hoverboard_cyan",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.CYAN, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_PURPLE = ITEMS.register("hoverboard_purple",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.PURPLE, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_BLUE = ITEMS.register("hoverboard_blue",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.BLUE, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_BROWN = ITEMS.register("hoverboard_brown",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.BROWN, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_GREEN = ITEMS.register("hoverboard_green",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.GREEN, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_RED = ITEMS.register("hoverboard_red",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.RED, itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> HOVERBOARD_BLACK = ITEMS.register("hoverboard_black",
            id -> new HoverboardItem(AntarchyNeoforgeEntites.HOVERBOARD.get(), DyeColor.BLACK, itemProps(id).stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.SignItem> PEACH_SIGN_ITEM = ITEMS.register("peach_sign",
            id -> new SignItem(AntarchyNeoforgeBlocks.PEACH_SIGN.get(), AntarchyNeoforgeBlocks.PEACH_WALL_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> PEACH_HANGING_SIGN_ITEM = ITEMS.register("peach_hanging_sign",
            id -> new HangingSignItem(AntarchyNeoforgeBlocks.PEACH_HANGING_SIGN.get(), AntarchyNeoforgeBlocks.PEACH_WALL_HANGING_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<Item> PEACH_BOAT = ITEMS.register("peach_boat",
            id -> new PeachBoatOnlyItem(AntarchyNeoforgeEntites.PEACH_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> PEACH_CHEST_BOAT = ITEMS.register("peach_chest_boat",
            id -> new PeachChestBoatItem(AntarchyNeoforgeEntites.PEACH_CHEST_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.SignItem> NADIR_SIGN_ITEM = ITEMS.register("nadir_sign",
            id -> new SignItem(AntarchyNeoforgeBlocks.NADIR_SIGN.get(), AntarchyNeoforgeBlocks.NADIR_WALL_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> NADIR_HANGING_SIGN_ITEM = ITEMS.register("nadir_hanging_sign",
            id -> new HangingSignItem(AntarchyNeoforgeBlocks.NADIR_HANGING_SIGN.get(), AntarchyNeoforgeBlocks.NADIR_WALL_HANGING_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<Item> NADIR_BOAT = ITEMS.register("nadir_boat",
            id -> new NadirBoatOnlyItem(AntarchyNeoforgeEntites.NADIR_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> NADIR_CHEST_BOAT = ITEMS.register("nadir_chest_boat",
            id -> new NadirChestBoatItem(AntarchyNeoforgeEntites.NADIR_CHEST_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_ROYAL_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_ROYAL_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_ROYAL_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_ROYAL_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_PLANKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_FENCE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_FENCE_GATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_PRESSURE_PLATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_BUTTON);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_LEAVES);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_FLOWERING_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_FLOWERING_LEAVES);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROYAL_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROYAL_SAPLING);
    public static final DeferredItem<net.minecraft.world.item.SignItem> ROYAL_SIGN_ITEM = ITEMS.register("royal_sign",
            id -> new SignItem(AntarchyNeoforgeBlocks.ROYAL_SIGN.get(), AntarchyNeoforgeBlocks.ROYAL_WALL_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> ROYAL_HANGING_SIGN_ITEM = ITEMS.register("royal_hanging_sign",
            id -> new HangingSignItem(AntarchyNeoforgeBlocks.ROYAL_HANGING_SIGN.get(), AntarchyNeoforgeBlocks.ROYAL_WALL_HANGING_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<Item> ROYAL_BOAT = ITEMS.register("royal_boat",
            id -> new RoyalBoatOnlyItem(AntarchyNeoforgeEntites.ROYAL_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> ROYAL_CHEST_BOAT = ITEMS.register("royal_chest_boat",
            id -> new RoyalChestBoatItem(AntarchyNeoforgeEntites.ROYAL_CHEST_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<BlockItem> TRUFFALO_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_LOG);
    public static final DeferredItem<BlockItem> TRUFFALO_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_WOOD);
    public static final DeferredItem<BlockItem> STRIPPED_TRUFFALO_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_TRUFFALO_LOG);
    public static final DeferredItem<BlockItem> STRIPPED_TRUFFALO_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STRIPPED_TRUFFALO_WOOD);
    public static final DeferredItem<BlockItem> TRUFFALO_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_PLANKS);
    public static final DeferredItem<BlockItem> TRUFFALO_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_STAIRS);
    public static final DeferredItem<BlockItem> TRUFFALO_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_SLAB);
    public static final DeferredItem<BlockItem> TRUFFALO_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_FENCE);
    public static final DeferredItem<BlockItem> TRUFFALO_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_FENCE_GATE);
    public static final DeferredItem<BlockItem> TRUFFALO_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_DOOR);
    public static final DeferredItem<BlockItem> TRUFFALO_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TRAPDOOR);
    public static final DeferredItem<BlockItem> TRUFFALO_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> TRUFFALO_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_BUTTON);
    public static final DeferredItem<BlockItem> TRUFFALO_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_SAPLING);
    public static final DeferredItem<SignItem> TRUFFALO_SIGN_ITEM = ITEMS.register("truffalo_sign",
            id -> new SignItem(AntarchyNeoforgeBlocks.TRUFFALO_SIGN.get(), AntarchyNeoforgeBlocks.TRUFFALO_WALL_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<HangingSignItem> TRUFFALO_HANGING_SIGN_ITEM = ITEMS.register("truffalo_hanging_sign",
            id -> new HangingSignItem(AntarchyNeoforgeBlocks.TRUFFALO_HANGING_SIGN.get(), AntarchyNeoforgeBlocks.TRUFFALO_WALL_HANGING_SIGN.get(), blockItemProps(id).stacksTo(16)));
    public static final DeferredItem<Item> TRUFFALO_BOAT = ITEMS.register("truffalo_boat",
            id -> new TruffaloBoatOnlyItem(AntarchyNeoforgeEntites.TRUFFALO_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<Item> TRUFFALO_CHEST_BOAT = ITEMS.register("truffalo_chest_boat",
            id -> new TruffaloChestBoatItem(AntarchyNeoforgeEntites.TRUFFALO_CHEST_BOAT_ENTITY.get(), itemProps(id).stacksTo(1)));
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_WHITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_WHITE);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_ORANGE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_ORANGE);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_MAGENTA_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_MAGENTA);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_LIGHT_BLUE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_LIGHT_BLUE);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_YELLOW_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_YELLOW);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_LIME_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_LIME);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_PINK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_PINK);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_GRAY_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_GRAY);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_LIGHT_GRAY_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_LIGHT_GRAY);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_CYAN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_CYAN);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_PURPLE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_PURPLE);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_BLUE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_BLUE);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_BROWN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_BROWN);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_GREEN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_GREEN);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_RED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_RED);
    public static final DeferredItem<BlockItem> TRUFFALO_TUFT_BLACK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TRUFFALO_TUFT_BLACK);
    public static final DeferredItem<CritterCageItem> CRITTER_CAGE = ITEMS.register("critter_cage",
            id -> new CritterCageItem(itemProps(id).stacksTo(16)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DUPLICATOR_SAPLING);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RED_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.RED_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROWN_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROWN_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAINBOW_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.RAINBOW_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TERMITE_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TERMITE_NEST);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANT_TRAP_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ANT_TRAP);

    public static final DeferredItem<Item> BUG_SPRAY = ITEMS.register("bug_spray",
            id -> new com.craisinlord.antarchy.content.item.BugSprayItem(itemProps(id).durability(128)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_ORE_ITEM = ITEMS.register("uranium_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.URANIUM_ORE.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_URANIUM_ORE_ITEM = ITEMS.register("deepslate_uranium_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.DEEPSLATE_URANIUM_ORE.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_ORE_ITEM = ITEMS.register("titanium_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.TITANIUM_ORE.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_TITANIUM_ORE_ITEM = ITEMS.register("deepslate_titanium_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.DEEPSLATE_TITANIUM_ORE.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> BLUESTONE_DUST = ITEMS.register("bluestone",
            id -> new BluestoneDustItem(AntarchyNeoforgeBlocks.BLUESTONE_WIRE.get(), itemProps(id).trimMaterial(com.craisinlord.antarchy.content.item.AntarchyTrimMaterials.BLUESTONE)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUESTONE_ORE_ITEM = ITEMS.register("bluestone_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.BLUESTONE_ORE.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUESTONE_BLOCK_ITEM = ITEMS.register("bluestone_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.BLUESTONE_BLOCK.get(), blockItemProps(id)));
    public static final DeferredItem<VortexLensBlockItem> VORTEX_LENS_ITEM = ITEMS.register("vortex_lens",
            id -> new VortexLensBlockItem(AntarchyNeoforgeBlocks.VORTEX_LENS.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> BLUESTONE_REPEATER_ITEM = ITEMS.register("bluestone_repeater",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.BLUESTONE_REPEATER.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> BLUESTONE_COMPARATOR_ITEM = ITEMS.register("bluestone_comparator",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.BLUESTONE_COMPARATOR.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> BLUESTONE_TORCH_ITEM = ITEMS.register("bluestone_torch",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.BLUESTONE_TORCH.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUESTONE_LAMP_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BLUESTONE_LAMP);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TIGERS_EYE_BLOCK_ITEM = ITEMS.register("tigers_eye_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.TIGERS_EYE_BLOCK.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BLOCK_ITEM = ITEMS.register("uranium_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.URANIUM_BLOCK.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BLOCK_ITEM = ITEMS.register("titanium_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.TITANIUM_BLOCK.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_URANIUM_BLOCK_ITEM = ITEMS.register("raw_uranium_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.RAW_URANIUM_BLOCK.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_TITANIUM_BLOCK_ITEM = ITEMS.register("raw_titanium_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.RAW_TITANIUM_BLOCK.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_ITEM = ITEMS.register("cut_uranium",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CUT_URANIUM.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_ITEM = ITEMS.register("cut_titanium",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CUT_TITANIUM.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_SLAB_ITEM = ITEMS.register("cut_uranium_slab",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CUT_URANIUM_SLAB.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_SLAB_ITEM = ITEMS.register("cut_titanium_slab",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CUT_TITANIUM_SLAB.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_STAIRS_ITEM = ITEMS.register("cut_uranium_stairs",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CUT_URANIUM_STAIRS.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_STAIRS_ITEM = ITEMS.register("cut_titanium_stairs",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CUT_TITANIUM_STAIRS.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_URANIUM_ITEM = ITEMS.register("chiseled_uranium",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CHISELED_URANIUM.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_TITANIUM_ITEM = ITEMS.register("chiseled_titanium",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.CHISELED_TITANIUM.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BULB_ITEM = ITEMS.register("uranium_bulb",
            id -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyNeoforgeBlocks.URANIUM_BULB.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BULB_ITEM = ITEMS.register("titanium_bulb",
            id -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyNeoforgeBlocks.TITANIUM_BULB.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_DOOR_ITEM = ITEMS.register("uranium_door",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.URANIUM_DOOR.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_DOOR_ITEM = ITEMS.register("titanium_door",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.TITANIUM_DOOR.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_TRAPDOOR_ITEM = ITEMS.register("uranium_trapdoor",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.URANIUM_TRAPDOOR.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_TRAPDOOR_ITEM = ITEMS.register("titanium_trapdoor",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.TITANIUM_TRAPDOOR.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BARS_ITEM = ITEMS.register("uranium_bars",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.URANIUM_BARS.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BARS_ITEM = ITEMS.register("titanium_bars",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.TITANIUM_BARS.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_ITEM = ITEMS.register("antimetal",
            id -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_ITEM = ITEMS.register("polished_antimetal",
            id -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.POLISHED_ANTIMETAL.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_STAIRS_ITEM = ITEMS.register("antimetal_stairs",
            id -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL_STAIRS.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_SLAB_ITEM = ITEMS.register("antimetal_slab",
            id -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL_SLAB.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_STAIRS_ITEM = ITEMS.register("polished_antimetal_stairs",
            id -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.POLISHED_ANTIMETAL_STAIRS.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_SLAB_ITEM = ITEMS.register("polished_antimetal_slab",
            id -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.POLISHED_ANTIMETAL_SLAB.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_SCAFFOLDING_ITEM = ITEMS.register("antimetal_scaffolding",
            id -> new AntimetalScaffoldingItem(AntarchyNeoforgeBlocks.ANTIMETAL_SCAFFOLDING.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_RAIL_ITEM = ITEMS.register("antimetal_rail",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL_RAIL.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_POWERED_RAIL_ITEM = ITEMS.register("antimetal_powered_rail",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL_POWERED_RAIL.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_DETECTOR_RAIL_ITEM = ITEMS.register("antimetal_detector_rail",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL_DETECTOR_RAIL.get(), blockItemProps(id)));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_ACTIVATOR_RAIL_ITEM = ITEMS.register("antimetal_activator_rail",
            id -> new CeilingPlacementTooltipBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL_ACTIVATOR_RAIL.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UPPER_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UPPER);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMALL_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SMALL_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MEDIUM_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LARGE_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LARGE_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BUDDING_BLOOD_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BUDDING_BLOOD_CRYSTAL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_ITEM = ITEMS.register("blood_crystal_block",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.BLOOD_CRYSTAL.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BLOOD_CRYSTAL_CRYSTAL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SAND_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_SAND);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIGRAVEL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ANTIGRAVEL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOAM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LOAM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PACKED_LOAM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PACKED_LOAM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOAM_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LOAM_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOAM_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LOAM_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOAM_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LOAM_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOAM_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LOAM_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_LOAM_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_LOAM_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MUCUS_ITEM = ITEMS.register("mucus",
            id -> new com.craisinlord.antarchy.content.item.MucusBlockItem(AntarchyNeoforgeBlocks.MUCUS.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SMOOTH_DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_SANDSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_SANDSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_SANDSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SMOOTH_DREAM_SANDSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SMOOTH_DREAM_SANDSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_DREAM_SANDSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_NYXIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_NYXIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> VERDANT_NYXIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.VERDANT_NYXIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_PILLAR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_TYPHONITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_TYPHONITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_TYPHONITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_TYPHONITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> VEINED_TYPHONITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.VEINED_TYPHONITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_SPIKE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TYPHONITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TYPHONITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_TYPHONITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_TYPHONITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_TYPHONITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_TYPHONITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_TYPHONITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_TYPHONITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SPIRALING_VINES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SPIRALING_VINES);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GOREVINE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.GOREVINE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WHIRLFLOWER_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.WHIRLFLOWER);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_FERN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_FERN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LARGE_NADIR_FERN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LARGE_NADIR_FERN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUSKBELL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DUSKBELL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_ROOTS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NADIR_ROOTS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> VERDANT_ROOTS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.VERDANT_ROOTS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEAD_STAR_CORAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DEAD_STAR_CORAL_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEAD_STAR_CORAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DEAD_STAR_CORAL);
    public static final DeferredItem<StandingAndWallBlockItem> DEAD_STAR_CORAL_FAN_ITEM = ITEMS.register("dead_star_coral_fan",
            id -> new StandingAndWallBlockItem(AntarchyNeoforgeBlocks.DEAD_STAR_CORAL_FAN.get(), AntarchyNeoforgeBlocks.DEAD_STAR_CORAL_WALL_FAN.get(), Direction.UP, blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STAR_CORAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STAR_CORAL_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STAR_CORAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.STAR_CORAL);
    public static final DeferredItem<StandingAndWallBlockItem> STAR_CORAL_FAN_ITEM = ITEMS.register("star_coral_fan",
            id -> new StandingAndWallBlockItem(AntarchyNeoforgeBlocks.STAR_CORAL_FAN.get(), AntarchyNeoforgeBlocks.STAR_CORAL_WALL_FAN.get(), Direction.UP, blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUCT_TAPE_ITEM = ITEMS.register("duct_tape",
            id -> new DuctTapeBlockItem(AntarchyNeoforgeBlocks.DUCT_TAPE.get(), blockItemProps(id).stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_ROOTED_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.INFESTED_ROOTED_DIRT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_COARSE_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.INFESTED_COARSE_DIRT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_MYRMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_MYRMITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_MYRMITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_MYRMITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_MYRMITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_MYRMITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_MYRMITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_MYRMITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_MYRMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_MYRMITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_MYRMITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_MYRMITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_MYRMITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_MYRMITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_MYRMITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_MYRMITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_MYRMITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_MYRMITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_PILLAR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BIOMITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOMITE_TURF_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BIOMITE_TURF);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOWART_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BIOWART);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOWART_TENDRILS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BIOWART_TENDRILS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_BROODSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_BROODSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_BROODSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_BROODSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_BROODSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_BROODSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_BROODSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_BROODSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_BROODSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_BROODSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_BROODSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_BROODSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_BROODSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROODSTONE_PILLAR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_COAL_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MYRMITE_COAL_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_URANIUM_ORE_ITEM = ITEMS.register("broodstone_uranium_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.BROODSTONE_URANIUM_ORE.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_TITANIUM_ORE_ITEM = ITEMS.register("broodstone_titanium_ore",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.BROODSTONE_TITANIUM_ORE.get(), blockItemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_NYXITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_NYXITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHITIN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHITIN_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_WALL);
    public static final DeferredItem<CeilingCompatiblePlacementTooltipBlockItem> POLISHED_NYXITE_PRESSURE_PLATE_ITEM = ITEMS.register("polished_nyxite_pressure_plate",
            id -> new CeilingCompatiblePlacementTooltipBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_PRESSURE_PLATE.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_BUTTON);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_NYXITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_NYXITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_NYXITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_NYXITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_NYXITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_NYXITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_PILLAR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_SHELLSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CRACKED_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CRACKED_SHELLSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SHELLSTONE_PILLAR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> FLYTRAP_GOO_BLOCK_ITEM = ITEMS.register("flytrap_goo_block",
            id -> new com.craisinlord.antarchy.content.item.FlytrapGooBlockItem(AntarchyNeoforgeBlocks.FLYTRAP_GOO_BLOCK.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PALE_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PALE_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_SPIKE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHITIN_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHITIN_SPIKE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POTENT_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POTENT_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UMBRAL_MOSS_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UMBRAL_MOSS_CARPET);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> AMBER_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.AMBER_MOSS_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> AMBER_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.AMBER_MOSS_CARPET);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> AMBER_LICHEN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.AMBER_LICHEN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BILE_VEIN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BILE_VEIN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CREEPVINE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CREEPVINE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUSH_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BLUSH_MOSS_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUSH_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BLUSH_MOSS_CARPET);
    public static final DeferredItem<com.craisinlord.antarchy.content.item.DreamTorchItem> DREAM_TORCH_ITEM = ITEMS.register("dream_torch",
            id -> new com.craisinlord.antarchy.content.item.DreamTorchItem(AntarchyNeoforgeBlocks.DREAM_TORCH.get(), AntarchyNeoforgeBlocks.DREAM_WALL_TORCH.get(), AntarchyNeoforgeBlocks.DREAM_CEILING_TORCH.get(), blockItemProps(id)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_LANTERN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_CAMPFIRE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_CAMPFIRE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LUMEN_FROGLIGHT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LUMEN_FROGLIGHT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROSEATE_FROGLIGHT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ROSEATE_FROGLIGHT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BED_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BED_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CRAWLING_BLIGHT_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CRAWLING_BLIGHT_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PRINCE_EGG_ITEM = ITEMS.register("prince_egg",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.PRINCE_EGG.get(), blockItemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PRINCESS_EGG_ITEM = ITEMS.register("princess_egg",
            id -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.PRINCESS_EGG.get(), blockItemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SPRINGBUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SPRINGBUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SPIT_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SPIT_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> JERRY_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.JERRY_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SKULKING_FRIGHT_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SKULKING_FRIGHT_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WASP_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.WASP_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> HUSHWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.HUSHWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> HANGING_CREEPROOTS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.HANGING_CREEPROOTS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GLOWCAP_MUSHROOM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.GLOWCAP_MUSHROOM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GLOWCAP_MUSHROOM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.GLOWCAP_MUSHROOM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOLTING_VINES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MOLTING_VINES);
    public static final DeferredItem<BucketItem> BILE_BUCKET = ITEMS.register("bile_bucket",
            id -> new BucketItem(AntarchyNeoforgeMisc.BILE.get(), itemProps(id).craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> ICHOR_BUCKET = ITEMS.register("ichor_bucket",
            id -> new BucketItem(AntarchyNeoforgeMisc.ICHOR.get(), itemProps(id).craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> ANTIWATER_BUCKET = ITEMS.register("antiwater_bucket",
            id -> new BucketItem(AntarchyNeoforgeMisc.ANTIWATER.get(), itemProps(id).craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> LUMEN_BUCKET = ITEMS.register("lumen_bucket",
            id -> new BucketItem(AntarchyNeoforgeMisc.LUMEN.get(), itemProps(id).craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.CloudBucketItem> CLOUD_BUCKET = ITEMS.register("cloud_bucket",
            id -> new com.craisinlord.antarchy.content.item.CloudBucketItem(AntarchyNeoforgeBlocks.CLOUD_BLOCK.get(), itemProps(id).craftRemainder(Items.BUCKET)));
    public static final DeferredItem<BloodCrystalShardItem> BLOOD_CRYSTAL_SHARD = ITEMS.register("blood_crystal_shard",
            id -> new BloodCrystalShardItem(itemProps(id).trimMaterial(com.craisinlord.antarchy.content.item.AntarchyTrimMaterials.BLOOD_CRYSTAL)));
    public static final DeferredItem<TemporalTunerItem> TEMPORAL_TUNER = ITEMS.register("temporal_tuner",
            id -> new TemporalTunerItem(itemProps(id).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<Item> BLOOD_CRYSTAL_APPLE = ITEMS.register("blood_crystal_apple",
            id -> new BloodCrystalAppleItem(itemProps(id)
                    .rarity(Rarity.RARE)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.2f)
                            .alwaysEdible()
                            .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1), 1.0f)).build())));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_HELMET = ITEMS.register("blood_crystal_helmet",
            id -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL.get(), ArmorType.HELMET, itemProps(id).rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_CHESTPLATE = ITEMS.register("blood_crystal_chestplate",
            id -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE, itemProps(id).rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_LEGGINGS = ITEMS.register("blood_crystal_leggings",
            id -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS, itemProps(id).rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_BOOTS = ITEMS.register("blood_crystal_boots",
            id -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL.get(), ArmorType.BOOTS, itemProps(id).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> TIGERS_EYE = ITEMS.registerSimpleItem("tigers_eye", props -> props.rarity(Rarity.UNCOMMON).trimMaterial(com.craisinlord.antarchy.content.item.AntarchyTrimMaterials.TIGERS_EYE));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_HELMET = ITEMS.register("tigers_eye_helmet",
            id -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(TIGERS_EYE_ARMOR_MATERIAL.get(), ArmorType.HELMET, itemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_CHESTPLATE = ITEMS.register("tigers_eye_chestplate",
            id -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(TIGERS_EYE_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE, itemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_LEGGINGS = ITEMS.register("tigers_eye_leggings",
            id -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(TIGERS_EYE_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS, itemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_BOOTS = ITEMS.register("tigers_eye_boots",
            id -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(TIGERS_EYE_ARMOR_MATERIAL.get(), ArmorType.BOOTS, itemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<BloodCrystalKatanaItem> BLOOD_CRYSTAL_KATANA = ITEMS.register("blood_crystal_katana",
            id -> new BloodCrystalKatanaItem(
                    BLOOD_CRYSTAL_KATANA_TIER,
                    itemProps(id).stacksTo(1).durability(1200).rarity(Rarity.RARE),
                    AntarchySettings.bloodCrystalKatanaAttackDamage(),
                    -2.2F
            ));
    public static final DeferredItem<BloodGodsBladeItem> BLOOD_GODS_BLADE = ITEMS.register("blood_gods_blade",
            id -> new BloodGodsBladeItem(
                    ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).durability(4096).rarity(Rarity.EPIC).fireResistant(),
                    24,
                    -2.4F
            ));
    public static final DeferredItem<MantisClawItem> MANTIS_CLAW = ITEMS.register("mantis_claw",
            id -> new MantisClawItem(ToolMaterial.IRON, itemProps(id).stacksTo(1).durability(50).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> HERCULES_HORN = ITEMS.registerSimpleItem("hercules_horn", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> KING_SCALE = ITEMS.registerSimpleItem("king_scale", props -> props.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> QUEEN_SCALE = ITEMS.registerSimpleItem("queen_scale", props -> props.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> CHITIN = ITEMS.registerSimpleItem("chitin", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> JERRY_NUCLEUS = ITEMS.register("jerry_nucleus",
            id -> new com.craisinlord.antarchy.content.item.JerryNucleusItem(itemProps(id).rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                            .build())));
    public static final DeferredItem<Item> STINK_BUG = ITEMS.registerSimpleItem("stink_bug", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SPRINGBUG_LEG = ITEMS.registerSimpleItem("springbug_leg", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<SpringyBootsItem> SPRINGY_BOOTS = ITEMS.register("springy_boots",
            id -> new SpringyBootsItem(SPRINGY_BOOTS_ARMOR_MATERIAL.get(), itemProps(id).stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().durability(ArmorType.BOOTS.getDurability(37))));
    public static final DeferredItem<Item> BRUTALFLY_WING = ITEMS.registerSimpleItem("brutalfly_wing", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BrutalflyElytraItem> BRUTALFLY_ELYTRA = ITEMS.register("brutalfly_elytra",
            id -> new BrutalflyElytraItem(itemProps(id).rarity(Rarity.UNCOMMON).durability(480)));
    public static final DeferredItem<Item> MANTICORE_WING = ITEMS.registerSimpleItem("manticore_wing", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ManticoreWingsItem> MANTICORE_WINGS = ITEMS.register("manticore_wings",
            id -> new ManticoreWingsItem(itemProps(id).rarity(Rarity.RARE).durability(480)));
    public static final DeferredItem<Item> CORNEA_EAR = ITEMS.register("cornea_ear",
            id -> new CorneaEarItem(itemProps(id)
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.4F)
                            .build())));
    public static final DeferredItem<Item> CORN = ITEMS.register("corn",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(3)
                            .saturationModifier(0.6F)
                            .build())));
    public static final DeferredItem<BlockItem> CORN_SEEDS = ITEMS.register("corn_seeds",
            id -> new BlockItem(AntarchyNeoforgeBlocks.CORN_CROP.get(), itemProps(id)));
    public static final DeferredItem<Item> RAW_VENISON = ITEMS.register("raw_venison",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.3F)
                            .build())));
    public static final DeferredItem<Item> BROODFRUIT = ITEMS.register("broodfruit",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                    .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0), 1.0F)).build())));
    public static final DeferredItem<Item> BROODFRUIT_COBBLER = ITEMS.register("broodfruit_cobbler",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.8F)
                            .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 90, 1), 1.0F)).build())));
    public static final DeferredItem<Item> RAW_BUG_MEAT = ITEMS.register("raw_bug_meat",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.1F)
                            .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)).build())));
    public static final DeferredItem<Item> COOKED_BUG_MEAT = ITEMS.register("cooked_bug_meat",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.8F)
                            .build())));
    public static final DeferredItem<Item> COOKED_VENISON = ITEMS.register("cooked_venison",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationModifier(0.8F)
                            .build())));
    public static final DeferredItem<Item> PEACH = ITEMS.register("peach",
            id -> new PeachItem(itemProps(id)));
    public static final DeferredItem<Item> PEACH_PIE = ITEMS.register("peach_pie",
            id -> new Item(itemProps(id)
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.3F)
                            .build())));
    public static final DeferredItem<Item> CORNBREAD = ITEMS.register("cornbread",
            id -> new Item(itemProps(id)
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.9F)
                            .build())));
    public static final DeferredItem<Item> POPCORN = ITEMS.register("popcorn",
            id -> new Item(itemProps(id)
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.1F)
                            .build())));
    public static final DeferredItem<Item> RAW_CORNDOG = ITEMS.register("raw_corndog",
            id -> new Item(itemProps(id)
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.7F)
                            .build())));
    public static final DeferredItem<Item> COOKED_CORNDOG = ITEMS.register("cooked_corndog",
            id -> new Item(itemProps(id)
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(14)
                            .saturationModifier(1.1F)
                            .build())));
    public static final DeferredItem<Item> HIGH_FRUCTOSE_CORN_SYRUP = ITEMS.register("high_fructose_corn_syrup",
            id -> new HighFructoseCornSyrupItem(itemProps(id)
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationModifier(0.0F)
                            .alwaysEdible()
                            .build(), Consumables.DEFAULT_DRINK)));
    public static final DeferredItem<RootBeerItem> ROOT_BEER = ITEMS.register("root_beer",
            id -> new RootBeerItem(itemProps(id)
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.4F)
                            .alwaysEdible()
                            .build(), Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)).build())));
    public static final DeferredItem<Item> FLYTRAP_GOO = ITEMS.registerSimpleItem("flytrap_goo",
            props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<VortexChargeItem> VORTEX_CHARGE = ITEMS.register("vortex_charge",
            id -> new VortexChargeItem(itemProps(id).stacksTo(64).rarity(Rarity.RARE), AntarchyNeoforgeEntites.VORTEX_CHARGE_PROJECTILE));
    public static final DeferredItem<Item> RAW_URANIUM_SCRAP = ITEMS.registerSimpleItem("raw_uranium_scrap", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_TITANIUM_SCRAP = ITEMS.registerSimpleItem("raw_titanium_scrap", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MUD_PIE = ITEMS.register("mud_pie",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.9F)
                            .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 200), 1.0F)).build())));
    public static final DeferredItem<RainbowSugarItem> RAINBOW_SUGAR = ITEMS.register("rainbow_sugar",
            id -> new RainbowSugarItem(itemProps(id)
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.0F).alwaysEdible().build())));
    public static final DeferredItem<Item> URANIUM_NUGGET = ITEMS.registerSimpleItem("uranium_nugget", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TITANIUM_NUGGET = ITEMS.registerSimpleItem("titanium_nugget", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot", props -> props.rarity(Rarity.UNCOMMON).fireResistant().trimMaterial(com.craisinlord.antarchy.content.item.AntarchyTrimMaterials.URANIUM));
    public static final DeferredItem<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot", props -> props.rarity(Rarity.UNCOMMON).fireResistant().trimMaterial(com.craisinlord.antarchy.content.item.AntarchyTrimMaterials.TITANIUM));
    public static final DeferredItem<Item> MOLEVORE_NOSE = ITEMS.registerSimpleItem("molevore_nose", props -> props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> MOLEWORM_ITEM = ITEMS.register("moleworm",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.0F)
                            .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)).build())));
    public static final DeferredItem<Item> WORM_TOOTH = ITEMS.register("worm_tooth",
            id -> new Item(itemProps(id).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<WormHookItem> WORM_HOOK = ITEMS.register("worm_hook",
            id -> new WormHookItem(itemProps(id).stacksTo(1).durability(256).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> STRATOSHARK_FIN = ITEMS.registerSimpleItem("stratoshark_fin", props -> props.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<StratosharkFinSoupItem> STRATOSHARK_FIN_SOUP = ITEMS.register("stratoshark_fin_soup",
            id -> new StratosharkFinSoupItem(itemProps(id)
                    .stacksTo(1)
                    .craftRemainder(Items.BOWL)
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.8F)
                            .build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, 0), 1.0F)).onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0), 1.0F)).build())));
    public static final DeferredItem<Item> KRAKEN_TOOTH = ITEMS.register("kraken_tooth",
            id -> new Item(itemProps(id).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BASILISK_FANG = ITEMS.registerSimpleItem("basilisk_fang", props -> props.rarity(Rarity.RARE));
    public static final DeferredItem<BasiliskDaggerItem> BASILISK_DAGGER = ITEMS.register("basilisk_dagger",
            id -> new BasiliskDaggerItem(ToolMaterial.IRON, itemProps(id).rarity(Rarity.RARE), 4, -1.8F));
    public static final DeferredItem<Item> EMPEROR_SCORPION_STINGER = ITEMS.registerSimpleItem("emperor_scorpion_stinger", props -> props.rarity(Rarity.RARE));
    public static final DeferredItem<ScorpionWhipItem> SCORPION_WHIP = ITEMS.register("scorpion_whip",
            id -> new ScorpionWhipItem(ToolMaterial.IRON, itemProps(id).rarity(Rarity.RARE).durability(384)));
    public static final DeferredItem<MogglesItem> MOGGLES = ITEMS.register("moggles",
            id -> new MogglesItem(MOGGLES_ARMOR_MATERIAL.get(), itemProps(id).rarity(Rarity.RARE).stacksTo(1)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.FallenKingCrownItem> FALLEN_KING_CROWN = ITEMS.register("fallen_king_crown",
            id -> new com.craisinlord.antarchy.content.item.FallenKingCrownItem(
                    FALLEN_KING_CROWN_ARMOR_MATERIAL.get(),
                    itemProps(id).rarity(Rarity.RARE)
            ));
    public static final DeferredItem<UltimateArmorItem> ULTIMATE_HELMET = ITEMS.register("ultimate_helmet",
            id -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL.get(), ArmorType.HELMET,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.HELMET.getDurability(62))));
    public static final DeferredItem<UltimateArmorItem> ULTIMATE_CHESTPLATE = ITEMS.register("ultimate_chestplate",
            id -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.CHESTPLATE.getDurability(62))));
    public static final DeferredItem<UltimateArmorItem> ULTIMATE_LEGGINGS = ITEMS.register("ultimate_leggings",
            id -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.LEGGINGS.getDurability(62))));
    public static final DeferredItem<UltimateArmorItem> ULTIMATE_BOOTS = ITEMS.register("ultimate_boots",
            id -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL.get(), ArmorType.BOOTS,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.BOOTS.getDurability(62))));
    public static final DeferredItem<UltimateSwordItem> ULTIMATE_SWORD = ITEMS.register("ultimate_sword",
            id -> new UltimateSwordItem(ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateSwordAttackDamage, -2.4F));
    public static final DeferredItem<UltimatePickaxeItem> ULTIMATE_PICKAXE = ITEMS.register("ultimate_pickaxe",
            id -> new UltimatePickaxeItem(ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimatePickaxeAttackDamage, -2.8F));
    public static final DeferredItem<UltimateAxeItem> ULTIMATE_AXE = ITEMS.register("ultimate_axe",
            id -> new UltimateAxeItem(ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateAxeAttackDamage, -3.0F));
    public static final DeferredItem<UtlimateShovelItem> ULTIMATE_SHOVEL = ITEMS.register("ultimate_shovel",
            id -> new UtlimateShovelItem(ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateShovelAttackDamage, -3.0F));
    public static final DeferredItem<UltimateHoeItem> ULTIMATE_HOE = ITEMS.register("ultimate_hoe",
            id -> new UltimateHoeItem(ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateHoeAttackDamage, 0.0F));
    public static final DeferredItem<UltimateBowItem> ULTIMATE_BOW = ITEMS.register("ultimate_bow",
            id -> new UltimateBowItem(itemProps(id).stacksTo(1).durability(768).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<UltimateCrossbowItem> ULTIMATE_CROSSBOW = ITEMS.register("ultimate_crossbow",
            id -> new UltimateCrossbowItem(itemProps(id).stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_HELMET = ITEMS.register("nightmare_helmet",
            id -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL.get(), ArmorType.HELMET,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.HELMET.getDurability(41))));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_CHESTPLATE = ITEMS.register("nightmare_chestplate",
            id -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.CHESTPLATE.getDurability(41))));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_LEGGINGS = ITEMS.register("nightmare_leggings",
            id -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.LEGGINGS.getDurability(41))));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_BOOTS = ITEMS.register("nightmare_boots",
            id -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL.get(), ArmorType.BOOTS,
                    itemProps(id).rarity(Rarity.EPIC).fireResistant().durability(ArmorType.BOOTS.getDurability(41))));
    public static final DeferredItem<NightmareSwordItem> NIGHTMARE_SWORD = ITEMS.register("nightmare_sword",
            id -> new NightmareSwordItem(ULTIMATE_TIER,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant(), -2.4F));

    public static final DeferredItem<RoyalGuardianArmorItem> ROYAL_GUARDIAN_HELMET = ITEMS.register("royal_guardian_helmet",
            id -> new RoyalGuardianArmorItem(ROYAL_GUARDIAN_ARMOR_MATERIAL.get(), ArmorType.HELMET, itemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalGuardianArmorItem> ROYAL_GUARDIAN_CHESTPLATE = ITEMS.register("royal_guardian_chestplate",
            id -> new RoyalGuardianArmorItem(ROYAL_GUARDIAN_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE, itemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalGuardianArmorItem> ROYAL_GUARDIAN_LEGGINGS = ITEMS.register("royal_guardian_leggings",
            id -> new RoyalGuardianArmorItem(ROYAL_GUARDIAN_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS, itemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalGuardianArmorItem> ROYAL_GUARDIAN_BOOTS = ITEMS.register("royal_guardian_boots",
            id -> new RoyalGuardianArmorItem(ROYAL_GUARDIAN_ARMOR_MATERIAL.get(), ArmorType.BOOTS, itemProps(id).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<RoyalAssailantArmorItem> ROYAL_ASSAILANT_HELMET = ITEMS.register("royal_assailant_helmet",
            id -> new RoyalAssailantArmorItem(ROYAL_ASSAILANT_ARMOR_MATERIAL.get(), ArmorType.HELMET, itemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalAssailantArmorItem> ROYAL_ASSAILANT_CHESTPLATE = ITEMS.register("royal_assailant_chestplate",
            id -> new RoyalAssailantArmorItem(ROYAL_ASSAILANT_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE, itemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalAssailantArmorItem> ROYAL_ASSAILANT_LEGGINGS = ITEMS.register("royal_assailant_leggings",
            id -> new RoyalAssailantArmorItem(ROYAL_ASSAILANT_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS, itemProps(id).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalAssailantArmorItem> ROYAL_ASSAILANT_BOOTS = ITEMS.register("royal_assailant_boots",
            id -> new RoyalAssailantArmorItem(ROYAL_ASSAILANT_ARMOR_MATERIAL.get(), ArmorType.BOOTS, itemProps(id).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<RoyalGuardianSwordItem> ROYAL_GUARDIAN_SWORD = ITEMS.register("royal_guardian_sword",
            id -> new RoyalGuardianSwordItem(ROYAL_GUARDIAN_TIER, itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<GiantFryingPanItem> GIANT_FRYING_PAN = ITEMS.register("giant_frying_pan",
            id -> new GiantFryingPanItem(ROYAL_GUARDIAN_TIER, itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalAssailantBattleAxeItem> ROYAL_ASSAILANT_BATTLEAXE = ITEMS.register("royal_assailant_battleaxe",
            id -> new RoyalAssailantBattleAxeItem(ROYAL_ASSAILANT_TIER, itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<RoyalGuardianShieldItem> ROYAL_GUARDIAN_SHIELD = ITEMS.register("royal_guardian_shield",
            id -> new RoyalGuardianShieldItem(
                    itemProps(id).durability(AntarchySettings.royalGuardianShieldDurability()).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<SizeRayItem> SHRINK_RAY = ITEMS.register("shrink_ray",
            id -> new SizeRayItem(
                    itemProps(id).stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyNeoforgeEntites.SHRINK_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.SHRINK,
                    Identifier.fromNamespaceAndPath(Antarchy.MODID, "shrink_ray"),
                    Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/shrink_ray.png"),
                    Identifier.fromNamespaceAndPath(Antarchy.MODID, "shrink_ray"),
                    "shrink_ray_active"
            ));
    public static final DeferredItem<SizeRayItem> GROWTH_RAY = ITEMS.register("growth_ray",
            id -> new SizeRayItem(
                    itemProps(id).stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyNeoforgeEntites.GROWTH_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.GROWTH,
                    Identifier.fromNamespaceAndPath(Antarchy.MODID, "growth_ray"),
                    Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/growth_ray.png"),
                    Identifier.fromNamespaceAndPath(Antarchy.MODID, "growth_ray"),
                    "growth_ray_active"
            ));
    public static final DeferredItem<GravityGunItem> GRAVITY_GUN = ITEMS.register("gravity_gun",
            id -> new GravityGunItem(itemProps(id).stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PortalGunItem> PORTAL_GUN = ITEMS.register("portal_gun",
            id -> new PortalGunItem(
                    itemProps(id).stacksTo(1).durability(256).rarity(Rarity.RARE),
                    AntarchyNeoforgeEntites.PORTAL_GUN_PORTAL,
                    AntarchyNeoforgeEntites.PORTAL_GUN_PROJECTILE,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_MASTER,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_BASE,
                    com.craisinlord.antarchy.content.portalgun.PortalGunVariant.DEFAULT
            ));
    public static final DeferredItem<PortalGunItem> PORTAL_GUN_ATLAS = ITEMS.register("portal_gun_atlas",
            id -> new PortalGunItem(
                    itemProps(id).stacksTo(1).durability(256).rarity(Rarity.RARE),
                    AntarchyNeoforgeEntites.PORTAL_GUN_PORTAL,
                    AntarchyNeoforgeEntites.PORTAL_GUN_PROJECTILE,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_MASTER,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_BASE,
                    com.craisinlord.antarchy.content.portalgun.PortalGunVariant.ATLAS
            ));
    public static final DeferredItem<PortalGunItem> PORTAL_GUN_P_BODY = ITEMS.register("portal_gun_p_body",
            id -> new PortalGunItem(
                    itemProps(id).stacksTo(1).durability(256).rarity(Rarity.RARE),
                    AntarchyNeoforgeEntites.PORTAL_GUN_PORTAL,
                    AntarchyNeoforgeEntites.PORTAL_GUN_PROJECTILE,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_MASTER,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_BASE,
                    com.craisinlord.antarchy.content.portalgun.PortalGunVariant.P_BODY
            ));
    public static final DeferredItem<PortalGunItem> PORTAL_GUN_CRAISIN = ITEMS.register("portal_gun_craisin",
            id -> new PortalGunItem(
                    itemProps(id).stacksTo(1).durability(256).rarity(Rarity.RARE),
                    AntarchyNeoforgeEntites.PORTAL_GUN_PORTAL,
                    AntarchyNeoforgeEntites.PORTAL_GUN_PROJECTILE,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_MASTER,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_BASE,
                    com.craisinlord.antarchy.content.portalgun.PortalGunVariant.CRAISIN
            ));
    public static final DeferredItem<PortalGunItem> PORTAL_GUN_LAYNCE = ITEMS.register("portal_gun_laynce",
            id -> new PortalGunItem(
                    itemProps(id).stacksTo(1).durability(256).rarity(Rarity.RARE),
                    AntarchyNeoforgeEntites.PORTAL_GUN_PORTAL,
                    AntarchyNeoforgeEntites.PORTAL_GUN_PROJECTILE,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_MASTER,
                    AntarchyNeoforgeBlocks.PORTAL_GUN_PORTAL_BASE,
                    com.craisinlord.antarchy.content.portalgun.PortalGunVariant.LAYNCE
            ));
    public static final DeferredItem<EyeOfTheStormItem> EYE_OF_THE_STORM = ITEMS.register("eye_of_the_storm",
            id -> new EyeOfTheStormItem(itemProps(id).stacksTo(1).durability(384).rarity(Rarity.EPIC)));
    public static final DeferredItem<SquidzookaItem> SQUIDZOOKA = ITEMS.register("squidzooka",
            id -> new SquidzookaItem(itemProps(id).stacksTo(1).durability(384).rarity(Rarity.RARE)));
    public static final DeferredItem<RpoLauncherItem> RPO_LAUNCHER = ITEMS.register("rpo_launcher",
            id -> new RpoLauncherItem(itemProps(id).stacksTo(1).durability(384).rarity(Rarity.RARE)));
    public static final DeferredItem<BattleAxeItem> BATTLE_AXE = ITEMS.register("battle_axe",
            id -> new BattleAxeItem(ToolMaterial.NETHERITE,
                    itemProps(id).stacksTo(1).rarity(Rarity.RARE).fireResistant(),
                    AntarchySettings::battleAxeAttackDamage, -3.1F));
    public static final DeferredItem<AttitudeAdjusterItem> ATTITUDE_ADJUSTER = ITEMS.register("attitude_adjuster",
            id -> new AttitudeAdjusterItem(ATTITUDE_ADJUSTER_TIER,
                    itemProps(id).stacksTo(1).durability(ATTITUDE_ADJUSTER_TIER.durability()).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<DiamondMinecartItem> DIAMOND_MINECART_ITEM = ITEMS.register("diamond_minecart",
            id -> new DiamondMinecartItem(AntarchyNeoforgeEntites.DIAMOND_MINECART, itemProps(id).stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<ReverieBottleItem> REVERIE_BOTTLE = ITEMS.register("reverie_bottle",
            id -> new ReverieBottleItem(itemProps(id).stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BIG_BERTHA_BLADE = ITEMS.registerSimpleItem("big_bertha_blade",
            props -> props.rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> BIG_BERTHA_HANDLE = ITEMS.registerSimpleItem("big_bertha_handle",
            props -> props.rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> BIG_BERTHA_HILT = ITEMS.registerSimpleItem("big_bertha_hilt",
            props -> props.rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<BigBerthaItem> BIG_BERTHA = ITEMS.register("big_bertha",
            id -> new BigBerthaItem(ToolMaterial.NETHERITE,
                    itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<Item> KRAKEN_TENTACLE = ITEMS.registerSimpleItem("kraken_tentacle",
            props -> props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> KRAKEN_KALAMARI = ITEMS.register("kraken_kalamari",
            id -> new Item(itemProps(id)
                    .rarity(Rarity.EPIC)
                    .food(new FoodProperties.Builder()
                            .nutrition(20)
                            .saturationModifier(1.2F)
                            .alwaysEdible()
                            .build())));

    public static final DeferredItem<KrakensGraspItem> KRAKENS_GRASP = ITEMS.register("krakens_grasp",
            id -> new KrakensGraspItem(
                    itemProps(id).stacksTo(1).durability(250).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<SpawnEggItem> EASTER_BUNNY_SPAWN_EGG = ITEMS.register("easter_bunny_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.EASTER_BUNNY.get())));
    public static final DeferredItem<SpawnEggItem> DORRIE_SPAWN_EGG = ITEMS.register("dorrie_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.DORRIE.get())));
    public static final DeferredItem<SpawnEggItem> OURANWOOD_DEER_SPAWN_EGG = ITEMS.register("ouranwood_deer_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.OURANWOOD_DEER.get())));
    public static final DeferredItem<SpawnEggItem> GLIMMER_SPAWN_EGG = ITEMS.register("glimmer_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.GLIMMER.get())));
    public static final DeferredItem<SpawnEggItem> ELKA_SPAWN_EGG = ITEMS.register("elka_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.ELKA.get())));
    public static final DeferredItem<net.minecraft.world.item.Item> SPIRIT_APPLE = ITEMS.register("spirit_apple",
            id -> new com.craisinlord.antarchy.content.item.SpiritAppleItem(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.3F)
                            .alwaysEdible()
                            .build())));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.GlimmerBottleItem> GLIMMER_BOTTLE = ITEMS.register("glimmer_bottle",
            id -> new com.craisinlord.antarchy.content.item.GlimmerBottleItem(itemProps(id)));
    public static final DeferredItem<SpawnEggItem> FLYING_SQUIRREL_SPAWN_EGG = ITEMS.register("flying_squirrel_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.FLYING_SQUIRREL.get())));
    public static final DeferredItem<SpawnEggItem> CATERPILLAR_SPAWN_EGG = ITEMS.register("caterpillar_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.CATERPILLAR.get())));
    public static final DeferredItem<SpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.BUTTERFLY.get())));
    public static final DeferredItem<SpawnEggItem> REVERIE_SPAWN_EGG = ITEMS.register("reverie_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.REVERIE.get())));
    public static final DeferredItem<SpawnEggItem> BRUTALFLY_SPAWN_EGG = ITEMS.register("brutalfly_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).spawnEgg(AntarchyNeoforgeEntites.BRUTALFLY.get())));
    public static final DeferredItem<SpawnEggItem> KING_SPAWN_EGG = ITEMS.register("king_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).fireResistant().spawnEgg(AntarchyNeoforgeEntites.KING.get())));
    public static final DeferredItem<SpawnEggItem> QUEEN_SPAWN_EGG = ITEMS.register("queen_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).fireResistant().spawnEgg(AntarchyNeoforgeEntites.QUEEN.get())));
    public static final DeferredItem<SpawnEggItem> PRINCE_SPAWN_EGG = ITEMS.register("prince_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).fireResistant().spawnEgg(AntarchyNeoforgeEntites.PRINCE.get())));
    public static final DeferredItem<SpawnEggItem> PRINCESS_SPAWN_EGG = ITEMS.register("princess_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).fireResistant().spawnEgg(AntarchyNeoforgeEntites.PRINCESS.get())));
    public static final DeferredItem<SpawnEggItem> RED_ANT_SPAWN_EGG = ITEMS.register("red_ant_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.RED_ANT.get())));
    public static final DeferredItem<SpawnEggItem> BROWN_ANT_SPAWN_EGG = ITEMS.register("brown_ant_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.BROWN_ANT.get())));
    public static final DeferredItem<SpawnEggItem> RAINBOW_ANT_SPAWN_EGG = ITEMS.register("rainbow_ant_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.RAINBOW_ANT.get())));
    public static final DeferredItem<SpawnEggItem> TERMITE_SPAWN_EGG = ITEMS.register("termite_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.TERMITE.get())));
    public static final DeferredItem<SpawnEggItem> MOLEWORM_SPAWN_EGG = ITEMS.register("moleworm_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.MOLEWORM.get())));
    public static final DeferredItem<SpawnEggItem> WORM_SPAWN_EGG = ITEMS.register("worm_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.WORM.get())));
    public static final DeferredItem<SpawnEggItem> MANTIS_SPAWN_EGG = ITEMS.register("mantis_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.MANTIS.get())));
    public static final DeferredItem<SpawnEggItem> ALPHA_MANTIS_SPAWN_EGG = ITEMS.register("alpha_mantis_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.UNCOMMON).spawnEgg(AntarchyNeoforgeEntites.ALPHA_MANTIS.get())));
    public static final DeferredItem<SpawnEggItem> ROLLY_POLLY_SPAWN_EGG = ITEMS.register("rolly_polly_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.ROLLY_POLLY.get())));
    public static final DeferredItem<SpawnEggItem> MOLEVORE_SPAWN_EGG = ITEMS.register("molevore_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.UNCOMMON).spawnEgg(AntarchyNeoforgeEntites.MOLEVORE.get())));
    public static final DeferredItem<SpawnEggItem> FLYTRAP_SPAWN_EGG = ITEMS.register("flytrap_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.UNCOMMON).spawnEgg(AntarchyNeoforgeEntites.FLYTRAP.get())));
    public static final DeferredItem<SpawnEggItem> APPLE_COW_SPAWN_EGG = ITEMS.register("apple_cow_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.APPLE_COW.get())));
    public static final DeferredItem<SpawnEggItem> GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("golden_apple_cow_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.GOLDEN_APPLE_COW.get())));
    public static final DeferredItem<SpawnEggItem> ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("enchanted_golden_apple_cow_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.ENCHANTED_GOLDEN_APPLE_COW.get())));
    public static final DeferredItem<SpawnEggItem> DR_TRAYAURUS_SPAWN_EGG = ITEMS.register("dr_trayaurus_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.DR_TRAYAURUS.get())));
    public static final DeferredItem<SpawnEggItem> WASP_SPAWN_EGG = ITEMS.register("wasp_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.WASP.get())));
    public static final DeferredItem<SpawnEggItem> BOMBER_SPAWN_EGG = ITEMS.register("bomber_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.BOMBER.get())));
    public static final DeferredItem<SpawnEggItem> SPRINGBUG_SPAWN_EGG = ITEMS.register("springbug_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.SPRINGBUG.get())));
    public static final DeferredItem<SpawnEggItem> SPIT_BUG_SPAWN_EGG = ITEMS.register("spit_bug_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.SPIT_BUG.get())));
    public static final DeferredItem<SpawnEggItem> STINK_BUG_SPAWN_EGG = ITEMS.register("stink_bug_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.STINK_BUG.get())));
    public static final DeferredItem<SpawnEggItem> STRATOSHARK_SPAWN_EGG = ITEMS.register("stratoshark_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.STRATOSHARK.get())));
    public static final DeferredItem<SpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).spawnEgg(AntarchyNeoforgeEntites.KRAKEN.get())));
    public static final DeferredItem<SpawnEggItem> MISSILE_SQUID_SPAWN_EGG = ITEMS.register("missile_squid_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.UNCOMMON).spawnEgg(AntarchyNeoforgeEntites.MISSILE_SQUID.get())));
    public static final DeferredItem<SpawnEggItem> OCTOPUS_BOMB_SPAWN_EGG = ITEMS.register("octopus_bomb_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.UNCOMMON).spawnEgg(AntarchyNeoforgeEntites.OCTOPUS_BOMB.get())));
    public static final DeferredItem<SpawnEggItem> NIGHTMARE_SPAWN_EGG = ITEMS.register("nightmare_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.RARE).spawnEgg(AntarchyNeoforgeEntites.NIGHTMARE.get())));
    public static final DeferredItem<SpawnEggItem> BED_BUG_SPAWN_EGG = ITEMS.register("bed_bug_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.BED_BUG.get())));
    public static final DeferredItem<SpawnEggItem> LUCID_SPAWN_EGG = ITEMS.register("lucid_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.RARE).spawnEgg(AntarchyNeoforgeEntites.LUCID.get())));
    public static final DeferredItem<SpawnEggItem> VORTEX_SPAWN_EGG = ITEMS.register("vortex_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.RARE).spawnEgg(AntarchyNeoforgeEntites.VORTEX.get())));
    public static final DeferredItem<SpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.SCORPION.get())));
    public static final DeferredItem<SpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.register("basilisk_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.BASILISK.get())));
    public static final DeferredItem<SpawnEggItem> EMPEROR_SCORPION_SPAWN_EGG = ITEMS.register("emperor_scorpion_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.EMPEROR_SCORPION.get())));
    public static final DeferredItem<LucidEyeItem> LUCID_EYE = ITEMS.register("lucid_eye",
            id -> new LucidEyeItem(itemProps(id).stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LucidPearlItem> LUCID_PEARL = ITEMS.register("lucid_pearl",
            id -> new LucidPearlItem(
                    itemProps(id).stacksTo(16).rarity(Rarity.UNCOMMON),
                    AntarchyNeoforgeEntites.LUCID_PEARL_PROJECTILE
            ));
    public static final DeferredItem<RoyalAssailantStaffItem> ROYAL_ASSAILANT_STAFF = ITEMS.register("royal_assailant_staff",
            id -> new RoyalAssailantStaffItem(itemProps(id).stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<WaterCannonItem> WATER_CANNON = ITEMS.register("water_cannon",
            id -> new WaterCannonItem(itemProps(id).stacksTo(1).durability(192).rarity(Rarity.RARE)));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_HELMET = ITEMS.register("primordial_helmet",
            id -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL.get(), ArmorType.HELMET, itemProps(id).stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_CHESTPLATE = ITEMS.register("primordial_chestplate",
            id -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE, itemProps(id).stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_LEGGINGS = ITEMS.register("primordial_leggings",
            id -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS, itemProps(id).stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_BOOTS = ITEMS.register("primordial_boots",
            id -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL.get(), ArmorType.BOOTS, itemProps(id).stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<SpawnEggItem> CRAWLING_BLIGHT_SPAWN_EGG = ITEMS.register("crawling_blight_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.CRAWLING_BLIGHT.get())));
    public static final DeferredItem<SpawnEggItem> SKULKING_FRIGHT_SPAWN_EGG = ITEMS.register("skulking_fright_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.SKULKING_FRIGHT.get())));
    public static final DeferredItem<SpawnEggItem> MANTICORE_SPAWN_EGG = ITEMS.register("manticore_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.MANTICORE.get())));
    public static final DeferredItem<SpawnEggItem> HERCULES_BEETLE_SPAWN_EGG = ITEMS.register("hercules_beetle_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.RARE).spawnEgg(AntarchyNeoforgeEntites.HERCULES_BEETLE.get())));
    public static final DeferredItem<SpawnEggItem> JERRY_SPAWN_EGG = ITEMS.register("jerry_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.JERRY.get())));
    public static final DeferredItem<SpawnEggItem> TORETERROR_SPAWN_EGG = ITEMS.register("toreterror_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).rarity(Rarity.EPIC).spawnEgg(AntarchyNeoforgeEntites.TORETERROR.get())));
    public static final DeferredItem<SpawnEggItem> CHEEP_SPAWN_EGG = ITEMS.register("cheep_spawn_egg",
            id -> new SpawnEggItem(itemProps(id).spawnEgg(AntarchyNeoforgeEntites.CHEEP.get())));
    public static final DeferredItem<Item> CHEEP_ITEM = ITEMS.register("cheep",
            id -> new Item(itemProps(id)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                            .build())));
    public static final DeferredItem<Item> COOKED_CHEEP = ITEMS.register("cooked_cheep",
            id -> new Item(itemProps(id)
                    .food(new FoodProperties.Builder()
                            .nutrition(5)
                            .saturationModifier(0.6F)
                            .build())));
    public static final DeferredItem<MobBucketItem> CHEEP_BUCKET = ITEMS.register("cheep_bucket",
            id -> new MobBucketItem(AntarchyNeoforgeEntites.CHEEP.get(), net.minecraft.world.level.material.Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, itemProps(id).craftRemainder(Items.BUCKET).stacksTo(1)));
    private AntarchyNeoforgeItems() {}

    public static Collection<DeferredHolder<Item,? extends Item>> getItemEntries() {
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
        return AntarchyNeoforgeBlocks.POTENT_NYXITE_BLOCK_ENTITY.get();
    }

    static BlockEntityType<AntNestBlockEntity> antNestBlockEntityType() {
        return AntarchyNeoforgeBlocks.ANT_NEST_BLOCK_ENTITY.get();
    }

    static BlockEntityType<WaspNestBlockEntity> waspNestBlockEntityType() {
        return AntarchyNeoforgeBlocks.WASP_NEST_BLOCK_ENTITY.get();
    }

    private static Item.Properties itemProps(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    private static Item.Properties blockItemProps(Identifier id) {
        return itemProps(id).useBlockDescriptionPrefix();
    }

    private static ResourceKey<EquipmentAsset> equipmentAsset(String path) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    private static EnumMap<ArmorType, Integer> createUltimateArmorDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 6);
        defense.put(ArmorType.LEGGINGS, 12);
        defense.put(ArmorType.CHESTPLATE, 16);
        defense.put(ArmorType.HELMET, 6);
        defense.put(ArmorType.BODY, 16);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createNightmareArmorDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 4);
        defense.put(ArmorType.LEGGINGS, 9);
        defense.put(ArmorType.CHESTPLATE, 11);
        defense.put(ArmorType.HELMET, 4);
        defense.put(ArmorType.BODY, 11);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createBloodCrystalArmorDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 2);
        defense.put(ArmorType.LEGGINGS, 5);
        defense.put(ArmorType.CHESTPLATE, 6);
        defense.put(ArmorType.HELMET, 2);
        defense.put(ArmorType.BODY, 6);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createDiamondArmorDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 3);
        defense.put(ArmorType.LEGGINGS, 6);
        defense.put(ArmorType.CHESTPLATE, 8);
        defense.put(ArmorType.HELMET, 3);
        defense.put(ArmorType.BODY, 11);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createMogglesArmorDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 0);
        defense.put(ArmorType.LEGGINGS, 0);
        defense.put(ArmorType.CHESTPLATE, 0);
        defense.put(ArmorType.HELMET, 2);
        defense.put(ArmorType.BODY, 2);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createPrimordialArmorDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 3);
        defense.put(ArmorType.LEGGINGS, 6);
        defense.put(ArmorType.CHESTPLATE, 8);
        defense.put(ArmorType.HELMET, 3);
        defense.put(ArmorType.BODY, 8);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createFallenKingCrownDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 0);
        defense.put(ArmorType.LEGGINGS, 0);
        defense.put(ArmorType.CHESTPLATE, 0);
        defense.put(ArmorType.HELMET, 2);
        defense.put(ArmorType.BODY, 2);
        return defense;
    }

    private static EnumMap<ArmorType, Integer> createSpringyBootsDefense() {
        EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, 3);
        defense.put(ArmorType.LEGGINGS, 0);
        defense.put(ArmorType.CHESTPLATE, 0);
        defense.put(ArmorType.HELMET, 0);
        defense.put(ArmorType.BODY, 0);
        return defense;
    }
}
