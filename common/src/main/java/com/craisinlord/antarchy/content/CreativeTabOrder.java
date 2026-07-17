package com.craisinlord.antarchy.content;

import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public final class CreativeTabOrder {
    private CreativeTabOrder() {}

    public static final Comparator<Item> COMPARATOR =
            Comparator.comparingInt(CreativeTabOrder::group)
                    .thenComparingInt(CreativeTabOrder::subOrder);

    public static int group(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return switch (path) {
            case "ouranwood_log", "ouranwood_wood", "mossy_ouranwood_log", "mossy_ouranwood_wood",
                 "stripped_ouranwood_log", "stripped_ouranwood_wood", "duplicator_log",
                 "ouranwood_planks", "ouranwood_stairs", "ouranwood_slab",
                 "ouranwood_fence", "ouranwood_fence_gate",
                 "ouranwood_door", "ouranwood_trapdoor",
                 "ouranwood_pressure_plate", "ouranwood_button",
                 "ouranwood_sign", "ouranwood_hanging_sign",
                 "ouranwood_boat", "ouranwood_chest_boat",
                 "peach_log", "peach_wood", "stripped_peach_log", "stripped_peach_wood",
                 "peach_planks", "peach_stairs", "peach_slab", "peach_fence", "peach_fence_gate",
                 "peach_door", "peach_trapdoor", "peach_pressure_plate", "peach_button",
                 "peach_sign", "peach_hanging_sign", "peach_boat", "peach_chest_boat" -> 0;
            case "nyxite", "polished_nyxite", "chiseled_nyxite", "nyxite_bricks",
                 "nyxite_stairs", "nyxite_slab", "nyxite_wall",
                 "polished_nyxite_stairs", "polished_nyxite_slab", "polished_nyxite_wall",
                 "nyxite_brick_stairs", "nyxite_brick_slab", "nyxite_brick_wall",
                 "pale_nyxite", "nyxite_spike", "potent_nyxite" -> 1;
            case "shellstone", "polished_shellstone", "shellstone_bricks", "chiseled_shellstone",
                 "mossy_shellstone_bricks", "cracked_shellstone_bricks",
                 "shellstone_stairs", "shellstone_slab", "shellstone_wall",
                 "polished_shellstone_stairs", "polished_shellstone_slab", "polished_shellstone_wall",
                 "shellstone_brick_stairs", "shellstone_brick_slab", "shellstone_brick_wall",
                 "mossy_shellstone_brick_stairs", "mossy_shellstone_brick_slab", "mossy_shellstone_brick_wall" -> 2;
            case "antimetal", "polished_antimetal" -> 3;
            case "dream_sand", "dream_sandstone", "chiseled_dream_sandstone",
                 "cut_dream_sandstone", "smooth_dream_sandstone",
                 "dream_sandstone_stairs", "dream_sandstone_slab", "dream_sandstone_wall",
                 "smooth_dream_sandstone_stairs", "smooth_dream_sandstone_slab",
                 "cut_dream_sandstone_slab" -> 4;
            case "umbral_moss_block", "umbral_moss_carpet",
                 "blush_moss_block", "blush_moss_carpet" -> 4;
            case "uranium_ore", "deepslate_uranium_ore", "titanium_ore", "deepslate_titanium_ore",
                 "uranium_block", "titanium_block",
                 "cut_uranium", "cut_titanium", "cut_uranium_slab", "cut_titanium_slab",
                 "cut_uranium_stairs", "cut_titanium_stairs",
                 "chiseled_uranium", "chiseled_titanium",
                 "uranium_door", "titanium_door", "uranium_trapdoor", "titanium_trapdoor",
                 "uranium_bars", "titanium_bars", "uranium_bulb", "titanium_bulb" -> 6;
            case "raw_uranium", "raw_titanium", "raw_uranium_scrap", "raw_titanium_scrap",
                 "raw_uranium_block", "raw_titanium_block" -> 7;
            case "blood_crystal_block", "small_blood_crystal_bud", "medium_blood_crystal_bud",
                 "large_blood_crystal_bud", "budding_blood_crystal", "blood_crystal_cluster",
                 "myrmite", "broodstone", "chiten_block", "chiten_spike", "amber_moss_block", "amber_moss_carpet" -> 8;
            case "ouranwood_leaves", "peach_leaves" -> 9;
            case "duplicator_sapling", "ouranwood_acorn", "peach_sapling", "corn_seeds" -> 10;
            case "orange_milkweed", "pink_milkweed", "camellia", "spider_lily", "lotus", "hushweed", "amber_lichen", "creepvine",
                 "triffid_goo_block", "cloud_block", "wasp_nest",
                 "red_ant_nest", "brown_ant_nest", "rainbow_ant_nest", "termite_nest",
                 "star_coral", "star_coral_fan", "star_coral_block",
                 "dead_star_coral", "dead_star_coral_fan", "dead_star_coral_block" -> 11;
            case "dream_torch", "dream_lantern", "dream_campfire", "dream_fire", "dream_fire_ceiling",
                 "lumen_froglight", "roseate_froglight" -> 12;
            case "antimetal_scaffolding" -> 13;
            case "infested_rooted_dirt", "infested_coarse_dirt", "bed_bug_egg" -> 14;
            case "ultimate_pickaxe", "ultimate_axe", "ultimate_shovel", "ultimate_hoe",
                 "duct_tape", "diamond_minecart", "critter_cage", "gravity_sculk_sensor", "miners_dream" -> 15;
            case "bile_bucket", "ichor_bucket", "antiwater_bucket", "cloud_bucket", "lumen_bucket" -> 16;
            case "moggles", "brutalfly_elytra", "fallen_king_crown", "jumpy_boots" -> 17;
            case "battle_axe", "basilisk_dagger", "squidzooka", "rpo_launcher", "shrink_ray", "growth_ray",
                 "gravity_gun", "water_cannon", "nightmare_sword", "lucid_pearl", "scorpion_whip", "blood_crystal_katana",
                 "big_bertha", "big_bertha_blade", "big_bertha_handle", "big_bertha_hilt",
                 "ultimate_sword", "ultimate_mace", "krakens_grasp" -> 18;
            case "primordial_helmet", "primordial_chestplate",
                 "primordial_leggings", "primordial_boots",
                 "blood_crystal_helmet", "blood_crystal_chestplate",
                 "blood_crystal_leggings", "blood_crystal_boots",
                 "nightmare_helmet", "nightmare_chestplate",
                 "nightmare_leggings", "nightmare_boots",
                 "ultimate_helmet", "ultimate_chestplate",
                 "ultimate_leggings", "ultimate_boots" -> 19;
            case "ultimate_bow", "ultimate_crossbow" -> 20;
            case "cloud_shark_fin_soup", "mud_pie", "peach_pie", "cornbread", "popcorn", "rainbow_sugar",
                 "blood_crystal_apple", "spirit_apple", "peach", "raw_corndog", "cooked_corndog",
                 "raw_venison", "cooked_venison" -> 21;
            case "blood_crystal_shard", "cloud_shark_fin", "basilisk_fang", "kraken_tooth", "kraken_tentacle",
                 "emperor_scorpion_stinger", "nightmare_scale", "molevore_nose",
                 "moleworm", "uranium_nugget", "titanium_nugget",
                 "uranium_ingot", "titanium_ingot", "lucid_eye",
                 "mantis_claw", "jumpy_bug_leg", "brutalfly_wing", "cornea_ear",
                 "primordial_scute", "triffid_goo", "vortex_eye",
                 "king_scale", "queen_scale", "chiten", "stink_bug", "corn", "high_fructose_corn_syrup" -> 22;
            case "reverie_bottle", "glimmer_bottle", "root_beer" -> 24;
            case "easter_bunny_spawn_egg", "flying_squirrel_spawn_egg", "caterpillar_spawn_egg",
                 "butterfly_spawn_egg", "reverie_spawn_egg", "brutalfly_spawn_egg",
                 "red_ant_spawn_egg", "brown_ant_spawn_egg", "rainbow_ant_spawn_egg", "termite_spawn_egg",
                 "moleworm_spawn_egg", "mantis_spawn_egg", "alpha_mantis_spawn_egg", "rolly_polly_spawn_egg",
                 "molevore_spawn_egg", "triffid_spawn_egg", "apple_cow_spawn_egg", "golden_apple_cow_spawn_egg",
                 "enchanted_golden_apple_cow_spawn_egg", "dr_trayaurus_spawn_egg", "wasp_spawn_egg",
                 "bomber_spawn_egg", "jumpy_bug_spawn_egg", "stink_bug_spawn_egg", "cloud_shark_spawn_egg",
                 "kraken_spawn_egg", "missile_squid_spawn_egg", "octopus_bomb_spawn_egg",
                 "nightmare_spawn_egg", "bed_bug_spawn_egg", "lucid_spawn_egg", "scorpion_spawn_egg",
                 "basilisk_spawn_egg", "emperor_scorpion_spawn_egg", "toreterror_spawn_egg",
                 "dorrie_spawn_egg", "glimmer_spawn_egg", "ouranwood_deer_spawn_egg", "elka_spawn_egg",
                 "hercules_beetle_spawn_egg", "creeping_horror_spawn_egg", "lurking_terror_spawn_egg" -> 25;
            default -> 99;
        };
    }

    public static int subOrder(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return switch (path) {
            case "ouranwood_log" -> 0;
            case "ouranwood_wood" -> 1;
            case "mossy_ouranwood_log" -> 2;
            case "mossy_ouranwood_wood" -> 3;
            case "stripped_ouranwood_log" -> 4;
            case "stripped_ouranwood_wood" -> 5;
            case "duplicator_log" -> 6;
            case "ouranwood_planks" -> 7;
            case "ouranwood_stairs" -> 8;
            case "ouranwood_slab" -> 9;
            case "ouranwood_fence" -> 10;
            case "ouranwood_fence_gate" -> 11;
            case "ouranwood_door" -> 12;
            case "ouranwood_trapdoor" -> 13;
            case "ouranwood_pressure_plate" -> 14;
            case "ouranwood_button" -> 15;
            case "ouranwood_sign" -> 16;
            case "ouranwood_hanging_sign" -> 17;
            case "ouranwood_boat" -> 18;
            case "ouranwood_chest_boat" -> 19;
            case "peach_log" -> 20;
            case "peach_wood" -> 21;
            case "stripped_peach_log" -> 22;
            case "stripped_peach_wood" -> 23;
            case "peach_planks" -> 24;
            case "peach_stairs" -> 25;
            case "peach_slab" -> 26;
            case "peach_fence" -> 27;
            case "peach_fence_gate" -> 28;
            case "peach_door" -> 29;
            case "peach_trapdoor" -> 30;
            case "peach_pressure_plate" -> 31;
            case "peach_button" -> 32;
            case "peach_sign" -> 33;
            case "peach_hanging_sign" -> 34;
            case "peach_boat" -> 35;
            case "peach_chest_boat" -> 36;
            case "ouranwood_leaves" -> 0;
            case "peach_leaves" -> 1;
            case "duplicator_sapling" -> 0;
            case "ouranwood_acorn" -> 1;
            case "peach_sapling" -> 2;
            case "corn_seeds" -> 3;
            case "orange_milkweed" -> 0;
            case "pink_milkweed" -> 1;
            case "camellia" -> 2;
            case "spider_lily" -> 3;
            case "hushweed" -> 4;
            case "amber_lichen" -> 5;
            case "creepvine" -> 6;
            case "triffid_goo_block" -> 7;
            case "cloud_block" -> 8;
            case "wasp_nest" -> 9;
            case "red_ant_nest" -> 10;
            case "brown_ant_nest" -> 11;
            case "rainbow_ant_nest" -> 12;
            case "termite_nest" -> 13;
            case "star_coral" -> 14;
            case "star_coral_fan" -> 15;
            case "star_coral_block" -> 16;
            case "dead_star_coral" -> 17;
            case "dead_star_coral_fan" -> 18;
            case "dead_star_coral_block" -> 19;
            case "lotus" -> 20;
            case "blood_crystal_block" -> 0;
            case "small_blood_crystal_bud" -> 1;
            case "medium_blood_crystal_bud" -> 2;
            case "large_blood_crystal_bud" -> 3;
            case "budding_blood_crystal" -> 4;
            case "blood_crystal_cluster" -> 5;
            case "myrmite" -> 6;
            case "broodstone" -> 7;
            case "chiten_block" -> 8;
            case "chiten_spike" -> 9;
            case "amber_moss_block" -> 10;
            case "amber_moss_carpet" -> 11;
            case "uranium_ore" -> 0;
            case "deepslate_uranium_ore" -> 1;
            case "titanium_ore" -> 2;
            case "deepslate_titanium_ore" -> 3;
            case "uranium_block" -> 4;
            case "titanium_block" -> 5;
            case "cut_uranium" -> 6;
            case "cut_titanium" -> 7;
            case "cut_uranium_slab" -> 8;
            case "cut_titanium_slab" -> 9;
            case "cut_uranium_stairs" -> 10;
            case "cut_titanium_stairs" -> 11;
            case "chiseled_uranium" -> 12;
            case "chiseled_titanium" -> 13;
            case "uranium_door" -> 14;
            case "titanium_door" -> 15;
            case "uranium_trapdoor" -> 16;
            case "titanium_trapdoor" -> 17;
            case "uranium_bars" -> 18;
            case "titanium_bars" -> 19;
            case "uranium_bulb" -> 20;
            case "titanium_bulb" -> 21;
            case "raw_uranium" -> 0;
            case "raw_titanium" -> 1;
            case "raw_uranium_scrap" -> 2;
            case "raw_titanium_scrap" -> 3;
            case "raw_uranium_block" -> 4;
            case "raw_titanium_block" -> 5;
            case "dream_torch" -> 0;
            case "dream_lantern" -> 1;
            case "dream_campfire" -> 2;
            case "dream_fire" -> 3;
            case "dream_fire_ceiling" -> 4;
            case "lumen_froglight" -> 5;
            case "roseate_froglight" -> 6;
            case "infested_rooted_dirt" -> 0;
            case "infested_coarse_dirt" -> 1;
            case "bed_bug_egg" -> 2;
            case "ultimate_pickaxe" -> 0;
            case "ultimate_axe" -> 1;
            case "ultimate_shovel" -> 2;
            case "ultimate_hoe" -> 3;
            case "duct_tape" -> 4;
            case "diamond_minecart" -> 5;
            case "critter_cage" -> 6;
            case "gravity_sculk_sensor" -> 7;
            case "miners_dream" -> 8;
            case "bile_bucket" -> 0;
            case "ichor_bucket" -> 1;
            case "antiwater_bucket" -> 2;
            case "cloud_bucket" -> 3;
            case "lumen_bucket" -> 4;
            case "moggles" -> 0;
            case "brutalfly_elytra" -> 1;
            case "fallen_king_crown" -> 2;
            case "jumpy_boots" -> 3;
            case "battle_axe" -> 0;
            case "basilisk_dagger" -> 1;
            case "squidzooka" -> 2;
            case "rpo_launcher" -> 3;
            case "shrink_ray" -> 4;
            case "growth_ray" -> 5;
            case "gravity_gun" -> 6;
            case "water_cannon" -> 7;
            case "nightmare_sword" -> 8;
            case "lucid_pearl" -> 9;
            case "scorpion_whip" -> 10;
            case "blood_crystal_katana" -> 11;
            case "big_bertha" -> 12;
            case "big_bertha_blade" -> 13;
            case "big_bertha_handle" -> 14;
            case "big_bertha_hilt" -> 15;
            case "ultimate_sword" -> 16;
            case "ultimate_mace" -> 17;
            case "krakens_grasp" -> 18;
            case "primordial_helmet" -> 0;
            case "primordial_chestplate" -> 1;
            case "primordial_leggings" -> 2;
            case "primordial_boots" -> 3;
            case "blood_crystal_helmet" -> 4;
            case "blood_crystal_chestplate" -> 5;
            case "blood_crystal_leggings" -> 6;
            case "blood_crystal_boots" -> 7;
            case "nightmare_helmet" -> 8;
            case "nightmare_chestplate" -> 9;
            case "nightmare_leggings" -> 10;
            case "nightmare_boots" -> 11;
            case "ultimate_helmet" -> 12;
            case "ultimate_chestplate" -> 13;
            case "ultimate_leggings" -> 14;
            case "ultimate_boots" -> 15;
            case "ultimate_bow" -> 0;
            case "ultimate_crossbow" -> 1;
            case "cloud_shark_fin_soup" -> 0;
            case "mud_pie" -> 1;
            case "peach_pie" -> 2;
            case "cornbread" -> 3;
            case "popcorn" -> 4;
            case "rainbow_sugar" -> 5;
            case "raw_corndog" -> 6;
            case "cooked_corndog" -> 7;
            case "raw_venison" -> 8;
            case "cooked_venison" -> 9;
            case "blood_crystal_apple" -> 10;
            case "spirit_apple" -> 11;
            case "peach" -> 12;
            case "reverie_bottle" -> 0;
            case "glimmer_bottle" -> 1;
            case "root_beer" -> 2;
            case "blood_crystal_shard" -> 0;
            case "cloud_shark_fin" -> 1;
            case "basilisk_fang" -> 2;
            case "kraken_tooth" -> 3;
            case "kraken_tentacle" -> 4;
            case "emperor_scorpion_stinger" -> 5;
            case "nightmare_scale" -> 6;
            case "molevore_nose" -> 7;
            case "moleworm" -> 8;
            case "uranium_nugget" -> 9;
            case "titanium_nugget" -> 10;
            case "uranium_ingot" -> 11;
            case "titanium_ingot" -> 12;
            case "lucid_eye" -> 13;
            case "mantis_claw" -> 14;
            case "jumpy_bug_leg" -> 15;
            case "brutalfly_wing" -> 16;
            case "cornea_ear" -> 17;
            case "primordial_scute" -> 18;
            case "triffid_goo" -> 19;
            case "vortex_eye" -> 20;
            case "king_scale" -> 21;
            case "queen_scale" -> 22;
            case "chiten" -> 23;
            case "stink_bug" -> 24;
            case "corn" -> 25;
            case "high_fructose_corn_syrup" -> 26;
            default -> 0;
        };
    }
}
