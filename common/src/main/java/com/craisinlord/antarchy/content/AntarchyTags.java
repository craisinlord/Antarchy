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

        private Blocks() {
        }
    }

    public static final class DamageType {
        public static final TagKey<net.minecraft.world.damagesource.DamageType> BYPASSES_BLOODGLASS =
                TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("antarchy", "bypasses_bloodglass"));

        private DamageType() {
        }
    }

    public static final class Entities {
        public static final TagKey<EntityType<?>> BASILISK_PREY = TagKey.create(Registries.ENTITY_TYPE, id("basilisk_prey"));
        public static final TagKey<EntityType<?>> DREAM_SAND_LOW_GRAVITY_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("dream_sand_low_gravity_blacklist"));
        public static final TagKey<EntityType<?>> GRAVITY_GUN_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("gravity_gun_blacklist"));
        public static final TagKey<EntityType<?>> INVERTED_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("inverted_immune"));
        public static final TagKey<EntityType<?>> LUCID_BOLT_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("lucid_bolt_immune"));
        public static final TagKey<EntityType<?>> DUCT_TAPE_STICK_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("duct_tape_stick_blacklist"));
        public static final TagKey<EntityType<?>> NIGHTMARE_NO_ATTACK = TagKey.create(Registries.ENTITY_TYPE, id("nightmare_no_attack"));
        public static final TagKey<EntityType<?>> SIZE_CHANGING_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("size_changing_immune"));
        public static final TagKey<EntityType<?>> PARALYSIS_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("paralysis_immune"));
        public static final TagKey<EntityType<?>> HUSHWEED_TARGETS = TagKey.create(Registries.ENTITY_TYPE, id("hushweed_targets"));
        public static final TagKey<EntityType<?>> TRIFFID_PREY = TagKey.create(Registries.ENTITY_TYPE, id("triffid_prey"));
        public static final TagKey<EntityType<?>> SCORPION_WHIP_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, id("scorpion_whip_immune"));
        public static final TagKey<EntityType<?>> SCORPION_WHIP_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, id("scorpion_whip_blacklist"));

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

        private Items() {
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> MANTIS_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("mantis_spawn_biomes"));
        public static final TagKey<Biome> MANTIS_OVERWORLD_SPAWN_BIOMES = TagKey.create(Registries.BIOME, id("mantis_overworld_spawn_biomes"));

        private Biomes() {
        }
    }
}
