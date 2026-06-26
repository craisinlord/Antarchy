package com.craisinlord.antarchy.content;

import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import com.craisinlord.antarchy.content.entity.LurkingTerrorEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AntarchyObjects {
    private static final Supplier<?> UNBOUND = () -> {
        throw new IllegalStateException("Antarchy object supplier was accessed before registration finished");
    };

    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<EasterBunnyEntity>> EASTER_BUNNY = (Supplier<EntityType<EasterBunnyEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<FlyingSquirrelEntity>> FLYING_SQUIRREL = (Supplier<EntityType<FlyingSquirrelEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<KrakenEntity>> KRAKEN = (Supplier<EntityType<KrakenEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<MissileSquidEntity>> MISSILE_SQUID = (Supplier<EntityType<MissileSquidEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<OctopusBombEntity>> OCTOPUS_BOMB = (Supplier<EntityType<OctopusBombEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<MolewormEntity>> MOLEWORM = (Supplier<EntityType<MolewormEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<MantisEntity>> MANTIS = (Supplier<EntityType<MantisEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<BedBugEntity>> BED_BUG = (Supplier<EntityType<BedBugEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<WaspEntity>> WASP = (Supplier<EntityType<WaspEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<BomberEntity>> BOMBER = (Supplier<EntityType<BomberEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<ScorpionEntity>> SCORPION = (Supplier<EntityType<ScorpionEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<CaterpillarEntity>> CATERPILLAR = (Supplier<EntityType<CaterpillarEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<ButterflyEntity>> BUTTERFLY = (Supplier<EntityType<ButterflyEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<ReverieEntity>> REVERIE = (Supplier<EntityType<ReverieEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<TriffidEntity>> TRIFFID = (Supplier<EntityType<TriffidEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<BrutalflyEntity>> BRUTALFLY = (Supplier<EntityType<BrutalflyEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<BrutalflyOrbEntity>> BRUTALFLY_ORB = (Supplier<EntityType<BrutalflyOrbEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<HushProjectileEntity>> HUSH_PROJECTILE = (Supplier<EntityType<HushProjectileEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<ToreterrorEntity>> TORETERROR = (Supplier<EntityType<ToreterrorEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<WaterBombEntity>> WATER_BOMB = (Supplier<EntityType<WaterBombEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<CreepingHorrorEntity>> CREEPING_HORROR = (Supplier<EntityType<CreepingHorrorEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<LurkingTerrorEntity>> LURKING_TERROR = (Supplier<EntityType<LurkingTerrorEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<StinkBugEntity>> STINK_BUG = (Supplier<EntityType<StinkBugEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> DUPLICATOR_LOG = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> DUPLICATOR_SAPLING = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> DUCT_TAPE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> INFESTED_ROOTED_DIRT = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> INFESTED_COARSE_DIRT = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> NYXITE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> POLISHED_SHELLSTONE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_BRICKS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> CHISELED_SHELLSTONE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MOSSY_SHELLSTONE_BRICKS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> CRACKED_SHELLSTONE_BRICKS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MOSSY_SHELLSTONE_BRICK_STAIRS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MOSSY_SHELLSTONE_BRICK_SLAB = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MOSSY_SHELLSTONE_BRICK_WALL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_STAIRS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_SLAB = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_WALL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> POLISHED_SHELLSTONE_STAIRS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> POLISHED_SHELLSTONE_SLAB = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> POLISHED_SHELLSTONE_WALL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_BRICK_STAIRS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_BRICK_SLAB = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SHELLSTONE_BRICK_WALL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> CLOUD_BLOCK = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> TRIFFID_GOO_BLOCK = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> PALE_NYXITE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> NYXITE_SPIKE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> POTENT_NYXITE = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> ANTIMETAL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> POLISHED_ANTIMETAL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> BUDDING_BLOOD_CRYSTAL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SMALL_BLOOD_CRYSTAL_BUD = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MEDIUM_BLOOD_CRYSTAL_BUD = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> LARGE_BLOOD_CRYSTAL_BUD = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> BLOOD_CRYSTAL_CRYSTAL = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Item> OURANWOOD_ACORN = (Supplier<Item>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Item> KRAKEN_TOOTH = (Supplier<Item>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Item> MOGGLES = (Supplier<Item>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Item> REVERIE_BOTTLE = (Supplier<Item>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Item> STINK_BUG_ITEM = (Supplier<Item>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<MobEffect>> DREAD = (Supplier<Holder<MobEffect>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<Attribute>> DOUBLE_DAMAGE_CHANCE = (Supplier<Holder<Attribute>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<Attribute>> BLOODGLASS_MAX_HEARTS = (Supplier<Holder<Attribute>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<MobEffect>> BLOODGLASS_WARD = (Supplier<Holder<MobEffect>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<MobEffect>> PARALYZED_EFFECT = (Supplier<Holder<MobEffect>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<MobEffect>> INVERTED_EFFECT = (Supplier<Holder<MobEffect>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Holder<MobEffect>> STINKY_EFFECT = (Supplier<Holder<MobEffect>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> OURANWOOD_ACORN_BLOCK = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MOSSY_OURANWOOD_LOG = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> MOSSY_OURANWOOD_WOOD = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> ORANGE_MILKWEED = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> PINK_MILKWEED = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> TORCHFLOWER_BUSH = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> BED_BUG_EGG = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> CREEPING_HORROR_EGGS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> LURKING_TERROR_EGGS = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> WASP_NEST = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> HUSHWEED = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<Block> SQUIRREL_NEST_BLOCK = (Supplier<Block>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = (Supplier<BlockEntityType<AntNestBlockEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<BlockEntityType<DreamCampfireBlockEntity>> DREAM_CAMPFIRE_BLOCK_ENTITY = (Supplier<BlockEntityType<DreamCampfireBlockEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<BlockEntityType<WaspNestBlockEntity>> WASP_NEST_BLOCK_ENTITY = (Supplier<BlockEntityType<WaspNestBlockEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<BlockEntityType<HushweedBlockEntity>> HUSHWEED_BLOCK_ENTITY = (Supplier<BlockEntityType<HushweedBlockEntity>>) UNBOUND;
    @SuppressWarnings("unchecked")
    public static Supplier<SimpleParticleType> STINKY_GAS = (Supplier<SimpleParticleType>) UNBOUND;

    private AntarchyObjects() {
    }

    public static void bind(
            Supplier<EntityType<EasterBunnyEntity>> easterBunny,
            Supplier<EntityType<FlyingSquirrelEntity>> flyingSquirrel,
            Supplier<EntityType<KrakenEntity>> kraken,
            Supplier<EntityType<MissileSquidEntity>> missileSquid,
            Supplier<EntityType<MolewormEntity>> moleworm,
            Supplier<EntityType<MantisEntity>> mantis,
            Supplier<EntityType<BedBugEntity>> bedBug,
            Supplier<EntityType<WaspEntity>> wasp,
            Supplier<EntityType<BomberEntity>> bomber,
            Supplier<EntityType<ScorpionEntity>> scorpion,
            Supplier<EntityType<CaterpillarEntity>> caterpillar,
            Supplier<EntityType<ButterflyEntity>> butterfly,
            Supplier<EntityType<ReverieEntity>> reverie,
            Supplier<EntityType<TriffidEntity>> triffid,
            Supplier<EntityType<BrutalflyEntity>> brutalfly,
            Supplier<EntityType<BrutalflyOrbEntity>> brutalflyOrb,
            Supplier<EntityType<HushProjectileEntity>> hushProjectile,
            Supplier<EntityType<ToreterrorEntity>> toreterror,
            Supplier<EntityType<WaterBombEntity>> waterBomb,
            Supplier<EntityType<CreepingHorrorEntity>> creepingHorror,
            Supplier<EntityType<LurkingTerrorEntity>> lurkingTerror,
            Supplier<EntityType<StinkBugEntity>> stinkBug,
            Supplier<Block> duplicatorLog,
            Supplier<Block> duplicatorSapling,
            Supplier<Block> ductTape,
            Supplier<Block> infestedRootedDirt,
            Supplier<Block> infestedCoarseDirt,
            Supplier<Block> nyxite,
            Supplier<Block> shellstone,
            Supplier<Block> polishedShellstone,
            Supplier<Block> shellstoneBricks,
            Supplier<Block> chiseledShellstone,
            Supplier<Block> mossyShellstoneBricks,
            Supplier<Block> crackedShellstoneBricks,
            Supplier<Block> mossyShellstoneBrickStairs,
            Supplier<Block> mossyShellstoneBrickSlab,
            Supplier<Block> mossyShellstoneBrickWall,
            Supplier<Block> shellstoneStairs,
            Supplier<Block> shellstoneSlab,
            Supplier<Block> shellstoneWall,
            Supplier<Block> polishedShellstoneStairs,
            Supplier<Block> polishedShellstoneSlab,
            Supplier<Block> polishedShellstoneWall,
            Supplier<Block> shellstoneBrickStairs,
            Supplier<Block> shellstoneBrickSlab,
            Supplier<Block> shellstoneBrickWall,
            Supplier<Block> cloudBlock,
            Supplier<Block> triffidGooBlock,
            Supplier<Block> paleNyxite,
            Supplier<Block> nyxiteSpike,
            Supplier<Block> potentNyxite,
            Supplier<Block> antimetal,
            Supplier<Block> polishedAntimetal,
            Supplier<Block> buddingBloodCrystal,
            Supplier<Block> smallBloodCrystalBud,
            Supplier<Block> mediumBloodCrystalBud,
            Supplier<Block> largeBloodCrystalBud,
            Supplier<Block> bloodCrystalCrystal,
            Supplier<Item> ouranwoodAcorn,
            Supplier<Item> krakenTooth,
            Supplier<Item> moggles,
            Supplier<Item> reverieBottle,
            Supplier<Item> stinkBugItem,
            Supplier<Holder<MobEffect>> dread,
            Supplier<Holder<MobEffect>> paralyzedEffect,
            Supplier<Holder<MobEffect>> invertedEffect,
            Supplier<Holder<MobEffect>> stinkyEffect,
            Supplier<Block> ouranwoodAcornBlock,
            Supplier<Block> mossyOuranwoodLog,
            Supplier<Block> mossyOuranwoodWood,
            Supplier<Block> orangeMilkweed,
            Supplier<Block> pinkMilkweed,
            Supplier<Block> torchflowerBush,
            Supplier<Block> bedBugEggs,
            Supplier<Block> creepingHorrorEggs,
            Supplier<Block> lurkingTerrorEggs,
            Supplier<Block> waspNest,
            Supplier<Block> hushweed,
            Supplier<Block> squirrelNestBlock,
            Supplier<BlockEntityType<AntNestBlockEntity>> antNestBlockEntity,
            Supplier<BlockEntityType<DreamCampfireBlockEntity>> dreamCampfireBlockEntity,
            Supplier<BlockEntityType<WaspNestBlockEntity>> waspNestBlockEntity,
            Supplier<BlockEntityType<HushweedBlockEntity>> hushweedBlockEntity,
            Supplier<SimpleParticleType> stinkyGas,
            Supplier<Holder<Attribute>> doubleDamageChance,
            Supplier<Holder<Attribute>> bloodglassMaxHearts,
            Supplier<Holder<MobEffect>> bloodglassWard
    ) {
        EASTER_BUNNY = easterBunny;
        FLYING_SQUIRREL = flyingSquirrel;
        KRAKEN = kraken;
        MISSILE_SQUID = missileSquid;
        MOLEWORM = moleworm;
        MANTIS = mantis;
        BED_BUG = bedBug;
        WASP = wasp;
        BOMBER = bomber;
        SCORPION = scorpion;
        CATERPILLAR = caterpillar;
        BUTTERFLY = butterfly;
        REVERIE = reverie;
        TRIFFID = triffid;
        BRUTALFLY = brutalfly;
        BRUTALFLY_ORB = brutalflyOrb;
        HUSH_PROJECTILE = hushProjectile;
        TORETERROR = toreterror;
        WATER_BOMB = waterBomb;
        CREEPING_HORROR = creepingHorror;
        LURKING_TERROR = lurkingTerror;
        STINK_BUG = stinkBug;
        DUPLICATOR_LOG = duplicatorLog;
        DUPLICATOR_SAPLING = duplicatorSapling;
        DUCT_TAPE = ductTape;
        INFESTED_ROOTED_DIRT = infestedRootedDirt;
        INFESTED_COARSE_DIRT = infestedCoarseDirt;
        NYXITE = nyxite;
        SHELLSTONE = shellstone;
        POLISHED_SHELLSTONE = polishedShellstone;
        SHELLSTONE_BRICKS = shellstoneBricks;
        CHISELED_SHELLSTONE = chiseledShellstone;
        MOSSY_SHELLSTONE_BRICKS = mossyShellstoneBricks;
        CRACKED_SHELLSTONE_BRICKS = crackedShellstoneBricks;
        MOSSY_SHELLSTONE_BRICK_STAIRS = mossyShellstoneBrickStairs;
        MOSSY_SHELLSTONE_BRICK_SLAB = mossyShellstoneBrickSlab;
        MOSSY_SHELLSTONE_BRICK_WALL = mossyShellstoneBrickWall;
        SHELLSTONE_STAIRS = shellstoneStairs;
        SHELLSTONE_SLAB = shellstoneSlab;
        SHELLSTONE_WALL = shellstoneWall;
        POLISHED_SHELLSTONE_STAIRS = polishedShellstoneStairs;
        POLISHED_SHELLSTONE_SLAB = polishedShellstoneSlab;
        POLISHED_SHELLSTONE_WALL = polishedShellstoneWall;
        SHELLSTONE_BRICK_STAIRS = shellstoneBrickStairs;
        SHELLSTONE_BRICK_SLAB = shellstoneBrickSlab;
        SHELLSTONE_BRICK_WALL = shellstoneBrickWall;
        CLOUD_BLOCK = cloudBlock;
        TRIFFID_GOO_BLOCK = triffidGooBlock;
        PALE_NYXITE = paleNyxite;
        NYXITE_SPIKE = nyxiteSpike;
        POTENT_NYXITE = potentNyxite;
        ANTIMETAL = antimetal;
        POLISHED_ANTIMETAL = polishedAntimetal;
        BUDDING_BLOOD_CRYSTAL = buddingBloodCrystal;
        SMALL_BLOOD_CRYSTAL_BUD = smallBloodCrystalBud;
        MEDIUM_BLOOD_CRYSTAL_BUD = mediumBloodCrystalBud;
        LARGE_BLOOD_CRYSTAL_BUD = largeBloodCrystalBud;
        BLOOD_CRYSTAL_CRYSTAL = bloodCrystalCrystal;
        OURANWOOD_ACORN = ouranwoodAcorn;
        KRAKEN_TOOTH = krakenTooth;
        MOGGLES = moggles;
        REVERIE_BOTTLE = reverieBottle;
        STINK_BUG_ITEM = stinkBugItem;
        DREAD = dread;
        PARALYZED_EFFECT = paralyzedEffect;
        INVERTED_EFFECT = invertedEffect;
        STINKY_EFFECT = stinkyEffect;
        OURANWOOD_ACORN_BLOCK = ouranwoodAcornBlock;
        MOSSY_OURANWOOD_LOG = mossyOuranwoodLog;
        MOSSY_OURANWOOD_WOOD = mossyOuranwoodWood;
        ORANGE_MILKWEED = orangeMilkweed;
        PINK_MILKWEED = pinkMilkweed;
        TORCHFLOWER_BUSH = torchflowerBush;
        BED_BUG_EGG = bedBugEggs;
        CREEPING_HORROR_EGGS = creepingHorrorEggs;
        LURKING_TERROR_EGGS = lurkingTerrorEggs;
        WASP_NEST = waspNest;
        HUSHWEED = hushweed;
        SQUIRREL_NEST_BLOCK = squirrelNestBlock;
        ANT_NEST_BLOCK_ENTITY = antNestBlockEntity;
        DREAM_CAMPFIRE_BLOCK_ENTITY = dreamCampfireBlockEntity;
        WASP_NEST_BLOCK_ENTITY = waspNestBlockEntity;
        HUSHWEED_BLOCK_ENTITY = hushweedBlockEntity;
        STINKY_GAS = stinkyGas;
        DOUBLE_DAMAGE_CHANCE = doubleDamageChance;
        BLOODGLASS_MAX_HEARTS = bloodglassMaxHearts;
        BLOODGLASS_WARD = bloodglassWard;
    }

    public static boolean isDuplicatorTreeBlock(BlockState state) {
        return state.is(DUPLICATOR_LOG.get()) || state.is(DUPLICATOR_SAPLING.get());
    }

    public static void setOctopusBomb(Supplier<EntityType<OctopusBombEntity>> supplier) {
        OCTOPUS_BOMB = supplier;
    }

    public static BlockBehaviour.Properties shellstoneProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops();
    }
}
