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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.sounds.SoundEvents;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public final class AntarchyNeoforgeItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Antarchy.MODID);
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Antarchy.MODID);

    // Tier constants
    public static final Tier ULTIMATE_TIER = new SimpleToolTier(
            3072,
            10.5F,
            0.0F,
            Tiers.NETHERITE.getIncorrectBlocksForDrops(),
            25,
            AntarchyNeoforgeItems::ultimateRepairIngredient
    );
    public static final Tier BLOOD_CRYSTAL_KATANA_TIER = new SimpleToolTier(
            1200,
            8.0F,
            0.0F,
            Tiers.DIAMOND.getIncorrectBlocksForDrops(),
            18,
            AntarchyNeoforgeItems::bloodCrystalRepairIngredient
    );

    public static final DeferredItem<Item> NIGHTMARE_SCALE = ITEMS.registerSimpleItem("nightmare_scale", new Item.Properties().rarity(Rarity.RARE).fireResistant());

    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> ULTIMATE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("ultimate",
            () -> new ArmorMaterial(
                    createUltimateArmorDefense(),
                    10,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    AntarchyNeoforgeItems::ultimateRepairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ultimate_armor"))),
                    2.0F,
                    (float) AntarchySettings.ultimateArmorKnockbackResistance()
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> NIGHTMARE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("nightmare",
            () -> new ArmorMaterial(
                    createNightmareArmorDefense(),
                    10,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(NIGHTMARE_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_armor"))),
                    3.0F,
                    (float) AntarchySettings.nightmareArmorKnockbackResistance()
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> BLOOD_CRYSTAL_ARMOR_MATERIAL = ARMOR_MATERIALS.register("blood_crystal",
            () -> new ArmorMaterial(
                    createBloodCrystalArmorDefense(),
                    30,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    AntarchyNeoforgeItems::bloodCrystalRepairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal"))),
                    0.0F,
                    0.0F
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> MOGGLES_ARMOR_MATERIAL = ARMOR_MATERIALS.register("moggles",
            () -> new ArmorMaterial(
                    createMogglesArmorDefense(),
                    12,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "moggles"))),
                    0.0F,
                    0.0F
            ));
    public static final DeferredItem<Item> PRIMORDIAL_SCUTE = ITEMS.registerSimpleItem("primordial_scute",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRIMORDIAL_ARMOR_MATERIAL = ARMOR_MATERIALS.register("primordial",
            () -> new ArmorMaterial(
                    createPrimordialArmorDefense(),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(PRIMORDIAL_SCUTE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "primordial"))),
                    3.0F,
                    0.1F
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> JUMPY_BOOTS_ARMOR_MATERIAL = ARMOR_MATERIALS.register("jumpy_boots",
            () -> new ArmorMaterial(
                    createJumpyBootsDefense(),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_BOOTS),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_boots"))),
                    3.0F,
                    0.1F
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> FALLEN_KING_CROWN_ARMOR_MATERIAL = ARMOR_MATERIALS.register("fallen_king_crown",
            () -> new ArmorMaterial(
                    createFallenKingCrownDefense(),
                    25,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "fallen_king_crown"))),
                    0.0F,
                    0.0F
            ));

    // Block items
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
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ORANGE_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.ORANGE_MILKWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PINK_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PINK_MILKWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TORCHFLOWER_BUSH_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TORCHFLOWER_BUSH);
    public static final DeferredItem<net.minecraft.world.item.SignItem> OURANWOOD_SIGN_ITEM = ITEMS.register("ouranwood_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), AntarchyNeoforgeBlocks.OURANWOOD_SIGN.get(), AntarchyNeoforgeBlocks.OURANWOOD_WALL_SIGN.get()));
    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> OURANWOOD_HANGING_SIGN_ITEM = ITEMS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(AntarchyNeoforgeBlocks.OURANWOOD_HANGING_SIGN.get(), AntarchyNeoforgeBlocks.OURANWOOD_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> OURANWOOD_BOAT = ITEMS.register("ouranwood_boat",
            () -> new OuranwoodBoatOnlyItem(AntarchyNeoforgeEntites.OURANWOOD_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> OURANWOOD_CHEST_BOAT = ITEMS.register("ouranwood_chest_boat",
            () -> new OuranwoodChestBoatItem(AntarchyNeoforgeEntites.OURANWOOD_CHEST_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DUPLICATOR_SAPLING);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RED_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.RED_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROWN_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BROWN_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAINBOW_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.RAINBOW_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.URANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DEEPSLATE_URANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TITANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DEEPSLATE_TITANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.URANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TITANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_URANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.RAW_URANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_TITANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.RAW_TITANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_URANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_TITANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_URANIUM_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_TITANIUM_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_URANIUM_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CUT_TITANIUM_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_URANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_URANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_TITANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BULB_ITEM = ITEMS.register("uranium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyNeoforgeBlocks.URANIUM_BULB.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BULB_ITEM = ITEMS.register("titanium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyNeoforgeBlocks.TITANIUM_BULB.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.URANIUM_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TITANIUM_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.URANIUM_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TITANIUM_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BARS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.URANIUM_BARS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BARS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.TITANIUM_BARS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_ITEM = ITEMS.register("antimetal",
            () -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.ANTIMETAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_ITEM = ITEMS.register("polished_antimetal",
            () -> new AntimetalBlockItem(AntarchyNeoforgeBlocks.POLISHED_ANTIMETAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_SCAFFOLDING_ITEM = ITEMS.register("antimetal_scaffolding",
            () -> new AntimetalScaffoldingItem(AntarchyNeoforgeBlocks.ANTIMETAL_SCAFFOLDING.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMALL_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.SMALL_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MEDIUM_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LARGE_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LARGE_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BUDDING_BLOOD_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BUDDING_BLOOD_CRYSTAL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_ITEM = ITEMS.register("blood_crystal_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyNeoforgeBlocks.BLOOD_CRYSTAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BLOOD_CRYSTAL_CRYSTAL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SAND_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_SAND);
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
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUCT_TAPE_ITEM = ITEMS.register("duct_tape",
            () -> new DuctTapeBlockItem(AntarchyNeoforgeBlocks.DUCT_TAPE.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_ROOTED_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.INFESTED_ROOTED_DIRT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_COARSE_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.INFESTED_COARSE_DIRT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CHISELED_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POLISHED_NYXITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_BRICK_WALL);
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
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TRIFFID_GOO_BLOCK_ITEM = ITEMS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.item.TriffidGooBlockItem(AntarchyNeoforgeBlocks.TRIFFID_GOO_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PALE_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.PALE_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.NYXITE_SPIKE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POTENT_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.POTENT_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UMBRAL_MOSS_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.UMBRAL_MOSS_CARPET);
    public static final DeferredItem<StandingAndWallBlockItem> DREAM_TORCH_ITEM = ITEMS.register("dream_torch",
            () -> new StandingAndWallBlockItem(AntarchyNeoforgeBlocks.DREAM_TORCH.get(), AntarchyNeoforgeBlocks.DREAM_WALL_TORCH.get(), new Item.Properties(), net.minecraft.core.Direction.UP));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_LANTERN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_CAMPFIRE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.DREAM_CAMPFIRE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BED_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.BED_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CREEPING_HORROR_EGGS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.CREEPING_HORROR_EGGS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LURKING_TERROR_EGGS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.LURKING_TERROR_EGGS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WASP_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.WASP_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> HUSHWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyNeoforgeBlocks.HUSHWEED);
    public static final DeferredItem<BucketItem> ICHOR_BUCKET = ITEMS.register("ichor_bucket",
            () -> new BucketItem(AntarchyNeoforgeMisc.ICHOR.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> ANTIWATER_BUCKET = ITEMS.register("antiwater_bucket",
            () -> new BucketItem(AntarchyNeoforgeMisc.ANTIWATER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.CloudBucketItem> CLOUD_BUCKET = ITEMS.register("cloud_bucket",
            () -> new com.craisinlord.antarchy.content.item.CloudBucketItem(AntarchyNeoforgeBlocks.CLOUD_BLOCK.get(), new Item.Properties().craftRemainder(Items.BUCKET)));
    public static final DeferredItem<BloodCrystalShardItem> BLOOD_CRYSTAL_SHARD = ITEMS.register("blood_crystal_shard",
            () -> new BloodCrystalShardItem(new Item.Properties()));
    public static final DeferredItem<Item> BLOOD_CRYSTAL_APPLE = ITEMS.register("blood_crystal_apple",
            () -> new BloodCrystalAppleItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.2f)
                            .effect(() -> new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1), 1.0f)
                            .alwaysEdible()
                            .build())));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_HELMET = ITEMS.register("blood_crystal_helmet",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_CHESTPLATE = ITEMS.register("blood_crystal_chestplate",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_LEGGINGS = ITEMS.register("blood_crystal_leggings",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_BOOTS = ITEMS.register("blood_crystal_boots",
            () -> new BloodCrystalArmorItem(BLOOD_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalKatanaItem> BLOOD_CRYSTAL_KATANA = ITEMS.register("blood_crystal_katana",
            () -> new BloodCrystalKatanaItem(
                    BLOOD_CRYSTAL_KATANA_TIER,
                    new Item.Properties().stacksTo(1).durability(1200).rarity(Rarity.RARE),
                    AntarchySettings.bloodCrystalKatanaAttackDamage(),
                    -2.2F
            ));
    public static final DeferredItem<Item> MANTIS_CLAW = ITEMS.registerSimpleItem("mantis_claw", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> KING_SCALE = ITEMS.register("king_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> QUEEN_SCALE = ITEMS.register("queen_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> JUMPY_BUG_LEG = ITEMS.registerSimpleItem("jumpy_bug_leg", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<JumpyBootsItem> JUMPY_BOOTS = ITEMS.register("jumpy_boots",
            () -> new JumpyBootsItem(JUMPY_BOOTS_ARMOR_MATERIAL, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))));
    public static final DeferredItem<Item> BRUTALFLY_WING = ITEMS.registerSimpleItem("brutalfly_wing", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BrutalflyElytraItem> BRUTALFLY_ELYTRA = ITEMS.register("brutalfly_elytra",
            () -> new BrutalflyElytraItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(480)));
    public static final DeferredItem<Item> CORNEA_EAR = ITEMS.register("cornea_ear",
            () -> new CorneaEarItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.4F)
                            .build())));
    public static final DeferredItem<Item> TRIFFID_GOO = ITEMS.registerSimpleItem("triffid_goo",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> VORTEX_EYE = ITEMS.register("vortex_eye",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> RAW_URANIUM_SCRAP = ITEMS.registerSimpleItem("raw_uranium_scrap", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_TITANIUM_SCRAP = ITEMS.registerSimpleItem("raw_titanium_scrap", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MUD_PIE = ITEMS.register("mud_pie",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.9F)
                            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F)
                            .build())));
    public static final DeferredItem<RainbowSugarItem> RAINBOW_SUGAR = ITEMS.register("rainbow_sugar",
            () -> new RainbowSugarItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.0F).alwaysEdible().build())));
    public static final DeferredItem<Item> URANIUM_NUGGET = ITEMS.registerSimpleItem("uranium_nugget", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TITANIUM_NUGGET = ITEMS.registerSimpleItem("titanium_nugget", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot", new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
    public static final DeferredItem<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot", new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
    public static final DeferredItem<Item> MOLEVORE_NOSE = ITEMS.registerSimpleItem("molevore_nose", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> MOLEWORM_ITEM = ITEMS.register("moleworm",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.0F)
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)
                            .build())));
    public static final DeferredItem<Item> CLOUD_SHARK_FIN = ITEMS.registerSimpleItem("cloud_shark_fin", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CloudSharkFinSoupItem> CLOUD_SHARK_FIN_SOUP = ITEMS.register("cloud_shark_fin_soup",
            () -> new CloudSharkFinSoupItem(new Item.Properties()
                    .stacksTo(1)
                    .craftRemainder(Items.BOWL)
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.8F)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
                            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0), 1.0F)
                            .build())));
    public static final DeferredItem<Item> KRAKEN_TOOTH = ITEMS.register("kraken_tooth",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BASILISK_FANG = ITEMS.registerSimpleItem("basilisk_fang", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BasiliskDaggerItem> BASILISK_DAGGER = ITEMS.register("basilisk_dagger",
            () -> new BasiliskDaggerItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE), 4, -1.8F));
    public static final DeferredItem<Item> EMPEROR_SCORPION_STINGER = ITEMS.registerSimpleItem("emperor_scorpion_stinger", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<ScorpionWhipItem> SCORPION_WHIP = ITEMS.register("scorpion_whip",
            () -> new ScorpionWhipItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE).durability(384)));
    public static final DeferredItem<MogglesItem> MOGGLES = ITEMS.register("moggles",
            () -> new MogglesItem(MOGGLES_ARMOR_MATERIAL, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.FallenKingCrownItem> FALLEN_KING_CROWN = ITEMS.register("fallen_king_crown",
            () -> new com.craisinlord.antarchy.content.item.FallenKingCrownItem(
                    FALLEN_KING_CROWN_ARMOR_MATERIAL,
                    new Item.Properties().rarity(Rarity.RARE)
            ));
    public static final DeferredItem<ArmorItem> ULTIMATE_HELMET = ITEMS.register("ultimate_helmet",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.HELMET.getDurability(41))));
    public static final DeferredItem<ArmorItem> ULTIMATE_CHESTPLATE = ITEMS.register("ultimate_chestplate",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(41))));
    public static final DeferredItem<ArmorItem> ULTIMATE_LEGGINGS = ITEMS.register("ultimate_leggings",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(41))));
    public static final DeferredItem<ArmorItem> ULTIMATE_BOOTS = ITEMS.register("ultimate_boots",
            () -> new UltimateArmorItem(ULTIMATE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(41))));
    public static final DeferredItem<UltimateSwordItem> ULTIMATE_SWORD = ITEMS.register("ultimate_sword",
            () -> new UltimateSwordItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateSwordAttackDamage, -2.4F));
    public static final DeferredItem<UltimatePickaxeItem> ULTIMATE_PICKAXE = ITEMS.register("ultimate_pickaxe",
            () -> new UltimatePickaxeItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimatePickaxeAttackDamage, -2.8F));
    public static final DeferredItem<UltimateAxeItem> ULTIMATE_AXE = ITEMS.register("ultimate_axe",
            () -> new UltimateAxeItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateAxeAttackDamage, -3.0F));
    public static final DeferredItem<UtlimateShovelItem> ULTIMATE_SHOVEL = ITEMS.register("ultimate_shovel",
            () -> new UtlimateShovelItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateShovelAttackDamage, -3.0F));
    public static final DeferredItem<UltimateHoeItem> ULTIMATE_HOE = ITEMS.register("ultimate_hoe",
            () -> new UltimateHoeItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateHoeAttackDamage, 0.0F));
    public static final DeferredItem<UltimateBowItem> ULTIMATE_BOW = ITEMS.register("ultimate_bow",
            () -> new UltimateBowItem(new Item.Properties().stacksTo(1).durability(768).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<UltimateCrossbowItem> ULTIMATE_CROSSBOW = ITEMS.register("ultimate_crossbow",
            () -> new UltimateCrossbowItem(new Item.Properties().stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_HELMET = ITEMS.register("nightmare_helmet",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.HELMET.getDurability(41))));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_CHESTPLATE = ITEMS.register("nightmare_chestplate",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(41))));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_LEGGINGS = ITEMS.register("nightmare_leggings",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(41))));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_BOOTS = ITEMS.register("nightmare_boots",
            () -> new NightmareArmorItem(NIGHTMARE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(41))));
    public static final DeferredItem<NightmareSwordItem> NIGHTMARE_SWORD = ITEMS.register("nightmare_sword",
            () -> new NightmareSwordItem(ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), -2.4F));
    public static final DeferredItem<SizeRayItem> SHRINK_RAY = ITEMS.register("shrink_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyNeoforgeEntites.SHRINK_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.SHRINK,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/shrink_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/shrink_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/shrink_ray.animation.json"),
                    "shrink_ray_active"
            ));
    public static final DeferredItem<SizeRayItem> GROWTH_RAY = ITEMS.register("growth_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyNeoforgeEntites.GROWTH_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.GROWTH,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/growth_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/growth_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/growth_ray.animation.json"),
                    "growth_ray_active"
            ));
    public static final DeferredItem<GravityGunItem> GRAVITY_GUN = ITEMS.register("gravity_gun",
            () -> new GravityGunItem(new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<SquidzookaItem> SQUIDZOOKA = ITEMS.register("squidzooka",
            () -> new SquidzookaItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)));
    public static final DeferredItem<BattleAxeItem> BATTLE_AXE = ITEMS.register("battle_axe",
            () -> new BattleAxeItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant(),
                    AntarchySettings::battleAxeAttackDamage, -3.1F));
    public static final DeferredItem<DiamondMinecartItem> DIAMOND_MINECART_ITEM = ITEMS.register("diamond_minecart",
            () -> new DiamondMinecartItem(AntarchyNeoforgeEntites.DIAMOND_MINECART, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<ReverieBottleItem> REVERIE_BOTTLE = ITEMS.register("reverie_bottle",
            () -> new ReverieBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BIG_BERTHA_BLADE = ITEMS.registerSimpleItem("big_bertha_blade",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> BIG_BERTHA_HANDLE = ITEMS.registerSimpleItem("big_bertha_handle",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> BIG_BERTHA_HILT = ITEMS.registerSimpleItem("big_bertha_hilt",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<BigBerthaItem> BIG_BERTHA = ITEMS.register("big_bertha",
            () -> new BigBerthaItem(Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    // Spawn eggs
    public static final DeferredItem<DeferredSpawnEggItem> EASTER_BUNNY_SPAWN_EGG = ITEMS.register("easter_bunny_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.EASTER_BUNNY, 0xFFF2B2, 0xFF85B5, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FLYING_SQUIRREL_SPAWN_EGG = ITEMS.register("flying_squirrel_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.FLYING_SQUIRREL, 0x7D6649, 0xDCC59C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CATERPILLAR_SPAWN_EGG = ITEMS.register("caterpillar_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.CATERPILLAR, 0xA8D96A, 0xF4E04D, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.BUTTERFLY, 0x111111, 0xFF7A00, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> REVERIE_SPAWN_EGG = ITEMS.register("reverie_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.REVERIE, 0xF2F2F2, 0xBFC3C7, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BRUTALFLY_SPAWN_EGG = ITEMS.register("brutalfly_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.BRUTALFLY, 0x4A2214, 0xFF8A1D, new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<DeferredSpawnEggItem> RED_ANT_SPAWN_EGG = ITEMS.register("red_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.RED_ANT, 0xA31818, 0x2B0909, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BROWN_ANT_SPAWN_EGG = ITEMS.register("brown_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.BROWN_ANT, 0x6A4320, 0x26160A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> RAINBOW_ANT_SPAWN_EGG = ITEMS.register("rainbow_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.RAINBOW_ANT, 0x56D4F0, 0xF66DBB, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOLEWORM_SPAWN_EGG = ITEMS.register("moleworm_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.MOLEWORM, 0x7A6150, 0xD2B8A3, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MANTIS_SPAWN_EGG = ITEMS.register("mantis_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.MANTIS, 0xF8F8F2, 0x63B44A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOLEVORE_SPAWN_EGG = ITEMS.register("molevore_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.MOLEVORE, 0x3E2E24, 0xB67B4F, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> TRIFFID_SPAWN_EGG = ITEMS.register("triffid_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.TRIFFID, 0x4C8F3A, 0xFF2FB3, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> APPLE_COW_SPAWN_EGG = ITEMS.register("apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.APPLE_COW, 0xFF1A1A, 0x32FF32, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("golden_apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.GOLDEN_APPLE_COW, 0xFFE14A, 0x32FF32, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("enchanted_golden_apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.ENCHANTED_GOLDEN_APPLE_COW, 0x7040B6, 0xFFE14A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DR_TRAYAURUS_SPAWN_EGG = ITEMS.register("dr_trayaurus_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.DR_TRAYAURUS, 0xB7A27B, 0x4A3D29, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> WASP_SPAWN_EGG = ITEMS.register("wasp_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.WASP, 0x111111, 0xF1D800, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BOMBER_SPAWN_EGG = ITEMS.register("bomber_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.BOMBER, 0x7A7A7A, 0xB32020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> JUMPY_BUG_SPAWN_EGG = ITEMS.register("jumpy_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.JUMPY_BUG, 0x111111, 0xFF7A00, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CLOUD_SHARK_SPAWN_EGG = ITEMS.register("cloud_shark_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.CLOUD_SHARK, 0xDDEAF4, 0x7F96A8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.KRAKEN, 0x163C53, 0x4F8E99, new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<DeferredSpawnEggItem> MISSILE_SQUID_SPAWN_EGG = ITEMS.register("missile_squid_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.MISSILE_SQUID, 0xD88FA7, 0x8D5269, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> NIGHTMARE_SPAWN_EGG = ITEMS.register("nightmare_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.NIGHTMARE, 0x22121C, 0xB51B2D, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<DeferredSpawnEggItem> BED_BUG_SPAWN_EGG = ITEMS.register("bed_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.BED_BUG, 0x3B2218, 0x611111, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LUCID_SPAWN_EGG = ITEMS.register("lucid_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.LUCID, 0xE53935, 0xF4D03F, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<DeferredSpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.SCORPION, 0xA8D8FF, 0xE04B5A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.register("basilisk_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.BASILISK, 0x4A7C40, 0xD4A040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> EMPEROR_SCORPION_SPAWN_EGG = ITEMS.register("emperor_scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.EMPEROR_SCORPION, 0x1A1A0A, 0x8B2200, new Item.Properties()));
    public static final DeferredItem<LucidEyeItem> LUCID_EYE = ITEMS.register("lucid_eye",
            () -> new LucidEyeItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LucidPearlItem> LUCID_PEARL = ITEMS.register("lucid_pearl",
            () -> new LucidPearlItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
                    AntarchyNeoforgeEntites.LUCID_PEARL_PROJECTILE
            ));
    public static final DeferredItem<WaterCannonItem> WATER_CANNON = ITEMS.register("water_cannon",
            () -> new WaterCannonItem(new Item.Properties().stacksTo(1).durability(192).rarity(Rarity.RARE)));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_HELMET = ITEMS.register("primordial_helmet",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_CHESTPLATE = ITEMS.register("primordial_chestplate",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_LEGGINGS = ITEMS.register("primordial_leggings",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_BOOTS = ITEMS.register("primordial_boots",
            () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<DeferredSpawnEggItem> TORETERROR_SPAWN_EGG = ITEMS.register("toreterror_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.TORETERROR, 0x90EE90, 0x5C4033, new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<DeferredSpawnEggItem> CREEPING_HORROR_SPAWN_EGG = ITEMS.register("creeping_horror_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.CREEPING_HORROR, 0x6B3A1F, 0x6B0000, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LURKING_TERROR_SPAWN_EGG = ITEMS.register("lurking_terror_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyNeoforgeEntites.LURKING_TERROR, 0x2D5A1B, 0x8B0000, new Item.Properties()));

    private AntarchyNeoforgeItems() {}

    public static Collection<DeferredHolder<Item,? extends Item>> getItemEntries() {
        return ITEMS.getEntries();
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
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
        defense.put(ArmorItem.Type.BODY, 16);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createNightmareArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 4);
        defense.put(ArmorItem.Type.LEGGINGS, 9);
        defense.put(ArmorItem.Type.CHESTPLATE, 11);
        defense.put(ArmorItem.Type.HELMET, 4);
        defense.put(ArmorItem.Type.BODY, 11);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createBloodCrystalArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 2);
        defense.put(ArmorItem.Type.LEGGINGS, 5);
        defense.put(ArmorItem.Type.CHESTPLATE, 6);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 6);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createMogglesArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 2);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createPrimordialArmorDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 8);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createFallenKingCrownDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 2);
        return defense;
    }

    private static EnumMap<ArmorItem.Type, Integer> createJumpyBootsDefense() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 0);
        defense.put(ArmorItem.Type.BODY, 0);
        return defense;
    }
}
