package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.BloodCrystalArmorItem;
import com.craisinlord.antarchy.content.item.BloodCrystalAppleItem;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.content.item.CritterCageItem;
import com.craisinlord.antarchy.fabric.item.DeferredSpawnEggItem;
import com.craisinlord.antarchy.content.item.LucidAnchorBlockItem;
import com.craisinlord.antarchy.content.item.LucidEyeItem;
import com.craisinlord.antarchy.content.item.LucidPearlItem;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.craisinlord.antarchy.content.item.BattleAxeItem;
import com.craisinlord.antarchy.content.item.AttitudeAdjusterItem;
import com.craisinlord.antarchy.content.item.BasiliskDaggerItem;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.KrakensGraspItem;
import com.craisinlord.antarchy.content.item.AntimetalBlockItem;
import com.craisinlord.antarchy.content.item.AntimetalScaffoldingItem;
import com.craisinlord.antarchy.content.item.CloudSharkFinSoupItem;
import com.craisinlord.antarchy.content.item.CorneaEarItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.MantisClawItem;
import com.craisinlord.antarchy.content.item.DuctTapeBlockItem;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.BluestoneDustItem;
import com.craisinlord.antarchy.content.item.CeilingPlacementTooltipBlockItem;
import com.craisinlord.antarchy.content.item.CeilingPlacementTooltipItem;
import com.craisinlord.antarchy.content.item.MobComingSoonTooltipItem;
import com.craisinlord.antarchy.content.item.MogglesItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateAxeItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateHoeItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimatePickaxeItem;
import com.craisinlord.antarchy.content.item.ultimate.UtlimateShovelItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateSwordItem;
import com.craisinlord.antarchy.content.item.DiamondMinecartItem;
import com.craisinlord.antarchy.content.item.ReverieBottleItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipItem;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import com.craisinlord.antarchy.content.item.SquidzookaItem;
import com.craisinlord.antarchy.content.item.RpoLauncherItem;
import com.craisinlord.antarchy.content.item.SimpleToolTier;
import com.craisinlord.antarchy.content.item.ultimate.UltimateArmorItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateBowItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateCrossbowItem;
import com.craisinlord.antarchy.content.item.NightmareArmorItem;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import com.craisinlord.antarchy.content.item.OuranwoodBoatOnlyItem;
import com.craisinlord.antarchy.content.item.OuranwoodChestBoatItem;
import com.craisinlord.antarchy.content.item.RainbowSugarItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Direction;

public final class AntarchyFabricItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Antarchy.MODID);


    private static final Tier ULTIMATE_TIER = new SimpleToolTier(
            3072,
            10.5F,
            0.0F,
            Tiers.NETHERITE.getIncorrectBlocksForDrops(),
            25,
            AntarchyFabricItems::ultimateRepairIngredient
    );


    private static final Tier BLOOD_CRYSTAL_KATANA_TIER = new SimpleToolTier(
            1200,
            8.0F,
            0.0F,
            Tiers.DIAMOND.getIncorrectBlocksForDrops(),
            18,
            AntarchyFabricItems::bloodCrystalRepairIngredient
    );
    private static final Tier ATTITUDE_ADJUSTER_TIER = new SimpleToolTier(
            3072,
            7.0F,
            0.0F,
            Tiers.NETHERITE.getIncorrectBlocksForDrops(),
            18,
            () -> Ingredient.of(AntarchyFabricItems.HERCULES_HORN.get())
    );


    public static final DeferredItem<Item> NIGHTMARE_SCALE = ITEMS.registerSimpleItem("nightmare_scale", new Item.Properties().rarity(Rarity.RARE).fireResistant());


    public static final DeferredItem<Item> PRIMORDIAL_SCUTE = ITEMS.registerSimpleItem("primordial_scute",
            new Item.Properties().rarity(Rarity.UNCOMMON));



    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DUPLICATOR_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOSSY_OURANWOOD_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOSSY_OURANWOOD_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STRIPPED_OURANWOOD_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STRIPPED_OURANWOOD_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_PLANKS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_FENCE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_FENCE_GATE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_DOOR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_TRAPDOOR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_PRESSURE_PLATE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_BUTTON);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_LEAVES);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_ACORN = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.OURANWOOD_ACORN_BLOCK);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_PEACH_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STRIPPED_PEACH_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_PEACH_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STRIPPED_PEACH_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_PLANKS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_FENCE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_FENCE_GATE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_DOOR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_TRAPDOOR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_PRESSURE_PLATE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_BUTTON);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_LEAVES);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PEACH_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PEACH_SAPLING);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_NADIR_LOG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STRIPPED_NADIR_LOG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_NADIR_WOOD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STRIPPED_NADIR_WOOD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_PLANKS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_FENCE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_FENCE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_FENCE_GATE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_DOOR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_TRAPDOOR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_PRESSURE_PLATE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_BUTTON);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_VEIL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_VEIL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NADIR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NADIR_SAPLING);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> ORANGE_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.ORANGE_MILKWEED);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PINK_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PINK_MILKWEED);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CAMELLIA_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CAMELLIA);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SPIDER_LILY_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SPIDER_LILY);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> GIANT_LILY_PAD_ITEM = ITEMS.register("giant_lily_pad",
            () -> new com.craisinlord.antarchy.content.item.GiantLilyPadItem(AntarchyFabricBlocks.GIANT_LILY_PAD.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SEASHELL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SEASHELL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LUCID_ANCHOR_ITEM = ITEMS.register("lucid_anchor",
            () -> new LucidAnchorBlockItem(AntarchyFabricBlocks.LUCID_ANCHOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOTUS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.LOTUS);


    public static final DeferredItem<net.minecraft.world.item.SignItem> OURANWOOD_SIGN_ITEM = ITEMS.register("ouranwood_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), AntarchyFabricBlocks.OURANWOOD_SIGN.get(), AntarchyFabricBlocks.OURANWOOD_WALL_SIGN.get()));


    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> OURANWOOD_HANGING_SIGN_ITEM = ITEMS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(AntarchyFabricBlocks.OURANWOOD_HANGING_SIGN.get(), AntarchyFabricBlocks.OURANWOOD_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> OURANWOOD_BOAT = ITEMS.register("ouranwood_boat",
            () -> new OuranwoodBoatOnlyItem(AntarchyFabricEntities.OURANWOOD_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<Item> OURANWOOD_CHEST_BOAT = ITEMS.register("ouranwood_chest_boat",
            () -> new OuranwoodChestBoatItem(AntarchyFabricEntities.OURANWOOD_CHEST_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<net.minecraft.world.item.SignItem> PEACH_SIGN_ITEM = ITEMS.register("peach_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), AntarchyFabricBlocks.PEACH_SIGN.get(), AntarchyFabricBlocks.PEACH_WALL_SIGN.get()));


    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> PEACH_HANGING_SIGN_ITEM = ITEMS.register("peach_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(AntarchyFabricBlocks.PEACH_HANGING_SIGN.get(), AntarchyFabricBlocks.PEACH_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> PEACH_BOAT = ITEMS.register("peach_boat",
            () -> new com.craisinlord.antarchy.content.item.PeachBoatOnlyItem(AntarchyFabricEntities.PEACH_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<Item> PEACH_CHEST_BOAT = ITEMS.register("peach_chest_boat",
            () -> new com.craisinlord.antarchy.content.item.PeachChestBoatItem(AntarchyFabricEntities.PEACH_CHEST_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<net.minecraft.world.item.SignItem> NADIR_SIGN_ITEM = ITEMS.register("nadir_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), AntarchyFabricBlocks.NADIR_SIGN.get(), AntarchyFabricBlocks.NADIR_WALL_SIGN.get()));


    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> NADIR_HANGING_SIGN_ITEM = ITEMS.register("nadir_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(AntarchyFabricBlocks.NADIR_HANGING_SIGN.get(), AntarchyFabricBlocks.NADIR_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> NADIR_BOAT = ITEMS.register("nadir_boat",
            () -> new com.craisinlord.antarchy.content.item.NadirBoatOnlyItem(AntarchyFabricEntities.NADIR_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<Item> NADIR_CHEST_BOAT = ITEMS.register("nadir_chest_boat",
            () -> new com.craisinlord.antarchy.content.item.NadirChestBoatItem(AntarchyFabricEntities.NADIR_CHEST_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<CritterCageItem> CRITTER_CAGE = ITEMS.register("critter_cage",
            () -> new CritterCageItem(new Item.Properties().stacksTo(16)));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DUPLICATOR_SAPLING);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> RED_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.RED_ANT_NEST);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROWN_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROWN_ANT_NEST);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAINBOW_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.RAINBOW_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TERMITE_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.TERMITE_NEST);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.URANIUM_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DEEPSLATE_URANIUM_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.TITANIUM_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DEEPSLATE_TITANIUM_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<Item> BLUESTONE_DUST = ITEMS.register("bluestone",
            () -> new BluestoneDustItem(AntarchyFabricBlocks.BLUESTONE_WIRE.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUESTONE_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BLUESTONE_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUESTONE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BLUESTONE_BLOCK);
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> BLUESTONE_REPEATER_ITEM = ITEMS.register("bluestone_repeater",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.BLUESTONE_REPEATER.get(), new Item.Properties()));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> BLUESTONE_COMPARATOR_ITEM = ITEMS.register("bluestone_comparator",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.BLUESTONE_COMPARATOR.get(), new Item.Properties()));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> BLUESTONE_TORCH_ITEM = ITEMS.register("bluestone_torch",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.BLUESTONE_TORCH.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUESTONE_LAMP_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BLUESTONE_LAMP);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.URANIUM_BLOCK, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.TITANIUM_BLOCK, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_URANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.RAW_URANIUM_BLOCK, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_TITANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.RAW_TITANIUM_BLOCK, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_URANIUM, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_TITANIUM, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_URANIUM_SLAB, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_TITANIUM_SLAB, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_URANIUM_STAIRS, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_TITANIUM_STAIRS, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_URANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHISELED_URANIUM, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHISELED_TITANIUM, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BULB_ITEM = ITEMS.register("uranium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyFabricBlocks.URANIUM_BULB.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BULB_ITEM = ITEMS.register("titanium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(AntarchyFabricBlocks.TITANIUM_BULB.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.URANIUM_DOOR, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.TITANIUM_DOOR, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.URANIUM_TRAPDOOR, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.TITANIUM_TRAPDOOR, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BARS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.URANIUM_BARS, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BARS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.TITANIUM_BARS, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_ITEM = ITEMS.register("antimetal",
            () -> new AntimetalBlockItem(AntarchyFabricBlocks.ANTIMETAL.get(), new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_ITEM = ITEMS.register("polished_antimetal",
            () -> new AntimetalBlockItem(AntarchyFabricBlocks.POLISHED_ANTIMETAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_STAIRS_ITEM = ITEMS.register("antimetal_stairs",
            () -> new AntimetalBlockItem(AntarchyFabricBlocks.ANTIMETAL_STAIRS.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_SLAB_ITEM = ITEMS.register("antimetal_slab",
            () -> new AntimetalBlockItem(AntarchyFabricBlocks.ANTIMETAL_SLAB.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_STAIRS_ITEM = ITEMS.register("polished_antimetal_stairs",
            () -> new AntimetalBlockItem(AntarchyFabricBlocks.POLISHED_ANTIMETAL_STAIRS.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_SLAB_ITEM = ITEMS.register("polished_antimetal_slab",
            () -> new AntimetalBlockItem(AntarchyFabricBlocks.POLISHED_ANTIMETAL_SLAB.get(), new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_SCAFFOLDING_ITEM = ITEMS.register("antimetal_scaffolding",
            () -> new AntimetalScaffoldingItem(AntarchyFabricBlocks.ANTIMETAL_SCAFFOLDING.get(), new Item.Properties()));


    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_RAIL_ITEM = ITEMS.register("antimetal_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.ANTIMETAL_RAIL.get(), new Item.Properties()));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_POWERED_RAIL_ITEM = ITEMS.register("antimetal_powered_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.ANTIMETAL_POWERED_RAIL.get(), new Item.Properties()));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_DETECTOR_RAIL_ITEM = ITEMS.register("antimetal_detector_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.ANTIMETAL_DETECTOR_RAIL.get(), new Item.Properties()));
    public static final DeferredItem<CeilingPlacementTooltipBlockItem> ANTIMETAL_ACTIVATOR_RAIL_ITEM = ITEMS.register("antimetal_activator_rail",
            () -> new CeilingPlacementTooltipBlockItem(AntarchyFabricBlocks.ANTIMETAL_ACTIVATOR_RAIL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UPPER_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.UPPER);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMALL_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SMALL_BLOOD_CRYSTAL_BUD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MEDIUM_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MEDIUM_BLOOD_CRYSTAL_BUD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> LARGE_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.LARGE_BLOOD_CRYSTAL_BUD);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BUDDING_BLOOD_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BUDDING_BLOOD_CRYSTAL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_ITEM = ITEMS.register("blood_crystal_block",
            () -> new net.minecraft.world.item.BlockItem(AntarchyFabricBlocks.BLOOD_CRYSTAL.get(), new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BLOOD_CRYSTAL_CRYSTAL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SAND_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_SAND);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIGRAVEL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.ANTIGRAVEL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOAM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.LOAM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MUCUS_ITEM = ITEMS.register("mucus",
            () -> new com.craisinlord.antarchy.content.item.MucusBlockItem(AntarchyFabricBlocks.MUCUS.get(), new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_SANDSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHISELED_DREAM_SANDSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_DREAM_SANDSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SMOOTH_DREAM_SANDSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_SANDSTONE_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_SANDSTONE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_SANDSTONE_WALL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SMOOTH_DREAM_SANDSTONE_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SMOOTH_DREAM_SANDSTONE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CUT_DREAM_SANDSTONE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEAD_STAR_CORAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DEAD_STAR_CORAL_BLOCK);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEAD_STAR_CORAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DEAD_STAR_CORAL);


    public static final DeferredItem<StandingAndWallBlockItem> DEAD_STAR_CORAL_FAN_ITEM = ITEMS.register("dead_star_coral_fan",
            () -> new StandingAndWallBlockItem(AntarchyFabricBlocks.DEAD_STAR_CORAL_FAN.get(), AntarchyFabricBlocks.DEAD_STAR_CORAL_WALL_FAN.get(), new Item.Properties(), Direction.UP));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STAR_CORAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STAR_CORAL_BLOCK);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> STAR_CORAL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.STAR_CORAL);


    public static final DeferredItem<StandingAndWallBlockItem> STAR_CORAL_FAN_ITEM = ITEMS.register("star_coral_fan",
            () -> new StandingAndWallBlockItem(AntarchyFabricBlocks.STAR_CORAL_FAN.get(), AntarchyFabricBlocks.STAR_CORAL_WALL_FAN.get(), new Item.Properties(), Direction.UP));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUCT_TAPE_ITEM = ITEMS.register("duct_tape",
            () -> new DuctTapeBlockItem(AntarchyFabricBlocks.DUCT_TAPE.get(), new Item.Properties().stacksTo(1)));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_ROOTED_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.INFESTED_ROOTED_DIRT);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_COARSE_DIRT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.INFESTED_COARSE_DIRT);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MYRMITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOMITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BIOMITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOMITE_TURF_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BIOMITE_TURF);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOWART_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BIOWART);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BIOWART_TENDRILS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BIOWART_TENDRILS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_BROODSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_BROODSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHISELED_BROODSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_BROODSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_BROODSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_BROODSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_BROODSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_PILLAR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MYRMITE_COAL_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MYRMITE_COAL_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_URANIUM_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROODSTONE_TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BROODSTONE_TITANIUM_ORE, new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_NYXITE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHISELED_NYXITE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHITIN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHITIN_BLOCK);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_WALL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_NYXITE_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_NYXITE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_NYXITE_WALL);
    public static final DeferredItem<com.craisinlord.antarchy.content.item.CeilingCompatiblePlacementTooltipBlockItem> POLISHED_NYXITE_PRESSURE_PLATE_ITEM = ITEMS.register("polished_nyxite_pressure_plate",
            () -> new com.craisinlord.antarchy.content.item.CeilingCompatiblePlacementTooltipBlockItem(AntarchyFabricBlocks.POLISHED_NYXITE_PRESSURE_PLATE.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_NYXITE_BUTTON);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_BRICK_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_BRICK_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_PILLAR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_SHELLSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_BRICKS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHISELED_SHELLSTONE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICKS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> CRACKED_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CRACKED_SHELLSTONE_BRICKS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICK_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICK_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICK_WALL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_WALL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_SHELLSTONE_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_SHELLSTONE_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POLISHED_SHELLSTONE_WALL);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_BRICK_STAIRS);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_BRICK_SLAB);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_PILLAR_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SHELLSTONE_PILLAR);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> TRIFFID_GOO_BLOCK_ITEM = ITEMS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.item.TriffidGooBlockItem(AntarchyFabricBlocks.TRIFFID_GOO_BLOCK.get(), new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> PALE_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.PALE_NYXITE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.NYXITE_SPIKE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHITIN_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CHITIN_SPIKE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> POTENT_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.POTENT_NYXITE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.UMBRAL_MOSS_BLOCK);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.UMBRAL_MOSS_CARPET);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> AMBER_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.AMBER_MOSS_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> AMBER_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.AMBER_MOSS_CARPET);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> AMBER_LICHEN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.AMBER_LICHEN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BILE_VEIN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BILE_VEIN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CREEPVINE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CREEPVINE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUSH_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BLUSH_MOSS_BLOCK);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLUSH_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BLUSH_MOSS_CARPET);


    public static final DeferredItem<com.craisinlord.antarchy.content.item.DreamTorchItem> DREAM_TORCH_ITEM = ITEMS.register("dream_torch",
            () -> new com.craisinlord.antarchy.content.item.DreamTorchItem(AntarchyFabricBlocks.DREAM_TORCH.get(), AntarchyFabricBlocks.DREAM_WALL_TORCH.get(), AntarchyFabricBlocks.DREAM_CEILING_TORCH.get(), new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_LANTERN);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_CAMPFIRE_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.DREAM_CAMPFIRE);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> BED_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.BED_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CREEPING_HORROR_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.CREEPING_HORROR_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> JUMPY_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.JUMPY_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SPIT_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.SPIT_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> JERRY_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.JERRY_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LURKING_TERROR_EGG_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.LURKING_TERROR_EGG);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> WASP_NEST_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.WASP_NEST);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> HUSHWEED_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.HUSHWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> HANGING_CREEPROOTS_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.HANGING_CREEPROOTS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GLOWCAP_MUSHROOM_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.GLOWCAP_MUSHROOM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GLOWCAP_MUSHROOM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.GLOWCAP_MUSHROOM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOLTING_VINES_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.MOLTING_VINES);


    public static final DeferredItem<BucketItem> BILE_BUCKET = ITEMS.register("bile_bucket",
            () -> new BucketItem(AntarchyFabricMisc.BILE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));


    public static final DeferredItem<BucketItem> ICHOR_BUCKET = ITEMS.register("ichor_bucket",
            () -> new BucketItem(AntarchyFabricMisc.ICHOR.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));


    public static final DeferredItem<BucketItem> ANTIWATER_BUCKET = ITEMS.register("antiwater_bucket",
            () -> new BucketItem(AntarchyFabricMisc.ANTIWATER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));


    public static final DeferredItem<BucketItem> LUMEN_BUCKET = ITEMS.register("lumen_bucket",
            () -> new BucketItem(AntarchyFabricMisc.LUMEN.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));


    public static final DeferredItem<net.minecraft.world.item.BlockItem> LUMEN_FROGLIGHT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.LUMEN_FROGLIGHT);


    public static final DeferredItem<net.minecraft.world.item.BlockItem> ROSEATE_FROGLIGHT_ITEM = ITEMS.registerSimpleBlockItem(AntarchyFabricBlocks.ROSEATE_FROGLIGHT);


    public static final DeferredItem<com.craisinlord.antarchy.content.item.CloudBucketItem> CLOUD_BUCKET = ITEMS.register("cloud_bucket",
            () -> new com.craisinlord.antarchy.content.item.CloudBucketItem(AntarchyFabricBlocks.CLOUD_BLOCK.get(), new Item.Properties().craftRemainder(Items.BUCKET)));


    public static final DeferredItem<com.craisinlord.antarchy.content.item.BloodCrystalShardItem> BLOOD_CRYSTAL_SHARD = ITEMS.register("blood_crystal_shard",
            () -> new com.craisinlord.antarchy.content.item.BloodCrystalShardItem(new Item.Properties()));


    public static final DeferredItem<Item> BLOOD_CRYSTAL_APPLE = ITEMS.register("blood_crystal_apple",
            () -> new BloodCrystalAppleItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.2f)
                            .effect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1), 1.0f)
                            .alwaysEdible()
                            .build())));


    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_HELMET = ITEMS.register("blood_crystal_helmet",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_CHESTPLATE = ITEMS.register("blood_crystal_chestplate",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_LEGGINGS = ITEMS.register("blood_crystal_leggings",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_BOOTS = ITEMS.register("blood_crystal_boots",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<Item> TIGERS_EYE = ITEMS.registerSimpleItem("tigers_eye", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_HELMET = ITEMS.register("tigers_eye_helmet",
            () -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.TIGERS_EYE_ARMOR_MATERIAL.get()), ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_CHESTPLATE = ITEMS.register("tigers_eye_chestplate",
            () -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.TIGERS_EYE_ARMOR_MATERIAL.get()), ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_LEGGINGS = ITEMS.register("tigers_eye_leggings",
            () -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.TIGERS_EYE_ARMOR_MATERIAL.get()), ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.TigerEyeArmorItem> TIGERS_EYE_BOOTS = ITEMS.register("tigers_eye_boots",
            () -> new com.craisinlord.antarchy.content.item.TigerEyeArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.TIGERS_EYE_ARMOR_MATERIAL.get()), ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<BloodCrystalKatanaItem> BLOOD_CRYSTAL_KATANA = ITEMS.register("blood_crystal_katana",
            () -> new BloodCrystalKatanaItem(
                    BLOOD_CRYSTAL_KATANA_TIER,
                    new Item.Properties().stacksTo(1).durability(1200).rarity(Rarity.RARE),
                    AntarchySettings.bloodCrystalKatanaAttackDamage(),
                    -2.2F
            ));


    public static final DeferredItem<MantisClawItem> MANTIS_CLAW = ITEMS.register("mantis_claw",
            () -> new MantisClawItem(Tiers.IRON, new Item.Properties().stacksTo(1).durability(50).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> HERCULES_HORN = ITEMS.registerSimpleItem("hercules_horn", new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<Item> KING_SCALE = ITEMS.register("king_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<Item> QUEEN_SCALE = ITEMS.register("queen_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<Item> CHITIN = ITEMS.registerSimpleItem("chitin", new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<Item> JERRY_NUCLEUS = ITEMS.register("jerry_nucleus",
            () -> new com.craisinlord.antarchy.content.item.JerryNucleusItem(new Item.Properties().rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                            .build())));


    public static final DeferredItem<Item> STINK_BUG_ITEM = ITEMS.registerSimpleItem("stink_bug", new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<Item> JUMPY_BUG_LEG = ITEMS.registerSimpleItem("jumpy_bug_leg", new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<com.craisinlord.antarchy.content.item.JumpyBootsItem> JUMPY_BOOTS = ITEMS.register("jumpy_boots",
            () -> new com.craisinlord.antarchy.content.item.JumpyBootsItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.JUMPY_BOOTS_ARMOR_MATERIAL.get()),
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))
            ));


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


    public static final DeferredItem<Item> CORN = ITEMS.register("corn",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(3)
                            .saturationModifier(0.6F)
                            .build())));


    public static final DeferredItem<net.minecraft.world.item.ItemNameBlockItem> CORN_SEEDS = ITEMS.register("corn_seeds",
            () -> new net.minecraft.world.item.ItemNameBlockItem(AntarchyFabricBlocks.CORN_CROP.get(), new Item.Properties()));


    public static final DeferredItem<Item> RAW_VENISON = ITEMS.register("raw_venison",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.3F)
                            .build())));
    public static final DeferredItem<Item> BROODFRUIT = ITEMS.register("broodfruit",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                            .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0), 1.0F)
                            .build())));
    public static final DeferredItem<Item> RAW_BUG_MEAT = ITEMS.register("raw_bug_meat",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.1F)
                            .effect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)
                            .build())));
    public static final DeferredItem<Item> COOKED_BUG_MEAT = ITEMS.register("cooked_bug_meat",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.8F)
                            .build())));


    public static final DeferredItem<Item> COOKED_VENISON = ITEMS.register("cooked_venison",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationModifier(0.8F)
                            .build())));


    public static final DeferredItem<Item> PEACH = ITEMS.register("peach",
            () -> new com.craisinlord.antarchy.content.item.PeachItem());


    public static final DeferredItem<Item> PEACH_PIE = ITEMS.register("peach_pie",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.3F)
                            .build())));


    public static final DeferredItem<Item> CORNBREAD = ITEMS.register("cornbread",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.9F)
                            .build())));


    public static final DeferredItem<Item> POPCORN = ITEMS.register("popcorn",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.1F)
                            .build())));


    public static final DeferredItem<Item> RAW_CORNDOG = ITEMS.register("raw_corndog",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.7F)
                            .build())));


    public static final DeferredItem<Item> COOKED_CORNDOG = ITEMS.register("cooked_corndog",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(14)
                            .saturationModifier(1.1F)
                            .build())));


    public static final DeferredItem<Item> HIGH_FRUCTOSE_CORN_SYRUP = ITEMS.register("high_fructose_corn_syrup",
            () -> new com.craisinlord.antarchy.content.item.HighFructoseCornSyrupItem(new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationModifier(0.0F)
                            .alwaysEdible()
                            .build())));


    public static final DeferredItem<com.craisinlord.antarchy.content.item.RootBeerItem> ROOT_BEER = ITEMS.register("root_beer",
            () -> new com.craisinlord.antarchy.content.item.RootBeerItem(new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.4F)
                            .effect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                            .alwaysEdible()
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
                            .effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F)
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
                            .effect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)
                            .build())));


    public static final DeferredItem<Item> CLOUD_SHARK_FIN = ITEMS.registerSimpleItem("cloud_shark_fin", new Item.Properties().rarity(Rarity.UNCOMMON));


    public static final DeferredItem<CloudSharkFinSoupItem> CLOUD_SHARK_FIN_SOUP = ITEMS.register("cloud_shark_fin_soup",
            () -> new CloudSharkFinSoupItem(new Item.Properties()
                    .stacksTo(1)
                    .craftRemainder(Items.BOWL)
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.8F)
                            .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
                            .effect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0), 1.0F)
                            .build())));


    public static final DeferredItem<Item> KRAKEN_TOOTH = ITEMS.register("kraken_tooth",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));


    public static final DeferredItem<Item> BASILISK_FANG = ITEMS.registerSimpleItem("basilisk_fang", new Item.Properties().rarity(Rarity.RARE));


    public static final DeferredItem<BasiliskDaggerItem> BASILISK_DAGGER = ITEMS.register("basilisk_dagger",
            () -> new BasiliskDaggerItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE), 4, -1.8F));


    public static final DeferredItem<Item> EMPEROR_SCORPION_STINGER = ITEMS.registerSimpleItem("emperor_scorpion_stinger", new Item.Properties().rarity(Rarity.RARE));


    public static final DeferredItem<ScorpionWhipItem> SCORPION_WHIP = ITEMS.register("scorpion_whip",
            () -> new ScorpionWhipItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE).durability(384)));


    public static final DeferredItem<MogglesItem> MOGGLES = ITEMS.register("moggles",
            () -> new MogglesItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.MOGGLES_ARMOR_MATERIAL.get()), new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));


    public static final DeferredItem<com.craisinlord.antarchy.content.item.FallenKingCrownItem> FALLEN_KING_CROWN = ITEMS.register("fallen_king_crown",
            () -> new com.craisinlord.antarchy.content.item.FallenKingCrownItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.FALLEN_KING_CROWN_ARMOR_MATERIAL.get()),
                    new Item.Properties().rarity(Rarity.RARE)
            ));


    public static final DeferredItem<ArmorItem> ULTIMATE_HELMET = ITEMS.register("ultimate_helmet",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.HELMET.getDurability(62))
            ));


    public static final DeferredItem<ArmorItem> ULTIMATE_CHESTPLATE = ITEMS.register("ultimate_chestplate",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(62))
            ));


    public static final DeferredItem<ArmorItem> ULTIMATE_LEGGINGS = ITEMS.register("ultimate_leggings",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(62))
            ));


    public static final DeferredItem<ArmorItem> ULTIMATE_BOOTS = ITEMS.register("ultimate_boots",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.BOOTS.getDurability(62))
            ));


    public static final DeferredItem<UltimateSwordItem> ULTIMATE_SWORD = ITEMS.register("ultimate_sword",
            () -> new UltimateSwordItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateSwordAttackDamage,
                    -2.4F
            ));


    public static final DeferredItem<UltimatePickaxeItem> ULTIMATE_PICKAXE = ITEMS.register("ultimate_pickaxe",
            () -> new UltimatePickaxeItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimatePickaxeAttackDamage,
                    -2.8F
            ));


    public static final DeferredItem<UltimateAxeItem> ULTIMATE_AXE = ITEMS.register("ultimate_axe",
            () -> new UltimateAxeItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateAxeAttackDamage,
                    -3.0F
            ));


    public static final DeferredItem<UtlimateShovelItem> ULTIMATE_SHOVEL = ITEMS.register("ultimate_shovel",
            () -> new UtlimateShovelItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateShovelAttackDamage,
                    -3.0F
            ));


    public static final DeferredItem<UltimateHoeItem> ULTIMATE_HOE = ITEMS.register("ultimate_hoe",
            () -> new UltimateHoeItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateHoeAttackDamage,
                    0.0F
            ));


    public static final DeferredItem<UltimateBowItem> ULTIMATE_BOW = ITEMS.register("ultimate_bow",
            () -> new UltimateBowItem(new Item.Properties().stacksTo(1).durability(768).rarity(Rarity.EPIC).fireResistant()));


    public static final DeferredItem<UltimateCrossbowItem> ULTIMATE_CROSSBOW = ITEMS.register("ultimate_crossbow",
            () -> new UltimateCrossbowItem(new Item.Properties().stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant()));


    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_HELMET = ITEMS.register("nightmare_helmet",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.HELMET.getDurability(41))
            ));


    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_CHESTPLATE = ITEMS.register("nightmare_chestplate",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(41))
            ));


    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_LEGGINGS = ITEMS.register("nightmare_leggings",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(41))
            ));


    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_BOOTS = ITEMS.register("nightmare_boots",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(AntarchyFabricMisc.NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.BOOTS.getDurability(41))
            ));


    public static final DeferredItem<NightmareSwordItem> NIGHTMARE_SWORD = ITEMS.register("nightmare_sword",
            () -> new NightmareSwordItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    -2.4F
            ));


    public static final DeferredItem<SizeRayItem> SHRINK_RAY = ITEMS.register("shrink_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyFabricEntities.SHRINK_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.SHRINK,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/shrink_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/shrink_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/shrink_ray.animation.json"),
                    "shrink_ray_active"
            ));


    public static final DeferredItem<SizeRayItem> GROWTH_RAY = ITEMS.register("growth_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    AntarchyFabricEntities.GROWTH_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.GROWTH,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/growth_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/growth_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/growth_ray.animation.json"),
                    "growth_ray_active"
            ));


    public static final DeferredItem<GravityGunItem> GRAVITY_GUN = ITEMS.register("gravity_gun",
            () -> new GravityGunItem(new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant()));


    // Temporarily disabled: keeping the code but not registering the item for now.
    // public static final DeferredItem<MinersDreamItem> MINERS_DREAM = ITEMS.register("miners_dream",
    //         () -> new MinersDreamItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));


    public static final DeferredItem<SquidzookaItem> SQUIDZOOKA = ITEMS.register("squidzooka",
            () -> new SquidzookaItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)));

    public static final DeferredItem<RpoLauncherItem> RPO_LAUNCHER = ITEMS.register("rpo_launcher",
            () -> new RpoLauncherItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)));


    public static final DeferredItem<BattleAxeItem> BATTLE_AXE = ITEMS.register("battle_axe",
            () -> new BattleAxeItem(
                    Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant(),
                    AntarchySettings::battleAxeAttackDamage,
                    -3.1F
            ));
    public static final DeferredItem<AttitudeAdjusterItem> ATTITUDE_ADJUSTER = ITEMS.register("attitude_adjuster",
            () -> new AttitudeAdjusterItem(
                    ATTITUDE_ADJUSTER_TIER,
                    new Item.Properties().stacksTo(1).durability(ATTITUDE_ADJUSTER_TIER.getUses()).rarity(Rarity.EPIC).fireResistant()
            ));


    public static final DeferredItem<DiamondMinecartItem> DIAMOND_MINECART_ITEM = ITEMS.register("diamond_minecart",
            () -> new DiamondMinecartItem(AntarchyFabricEntities.DIAMOND_MINECART, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));


    public static final DeferredItem<ReverieBottleItem> REVERIE_BOTTLE = ITEMS.register("reverie_bottle",
            () -> new ReverieBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));


    public static final DeferredItem<Item> BIG_BERTHA_BLADE = ITEMS.registerSimpleItem(
            "big_bertha_blade",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );


    public static final DeferredItem<Item> BIG_BERTHA_HANDLE = ITEMS.registerSimpleItem(
            "big_bertha_handle",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );


    public static final DeferredItem<Item> BIG_BERTHA_HILT = ITEMS.registerSimpleItem(
            "big_bertha_hilt",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );


    public static final DeferredItem<BigBerthaItem> BIG_BERTHA = ITEMS.register("big_bertha",
            () -> new BigBerthaItem(
                    Tiers.NETHERITE,
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
                            .fireResistant()
            ));

    public static final DeferredItem<Item> KRAKEN_TENTACLE = ITEMS.registerSimpleItem(
            "kraken_tentacle",
            new Item.Properties().rarity(Rarity.RARE)
    );
    public static final DeferredItem<Item> KRAKEN_KALAMARI = ITEMS.register("kraken_kalamari",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .food(new FoodProperties.Builder()
                            .nutrition(20)
                            .saturationModifier(1.2F)
                            .alwaysEdible()
                            .build())));

    public static final DeferredItem<KrakensGraspItem> KRAKENS_GRASP = ITEMS.register("krakens_grasp",
            () -> new KrakensGraspItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(250)
                            .rarity(Rarity.EPIC)
                            .fireResistant()
            ));



    public static final DeferredItem<DeferredSpawnEggItem> EASTER_BUNNY_SPAWN_EGG = ITEMS.register("easter_bunny_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.EASTER_BUNNY, 0xFFF2B2, 0xFF85B5, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> FLYING_SQUIRREL_SPAWN_EGG = ITEMS.register("flying_squirrel_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.FLYING_SQUIRREL, 0x7D6649, 0xDCC59C, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> CATERPILLAR_SPAWN_EGG = ITEMS.register("caterpillar_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.CATERPILLAR, 0xA8D96A, 0xF4E04D, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.BUTTERFLY, 0x7A4A1E, 0xFF7A00, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> REVERIE_SPAWN_EGG = ITEMS.register("reverie_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.REVERIE, 0xF2F2F2, 0xBFC3C7, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> BRUTALFLY_SPAWN_EGG = ITEMS.register("brutalfly_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.BRUTALFLY, 0x4A2214, 0xFF8A1D, new Item.Properties().rarity(Rarity.EPIC)));


    public static final DeferredItem<DeferredSpawnEggItem> RED_ANT_SPAWN_EGG = ITEMS.register("red_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.RED_ANT, 0xA31818, 0x2B0909, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> BROWN_ANT_SPAWN_EGG = ITEMS.register("brown_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.BROWN_ANT, 0x6A4320, 0x26160A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> RAINBOW_ANT_SPAWN_EGG = ITEMS.register("rainbow_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.RAINBOW_ANT, 0x56D4F0, 0xF66DBB, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> TERMITE_SPAWN_EGG = ITEMS.register("termite_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.TERMITE, 0xD4B96A, 0xFF6B1A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> MOLEWORM_SPAWN_EGG = ITEMS.register("moleworm_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.MOLEWORM, 0xB8B8B8, 0x8A623A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> MANTIS_SPAWN_EGG = ITEMS.register("mantis_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.MANTIS, 0xF8F8F2, 0x63B44A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> ALPHA_MANTIS_SPAWN_EGG = ITEMS.register("alpha_mantis_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.ALPHA_MANTIS, 0x8FDD6C, 0x2F5D22, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> ROLLY_POLLY_SPAWN_EGG = ITEMS.register("rolly_polly_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.ROLLY_POLLY, 0x7284A3, 0xBCC8DB, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> MOLEVORE_SPAWN_EGG = ITEMS.register("molevore_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.MOLEVORE, 0x4A4A4A, 0x6B4A2B, new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<DeferredSpawnEggItem> TRIFFID_SPAWN_EGG = ITEMS.register("triffid_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.TRIFFID, 0x4C8F3A, 0xFF2FB3, new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<DeferredSpawnEggItem> APPLE_COW_SPAWN_EGG = ITEMS.register("apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.APPLE_COW, 0xFF1A1A, 0x32FF32, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("golden_apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.GOLDEN_APPLE_COW, 0xFFE14A, 0x32FF32, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("enchanted_golden_apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.ENCHANTED_GOLDEN_APPLE_COW, 0x7040B6, 0xFFE14A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> DR_TRAYAURUS_SPAWN_EGG = ITEMS.register("dr_trayaurus_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.DR_TRAYAURUS, 0xB7A27B, 0x4A3D29, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> WASP_SPAWN_EGG = ITEMS.register("wasp_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.WASP, 0x111111, 0xF1D800, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> BOMBER_SPAWN_EGG = ITEMS.register("bomber_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.BOMBER, 0x7A7A7A, 0xB32020, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> JUMPY_BUG_SPAWN_EGG = ITEMS.register("jumpy_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.JUMPY_BUG, 0x0A1636, 0x8A3E00, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> SPIT_BUG_SPAWN_EGG = ITEMS.register("spit_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.SPIT_BUG, 0x6B4A2B, 0x7ED957, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> STINK_BUG_SPAWN_EGG = ITEMS.register("stink_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.STINK_BUG, 0x111111, 0xFF7A00, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> CLOUD_SHARK_SPAWN_EGG = ITEMS.register("cloud_shark_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.CLOUD_SHARK, 0xDDEAF4, 0x7F96A8, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.KRAKEN, 0x163C53, 0x4F8E99, new Item.Properties().rarity(Rarity.EPIC)));


    public static final DeferredItem<DeferredSpawnEggItem> MISSILE_SQUID_SPAWN_EGG = ITEMS.register("missile_squid_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.MISSILE_SQUID, 0xD88FA7, 0x8D5269, new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<DeferredSpawnEggItem> OCTOPUS_BOMB_SPAWN_EGG = ITEMS.register("octopus_bomb_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.OCTOPUS_BOMB, 0xC882C8, 0x5C1A7A, new Item.Properties().rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<DeferredSpawnEggItem> NIGHTMARE_SPAWN_EGG = ITEMS.register("nightmare_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.NIGHTMARE, 0x22121C, 0xB51B2D, new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<DeferredSpawnEggItem> BED_BUG_SPAWN_EGG = ITEMS.register("bed_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.BED_BUG, 0x3B2218, 0x611111, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> LUCID_SPAWN_EGG = ITEMS.register("lucid_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.LUCID, 0xE53935, 0xF4D03F, new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<DeferredSpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.SCORPION, 0xA8D8FF, 0xE04B5A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.register("basilisk_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.BASILISK, 0x4A7C40, 0xD4A040, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> EMPEROR_SCORPION_SPAWN_EGG = ITEMS.register("emperor_scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.EMPEROR_SCORPION, 0x3A3242, 0xD8CDB4, new Item.Properties()));


    public static final DeferredItem<LucidEyeItem> LUCID_EYE = ITEMS.register("lucid_eye",
            () -> new LucidEyeItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));


    public static final DeferredItem<LucidPearlItem> LUCID_PEARL = ITEMS.register("lucid_pearl",
            () -> new LucidPearlItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
                    AntarchyFabricEntities.LUCID_PEARL_PROJECTILE
            ));


    public static final DeferredItem<WaterCannonItem> WATER_CANNON = ITEMS.register("water_cannon",
            () -> new WaterCannonItem(new Item.Properties().stacksTo(1).durability(192).rarity(Rarity.RARE)));


    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_HELMET = ITEMS.register("primordial_helmet",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.HELMET.getDurability(37))));


    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_CHESTPLATE = ITEMS.register("primordial_chestplate",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));


    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_LEGGINGS = ITEMS.register("primordial_leggings",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(37))));


    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_BOOTS = ITEMS.register("primordial_boots",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(AntarchyFabricMisc.PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))));


    public static final DeferredItem<DeferredSpawnEggItem> CREEPING_HORROR_SPAWN_EGG = ITEMS.register("creeping_horror_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.CREEPING_HORROR, 0x6B3A1F, 0x6B0000, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> LURKING_TERROR_SPAWN_EGG = ITEMS.register("lurking_terror_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.LURKING_TERROR, 0x2D5A1B, 0x8B0000, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> HERCULES_BEETLE_SPAWN_EGG = ITEMS.register("hercules_beetle_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.HERCULES_BEETLE, 0x6B1F2A, 0xD4AF37, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<DeferredSpawnEggItem> JERRY_SPAWN_EGG = ITEMS.register("jerry_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.JERRY, 0x6D2232, 0x8ED34A, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> TORETERROR_SPAWN_EGG = ITEMS.register("toreterror_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.TORETERROR, 0x90EE90, 0x5C4033, new Item.Properties().rarity(Rarity.EPIC)));


    public static final DeferredItem<DeferredSpawnEggItem> CHEEP_SPAWN_EGG = ITEMS.register("cheep_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.CHEEP, 0xFF00AA, 0x00FF44, new Item.Properties()));

    public static final DeferredItem<Item> CHEEP_ITEM = ITEMS.register("cheep",
            () -> new Item(new Item.Properties()
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                            .build())));

    public static final DeferredItem<DeferredSpawnEggItem> DORRIE_SPAWN_EGG = ITEMS.register("dorrie_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.DORRIE, 0x6F8CFF, 0xD2F2FF, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> OURANWOOD_DEER_SPAWN_EGG = ITEMS.register("ouranwood_deer_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.OURANWOOD_DEER, 0x8A6D4B, 0xE8D9B5, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> GLIMMER_SPAWN_EGG = ITEMS.register("glimmer_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.GLIMMER, 0x7DFFFF, 0x2AC7D0, new Item.Properties()));


    public static final DeferredItem<DeferredSpawnEggItem> ELKA_SPAWN_EGG = ITEMS.register("elka_spawn_egg",
            () -> new DeferredSpawnEggItem(AntarchyFabricEntities.ELKA, 0x6B2FA0, 0xFFFFFF, new Item.Properties()));


    public static final DeferredItem<net.minecraft.world.item.Item> SPIRIT_APPLE = ITEMS.register("spirit_apple",
            () -> new com.craisinlord.antarchy.content.item.SpiritAppleItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.3F)
                            .alwaysEdible()
                            .build())));


    public static final DeferredItem<com.craisinlord.antarchy.content.item.GlimmerBottleItem> GLIMMER_BOTTLE = ITEMS.register("glimmer_bottle",
            com.craisinlord.antarchy.content.item.GlimmerBottleItem::new);



    public static Item cloudBucketItem() {
        return CLOUD_BUCKET.get();
    }



    public static Item diamondMinecartItem() {
        return DIAMOND_MINECART_ITEM.get();
    }



    public static Ingredient bloodCrystalRepairIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal_shard"))
                .orElse(Items.AIR));
    }



    public static Ingredient ultimateRepairIngredient() {
        return Ingredient.of(TITANIUM_INGOT.get());
    }


    public static void register() {
        ITEMS.register();
    }

}
