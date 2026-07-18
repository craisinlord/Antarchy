package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import com.craisinlord.antarchy.content.CreativeTabOrder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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
            sortedItems.sort(CreativeTabOrder.COMPARATOR);
            sortedItems.forEach(item -> {
                if (item == AntarchyNeoforgeItems.GLIMMER_BOTTLE.get()) {
                    for (com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant variant : com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant.values()) {
                        event.accept(com.craisinlord.antarchy.content.item.GlimmerBottleItem.create(variant));
                    }
                } else {
                    event.accept(item);
                }
            });
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
            event.accept(AntarchyNeoforgeItems.TERMITE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MOLEWORM_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MANTIS_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.ALPHA_MANTIS_SPAWN_EGG.get());
//            event.accept(AntarchyNeoforgeItems.ROLLY_POLLY_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MOLEVORE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.TRIFFID_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.APPLE_COW_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.GOLDEN_APPLE_COW_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.DR_TRAYAURUS_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.WASP_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BOMBER_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.JUMPY_BUG_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.SPIT_BUG_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.STINK_BUG_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.CLOUD_SHARK_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.KRAKEN_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.MISSILE_SQUID_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.OCTOPUS_BOMB_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.NIGHTMARE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BED_BUG_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.LUCID_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.SCORPION_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.BASILISK_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.EMPEROR_SCORPION_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.TORETERROR_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.CREEPING_HORROR_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.LURKING_TERROR_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.HERCULES_BEETLE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.DORRIE_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_DEER_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.GLIMMER_SPAWN_EGG.get());
            event.accept(AntarchyNeoforgeItems.ELKA_SPAWN_EGG.get());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(AntarchyNeoforgeItems.OURANWOOD_LEAVES_ITEM.get());
            event.accept(AntarchyNeoforgeItems.OURANWOOD_ACORN.get());
            event.accept(AntarchyNeoforgeItems.PEACH_LEAVES_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_SAPLING_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DUPLICATOR_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DUPLICATOR_SAPLING_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ORANGE_MILKWEED_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PINK_MILKWEED_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CAMELLIA_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SPIDER_LILY_ITEM.get());
            event.accept(AntarchyNeoforgeItems.LOTUS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.GIANT_LILY_PAD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.SEASHELL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.HUSHWEED_ITEM.get());
            event.accept(AntarchyNeoforgeItems.AMBER_LICHEN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CREEPVINE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.HANGING_CREEPROOTS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MOLTING_VINES_ITEM.get());
            event.accept(AntarchyNeoforgeItems.GLOWCAP_MUSHROOM_ITEM.get());
            event.accept(AntarchyNeoforgeItems.GLOWCAP_MUSHROOM_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.INFESTED_ROOTED_DIRT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.INFESTED_COARSE_DIRT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TRIFFID_GOO_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BED_BUG_EGG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CREEPING_HORROR_EGG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.LURKING_TERROR_EGG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.JUMPY_BUG_EGG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.WASP_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.RED_ANT_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROWN_ANT_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.RAINBOW_ANT_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TERMITE_NEST_ITEM.get());
            event.accept(AntarchyNeoforgeItems.URANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEEPSLATE_URANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.TITANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEEPSLATE_TITANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.LUMEN_FROGLIGHT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ROSEATE_FROGLIGHT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CORN_SEEDS.get());
            event.accept(AntarchyNeoforgeItems.STAR_CORAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.STAR_CORAL_FAN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.STAR_CORAL_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEAD_STAR_CORAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEAD_STAR_CORAL_FAN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.DEAD_STAR_CORAL_BLOCK_ITEM.get());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(new ItemStack(Items.SHEARS), new ItemStack(AntarchyNeoforgeItems.DUCT_TAPE_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(Items.MINECART), new ItemStack(AntarchyNeoforgeItems.DIAMOND_MINECART_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(AntarchyNeoforgeItems.BILE_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.ICHOR_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.ANTIWATER_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.LUMEN_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.CLOUD_BUCKET.get());
            event.accept(AntarchyNeoforgeItems.REVERIE_BOTTLE.get());
            event.accept(AntarchyNeoforgeItems.GLIMMER_BOTTLE.get());
            event.accept(AntarchyNeoforgeItems.CRITTER_CAGE.get());
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
            event.accept(AntarchyNeoforgeItems.SPIRIT_APPLE.get());
            event.accept(AntarchyNeoforgeItems.MANTIS_CLAW.get());
            event.accept(AntarchyNeoforgeItems.HERCULES_HORN.get());
            event.accept(AntarchyNeoforgeItems.KING_SCALE.get());
            event.accept(AntarchyNeoforgeItems.QUEEN_SCALE.get());
            event.accept(AntarchyNeoforgeItems.CHITEN.get());
            event.accept(AntarchyNeoforgeItems.STINK_BUG.get());
            event.accept(AntarchyNeoforgeItems.BRUTALFLY_WING.get());
            event.accept(AntarchyNeoforgeItems.PRIMORDIAL_SCUTE.get());
            event.accept(AntarchyNeoforgeItems.TRIFFID_GOO.get());
            event.accept(AntarchyNeoforgeItems.VORTEX_EYE.get());
            event.accept(AntarchyNeoforgeItems.EMPEROR_SCORPION_STINGER.get());
            event.accept(AntarchyNeoforgeItems.KRAKEN_TOOTH.get());
            event.accept(AntarchyNeoforgeItems.KRAKEN_TENTACLE.get());
            event.accept(AntarchyNeoforgeItems.JUMPY_BUG_LEG.get());
            event.accept(AntarchyNeoforgeItems.CORNEA_EAR.get());
            event.accept(AntarchyNeoforgeItems.CORN.get());
            event.accept(AntarchyNeoforgeItems.HIGH_FRUCTOSE_CORN_SYRUP.get());
            event.accept(AntarchyNeoforgeItems.PEACH.get());
            event.accept(AntarchyNeoforgeItems.PEACH_PIE.get());
            event.accept(AntarchyNeoforgeItems.CORNBREAD.get());
            event.accept(AntarchyNeoforgeItems.POPCORN.get());
            event.accept(AntarchyNeoforgeItems.RAW_CORNDOG.get());
            event.accept(AntarchyNeoforgeItems.COOKED_CORNDOG.get());
            event.accept(AntarchyNeoforgeItems.RAW_VENISON.get());
            event.accept(AntarchyNeoforgeItems.COOKED_VENISON.get());
            event.accept(AntarchyNeoforgeItems.RAW_BUG_MEAT.get());
            event.accept(AntarchyNeoforgeItems.COOKED_BUG_MEAT.get());
            event.accept(AntarchyNeoforgeItems.BROODFRUIT.get());
            event.accept(AntarchyNeoforgeItems.CHEEP_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ROOT_BEER.get());
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
            event.accept(AntarchyNeoforgeItems.PEACH_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_WOOD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.STRIPPED_PEACH_LOG_ITEM.get());
            event.accept(AntarchyNeoforgeItems.STRIPPED_PEACH_WOOD_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_PLANKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_FENCE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_FENCE_GATE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_BOAT.get());
            event.accept(AntarchyNeoforgeItems.PEACH_CHEST_BOAT.get());
            event.accept(AntarchyNeoforgeItems.PEACH_DOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_TRAPDOOR_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_PRESSURE_PLATE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_BUTTON_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_SIGN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.PEACH_HANGING_SIGN_ITEM.get());
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
            event.accept(AntarchyNeoforgeItems.BLUSH_MOSS_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BLUSH_MOSS_CARPET_ITEM.get());
            event.accept(AntarchyNeoforgeItems.LUMEN_FROGLIGHT_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ROSEATE_FROGLIGHT_ITEM.get());
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
            event.accept(AntarchyNeoforgeItems.ANTIMETAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_ANTIMETAL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.ANTIMETAL_SCAFFOLDING_ITEM.get());
            event.accept(AntarchyNeoforgeItems.UMBRAL_MOSS_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.UMBRAL_MOSS_CARPET_ITEM.get());
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
            event.accept(AntarchyNeoforgeItems.MYRMITE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_BROODSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHISELED_BROODSTONE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_BRICKS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_BROODSTONE_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_BROODSTONE_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.POLISHED_BROODSTONE_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_BRICK_STAIRS_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_BRICK_SLAB_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_BRICK_WALL_ITEM.get());
            event.accept(AntarchyNeoforgeItems.MYRMITE_COAL_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_URANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BROODSTONE_TITANIUM_ORE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.BILE_VEIN_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHITEN_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.CHITEN_SPIKE_ITEM.get());
            event.accept(AntarchyNeoforgeItems.AMBER_MOSS_BLOCK_ITEM.get());
            event.accept(AntarchyNeoforgeItems.AMBER_MOSS_CARPET_ITEM.get());
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
            event.accept(AntarchyNeoforgeItems.RPO_LAUNCHER.get());
            event.accept(AntarchyNeoforgeItems.KRAKENS_GRASP.get());
            event.accept(AntarchyNeoforgeItems.SHRINK_RAY.get());
            event.accept(AntarchyNeoforgeItems.GROWTH_RAY.get());
            event.accept(AntarchyNeoforgeItems.GRAVITY_GUN.get());
            event.accept(AntarchyNeoforgeItems.WATER_CANNON.get());
            event.accept(AntarchyNeoforgeItems.ATTITUDE_ADJUSTER.get());
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
            event.accept(AntarchyNeoforgeItems.JUMPY_BOOTS.get());
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.SHRINKING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STRONG_SHRINKING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.EXTREME_SHRINKING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.PARALYSIS));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.LONG_PARALYSIS));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STINKY_POTION));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.LONG_STINKY));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.HASTE));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STRONG_HASTE));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.GROWING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.STRONG_GROWING));
            event.accept(PotionContents.createItemStack(Items.POTION, AntarchyNeoforgeMisc.EXTREME_GROWING));
        }
    }

    private static int creativeTabGroup(net.minecraft.world.item.Item item) {
        return CreativeTabOrder.group(item);
    }

    private static int creativeTabSubOrder(net.minecraft.world.item.Item item) {
        return CreativeTabOrder.subOrder(item);
    }
}
