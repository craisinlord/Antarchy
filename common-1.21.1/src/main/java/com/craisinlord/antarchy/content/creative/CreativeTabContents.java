package com.craisinlord.antarchy.content.creative;

import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import com.craisinlord.antarchy.content.item.GlimmerBottleItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateGearHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;

public final class CreativeTabContents {
    private static final String MODID = "antarchy";

    private CreativeTabContents() {}

    private record CatalogEntry(String path, String neoTabPath, String insertAfterPath) {}

    public interface AntarchyTabOutput {
        void accept(ItemLike item);
        void accept(ItemStack stack);
    }

    public interface NeoForgeVanillaTabOutput extends AntarchyTabOutput {
        void insertAfter(ItemLike anchor, ItemLike item);
    }

    private static final List<CatalogEntry> CATALOG = List.of(
            entry("ouranwood_log", "building_blocks"),
            entry("ouranwood_wood", "building_blocks"),
            entry("mossy_ouranwood_log", "building_blocks"),
            entry("mossy_ouranwood_wood", "building_blocks"),
            entry("stripped_ouranwood_log", "building_blocks"),
            entry("stripped_ouranwood_wood", "building_blocks"),
            entry("ouranwood_planks", "building_blocks"),
            entry("ouranwood_stairs", "building_blocks"),
            entry("ouranwood_slab", "building_blocks"),
            entry("ouranwood_fence", "building_blocks"),
            entry("ouranwood_fence_gate", "building_blocks"),
            entry("ouranwood_boat", "building_blocks"),
            entry("ouranwood_chest_boat", "building_blocks"),
            entry("ouranwood_door", "building_blocks"),
            entry("ouranwood_trapdoor", "building_blocks"),
            entry("ouranwood_pressure_plate", "building_blocks"),
            entry("ouranwood_button", "building_blocks"),
            entry("ouranwood_sign", "building_blocks"),
            entry("ouranwood_hanging_sign", "building_blocks"),
            entry("peach_log", "building_blocks"),
            entry("peach_wood", "building_blocks"),
            entry("stripped_peach_log", "building_blocks"),
            entry("stripped_peach_wood", "building_blocks"),
            entry("peach_planks", "building_blocks"),
            entry("peach_stairs", "building_blocks"),
            entry("peach_slab", "building_blocks"),
            entry("peach_fence", "building_blocks"),
            entry("peach_fence_gate", "building_blocks"),
            entry("peach_boat", "building_blocks"),
            entry("peach_chest_boat", "building_blocks"),
            entry("peach_door", "building_blocks"),
            entry("peach_trapdoor", "building_blocks"),
            entry("peach_pressure_plate", "building_blocks"),
            entry("peach_button", "building_blocks"),
            entry("peach_sign", "building_blocks"),
            entry("peach_hanging_sign", "building_blocks"),
            entry("shellstone", "building_blocks"),
            entry("shellstone_stairs", "building_blocks"),
            entry("shellstone_slab", "building_blocks"),
            entry("shellstone_wall", "building_blocks"),
            entry("polished_shellstone", "building_blocks"),
            entry("polished_shellstone_stairs", "building_blocks"),
            entry("polished_shellstone_slab", "building_blocks"),
            entry("polished_shellstone_wall", "building_blocks"),
            entry("shellstone_bricks", "building_blocks"),
            entry("shellstone_brick_stairs", "building_blocks"),
            entry("shellstone_brick_slab", "building_blocks"),
            entry("shellstone_brick_wall", "building_blocks"),
            entry("chiseled_shellstone", "building_blocks"),
            entry("mossy_shellstone_bricks", "building_blocks"),
            entry("mossy_shellstone_brick_stairs", "building_blocks"),
            entry("mossy_shellstone_brick_slab", "building_blocks"),
            entry("mossy_shellstone_brick_wall", "building_blocks"),
            entry("cracked_shellstone_bricks", "building_blocks"),
            entry("shellstone_pillar", "building_blocks"),
            entry("blush_moss_block", "building_blocks"),
            entry("blush_moss_carpet", "building_blocks"),
            entry("lumen_froglight", "natural_blocks"),
            entry("roseate_froglight", "natural_blocks"),
            entry("nyxite", "building_blocks"),
            entry("nyxite_stairs", "building_blocks"),
            entry("nyxite_slab", "building_blocks"),
            entry("nyxite_wall", "building_blocks"),
            entry("polished_nyxite", "building_blocks"),
            entry("polished_nyxite_stairs", "building_blocks"),
            entry("polished_nyxite_slab", "building_blocks"),
            entry("polished_nyxite_wall", "building_blocks"),
            entry("polished_nyxite_pressure_plate", "building_blocks"),
            entry("polished_nyxite_button", "building_blocks"),
            entry("nyxite_bricks", "building_blocks"),
            entry("nyxite_brick_stairs", "building_blocks"),
            entry("nyxite_brick_slab", "building_blocks"),
            entry("nyxite_brick_wall", "building_blocks"),
            entry("chiseled_nyxite", "building_blocks"),
            entry("nyxite_pillar", "building_blocks"),
            entry("pale_nyxite", "building_blocks"),
            entry("nyxite_spike", "building_blocks"),
            entry("potent_nyxite", "building_blocks"),
            entry("antimetal", "building_blocks"),
            entry("antimetal_stairs", "building_blocks"),
            entry("antimetal_slab", "building_blocks"),
            entry("polished_antimetal", "building_blocks"),
            entry("polished_antimetal_stairs", "building_blocks"),
            entry("polished_antimetal_slab", "building_blocks"),
            entry("antimetal_scaffolding", "building_blocks"),
            entry("antimetal_rail", "redstone_blocks"),
            entry("antimetal_powered_rail", "redstone_blocks"),
            entry("antimetal_detector_rail", "redstone_blocks"),
            entry("antimetal_activator_rail", "redstone_blocks"),
            entry("upper", "redstone_blocks"),
            entry("umbral_moss_block", "building_blocks"),
            entry("umbral_moss_carpet", "building_blocks"),
            entry("dream_torch", "building_blocks"),
            entry("dream_lantern", "building_blocks"),
            entry("dream_campfire", "building_blocks"),
            entry("dream_sand", "building_blocks"),
            entry("dream_sandstone", "building_blocks"),
            entry("chiseled_dream_sandstone", "building_blocks"),
            entry("cut_dream_sandstone", "building_blocks"),
            entry("smooth_dream_sandstone", "building_blocks"),
            entry("dream_sandstone_stairs", "building_blocks"),
            entry("dream_sandstone_slab", "building_blocks"),
            entry("dream_sandstone_wall", "building_blocks"),
            entry("smooth_dream_sandstone_stairs", "building_blocks"),
            entry("smooth_dream_sandstone_slab", "building_blocks"),
            entry("cut_dream_sandstone_slab", "building_blocks"),
            entry("antigravel", "natural_blocks"),
            entry("budding_blood_crystal", "building_blocks"),
            entry("blood_crystal_block", "building_blocks"),
            entry("blood_crystal_cluster", "building_blocks"),
            entry("small_blood_crystal_bud", "building_blocks"),
            entry("medium_blood_crystal_bud", "building_blocks"),
            entry("large_blood_crystal_bud", "building_blocks"),
            entry("myrmite", "building_blocks"),
            entry("biomite", "building_blocks"),
            entry("biomite_turf", "building_blocks"),
            entry("biowart", "building_blocks"),
            entry("biowart_tendrils", "building_blocks"),
            entry("loam", "natural_blocks"),
            entry("broodstone", "building_blocks"),
            entry("polished_broodstone", "building_blocks"),
            entry("chiseled_broodstone", "building_blocks"),
            entry("broodstone_bricks", "building_blocks"),
            entry("broodstone_stairs", "building_blocks"),
            entry("broodstone_slab", "building_blocks"),
            entry("broodstone_wall", "building_blocks"),
            entry("polished_broodstone_stairs", "building_blocks"),
            entry("polished_broodstone_slab", "building_blocks"),
            entry("polished_broodstone_wall", "building_blocks"),
            entry("broodstone_brick_stairs", "building_blocks"),
            entry("broodstone_brick_slab", "building_blocks"),
            entry("broodstone_brick_wall", "building_blocks"),
            entry("broodstone_pillar", "building_blocks"),
            entry("myrmite_coal_ore", "building_blocks"),
            entry("broodstone_uranium_ore", "building_blocks"),
            entry("broodstone_titanium_ore", "building_blocks"),
            entry("bile_vein", "building_blocks"),
            entry("chitin_block", "building_blocks"),
            entry("chitin_spike", "building_blocks"),
            entry("amber_moss_block", "building_blocks"),
            entry("amber_moss_carpet", "building_blocks"),
            entry("uranium_block", "building_blocks"),
            entry("raw_uranium_block", "building_blocks"),
            entry("cut_uranium", "building_blocks"),
            entry("cut_uranium_stairs", "building_blocks"),
            entry("cut_uranium_slab", "building_blocks"),
            entry("chiseled_uranium", "building_blocks"),
            entry("uranium_door", "building_blocks"),
            entry("uranium_trapdoor", "building_blocks"),
            entry("uranium_bars", "building_blocks"),
            entry("uranium_bulb", "building_blocks"),
            entry("bluestone_block", "redstone_blocks"),
            entry("bluestone_repeater", "redstone_blocks"),
            entry("bluestone_comparator", "redstone_blocks"),
            entry("bluestone_torch", "redstone_blocks"),
            entry("bluestone_lamp", "redstone_blocks"),
            entry("titanium_block", "building_blocks"),
            entry("raw_titanium_block", "building_blocks"),
            entry("cut_titanium", "building_blocks"),
            entry("cut_titanium_stairs", "building_blocks"),
            entry("cut_titanium_slab", "building_blocks"),
            entry("chiseled_titanium", "building_blocks"),
            entry("titanium_door", "building_blocks"),
            entry("titanium_trapdoor", "building_blocks"),
            entry("titanium_bars", "building_blocks"),
            entry("titanium_bulb", "building_blocks"),
            entry("ouranwood_leaves", "natural_blocks"),
            entry("ouranwood_acorn", "natural_blocks"),
            entry("peach_leaves", "natural_blocks"),
            entry("peach_sapling", "natural_blocks"),
            entry("duplicator_log", "natural_blocks"),
            entry("duplicator_sapling", "natural_blocks"),
            entry("orange_milkweed", "natural_blocks"),
            entry("pink_milkweed", "natural_blocks"),
            entry("camellia", "natural_blocks"),
            entry("spider_lily", "natural_blocks"),
            entry("lotus", "natural_blocks"),
            entry("giant_lily_pad", "natural_blocks"),
            entry("seashell", "natural_blocks"),
            entry("hushweed", "natural_blocks"),
            entry("amber_lichen", "natural_blocks"),
            entry("mucus", "natural_blocks"),
            entry("creepvine", "natural_blocks"),
            entry("hanging_creeproots", "natural_blocks"),
            entry("molting_vines", "natural_blocks"),
            entry("glowcap_mushroom", "natural_blocks"),
            entry("glowcap_mushroom_block", "natural_blocks"),
            entry("infested_rooted_dirt", "natural_blocks"),
            entry("infested_coarse_dirt", "natural_blocks"),
            entry("triffid_goo_block", "natural_blocks"),
            entry("bed_bug_egg", "natural_blocks"),
            entry("creeping_horror_egg", "natural_blocks"),
            entry("lurking_terror_egg", "natural_blocks"),
            entry("jumpy_bug_egg", "natural_blocks"),
            entry("spit_bug_egg", "natural_blocks"),
            entry("jerry_egg", "natural_blocks"),
            entry("wasp_nest", "natural_blocks"),
            entry("red_ant_nest", "natural_blocks"),
            entry("brown_ant_nest", "natural_blocks"),
            entry("rainbow_ant_nest", "natural_blocks"),
            entry("termite_nest", "natural_blocks"),
            entry("uranium_ore", "natural_blocks"),
            entry("deepslate_uranium_ore", "natural_blocks"),
            entry("titanium_ore", "natural_blocks"),
            entry("deepslate_titanium_ore", "natural_blocks"),
            entry("bluestone_ore", "redstone_blocks"),
            entry("corn_seeds", "natural_blocks"),
            entry("star_coral", "natural_blocks"),
            entry("star_coral_fan", "natural_blocks"),
            entry("star_coral_block", "natural_blocks"),
            entry("dead_star_coral", "natural_blocks"),
            entry("dead_star_coral_fan", "natural_blocks"),
            entry("dead_star_coral_block", "natural_blocks"),
            entry("duct_tape", "tools_and_utilities", "shears"),
            entry("diamond_minecart", "tools_and_utilities", "minecart"),
            entry("bile_bucket", "tools_and_utilities"),
            entry("ichor_bucket", "tools_and_utilities"),
            entry("antiwater_bucket", "tools_and_utilities"),
            entry("lumen_bucket", "tools_and_utilities"),
            entry("cloud_bucket", "tools_and_utilities"),
            entry("lucid_anchor", "tools_and_utilities"),
            entry("reverie_bottle", "tools_and_utilities"),
            entry("glimmer_bottle", "tools_and_utilities"),
            entry("critter_cage", "tools_and_utilities"),
            entry("ultimate_pickaxe", "tools_and_utilities"),
            entry("ultimate_axe", "tools_and_utilities"),
            entry("ultimate_shovel", "tools_and_utilities"),
            entry("ultimate_hoe", "tools_and_utilities"),
            entry("battle_axe", "combat"),
            entry("big_bertha", "combat"),
            entry("basilisk_dagger", "combat"),
            entry("ultimate_helmet", "combat"),
            entry("ultimate_chestplate", "combat"),
            entry("ultimate_leggings", "combat"),
            entry("ultimate_boots", "combat"),
            entry("ultimate_sword", "combat"),
            entry("ultimate_bow", "combat"),
            entry("ultimate_crossbow", "combat"),
            entry("squidzooka", "combat"),
            entry("rpo_launcher", "combat"),
            entry("krakens_grasp", "combat"),
            entry("shrink_ray", "combat"),
            entry("growth_ray", "combat"),
            entry("gravity_gun", "combat"),
            entry("water_cannon", "combat"),
            entry("attitude_adjuster", "combat"),
            entry("lucid_pearl", "combat"),
            entry("primordial_helmet", "combat"),
            entry("primordial_chestplate", "combat"),
            entry("primordial_leggings", "combat"),
            entry("primordial_boots", "combat"),
            entry("nightmare_helmet", "combat"),
            entry("nightmare_chestplate", "combat"),
            entry("nightmare_leggings", "combat"),
            entry("nightmare_boots", "combat"),
            entry("nightmare_sword", "combat"),
            entry("blood_crystal_katana", "combat"),
            entry("blood_crystal_helmet", "combat"),
            entry("blood_crystal_chestplate", "combat"),
            entry("blood_crystal_leggings", "combat"),
            entry("blood_crystal_boots", "combat"),
            entry("tigers_eye_helmet", "combat"),
            entry("tigers_eye_chestplate", "combat"),
            entry("tigers_eye_leggings", "combat"),
            entry("tigers_eye_boots", "combat"),
            entry("moggles", "combat"),
            entry("scorpion_whip", "combat"),
            entry("fallen_king_crown", "combat"),
            entry("brutalfly_elytra", "combat"),
            entry("jumpy_boots", "combat"),
            entry("raw_uranium_scrap", "ingredients"),
            entry("raw_titanium_scrap", "ingredients"),
            entry("bluestone", "redstone_blocks"),
            entry("raw_uranium", "ingredients"),
            entry("raw_titanium", "ingredients"),
            entry("uranium_nugget", "ingredients"),
            entry("titanium_nugget", "ingredients"),
            entry("uranium_ingot", "ingredients"),
            entry("titanium_ingot", "ingredients"),
            entry("cloud_shark_fin", "ingredients"),
            entry("cloud_shark_fin_soup", "ingredients"),
            entry("basilisk_fang", "ingredients"),
            entry("nightmare_scale", "ingredients"),
            entry("molevore_nose", "ingredients"),
            entry("moleworm", "ingredients"),
            entry("mud_pie", "ingredients"),
            entry("lucid_eye", "ingredients"),
            entry("blood_crystal_shard", "ingredients"),
            entry("tigers_eye", "ingredients"),
            entry("blood_crystal_apple", "ingredients"),
            entry("spirit_apple", "ingredients"),
            entry("mantis_claw", "combat"),
            entry("hercules_horn", "ingredients"),
            entry("king_scale", "ingredients"),
            entry("queen_scale", "ingredients"),
            entry("chitin", "ingredients"),
            entry("jerry_nucleus", "ingredients"),
            entry("stink_bug", "ingredients"),
            entry("brutalfly_wing", "ingredients"),
            entry("primordial_scute", "ingredients"),
            entry("triffid_goo", "ingredients"),
            entry("vortex_eye", "ingredients"),
            entry("emperor_scorpion_stinger", "ingredients"),
            entry("kraken_tooth", "ingredients"),
            entry("kraken_tentacle", "ingredients"),
            entry("kraken_kalamari", "ingredients"),
            entry("jumpy_bug_leg", "ingredients"),
            entry("cornea_ear", "ingredients"),
            entry("corn", "ingredients"),
            entry("high_fructose_corn_syrup", "ingredients"),
            entry("peach", "ingredients"),
            entry("peach_pie", "ingredients"),
            entry("cornbread", "ingredients"),
            entry("popcorn", "ingredients"),
            entry("raw_corndog", "ingredients"),
            entry("cooked_corndog", "ingredients"),
            entry("raw_venison", "ingredients"),
            entry("cooked_venison", "ingredients"),
            entry("raw_bug_meat", "ingredients"),
            entry("cooked_bug_meat", "ingredients"),
            entry("broodfruit", "ingredients"),
            entry("cheep", "ingredients"),
            entry("root_beer", "ingredients"),
            entry("rainbow_sugar", "ingredients"),
            entry("big_bertha_blade", "ingredients"),
            entry("big_bertha_handle", "ingredients"),
            entry("big_bertha_hilt", "ingredients"),
            entry("easter_bunny_spawn_egg", "spawn_eggs"),
            entry("flying_squirrel_spawn_egg", "spawn_eggs"),
            entry("caterpillar_spawn_egg", "spawn_eggs"),
            entry("butterfly_spawn_egg", "spawn_eggs"),
            entry("reverie_spawn_egg", "spawn_eggs"),
            entry("brutalfly_spawn_egg", "spawn_eggs"),
            entry("red_ant_spawn_egg", "spawn_eggs"),
            entry("brown_ant_spawn_egg", "spawn_eggs"),
            entry("rainbow_ant_spawn_egg", "spawn_eggs"),
            entry("termite_spawn_egg", "spawn_eggs"),
            entry("moleworm_spawn_egg", "spawn_eggs"),
            entry("mantis_spawn_egg", "spawn_eggs"),
            entry("alpha_mantis_spawn_egg", "spawn_eggs"),
            entry("rolly_polly_spawn_egg", "spawn_eggs"),
            entry("molevore_spawn_egg", "spawn_eggs"),
            entry("triffid_spawn_egg", "spawn_eggs"),
            entry("apple_cow_spawn_egg", "spawn_eggs"),
            entry("golden_apple_cow_spawn_egg", "spawn_eggs"),
            entry("enchanted_golden_apple_cow_spawn_egg", "spawn_eggs"),
            entry("dr_trayaurus_spawn_egg", "spawn_eggs"),
            entry("wasp_spawn_egg", "spawn_eggs"),
            entry("bomber_spawn_egg", "spawn_eggs"),
            entry("jumpy_bug_spawn_egg", "spawn_eggs"),
            entry("spit_bug_spawn_egg", "spawn_eggs"),
            entry("stink_bug_spawn_egg", "spawn_eggs"),
            entry("cloud_shark_spawn_egg", "spawn_eggs"),
            entry("kraken_spawn_egg", "spawn_eggs"),
            entry("missile_squid_spawn_egg", "spawn_eggs"),
            entry("octopus_bomb_spawn_egg", "spawn_eggs"),
            entry("nightmare_spawn_egg", "spawn_eggs"),
            entry("bed_bug_spawn_egg", "spawn_eggs"),
            entry("lucid_spawn_egg", "spawn_eggs"),
            entry("scorpion_spawn_egg", "spawn_eggs"),
            entry("basilisk_spawn_egg", "spawn_eggs"),
            entry("emperor_scorpion_spawn_egg", "spawn_eggs"),
            entry("toreterror_spawn_egg", "spawn_eggs"),
            entry("creeping_horror_spawn_egg", "spawn_eggs"),
            entry("lurking_terror_spawn_egg", "spawn_eggs"),
            entry("hercules_beetle_spawn_egg", "spawn_eggs"),
            entry("jerry_spawn_egg", "spawn_eggs"),
            entry("dorrie_spawn_egg", "spawn_eggs"),
            entry("ouranwood_deer_spawn_egg", "spawn_eggs"),
            entry("glimmer_spawn_egg", "spawn_eggs"),
            entry("elka_spawn_egg", "spawn_eggs")
    );

    private static final List<String> ANTARCHY_POTION_FAMILIES = List.of(
            "dread", "long_dread", "inversion", "long_inversion", "stinky", "long_stinky",
            "paralysis", "long_paralysis", "haste", "strong_haste", "shrinking",
            "strong_shrinking", "extreme_shrinking", "growing", "strong_growing", "extreme_growing"
    );

    private static final Map<String, Integer> ORDER_INDEX = createOrderIndex();

    private static CatalogEntry entry(String path, String neoTabPath) {
        return new CatalogEntry(path, neoTabPath, null);
    }

    private static CatalogEntry entry(String path, String neoTabPath, String insertAfterPath) {
        return new CatalogEntry(path, neoTabPath, insertAfterPath);
    }

    private static Map<String, Integer> createOrderIndex() {
        HashMap<String, Integer> index = new HashMap<>();
        for (int i = 0; i < CATALOG.size(); i++) {
            index.put(CATALOG.get(i).path(), i);
        }
        return index;
    }

    public static int orderIndex(Item item) {
        return ORDER_INDEX.getOrDefault(path(item), Integer.MAX_VALUE);
    }

    public static void populateAntarchyTab(AntarchyTabOutput output) {
        for (CatalogEntry entry : CATALOG) {
            if ("glimmer_bottle".equals(entry.path())) {
                for (GlimmerVariant variant : GlimmerVariant.values()) {
                    output.accept(GlimmerBottleItem.create(variant));
                }
                continue;
            }
            output.accept(item(entry.path()));
        }
        for (String potionPath : ANTARCHY_POTION_FAMILIES) {
            acceptPotionFamily(output, potionPath);
        }
    }

    public static boolean populateNeoForgeVanillaTab(ResourceKey<CreativeModeTab> tabKey, HolderLookup.Provider holders,
                                                     NeoForgeVanillaTabOutput output) {
        String tabPath = tabKey.location().getPath();
        boolean matched = false;
        for (CatalogEntry entry : CATALOG) {
            if (!tabPath.equals(entry.neoTabPath())) {
                continue;
            }
            matched = true;
            if (entry.insertAfterPath() != null) {
                output.insertAfter(vanillaItem(entry.insertAfterPath()), item(entry.path()));
                continue;
            }
            if ("combat".equals(tabPath)) {
                if (isUltimateArmor(entry.path())) {
                    output.accept(UltimateGearHelper.createUltimateArmorStack(item(entry.path()), holders));
                    continue;
                }
                if ("ultimate_bow".equals(entry.path())) {
                    output.accept(UltimateGearHelper.createUltimateBowStack(item(entry.path()), holders));
                    continue;
                }
            }
            output.accept(item(entry.path()));
        }
        if ("combat".equals(tabPath)) {
            for (String potionPath : ANTARCHY_POTION_FAMILIES) {
                Holder<Potion> potion = potion(potionPath);
                if (potion != null) {
                    output.accept(PotionContents.createItemStack(Items.POTION, potion));
                }
            }
            matched = true;
        }
        return matched;
    }

    private static boolean isUltimateArmor(String path) {
        return "ultimate_helmet".equals(path)
                || "ultimate_chestplate".equals(path)
                || "ultimate_leggings".equals(path)
                || "ultimate_boots".equals(path);
    }

    private static void acceptPotionFamily(AntarchyTabOutput output, String path) {
        Holder<Potion> potion = potion(path);
        if (potion == null) {
            return;
        }
        output.accept(PotionContents.createItemStack(Items.POTION, potion));
        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, potion));
        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, potion));
        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, potion));
    }

    private static String path(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MODID, path));
    }

    private static Item vanillaItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(path));
    }

    private static Holder<Potion> potion(String path) {
        Potion potion = BuiltInRegistries.POTION.get(ResourceLocation.fromNamespaceAndPath(MODID, path));
        return potion == null ? null : BuiltInRegistries.POTION.wrapAsHolder(potion);
    }
}
