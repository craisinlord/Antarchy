package com.craisinlord.antarchy.content;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

public final class AntarchyTags {
    private AntarchyTags() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    public static final class Blocks {
        public static final TagKey<Block> DUPLICATOR_TREE_BLACKLIST = TagKey.create(Registries.BLOCK, id("duplicator_tree_blacklist"));
        public static final TagKey<Block> GRAVITY_GUN_BLACKLIST = TagKey.create(Registries.BLOCK, id("gravity_gun_blacklist"));
        public static final TagKey<Block> DREAM_FIRE_BASE_BLOCKS = TagKey.create(Registries.BLOCK, id("dream_fire_base_blocks"));
        public static final TagKey<Block> POTENT_NYXITE_ACTIVATION_BLOCKS = TagKey.create(Registries.BLOCK, id("potent_nyxite_activation_blocks"));
        public static final TagKey<Block> CORNEA_STALK_PLANTABLE = TagKey.create(Registries.BLOCK, id("cornea_stalk_plantable"));
        public static final TagKey<Block> MOLEVORE_BREAKABLE_BLOCKS = TagKey.create(Registries.BLOCK, id("molevore_breakable_blocks"));
        public static final TagKey<Block> NIGHTMARE_BREAKABLE = TagKey.create(Registries.BLOCK, id("nightmare_breakable"));
        public static final TagKey<Block> MOLEVORE_SEE_THROUGH_BLOCKS = TagKey.create(Registries.BLOCK, id("molevore_see_through_blocks"));
        public static final TagKey<Block> TERMITE_FOODS = TagKey.create(Registries.BLOCK, id("termite_foods"));
        public static final TagKey<Block> UMBRAL_MOSS_REPLACEABLE = TagKey.create(Registries.BLOCK, id("umbral_moss_replaceable"));
        public static final TagKey<Block> BLUSH_MOSS_REPLACEABLE = TagKey.create(Registries.BLOCK, id("blush_moss_replaceable"));
        public static final TagKey<Block> BIOWART_REPLACEABLE = TagKey.create(Registries.BLOCK, id("biowart_replaceable"));
        public static final TagKey<Block> BIOWART_TENDRILS_PLANTABLE = TagKey.create(Registries.BLOCK, id("biowart_tendrils_plantable"));
        public static final TagKey<Block> HERCULES_BEETLE_CHARGE_IMMUNE_BLOCKS = TagKey.create(Registries.BLOCK, id("hercules_beetle_charge_immune_blocks"));
        public static final TagKey<Block> ELYTHIA_PORTAL_FRAMES = TagKey.create(Registries.BLOCK, id("elythia_portal_frames"));
        public static final TagKey<Block> THORAXIS_PORTAL_FRAMES = TagKey.create(Registries.BLOCK, id("thoraxis_portal_frames"));
        public static final TagKey<Block> CAVARYN_PORTAL_FRAMES = TagKey.create(Registries.BLOCK, id("cavaryn_portal_frames"));
        public static final TagKey<Block> RAINBOW_PORTAL_FRAMES = TagKey.create(Registries.BLOCK, id("rainbow_portal_frames"));
        public static final TagKey<Block> ROLLY_CAVERN_CARVEABLE = TagKey.create(Registries.BLOCK, id("rolly_cavern_carveable"));
        public static final TagKey<Block> MINERS_DREAM_CARVABLE = TagKey.create(Registries.BLOCK, id("miners_dream_carvable"));
        public static final TagKey<Block> BLUESTONE_COMPONENTS = TagKey.create(Registries.BLOCK, id("bluestone_components"));
        public static final TagKey<Block> BLUESTONE_CONNECTABLE = TagKey.create(Registries.BLOCK, id("bluestone_connectable"));
        public static final TagKey<Block> ANTIMETAL_INVERSION_BLOCKS = TagKey.create(Registries.BLOCK, id("antimetal_inversion_blocks"));
        public static final TagKey<Block> TIGER_EYE_CAMOUFLAGE_BLACKLIST = TagKey.create(Registries.BLOCK, id("tiger_eye_camouflage_blacklist"));
        public static final TagKey<Block> LUCID_ANCHOR_BASE_BLOCKS = TagKey.create(Registries.BLOCK, id("lucid_anchor_base_blocks"));

        private Blocks() {
        }
    }

    public static final class DamageType {
        public static final TagKey<net.minecraft.world.damagesource.DamageType> BYPASSES_BLOODGLASS =
                TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("antarchy", "bypasses_bloodglass"));
        public static final TagKey<net.minecraft.world.damagesource.DamageType> ANTARCHY_MAGIC_BURST =
                TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("antarchy", "antarchy_magic_burst"));

        private DamageType() {
        }
    }

    public static final class Entities {
        public static final TagKey<EntityType<?>> BASILISK_PREY = TagKey.create(Registries.ENTITY_TYPE, id("basilisk_prey"));
        public static final TagKey<EntityType<?>> SPELL_RESISTANT_BOSS = TagKey.create(Registries.ENTITY_TYPE, id("spell_resistant_boss"));
        public static final TagKey<EntityType<?>> DREAM_SAND_LOW_GRAVITY_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("dream_sand_low_gravity_blacklist"));
        public static final TagKey<EntityType<?>> GRAVITY_GUN_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("gravity_gun_blacklist"));
        public static final TagKey<EntityType<?>> INVERTED_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("inverted_immune"));
        public static final TagKey<EntityType<?>> LUCID_BOLT_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("lucid_bolt_immune"));
        public static final TagKey<EntityType<?>> WIND_VORTEX_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("wind_vortex_immune"));
        public static final TagKey<EntityType<?>> DUCT_TAPE_STICK_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("duct_tape_stick_blacklist"));
        public static final TagKey<EntityType<?>> NIGHTMARE_NO_ATTACK = TagKey.create(Registries.ENTITY_TYPE, id("nightmare_no_attack"));
        public static final TagKey<EntityType<?>> SIZE_CHANGING_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("size_changing_immune"));
        public static final TagKey<EntityType<?>> CRITTER_CAGE_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("critter_cage_blacklist"));
        public static final TagKey<EntityType<?>> PARALYSIS_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("paralysis_immune"));
        public static final TagKey<EntityType<?>> HUSHWEED_TARGETS = TagKey.create(Registries.ENTITY_TYPE, id("hushweed_targets"));
        public static final TagKey<EntityType<?>> TRIFFID_PREY = TagKey.create(Registries.ENTITY_TYPE, id("triffid_prey"));
        public static final TagKey<EntityType<?>> STINKY_REPELLED_HOSTILES = TagKey.create(Registries.ENTITY_TYPE, id("stinky_repelled_hostiles"));
        public static final TagKey<EntityType<?>> SCORPION_WHIP_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("scorpion_whip_immune"));
        public static final TagKey<EntityType<?>> SCORPION_WHIP_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("scorpion_whip_blacklist"));
        public static final TagKey<EntityType<?>> TIGER_EYE_DETECTION_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("tiger_eye_detection_immune"));
        public static final TagKey<EntityType<?>> LUCID_ANCHOR_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("lucid_anchor_immune"));
        public static final TagKey<EntityType<?>> DIMENSIONAL_TEAR_COMMON_SPAWNS = TagKey.create(Registries.ENTITY_TYPE, id("dimensional_tear_common_spawns"));
        public static final TagKey<EntityType<?>> DIMENSIONAL_TEAR_RARE_SPAWNS = TagKey.create(Registries.ENTITY_TYPE, id("dimensional_tear_rare_spawns"));
        public static final TagKey<EntityType<?>> TIME_DILATION_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("time_dilation_immune"));

        private Entities() {
        }
    }

    public static final class SoundEvents {
        
        public static final TagKey<SoundEvent> DREAD_HALLUCINATION_SOUNDS = TagKey.create(Registries.SOUND_EVENT, id("dread_hallucination_sounds"));

        private SoundEvents() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> EASTER_BUNNY_SPAWN_EGG_BLACKLIST = TagKey.create(Registries.ITEM, id("easter_bunny_spawn_egg_blacklist"));
        public static final TagKey<Item> DUCT_TAPE_BLACKLIST = TagKey.create(Registries.ITEM, id("duct_tape_blacklist"));
        public static final TagKey<Item> BROWN_ANT_ACTIVATION_ITEMS = TagKey.create(Registries.ITEM, id("brown_ant_activation_items"));
        public static final TagKey<Item> BROWN_ANT_BREEDING_FOODS = TagKey.create(Registries.ITEM, id("brown_ant_breeding_foods"));
        public static final TagKey<Item> MULTISHOT_ENCHANTABLE = TagKey.create(Registries.ITEM, id("multishot_enchantable"));
        public static final TagKey<Item> RED_ANT_ACTIVATION_ITEMS = TagKey.create(Registries.ITEM, id("red_ant_activation_items"));
        public static final TagKey<Item> RED_ANT_BREEDING_FOODS = TagKey.create(Registries.ITEM, id("red_ant_breeding_foods"));
        public static final TagKey<Item> RAINBOW_ANT_ACTIVATION_ITEMS = TagKey.create(Registries.ITEM, id("rainbow_ant_activation_items"));
        public static final TagKey<Item> RAINBOW_ANT_BREEDING_FOODS = TagKey.create(Registries.ITEM, id("rainbow_ant_breeding_foods"));
        public static final TagKey<Item> TERMITE_ACTIVATION_ITEMS = TagKey.create(Registries.ITEM, id("termite_activation_items"));
        public static final TagKey<Item> TERMITE_FOODS = TagKey.create(Registries.ITEM, id("termite_foods"));
        public static final TagKey<Item> TERMITE_BREEDING_FOODS = TagKey.create(Registries.ITEM, id("termite_breeding_foods"));
        public static final TagKey<Item> FLYING_SQUIRREL_NUTS = TagKey.create(Registries.ITEM, id("flying_squirrel_nuts"));
        public static final TagKey<Item> CATERPILLAR_FOODS = TagKey.create(Registries.ITEM, id("caterpillar_foods"));
        public static final TagKey<Item> BUTTERFLY_BREEDING_FOODS = TagKey.create(Registries.ITEM, id("butterfly_breeding_foods"));
        public static final TagKey<Item> REVERIE_DUPLICATION_ITEMS = TagKey.create(Registries.ITEM, id("reverie_duplication_items"));
        public static final TagKey<Item> HERCULES_BEETLE_FOOD = TagKey.create(Registries.ITEM, id("hercules_beetle_food"));
        public static final TagKey<Item> ROYAL_MOUNT_FOOD = TagKey.create(Registries.ITEM, id("royal_mount_food"));
        public static final TagKey<Item> ROLLY_POLLY_FOOD = TagKey.create(Registries.ITEM, id("rolly_polly_food"));
        public static final TagKey<Item> OURANWOOD_DEER_FOOD = TagKey.create(Registries.ITEM, id("ouranwood_deer_food"));
        public static final TagKey<Item> GLIMMER_AUGMENT_APPLE_COW = TagKey.create(Registries.ITEM, id("glimmer_augment_apple_cow"));
        public static final TagKey<Item> GLIMMER_AUGMENT_OURANWOOD_DEER = TagKey.create(Registries.ITEM, id("glimmer_augment_ouranwood_deer"));
        public static final TagKey<Item> GLIMMER_AUGMENT_FROG = TagKey.create(Registries.ITEM, id("glimmer_augment_frog"));
        public static final TagKey<Item> GLIMMER_AUGMENT_ANT = TagKey.create(Registries.ITEM, id("glimmer_augment_ant"));
        public static final TagKey<Item> GLIMMER_AUGMENT_ELKA = TagKey.create(Registries.ITEM, id("glimmer_augment_elka"));
        public static final TagKey<Item> HFCS_CANNOT_AMERICANIZE = TagKey.create(Registries.ITEM, id("hfcs_cannot_americanize"));
        public static final TagKey<Item> TIGER_EYE_ARMOR = TagKey.create(Registries.ITEM, id("tigers_eye_armor"));

        private Items() {
        }
    }

    public static final class Fluids {
        public static final TagKey<Fluid> LUMEN = TagKey.create(Registries.FLUID, id("lumen"));
        public static final TagKey<Fluid> GIANT_LILY_PAD_SUPPORTING_FLUIDS = TagKey.create(Registries.FLUID, id("giant_lily_pad_supporting_fluids"));
        public static final TagKey<Fluid> ANTIWATER = TagKey.create(Registries.FLUID, id("antiwater"));

        private Fluids() {
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> MANTIS_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("mantis_spawn_biomes"));
        public static final TagKey<Biome> MANTIS_OVERWORLD_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("mantis_overworld_spawn_biomes"));
        public static final TagKey<Biome> ELYTHIA_FIREFLY_PARTICLE_BIOMES = TagKey.create(Registries.BIOME, id("elythia_firefly_particle_biomes"));
        public static final TagKey<Biome> WILD_CORN_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("wild_corn_spawn_biomes"));
        public static final TagKey<Biome> USES_HORDE_SYSTEM = TagKey.create(Registries.BIOME, id("uses_horde_system"));

        private Biomes() {
        }
    }
}
