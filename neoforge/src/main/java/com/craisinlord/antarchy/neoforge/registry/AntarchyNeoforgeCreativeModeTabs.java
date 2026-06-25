package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.Comparator;

public class AntarchyNeoforgeCreativeModeTabs {
    private AntarchyNeoforgeCreativeModeTabs() {}

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Antarchy.MODID);

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(AntarchyNeoforgeCreativeModeTabs::buildCreativeTabs);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANTARCHY_TAB = CREATIVE_MODE_TABS.register("antarchy",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.antarchy.antarchy"))
                    .icon(() -> new ItemStack(AntarchyNeoforgeItems.BIG_BERTHA.get()))
                    .displayItems((parameters, output) -> {})
                    .build());


    static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ANTARCHY_TAB.getKey()) {
            java.util.ArrayList<Item> sortedItems = new java.util.ArrayList<>();
            for (var holder : AntarchyNeoforgeItems.ITEMS.getEntries()) {
                Item item = holder.get();
                if (item != Items.AIR) sortedItems.add(item);
            }
            sortedItems.sort(Comparator
                    .<Item>comparingInt(AntarchyNeoforgeCreativeModeTabs::creativeTabGroup)
                    .thenComparing(item -> BuiltInRegistries.ITEM.getKey(item).getPath()));
            int n = sortedItems.size();
            for (int i = Math.max(0, n - 20); i < n; i++) {
                Item it = sortedItems.get(i);
            }
            sortedItems.forEach(event::accept);
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(AntarchyNeoforgeItems.EASTER_BUNNY_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.FLYING_SQUIRREL_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.CATERPILLAR_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BUTTERFLY_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.REVERIE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BRUTALFLY_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.RED_ANT_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BROWN_ANT_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.RAINBOW_ANT_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MOLEWORM_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MANTIS_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MOLEVORE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.TRIFFID_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.APPLE_COW_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.GOLDEN_APPLE_COW_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.DR_TRAYAURUS_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.WASP_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BOMBER_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.CLOUD_SHARK_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.KRAKEN_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MISSILE_SQUID_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BED_BUG_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.LUCID_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.SCORPION_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BASILISK_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.EMPEROR_SCORPION_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.TORETERROR_SPAWN_EGG.get());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(AntarchyNeoforgeItems.OURANWOOD_LEAVES_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_ACORN.get());
            event.accept(AntarchyNeoforgeItems.DUPLICATOR_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DUPLICATOR_SAPLING_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ORANGE_MILKWEED_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PINK_MILKWEED_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TORCHFLOWER_BUSH_ITEM.get());
            event.accept(AntarchyNeoforgeItems.HUSHWEED_ITEM.get());
            event.accept(AntarchyNeoforgeItems.INFESTED_ROOTED_DIRT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.INFESTED_COARSE_DIRT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TRIFFID_GOO_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BED_BUG_EGG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.WASP_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.RED_ANT_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROWN_ANT_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.RAINBOW_ANT_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEEPSLATE_URANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEEPSLATE_TITANIUM_ORE_ITEM.get());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(new ItemStack(Items.SHEARS), new ItemStack(AntarchyNeoforgeItems.DUCT_TAPE_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(Items.MINECART), new ItemStack(AntarchyNeoforgeItems.DIAMOND_MINECART_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(AntarchyNeoforgeItems.ICHOR_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.ANTIWATER_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.CLOUD_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.REVERIE_BOTTLE.get());
            event.accept(AntarchyNeoforgeItems.ULTIMATE_PICKAXE.get());
            event.accept(AntarchyNeoforgeItems.ULTIMATE_AXE.get());
            event.accept(AntarchyNeoforgeItems.ULTIMATE_SHOVEL.get());
            event.accept(AntarchyNeoforgeItems.ULTIMATE_HOE.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(AntarchyNeoforgeItems.RAW_URANIUM_SCRAP.get());
            event.accept(AntarchyNeoforgeItems.RAW_TITANIUM_SCRAP.get());
            event.accept(AntarchyNeoforgeItems.RAW_URANIUM.get());
            event.accept(AntarchyNeoforgeItems.RAW_TITANIUM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_NUGGET.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_NUGGET.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_INGOT.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_INGOT.get());
            event.accept(AntarchyNeoforgeItems.CLOUD_SHARK_FIN.get());
            event.accept(AntarchyNeoforgeItems.CLOUD_SHARK_FIN_SOUP.get());
            event.accept(AntarchyNeoforgeItems.BASILISK_FANG.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_SCALE.get());
            event.accept(AntarchyNeoforgeItems.MOLEVORE_NOSE.get());
            event.accept(AntarchyNeoforgeItems.MOLEWORM_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MUD_PIE.get());
            event.accept(AntarchyNeoforgeItems.LUCID_EYE.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_SHARD.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_APPLE.get());
            event.accept(AntarchyNeoforgeItems.MANTIS_CLAW.get());
            event.accept(AntarchyNeoforgeItems.BRUTALFLY_WING.get());
            event.accept(AntarchyNeoforgeItems.PRIMORDIAL_SCUTE.get());
            event.accept(AntarchyNeoforgeItems.TRIFFID_GOO.get());
            event.accept(AntarchyNeoforgeItems.VORTEX_EYE.get());
            event.accept(AntarchyNeoforgeItems.EMPEROR_SCORPION_STINGER.get());
            event.accept(AntarchyNeoforgeItems.KRAKEN_TOOTH.get());
            event.accept(AntarchyNeoforgeItems.JUMPY_BUG_LEG.get());
            event.accept(AntarchyNeoforgeItems.CORNEA_EAR.get());
            event.accept(AntarchyNeoforgeItems.RAINBOW_SUGAR.get());
            event.accept(AntarchyNeoforgeItems.BIG_BERTHA_BLADE.get());
            event.accept(AntarchyNeoforgeItems.BIG_BERTHA_HANDLE.get());
            event.accept(AntarchyNeoforgeItems.BIG_BERTHA_HILT.get());
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(AntarchyNeoforgeItems.OURANWOOD_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_WOOD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOSSY_OURANWOOD_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOSSY_OURANWOOD_WOOD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.STRIPPED_OURANWOOD_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.STRIPPED_OURANWOOD_WOOD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_PLANKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_FENCE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_FENCE_GATE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_BOAT.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_CHEST_BOAT.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_DOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_TRAPDOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_PRESSURE_PLATE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_BUTTON_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_SIGN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_HANGING_SIGN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_SHELLSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_SHELLSTONE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_SHELLSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_SHELLSTONE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_BRICKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_BRICK_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_BRICK_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SHELLSTONE_BRICK_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHISELED_SHELLSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOSSY_SHELLSTONE_BRICKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOSSY_SHELLSTONE_BRICK_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOSSY_SHELLSTONE_BRICK_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOSSY_SHELLSTONE_BRICK_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CRACKED_SHELLSTONE_BRICKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_NYXITE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_NYXITE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_NYXITE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_NYXITE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_BRICKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_BRICK_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_BRICK_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_BRICK_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHISELED_NYXITE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PALE_NYXITE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.NYXITE_SPIKE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POTENT_NYXITE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.UMBRAL_MOSS_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.UMBRAL_MOSS_CARPET_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ANTIMETAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_ANTIMETAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ANTIMETAL_SCAFFOLDING_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_TORCH_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_LANTERN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_CAMPFIRE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_SAND_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_SANDSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHISELED_DREAM_SANDSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_DREAM_SANDSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SMOOTH_DREAM_SANDSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_SANDSTONE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_SANDSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DREAM_SANDSTONE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SMOOTH_DREAM_SANDSTONE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SMOOTH_DREAM_SANDSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_DREAM_SANDSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BUDDING_BLOOD_CRYSTAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_CRYSTAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SMALL_BLOOD_CRYSTAL_BUD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MEDIUM_BLOOD_CRYSTAL_BUD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.LARGE_BLOOD_CRYSTAL_BUD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.RAW_URANIUM_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_URANIUM_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_URANIUM_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_URANIUM_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHISELED_URANIUM_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_DOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_TRAPDOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_BARS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_BULB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.RAW_TITANIUM_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_TITANIUM_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_TITANIUM_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CUT_TITANIUM_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHISELED_TITANIUM_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_DOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_TRAPDOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_BARS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_BULB_ITEM.get());
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(AntarchyNeoforgeItems.BATTLE_AXE.get());
            event.accept(AntarchyNeoforgeItems.BIG_BERTHA.get());
            event.accept(AntarchyNeoforgeItems.BASILISK_DAGGER.get());
            event.accept(UltimateGearHelper.createUltimateArmorStack(AntarchyNeoforgeItems.ULTIMATE_HELMET.get(), event.getParameters().holders()));
            event.accept(UltimateGearHelper.createUltimateArmorStack(AntarchyNeoforgeItems.ULTIMATE_CHESTPLATE.get(), event.getParameters().holders()));
            event.accept(UltimateGearHelper.createUltimateArmorStack(AntarchyNeoforgeItems.ULTIMATE_LEGGINGS.get(), event.getParameters().holders()));
            event.accept(UltimateGearHelper.createUltimateArmorStack(AntarchyNeoforgeItems.ULTIMATE_BOOTS.get(), event.getParameters().holders()));
            event.accept(AntarchyNeoforgeItems.ULTIMATE_SWORD.get());
            event.accept(UltimateGearHelper.createUltimateBowStack(AntarchyNeoforgeItems.ULTIMATE_BOW.get(), event.getParameters().holders()));
            event.accept(AntarchyNeoforgeItems.ULTIMATE_CROSSBOW.get());
            event.accept(AntarchyNeoforgeItems.SQUIDZOOKA.get());
            event.accept(AntarchyNeoforgeItems.SHRINK_RAY.get());
            event.accept(AntarchyNeoforgeItems.GROWTH_RAY.get());
            event.accept(AntarchyNeoforgeItems.GRAVITY_GUN.get());
            event.accept(AntarchyNeoforgeItems.WATER_CANNON.get());
            event.accept(AntarchyNeoforgeItems.LUCID_PEARL.get());
            event.accept(AntarchyNeoforgeItems.PRIMORDIAL_HELMET.get());
            event.accept(AntarchyNeoforgeItems.PRIMORDIAL_CHESTPLATE.get());
            event.accept(AntarchyNeoforgeItems.PRIMORDIAL_LEGGINGS.get());
            event.accept(AntarchyNeoforgeItems.PRIMORDIAL_BOOTS.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_HELMET.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_CHESTPLATE.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_LEGGINGS.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_BOOTS.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_SWORD.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_KATANA.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_HELMET.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_CHESTPLATE.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_LEGGINGS.get());
            event.accept(AntarchyNeoforgeItems.BLOOD_CRYSTAL_BOOTS.get());
            event.accept(AntarchyNeoforgeItems.MOGGLES.get());
            event.accept(AntarchyNeoforgeItems.SCORPION_WHIP.get());
            event.accept(AntarchyNeoforgeItems.FALLEN_KING_CROWN.get());
            event.accept(AntarchyNeoforgeItems.BRUTALFLY_ELYTRA.get());
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.SHRINKING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STRONG_SHRINKING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.EXTREME_SHRINKING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.PARALYSIS));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.LONG_PARALYSIS));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.HASTE));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STRONG_HASTE));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.GROWING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STRONG_GROWING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.EXTREME_GROWING));
        }
    }

    private static int creativeTabGroup(net.minecraft.world.item.Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return switch (path) {
            // 0 - Ouranwood blocks
            case "ouranwood_log", "ouranwood_wood", "mossy_ouranwood_log", "mossy_ouranwood_wood",
                 "stripped_ouranwood_log", "stripped_ouranwood_wood", "duplicator_log",
                 "ouranwood_planks", "ouranwood_stairs", "ouranwood_slab",
                 "ouranwood_fence", "ouranwood_fence_gate",
                 "ouranwood_door", "ouranwood_trapdoor",
                 "ouranwood_pressure_plate", "ouranwood_button",
                 "ouranwood_sign", "ouranwood_hanging_sign",
                 "ouranwood_boat", "ouranwood_chest_boat" -> 0;

            // 1 - Nyxite blocks
            case "nyxite", "polished_nyxite", "chiseled_nyxite", "nyxite_bricks",
                 "nyxite_stairs", "nyxite_slab", "nyxite_wall",
                 "polished_nyxite_stairs", "polished_nyxite_slab", "polished_nyxite_wall",
                 "nyxite_brick_stairs", "nyxite_brick_slab", "nyxite_brick_wall",
                 "pale_nyxite", "nyxite_spike", "potent_nyxite" -> 1;

            // 2 - Shellstone blocks
            case "shellstone", "polished_shellstone", "shellstone_bricks", "chiseled_shellstone",
                 "mossy_shellstone_bricks", "cracked_shellstone_bricks",
                 "shellstone_stairs", "shellstone_slab", "shellstone_wall",
                 "polished_shellstone_stairs", "polished_shellstone_slab", "polished_shellstone_wall",
                 "shellstone_brick_stairs", "shellstone_brick_slab", "shellstone_brick_wall",
                 "mossy_shellstone_brick_stairs", "mossy_shellstone_brick_slab", "mossy_shellstone_brick_wall" -> 2;

            // 3 - Antimetal blocks
            case "antimetal", "polished_antimetal" -> 3;

            // 4 - Dream sand and blocks
            case "dream_sand", "dream_sandstone", "chiseled_dream_sandstone",
                 "cut_dream_sandstone", "smooth_dream_sandstone",
                 "dream_sandstone_stairs", "dream_sandstone_slab", "dream_sandstone_wall",
                 "smooth_dream_sandstone_stairs", "smooth_dream_sandstone_slab",
                 "cut_dream_sandstone_slab" -> 4;

            // 5 - Umbral moss
            case "umbral_moss_block", "umbral_moss_carpet" -> 5;

            // 6 - All ores + metal storage/decorative blocks
            case "uranium_ore", "deepslate_uranium_ore", "titanium_ore", "deepslate_titanium_ore",
                 "uranium_block", "titanium_block",
                 "cut_uranium", "cut_titanium", "cut_uranium_slab", "cut_titanium_slab",
                 "cut_uranium_stairs", "cut_titanium_stairs",
                 "chiseled_uranium", "chiseled_titanium",
                 "uranium_door", "titanium_door", "uranium_trapdoor", "titanium_trapdoor",
                 "uranium_bars", "titanium_bars", "uranium_bulb", "titanium_bulb" -> 6;

            // 7 - Raw ores
            case "raw_uranium", "raw_titanium", "raw_uranium_scrap", "raw_titanium_scrap",
                 "raw_uranium_block", "raw_titanium_block" -> 7;

            // 8 - Blood crystal blocks (not the shard ingredient)
            case "blood_crystal_block", "small_blood_crystal_bud", "medium_blood_crystal_bud",
                 "large_blood_crystal_bud", "budding_blood_crystal", "blood_crystal_cluster" -> 8;

            // 9 - Ouranwood leaves
            case "ouranwood_leaves" -> 9;

            // 10 - Saplings / seeds
            case "duplicator_sapling", "ouranwood_acorn" -> 10;

            // 11 - Plants and flowers
            case "orange_milkweed", "pink_milkweed", "torchflower_bush", "hushweed",
                 "triffid_goo_block", "wasp_nest",
                 "red_ant_nest", "brown_ant_nest", "rainbow_ant_nest" -> 11;

            // 12 - Dream lighting
            case "dream_torch", "dream_lantern", "dream_campfire" -> 12;

            // 13 - Antimetal scaffolding
            case "antimetal_scaffolding" -> 13;

            // 14 - Infested blocks
            case "infested_rooted_dirt", "infested_coarse_dirt", "bed_bug_egg" -> 14;

            // 15 - Tools
            case "ultimate_pickaxe", "ultimate_axe", "ultimate_shovel", "ultimate_hoe",
                 "duct_tape", "diamond_minecart", "gravity_sculk_sensor" -> 15;

            // 16 - Liquid buckets
            case "ichor_bucket", "antiwater_bucket", "cloud_bucket" -> 16;

            // 17 - Misc wearables / gadgets
            case "moggles", "brutalfly_elytra", "fallen_king_crown" -> 17;

            // 18 - Weapons
            case "battle_axe", "basilisk_dagger", "squidzooka", "shrink_ray", "growth_ray",
                 "gravity_gun", "water_cannon", "nightmare_sword", "lucid_pearl", "scorpion_whip", "blood_crystal_katana",
                 "big_bertha", "big_bertha_blade", "big_bertha_handle", "big_bertha_hilt",
                 "ultimate_sword", "ultimate_mace" -> 18;

            // 19 - Armor sets
            case "primordial_helmet", "primordial_chestplate",
                 "primordial_leggings", "primordial_boots",
                 "blood_crystal_helmet", "blood_crystal_chestplate",
                 "blood_crystal_leggings", "blood_crystal_boots",
                 "nightmare_helmet", "nightmare_chestplate",
                 "nightmare_leggings", "nightmare_boots",
                 "ultimate_helmet", "ultimate_chestplate",
                 "ultimate_leggings", "ultimate_boots" -> 19;

            // 20 - Ranged weapons
            case "ultimate_bow", "ultimate_crossbow" -> 20;

            // 21 - Foods
            case "cloud_shark_fin_soup", "mud_pie", "rainbow_sugar",
                 "blood_crystal_apple" -> 21;

            // 22 - Crafting ingredients
            case "blood_crystal_shard", "cloud_shark_fin", "basilisk_fang", "kraken_tooth",
                 "emperor_scorpion_stinger", "nightmare_scale", "molevore_nose",
                 "moleworm", "uranium_nugget", "titanium_nugget",
                 "uranium_ingot", "titanium_ingot", "lucid_eye",
                 "mantis_claw", "jumpy_bug_leg", "brutalfly_wing", "cornea_ear",
                 "primordial_scute", "triffid_goo", "vortex_eye" -> 22;

            // 23 - Arrows (reserved for future arrow types)

            // 24 - Potions
            case "reverie_bottle" -> 24;

            // 25 - Spawn eggs
            default -> {
                if (!path.endsWith("_spawn_egg")) {
                    org.slf4j.LoggerFactory.getLogger("Antarchy/CreativeTab").warn("Unmatched creative tab item: '{}'", path);
                }
                yield path.endsWith("_spawn_egg") ? 25 : 99;
            }
        };
    }
}
