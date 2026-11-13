package com.craisinlord.antarchy.content;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.item.AntarchyItemComponent;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DimensionalTearMarkerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.FossilBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity;
import com.craisinlord.antarchy.content.block.entity.RemembranceTorchBlockEntity;
import com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.craisinlord.antarchy.content.block.entity.VortexLensBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.CheepEntity;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import com.craisinlord.antarchy.content.entity.CritterCageProjectileEntity;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import com.craisinlord.antarchy.content.entity.ElkaEntity;
import com.craisinlord.antarchy.content.entity.FlytrapEntity;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import com.craisinlord.antarchy.content.entity.LaunchedAnvilEntity;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.RollyPollyEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.craisinlord.antarchy.content.entity.WormHookProjectileEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmarePortalEntity;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.antarchy.content.entity.royal.PrinceEntity;
import com.craisinlord.antarchy.content.entity.royal.PrincessEntity;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalAssailantBlackHoleEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalBoltEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalElementalProjectileEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexChargeProjectileEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import com.craisinlord.antarchy.content.entity.vortex.WindVortexEntity;
import com.craisinlord.antarchy.content.menu.DorrieInventoryMenu;
import com.craisinlord.antarchy.content.menu.GiantFryingPanMenu;
import com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public final class AntarchyObjects {
    private static final Supplier<?> UNBOUND = () -> {
        throw new IllegalStateException("Antarchy object supplier was accessed before registration finished");
    };

    public static final Supplier<EntityType<EasterBunnyEntity>> EASTER_BUNNY = lookup(Registries.ENTITY_TYPE, "easter_bunny");
    public static final Supplier<EntityType<FlyingSquirrelEntity>> FLYING_SQUIRREL = lookup(Registries.ENTITY_TYPE, "flying_squirrel");
    public static final Supplier<EntityType<KrakenEntity>> KRAKEN = lookup(Registries.ENTITY_TYPE, "kraken");
    public static final Supplier<EntityType<MissileSquidEntity>> MISSILE_SQUID = lookup(Registries.ENTITY_TYPE, "missile_squid");
    public static final Supplier<EntityType<TentacleEntity>> TENTACLE = lookup(Registries.ENTITY_TYPE, "tentacle");
    public static final Supplier<EntityType<KrakensGraspThrownTrident>> KRAKENS_GRASP_TRIDENT = lookup(Registries.ENTITY_TYPE, "krakens_grasp_trident");
    public static final Supplier<EntityType<OctopusBombEntity>> OCTOPUS_BOMB = lookup(Registries.ENTITY_TYPE, "octopus_bomb");
    public static final Supplier<EntityType<NightmareEntity>> NIGHTMARE = lookup(Registries.ENTITY_TYPE, "nightmare");
    public static final Supplier<EntityType<NightmarePortalEntity>> NIGHTMARE_PORTAL = lookup(Registries.ENTITY_TYPE, "nightmare_portal");
    public static final Supplier<EntityType<DimensionalTearEntity>> DIMENSIONAL_TEAR = lookup(Registries.ENTITY_TYPE, "dimensional_tear");
    public static final Supplier<Block> DIMENSIONAL_TEAR_MARKER = lookup(Registries.BLOCK, "dimensional_tear_marker");
    public static final Supplier<Block> REMEMBRANCE_TORCH = lookup(Registries.BLOCK, "remembrance_torch");
    public static final Supplier<BlockEntityType<RemembranceTorchBlockEntity>> REMEMBRANCE_TORCH_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "remembrance_torch");
    public static final Supplier<BlockEntityType<DimensionalTearMarkerBlockEntity>> DIMENSIONAL_TEAR_MARKER_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "dimensional_tear_marker");
    public static final Supplier<EntityType<NightmareBiteEntity>> NIGHTMARE_BITE = lookup(Registries.ENTITY_TYPE, "nightmare_bite");
    public static final Supplier<EntityType<LucidEntity>> LUCID = lookup(Registries.ENTITY_TYPE, "lucid");
    public static final Supplier<EntityType<VortexEntity>> VORTEX = lookup(Registries.ENTITY_TYPE, "vortex");
    public static final Supplier<EntityType<WindVortexEntity>> WIND_VORTEX = lookup(Registries.ENTITY_TYPE, "wind_vortex");
    public static final Supplier<EntityType<VortexChargeProjectileEntity>> VORTEX_CHARGE_PROJECTILE = lookup(Registries.ENTITY_TYPE, "vortex_charge_projectile");
    public static final Supplier<EntityType<RoyalBlackHoleEntity>> ROYAL_BLACK_HOLE = lookup(Registries.ENTITY_TYPE, "royal_black_hole");
    public static final Supplier<EntityType<RoyalAssailantBlackHoleEntity>> ROYAL_ASSAILANT_BLACK_HOLE = lookup(Registries.ENTITY_TYPE, "royal_assailant_black_hole");
    public static final Supplier<Block> VORTEX_LENS = lookup(Registries.BLOCK, "vortex_lens");
    public static final Supplier<BlockEntityType<VortexLensBlockEntity>> VORTEX_LENS_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "vortex_lens");
    public static final Supplier<EntityType<MolewormEntity>> MOLEWORM = lookup(Registries.ENTITY_TYPE, "moleworm");
    public static final Supplier<EntityType<MantisEntity>> MANTIS = lookup(Registries.ENTITY_TYPE, "mantis");
    public static final Supplier<EntityType<BedBugEntity>> BED_BUG = lookup(Registries.ENTITY_TYPE, "bed_bug");
    public static final Supplier<EntityType<WaspEntity>> WASP = lookup(Registries.ENTITY_TYPE, "wasp");
    public static final Supplier<EntityType<BomberEntity>> BOMBER = lookup(Registries.ENTITY_TYPE, "bomber");
    public static final Supplier<EntityType<ScorpionEntity>> SCORPION = lookup(Registries.ENTITY_TYPE, "scorpion");
    public static final Supplier<EntityType<CaterpillarEntity>> CATERPILLAR = lookup(Registries.ENTITY_TYPE, "caterpillar");
    public static final Supplier<EntityType<ButterflyEntity>> BUTTERFLY = lookup(Registries.ENTITY_TYPE, "butterfly");
    public static final Supplier<EntityType<ReverieEntity>> REVERIE = lookup(Registries.ENTITY_TYPE, "reverie");
    public static final Supplier<EntityType<FlytrapEntity>> FLYTRAP = lookup(Registries.ENTITY_TYPE, "flytrap");
    public static final Supplier<EntityType<BrutalflyEntity>> BRUTALFLY = lookup(Registries.ENTITY_TYPE, "brutalfly");
    public static final Supplier<EntityType<BrutalflyOrbEntity>> BRUTALFLY_ORB = lookup(Registries.ENTITY_TYPE, "brutalfly_orb");
    public static final Supplier<EntityType<HushProjectileEntity>> HUSH_PROJECTILE = lookup(Registries.ENTITY_TYPE, "hush_projectile");
    public static final Supplier<EntityType<ToreterrorEntity>> TORETERROR = lookup(Registries.ENTITY_TYPE, "toreterror");
    public static final Supplier<EntityType<WaterBombEntity>> WATER_BOMB = lookup(Registries.ENTITY_TYPE, "water_bomb");
    public static final Supplier<EntityType<CrawlingBlightEntity>> CRAWLING_BLIGHT = lookup(Registries.ENTITY_TYPE, "crawling_blight");
    public static final Supplier<EntityType<SkulkingFrightEntity>> SKULKING_FRIGHT = lookup(Registries.ENTITY_TYPE, "skulking_fright");
    public static final Supplier<EntityType<ManticoreEntity>> MANTICORE = lookup(Registries.ENTITY_TYPE, "manticore");
    public static final Supplier<EntityType<StinkBugEntity>> STINK_BUG = lookup(Registries.ENTITY_TYPE, "stink_bug");
    public static final Supplier<EntityType<RollyPollyEntity>> ROLLY_POLLY = lookup(Registries.ENTITY_TYPE, "rolly_polly");
    public static final Supplier<EntityType<CheepEntity>> CHEEP = lookup(Registries.ENTITY_TYPE, "cheep");
    public static final Supplier<EntityType<DorrieEntity>> DORRIE = lookup(Registries.ENTITY_TYPE, "dorrie");
    public static final Supplier<EntityType<HerculesBeetleEntity>> HERCULES_BEETLE = lookup(Registries.ENTITY_TYPE, "hercules_beetle");
    public static final Supplier<EntityType<PrinceEntity>> PRINCE = lookup(Registries.ENTITY_TYPE, "prince");
    public static final Supplier<EntityType<PrincessEntity>> PRINCESS = lookup(Registries.ENTITY_TYPE, "princess");
    public static final Supplier<EntityType<RoyalBoltEntity>> ROYAL_BOLT = lookup(Registries.ENTITY_TYPE, "royal_bolt");
    public static final Supplier<EntityType<RoyalElementalProjectileEntity>> ROYAL_ELEMENTAL_PROJECTILE = lookup(Registries.ENTITY_TYPE, "royal_elemental_projectile");
    public static final Supplier<EntityType<RoyalIceSpikeEntity>> ROYAL_ICE_SPIKE = lookup(Registries.ENTITY_TYPE, "royal_ice_spike");
    public static final Supplier<EntityType<KingEntity>> KING = lookup(Registries.ENTITY_TYPE, "king");
    public static final Supplier<EntityType<QueenEntity>> QUEEN = lookup(Registries.ENTITY_TYPE, "queen");
    public static final Supplier<Block> PRINCE_EGG = lookup(Registries.BLOCK, "prince_egg");
    public static final Supplier<Block> PRINCESS_EGG = lookup(Registries.BLOCK, "princess_egg");
    public static final Supplier<BlockEntityType<RoyalEggBlockEntity>> ROYAL_EGG_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "royal_egg");
    public static final Supplier<EntityType<OuranwoodDeerEntity>> OURANWOOD_DEER = lookup(Registries.ENTITY_TYPE, "ouranwood_deer");
    public static final Supplier<EntityType<GlimmerEntity>> GLIMMER = lookup(Registries.ENTITY_TYPE, "glimmer");
    public static final Supplier<EntityType<StratosharkEntity>> STRATOSHARK = lookup(Registries.ENTITY_TYPE, "stratoshark");
    public static final Supplier<Item> SPIRIT_APPLE = lookup(Registries.ITEM, "spirit_apple");
    public static final Supplier<EntityType<ElkaEntity>> ELKA = lookup(Registries.ENTITY_TYPE, "elka");
    public static final Supplier<Item> PEACH = lookup(Registries.ITEM, "peach");
    public static final Supplier<Item> CORN = lookup(Registries.ITEM, "corn");
    public static final Supplier<Item> CORN_SEEDS = lookup(Registries.ITEM, "corn_seeds");
    public static final Supplier<Block> WILD_CORN = lookup(Registries.BLOCK, "wild_corn");
    public static final Supplier<Block> STAR_CORAL_BLOCK = lookup(Registries.BLOCK, "star_coral_block");
    public static final Supplier<Block> STAR_CORAL = lookup(Registries.BLOCK, "star_coral");
    public static final Supplier<Block> STAR_CORAL_FAN = lookup(Registries.BLOCK, "star_coral_fan");
    public static final Supplier<Item> COOKED_CORNDOG = lookup(Registries.ITEM, "cooked_corndog");
    public static final Supplier<Item> GLIMMER_BOTTLE = lookup(Registries.ITEM, "glimmer_bottle");
    public static final Supplier<MenuType<DorrieInventoryMenu>> DORRIE_INVENTORY_MENU = lookup(Registries.MENU, "dorrie_inventory");
    public static final Supplier<MenuType<RoyalJudgmentMenu>> ROYAL_JUDGMENT_MENU = lookup(Registries.MENU, "royal_judgment");
    public static final Supplier<MenuType<GiantFryingPanMenu>> GIANT_FRYING_PAN_MENU = lookup(Registries.MENU, "giant_frying_pan");
    public static final Supplier<Block> DUPLICATOR_LOG = lookup(Registries.BLOCK, "duplicator_log");
    public static final Supplier<Block> DUPLICATOR_SAPLING = lookup(Registries.BLOCK, "duplicator_sapling");
    public static final Supplier<Block> DUCT_TAPE = lookup(Registries.BLOCK, "duct_tape");
    public static final Supplier<Block> INFESTED_ROOTED_DIRT = lookup(Registries.BLOCK, "infested_rooted_dirt");
    public static final Supplier<Block> INFESTED_COARSE_DIRT = lookup(Registries.BLOCK, "infested_coarse_dirt");
    public static final Supplier<Block> NYXITE = lookup(Registries.BLOCK, "nyxite");
    public static final Supplier<Block> SHELLSTONE = lookup(Registries.BLOCK, "shellstone");
    public static final Supplier<Block> POLISHED_SHELLSTONE = lookup(Registries.BLOCK, "polished_shellstone");
    public static final Supplier<Block> SHELLSTONE_BRICKS = lookup(Registries.BLOCK, "shellstone_bricks");
    public static final Supplier<Block> CHISELED_SHELLSTONE = lookup(Registries.BLOCK, "chiseled_shellstone");
    public static final Supplier<Block> MOSSY_SHELLSTONE_BRICKS = lookup(Registries.BLOCK, "mossy_shellstone_bricks");
    public static final Supplier<Block> CRACKED_SHELLSTONE_BRICKS = lookup(Registries.BLOCK, "cracked_shellstone_bricks");
    public static final Supplier<Block> MOSSY_SHELLSTONE_BRICK_STAIRS = lookup(Registries.BLOCK, "mossy_shellstone_brick_stairs");
    public static final Supplier<Block> MOSSY_SHELLSTONE_BRICK_SLAB = lookup(Registries.BLOCK, "mossy_shellstone_brick_slab");
    public static final Supplier<Block> MOSSY_SHELLSTONE_BRICK_WALL = lookup(Registries.BLOCK, "mossy_shellstone_brick_wall");
    public static final Supplier<Block> SHELLSTONE_STAIRS = lookup(Registries.BLOCK, "shellstone_stairs");
    public static final Supplier<Block> SHELLSTONE_SLAB = lookup(Registries.BLOCK, "shellstone_slab");
    public static final Supplier<Block> SHELLSTONE_WALL = lookup(Registries.BLOCK, "shellstone_wall");
    public static final Supplier<Block> POLISHED_SHELLSTONE_STAIRS = lookup(Registries.BLOCK, "polished_shellstone_stairs");
    public static final Supplier<Block> POLISHED_SHELLSTONE_SLAB = lookup(Registries.BLOCK, "polished_shellstone_slab");
    public static final Supplier<Block> POLISHED_SHELLSTONE_WALL = lookup(Registries.BLOCK, "polished_shellstone_wall");
    public static final Supplier<Block> SHELLSTONE_BRICK_STAIRS = lookup(Registries.BLOCK, "shellstone_brick_stairs");
    public static final Supplier<Block> SHELLSTONE_BRICK_SLAB = lookup(Registries.BLOCK, "shellstone_brick_slab");
    public static final Supplier<Block> SHELLSTONE_BRICK_WALL = lookup(Registries.BLOCK, "shellstone_brick_wall");
    public static final Supplier<Block> CLOUD_BLOCK = lookup(Registries.BLOCK, "cloud_block");
    public static final Supplier<Block> FLYTRAP_GOO_BLOCK = lookup(Registries.BLOCK, "flytrap_goo_block");
    public static final Supplier<Block> PALE_NYXITE = lookup(Registries.BLOCK, "pale_nyxite");
    public static final Supplier<Block> NYXITE_SPIKE = lookup(Registries.BLOCK, "nyxite_spike");
    public static final Supplier<Block> CHITIN_BLOCK = lookup(Registries.BLOCK, "chitin_block");
    public static final Supplier<Block> CHITIN_SPIKE = lookup(Registries.BLOCK, "chitin_spike");
    public static final Supplier<Block> POTENT_NYXITE = lookup(Registries.BLOCK, "potent_nyxite");
    public static final Supplier<Block> ANTIMETAL = lookup(Registries.BLOCK, "antimetal");
    public static final Supplier<Block> POLISHED_ANTIMETAL = lookup(Registries.BLOCK, "polished_antimetal");
    public static final Supplier<Block> BUDDING_BLOOD_CRYSTAL = lookup(Registries.BLOCK, "budding_blood_crystal");
    public static final Supplier<Block> SMALL_BLOOD_CRYSTAL_BUD = lookup(Registries.BLOCK, "small_blood_crystal_bud");
    public static final Supplier<Block> MEDIUM_BLOOD_CRYSTAL_BUD = lookup(Registries.BLOCK, "medium_blood_crystal_bud");
    public static final Supplier<Block> LARGE_BLOOD_CRYSTAL_BUD = lookup(Registries.BLOCK, "large_blood_crystal_bud");
    public static final Supplier<Block> BLOOD_CRYSTAL_CRYSTAL = lookup(Registries.BLOCK, "blood_crystal_cluster");
    public static final Supplier<Item> OURANWOOD_ACORN = lookup(Registries.ITEM, "ouranwood_acorn");
    public static final Supplier<Item> KRAKEN_TOOTH = lookup(Registries.ITEM, "kraken_tooth");
    public static final Supplier<Item> KRAKEN_TENTACLE = lookup(Registries.ITEM, "kraken_tentacle");
    public static final Supplier<Item> KRAKENS_GRASP = lookup(Registries.ITEM, "krakens_grasp");
    public static final Supplier<Item> MOGGLES = lookup(Registries.ITEM, "moggles");
    public static final Supplier<Item> REVERIE_BOTTLE = lookup(Registries.ITEM, "reverie_bottle");
    public static final Supplier<Item> CRITTER_CAGE = lookup(Registries.ITEM, "critter_cage");
    public static final Supplier<Item> STINK_BUG_ITEM = lookup(Registries.ITEM, "stink_bug");
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> DREAD = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<Attribute>> DOUBLE_DAMAGE_CHANCE = (Supplier<Holder<Attribute>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<Attribute>> BLOODGLASS_MAX_HEARTS = (Supplier<Holder<Attribute>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> BLOODGLASS_WARD = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> PARALYZED_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> INVERTED_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> STINKY_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> SAUCED_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> GLIMMERING_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> REMEMBRANCE_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> ROYAL_VITALITY_PENALTY_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> BLEEDING_EFFECT = (Supplier<MobEffect>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<MobEffect> BLOOD_MOON_EFFECT = (Supplier<MobEffect>) UNBOUND;
    public static final Supplier<Fluid> LUMEN = lookup(Registries.FLUID, "lumen");
    public static final Supplier<Fluid> FLOWING_LUMEN = lookup(Registries.FLUID, "flowing_lumen");
    public static final Supplier<Item> LUMEN_BUCKET = lookup(Registries.ITEM, "lumen_bucket");
    public static final Supplier<Block> LUMEN_BLOCK = lookup(Registries.BLOCK, "lumen");
    public static final Supplier<Block> LUMEN_FROGLIGHT = lookup(Registries.BLOCK, "lumen_froglight");
    public static final Supplier<Block> GLIMMERING_REED = lookup(Registries.BLOCK, "glimmering_reed");
    public static final Supplier<Block> OURANWOOD_ACORN_BLOCK = lookup(Registries.BLOCK, "ouranwood_acorn");
    public static final Supplier<Block> MOSSY_OURANWOOD_LOG = lookup(Registries.BLOCK, "mossy_ouranwood_log");
    public static final Supplier<Block> MOSSY_OURANWOOD_WOOD = lookup(Registries.BLOCK, "mossy_ouranwood_wood");
    public static final Supplier<Block> ORANGE_MILKWEED = lookup(Registries.BLOCK, "orange_milkweed");
    public static final Supplier<Block> PINK_MILKWEED = lookup(Registries.BLOCK, "pink_milkweed");
    public static final Supplier<Block> BED_BUG_EGG = lookup(Registries.BLOCK, "bed_bug_egg");
    public static final Supplier<Block> CRAWLING_BLIGHT_EGGS = lookup(Registries.BLOCK, "crawling_blight_egg");
    public static final Supplier<Block> SKULKING_FRIGHT_EGGS = lookup(Registries.BLOCK, "skulking_fright_egg");
    public static final Supplier<Block> WASP_NEST = lookup(Registries.BLOCK, "wasp_nest");
    public static final Supplier<Block> UMBRAL_MOSS_BLOCK = lookup(Registries.BLOCK, "umbral_moss_block");
    public static final Supplier<Block> UMBRAL_MOSS_CARPET = lookup(Registries.BLOCK, "umbral_moss_carpet");
    public static final Supplier<Block> BLUSH_MOSS_BLOCK = lookup(Registries.BLOCK, "blush_moss_block");
    public static final Supplier<Block> BLUSH_MOSS_CARPET = lookup(Registries.BLOCK, "blush_moss_carpet");
    public static final Supplier<Block> HUSHWEED = lookup(Registries.BLOCK, "hushweed");
    public static final Supplier<Block> SQUIRREL_NEST_BLOCK = lookup(Registries.BLOCK, "ouranwood_squirrel_nest");
    public static final Supplier<Block> BRUTALFLY_COCOON_SPAWNER = lookup(Registries.BLOCK, "brutalfly_cocoon_spawner");
    public static final Supplier<Block> GIANT_LILY_PAD = lookup(Registries.BLOCK, "giant_lily_pad");
    public static final Supplier<Block> LOTUS = lookup(Registries.BLOCK, "lotus");
    public static final Supplier<Block> SEASHELL = lookup(Registries.BLOCK, "seashell");
    public static final Supplier<Block> CRITTER_CAGE_BLOCK = lookup(Registries.BLOCK, "critter_cage_block");
    public static final Supplier<Block> LUCID_ANCHOR = lookup(Registries.BLOCK, "lucid_anchor");
    public static final Supplier<BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "ant_nest");
    public static final Supplier<BlockEntityType<DreamCampfireBlockEntity>> DREAM_CAMPFIRE_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "dream_campfire");
    public static final Supplier<BlockEntityType<WaspNestBlockEntity>> WASP_NEST_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "wasp_nest");
    public static final Supplier<BlockEntityType<HushweedBlockEntity>> HUSHWEED_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "hushweed");
    public static final Supplier<BlockEntityType<SeashellBlockEntity>> SEASHELL_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "seashell");
    public static final Supplier<BlockEntityType<CritterCageBlockEntity>> CRITTER_CAGE_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "critter_cage_block");
    public static final Supplier<BlockEntityType<LucidAnchorBlockEntity>> LUCID_ANCHOR_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "lucid_anchor");
    public static final Supplier<BlockEntityType<FossilBlockEntity>> FOSSIL_BLOCK_ENTITY = lookup(Registries.BLOCK_ENTITY_TYPE, "fossil");
    public static final Supplier<SimpleParticleType> STINKY_GAS = lookup(Registries.PARTICLE_TYPE, "stinky_gas");
    public static final Supplier<SimpleParticleType> BUG_SPRAY_PARTICLE = lookup(Registries.PARTICLE_TYPE, "bug_spray");
    public static final Supplier<SimpleParticleType> NIGHTMARE_FIRE_FLAME = lookup(Registries.PARTICLE_TYPE, "nightmare_fire_flame");
    public static final Supplier<SimpleParticleType> STINKY_FLY = lookup(Registries.PARTICLE_TYPE, "stinky_fly");
    public static final Supplier<SimpleParticleType> PEACH_LEAVES_PARTICLE = lookup(Registries.PARTICLE_TYPE, "peach_leaves_particle");
    public static final Supplier<SimpleParticleType> LOTUS_POLLEN = lookup(Registries.PARTICLE_TYPE, "lotus_pollen");
    public static final Supplier<SimpleParticleType> TEMPORAL_GLINT_SLOW = lookup(Registries.PARTICLE_TYPE, "temporal_glint_slow");
    public static final Supplier<SimpleParticleType> TEMPORAL_GLINT_FAST = lookup(Registries.PARTICLE_TYPE, "temporal_glint_fast");
    public static final Supplier<SimpleParticleType> LUCID_BOLT_IMPACT_SMALL = lookup(Registries.PARTICLE_TYPE, "lucid_bolt_impact_small");
    public static final Supplier<SimpleParticleType> LUCID_BOLT_IMPACT_LARGE = lookup(Registries.PARTICLE_TYPE, "lucid_bolt_impact_large");
    public static final Supplier<EntityType<CritterCageProjectileEntity>> CRITTER_CAGE_PROJECTILE = lookup(Registries.ENTITY_TYPE, "critter_cage_projectile");
    public static final Supplier<EntityType<LaunchedAnvilEntity>> LAUNCHED_ANVIL = lookup(Registries.ENTITY_TYPE, "launched_anvil");
    public static final Supplier<EntityType<WormHookProjectileEntity>> WORM_HOOK_PROJECTILE = lookup(Registries.ENTITY_TYPE, "worm_hook_projectile");
    public static final Supplier<Item> WORM_HOOK = lookup(Registries.ITEM, "worm_hook");

    private AntarchyObjects() {
    }

    private static <T> Supplier<T> lookup(ResourceKey<? extends Registry<? super T>> registryKey, String path) {
        return new Supplier<>() {
            private T value;

            @Override
            @SuppressWarnings("unchecked")
            public T get() {
                if (this.value == null) {
                    Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryKey.location());
                    Object found = registry == null
                            ? null
                            : registry.getOptional(new ResourceLocation(Antarchy.MODID, path)).orElse(null);
                    if (found == null) {
                        throw new IllegalStateException("Antarchy object supplier was accessed before registration finished");
                    }
                    this.value = (T) found;
                }
                return this.value;
            }
        };
    }

    public static void setDread(Supplier<MobEffect> supplier) {
        DREAD = supplier;
    }

    public static void setDoubleDamageChance(Supplier<Holder<Attribute>> supplier) {
        DOUBLE_DAMAGE_CHANCE = supplier;
    }

    public static void setBloodglassMaxHearts(Supplier<Holder<Attribute>> supplier) {
        BLOODGLASS_MAX_HEARTS = supplier;
    }

    public static void setBloodglassWard(Supplier<MobEffect> supplier) {
        BLOODGLASS_WARD = supplier;
    }

    public static void setParalyzedEffect(Supplier<MobEffect> supplier) {
        PARALYZED_EFFECT = supplier;
    }

    public static void setInvertedEffect(Supplier<MobEffect> supplier) {
        INVERTED_EFFECT = supplier;
    }

    public static void setStinkyEffect(Supplier<MobEffect> supplier) {
        STINKY_EFFECT = supplier;
    }

    public static void setSaucedEffect(Supplier<MobEffect> supplier) {
        SAUCED_EFFECT = supplier;
    }

    public static void setGlimmeringEffect(Supplier<MobEffect> supplier) {
        GLIMMERING_EFFECT = supplier;
    }

    public static void setRemembranceEffect(Supplier<MobEffect> supplier) {
        REMEMBRANCE_EFFECT = supplier;
    }

    public static void setRoyalVitalityPenaltyEffect(Supplier<MobEffect> supplier) {
        ROYAL_VITALITY_PENALTY_EFFECT = supplier;
    }

    public static void setBleedingEffect(Supplier<MobEffect> supplier) {
        BLEEDING_EFFECT = supplier;
    }

    public static void setBloodMoonEffect(Supplier<MobEffect> supplier) {
        BLOOD_MOON_EFFECT = supplier;
    }

    @SuppressWarnings("unchecked")
    public static AntarchyItemComponent<GlimmerVariant> GLIMMER_VARIANT_COMPONENT =
            AntarchyItemComponent.string("antarchy_glimmer_variant", value -> {
                try { return GlimmerVariant.valueOf(value); }
                catch (IllegalArgumentException ignored) { return null; }
            }, value -> value.name());
    public static Supplier<Attribute> ENTITY_REACH = () -> null;
    @SuppressWarnings("unchecked")
    public static AntarchyItemComponent<Unit> AMERICAN_COMPONENT = AntarchyItemComponent.marker("antarchy_american");
    @SuppressWarnings("unchecked")
    public static AntarchyItemComponent<ResourceLocation> CRITTER_CAGE_ENTITY_TYPE_COMPONENT =
            AntarchyItemComponent.string("antarchy_cage_entity_type", ResourceLocation::new, ResourceLocation::toString);
    @SuppressWarnings("unchecked")
    public static AntarchyItemComponent<Integer> CRITTER_CAGE_PRIMARY_COLOR_COMPONENT = AntarchyItemComponent.integer("antarchy_cage_primary_color");
    @SuppressWarnings("unchecked")
    public static AntarchyItemComponent<Integer> CRITTER_CAGE_SECONDARY_COLOR_COMPONENT = AntarchyItemComponent.integer("antarchy_cage_secondary_color");

    public static boolean isDuplicatorTreeBlock(BlockState state) {
        return state.is(DUPLICATOR_LOG.get()) || state.is(DUPLICATOR_SAPLING.get());
    }

    public static BlockBehaviour.Properties shellstoneProperties() {
        return BlockBehaviour.Properties.copy(Blocks.STONE).requiresCorrectToolForDrops();
    }
}
