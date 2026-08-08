package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatVersion;
import com.craisinlord.antarchy.content.AntarchyGameRules;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipTetherSync;
import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.forge.registry.*;
import com.craisinlord.antarchy.forge.network.AntarchyGravityNetworking;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;



@Mod(Antarchy.MODID)
public class AntarchyForge {
    public static IEventBus modEventBusTempHolder = null;

    public AntarchyForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBusTempHolder = modEventBus;
        bootstrapGameRules();
        AntarchyConfigModuleForge.init();
        AntarchySoundEvents.bind(
                AntarchyForgeSounds.SQUIDZOOKA_FIRE,
                AntarchyForgeSounds.SHRINK_RAY_SOUND,
                AntarchyForgeSounds.GROWTH_RAY_SOUND,
                AntarchyForgeSounds.SIZE_RAY_CHARGE,
                AntarchyForgeSounds.ANT_AMBIENT,
                AntarchyForgeSounds.ANT_IDLE,
                AntarchyForgeSounds.ANT_HURT,
                AntarchyForgeSounds.ANT_BITE,
                AntarchyForgeSounds.ANT_GATHER,
                AntarchyForgeSounds.ANT_NEST,
                AntarchyForgeSounds.CLOUD_SHARK_BITE,
                AntarchyForgeSounds.CLOUD_SHARK_IDLE,
                AntarchyForgeSounds.CLOUD_SHARK_HURT,
                AntarchyForgeSounds.CLOUD_SHARK_DEATH,
                AntarchyForgeSounds.CLOUD_SHARK_FLY,
                AntarchyForgeSounds.CATERPILLAR_IDLE,
                AntarchyForgeSounds.CATERPILLAR_HURT,
                AntarchyForgeSounds.CATERPILLAR_CRAWL,
                AntarchyForgeSounds.BUTTERFLY_HURT,
                AntarchyForgeSounds.BRUTALFLY_IDLE,
                AntarchyForgeSounds.BRUTALFLY_DEATH,
                AntarchyForgeSounds.ELYTHIA_FIREFLY_AMBIENT,
                AntarchyForgeSounds.MISSILE_SQUID_AMBIENT,
                AntarchyForgeSounds.MISSILE_SQUID_HURT,
                AntarchyForgeSounds.MISSILE_SQUID_DEATH,
                AntarchyForgeSounds.MISSILE_SQUID_ATTACK,
                AntarchyForgeSounds.KRAKEN_FLYING_LOOP,
                AntarchyForgeSounds.KRAKEN_FLYING_SIDEWAYS_LOOP,
                AntarchyForgeSounds.KRAKEN_ATTACK,
                AntarchyForgeSounds.KRAKEN_SPIN,
                AntarchyForgeSounds.KRAKEN_ROAR,
                AntarchyForgeSounds.KRAKEN_SUMMON,
                AntarchyForgeSounds.KRAKEN_HURT,
                AntarchyForgeSounds.KRAKEN_DEATH,
                AntarchyForgeSounds.BASILISK_IDLE_LOOP,
                AntarchyForgeSounds.BASILISK_SLITHER_LOOP,
                AntarchyForgeSounds.BASILISK_BITE,
                AntarchyForgeSounds.BASILISK_HISS,
                AntarchyForgeSounds.BASILISK_HURT,
                AntarchyForgeSounds.BASILISK_DEATH,
                AntarchyForgeSounds.THORAXIS_NIGHTMARE_WASTES_AMBIENT,
                AntarchyForgeSounds.THORAXIS_NIGHTMARE_WASTES_ADDITIONS,
                AntarchyForgeSounds.THORAXIS_NIGHTMARE_WASTES_MOOD,
                AntarchyForgeSounds.THORAXIS_DREAM_DUNES_AMBIENT,
                AntarchyForgeSounds.THORAXIS_DREAM_DUNES_ADDITIONS,
                AntarchyForgeSounds.THORAXIS_DREAM_DUNES_MOOD,
                AntarchyForgeSounds.THORAXIS_LUCID_POOLS_AMBIENT,
                AntarchyForgeSounds.THORAXIS_LUCID_POOLS_ADDITIONS,
                AntarchyForgeSounds.THORAXIS_LUCID_POOLS_MOOD,
                AntarchyForgeSounds.LUCID_AMBIENT,
                AntarchyForgeSounds.LUCID_FLYING,
                AntarchyForgeSounds.LUCID_ATTACK,
                AntarchyForgeSounds.LUCID_BOLT_SOUND,
                AntarchyForgeSounds.LUCID_HURT,
                AntarchyForgeSounds.LUCID_DEATH,
                AntarchyForgeSounds.REVERIE_IDLE,
                AntarchyForgeSounds.REVERIE_HURT,
                AntarchyForgeSounds.REVERIE_WORRY,
                AntarchyForgeSounds.REVERIE_SAVE,
                AntarchyForgeSounds.REVERIE_JOIN_PLAYER,
                AntarchyForgeSounds.REVERIE_ALERT,
                AntarchyForgeSounds.FLYING_SQUIRREL_IDLE,
                AntarchyForgeSounds.FLYING_SQUIRREL_BEG,
                AntarchyForgeSounds.FLYING_SQUIRREL_NUT,
                AntarchyForgeSounds.FLYING_SQUIRREL_GLIDE_LOOP,
                AntarchyForgeSounds.FLYING_SQUIRREL_HURT,
                AntarchyForgeSounds.FLYING_SQUIRREL_DEATH,
                AntarchyForgeSounds.NIGHTMARE_IDLE,
                AntarchyForgeSounds.NIGHTMARE_HURT,
                AntarchyForgeSounds.NIGHTMARE_ROAR,
                AntarchyForgeSounds.NIGHTMARE_DEATH,
                AntarchyForgeSounds.NIGHTMARE_BITE,
                AntarchyForgeSounds.NIGHTMARE_FLAP,
                AntarchyForgeSounds.TRIFFID_ATTACK,
                AntarchyForgeSounds.TRIFFID_GRAB,
                AntarchyForgeSounds.TRIFFID_HURT,
                AntarchyForgeSounds.TRIFFID_DEATH,
                AntarchyForgeSounds.TRIFFID_HISS,
                AntarchyForgeSounds.TRIFFID_GROWL,
                AntarchyForgeSounds.MANTIS_AMBIENT,
                AntarchyForgeSounds.MANTIS_HURT,
                AntarchyForgeSounds.MANTIS_ATTACK,
                AntarchyForgeSounds.MANTIS_FLY_LOOP,
                AntarchyForgeSounds.GRAVITY_GUN_PICKUP,
                AntarchyForgeSounds.GRAVITY_GUN_DROP,
                AntarchyForgeSounds.GRAVITY_GUN_HOLD_LOOP,
                AntarchyForgeSounds.GRAVITY_GUN_LAUNCH,
                AntarchyForgeSounds.GRAVITY_GUN_DRYFIRE,
                AntarchyForgeSounds.BED_BUG_AMBIENT,
                AntarchyForgeSounds.BED_BUG_HURT,
                AntarchyForgeSounds.BED_BUG_ATTACK,
                AntarchyForgeSounds.WASP_IDLE,
                AntarchyForgeSounds.WASP_HURT,
                AntarchyForgeSounds.WASP_ATTACK,
                AntarchyForgeSounds.WASP_DEATH,
                AntarchyForgeSounds.SCORPION_AMBIENT,
                AntarchyForgeSounds.SCORPION_HURT,
                AntarchyForgeSounds.SCORPION_ATTACK,
                AntarchyForgeSounds.EMPEROR_SCORPION_AMBIENT,
                AntarchyForgeSounds.EMPEROR_SCORPION_HURT,
                AntarchyForgeSounds.EMPEROR_SCORPION_ATTACK,
                AntarchyForgeSounds.EMPEROR_SCORPION_ROAR,
                AntarchyForgeSounds.MOLEWORM_AMBIENT,
                AntarchyForgeSounds.MOLEWORM_HURT,
                AntarchyForgeSounds.MOLEWORM_ATTACK,
                AntarchyForgeSounds.MOLEWORM_DIG,
                AntarchyForgeSounds.MOLEVORE_AMBIENT,
                AntarchyForgeSounds.MOLEVORE_HURT,
                AntarchyForgeSounds.MOLEVORE_ATTACK,
                AntarchyForgeSounds.MOLEVORE_DIG,
                AntarchyForgeSounds.BOMBER_WALK,
                AntarchyForgeSounds.BOMBER_KNOCK,
                AntarchyForgeSounds.BOMBER_EXPLODE,
                AntarchyForgeSounds.TORETERROR_IDLE,
                AntarchyForgeSounds.TORETERROR_HURT,
                AntarchyForgeSounds.TORETERROR_DEATH,
                AntarchyForgeSounds.TORETERROR_BOMBER_FIRE,
                AntarchyForgeSounds.TORETERROR_SPIN,
                AntarchyForgeSounds.TORETERROR_RICOCHET,
                AntarchyForgeSounds.WATER_CANNON_FIRE,
                AntarchyForgeSounds.TORETERROR_JUMP_UP,
                AntarchyForgeSounds.TORETERROR_JUMP_LAND,
                AntarchyForgeSounds.STINKY_FLY,
                AntarchyForgeSounds.STINK_BUG_FART,
                AntarchyForgeSounds.STINK_BUG_IDLE,
                AntarchyForgeSounds.CAVARYN_HEARTBEAT,
                AntarchyForgeSounds.ROLLY_POLLY_IDLE,
                AntarchyForgeSounds.CREEPING_HORROR_GROWL,
                AntarchyForgeSounds.CREEPING_HORROR_HURT,
                AntarchyForgeSounds.CREEPING_HORROR_BITE,
                AntarchyForgeSounds.LURKING_TERROR_SNARL,
                AntarchyForgeSounds.LURKING_TERROR_HURT,
                AntarchyForgeSounds.LURKING_TERROR_BITE,
                AntarchyForgeSounds.LURKING_TERROR_FLY_LOOP,
                AntarchyForgeSounds.DUCT_TAPE_USE,
                AntarchyForgeSounds.HERCULES_BEETLE_IDLE,
                AntarchyForgeSounds.HERCULES_BEETLE_HURT,
                AntarchyForgeSounds.HERCULES_BEETLE_ATTACK,
                AntarchyForgeSounds.HERCULES_BEETLE_CRY,
                AntarchyForgeSounds.HERCULES_BEETLE_CHARGE_START,
                AntarchyForgeSounds.HERCULES_BEETLE_KNOCKED_DOWN,
                AntarchyForgeSounds.JUMPY_BUG_IDLE,
                AntarchyForgeSounds.SPIT_BUG_IDLE,
                AntarchyForgeSounds.JUMPY_BUG_HURT,
                AntarchyForgeSounds.JUMPY_BUG_JUMP,
                AntarchyForgeSounds.SPIT_BUG_HURT,
                AntarchyForgeSounds.SPIT_BUG_SPIT,
                AntarchyForgeSounds.BRUTALFLY_SPIT,
                AntarchyForgeSounds.ROLLY_POLLY_WHEEL_MODE,
                AntarchyForgeSounds.ROLLY_POLLY_NORMAL_MODE,
                AntarchyForgeSounds.ROLLY_POLLY_ROLL,
                AntarchyForgeSounds.JERRY_YOUNG_IDLE,
                AntarchyForgeSounds.JERRY_YOUNG_HURT,
                AntarchyForgeSounds.JERRY_YOUNG_DEATH,
                AntarchyForgeSounds.JERRY_YOUNG_ATTACK,
                AntarchyForgeSounds.JERRY_ADULT_IDLE,
                AntarchyForgeSounds.JERRY_ADULT_HURT,
                AntarchyForgeSounds.JERRY_ADULT_DEATH,
                AntarchyForgeSounds.JERRY_ADULT_ATTACK
        );
        bindCommonObjects();
        ScorpionWhipTetherSync.setSink((player, targetId) -> com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore.sendToTrackingEntity(
                player,
                new com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload(player.getId(), targetId)
        ));
        BloodCrystalKatanaItem.setTrailCallback((player, durationTicks) -> com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore.sendToTrackingEntity(
                player,
                new BloodCrystalKatanaTrailPayload(player.getId(), durationTicks)
        ));
        com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.setSyncDispatcher(AntarchyGravityNetworking::syncEntity);
        com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.setSendToPlayer(
                com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore::sendToPlayer);
        com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.setSyncSelfAndTracking(player ->
                com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore.sendToTrackingEntity(
                        player,
                        com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.payload(player)
                ));
        com.craisinlord.antarchy.content.effect.AntarchySizeScaling.register(
                com.craisinlord.antarchy.forge.registry.AntarchyForgeMisc.SHRINKING_EFFECT,
                com.craisinlord.antarchy.forge.registry.AntarchyForgeMisc.GROWTH_EFFECT
        );
        if (isSupportedInfinityLoaded()) {
            InfinityCompat.bind(new ForgeInfinityCompat());
        }
        AntarchyForgeEvents.register(modEventBus);
        AntarchyForgeSounds.register(modEventBus);
        AntarchyForgeEntites.register(modEventBus);
        AntarchyForgeBlocks.register(modEventBus);
        AntarchyForgeItems.register(modEventBus);
        AntarchyForgeFluidTypes.register(modEventBus);
        AntarchyForgeMisc.register(modEventBus);
        AntarchyForgeSpawnPlacements.register(modEventBus);
        AntarchyForgeCreativeModeTabs.register(modEventBus);
        AntarchyForgeEntityAttributes.register(modEventBus);
        AntarchyForgePayloadHandlers.register();
        Antarchy.init();
    }

    private static void bootstrapGameRules() {
        AntarchyGameRules.bootstrap((name, category, defaultValue) -> {
            net.minecraft.world.level.GameRules.Type<net.minecraft.world.level.GameRules.BooleanValue> type =
                    net.minecraft.world.level.GameRules.BooleanValue.create(defaultValue);
            return net.minecraft.world.level.GameRules.register(name, category, type);
        });
    }

    private static void bindCommonObjects() {
        com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity.TYPE = AntarchyForgeEntites.UPWARD_FALLING_BLOCK;
        PermanentPortalType.bindBlocks(
                () -> AntarchyForgeBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyForgeBlocks.ELYTHIA_PORTAL.get(),
                () -> AntarchyForgeBlocks.NYXITE.get(),
                () -> AntarchyForgeBlocks.THORAXIS_PORTAL.get(),
                () -> AntarchyForgeBlocks.MYRMITE.get(),
                () -> AntarchyForgeBlocks.CAVARYN_PORTAL.get()
        );
        AntarchyObjects.bind(
                AntarchyForgeEntites.EASTER_BUNNY,
                AntarchyForgeEntites.FLYING_SQUIRREL,
                AntarchyForgeEntites.KRAKEN,
                AntarchyForgeEntites.MISSILE_SQUID,
                AntarchyForgeEntites.MOLEWORM,
                AntarchyForgeEntites.MANTIS,
                AntarchyForgeEntites.BED_BUG,
                AntarchyForgeEntites.WASP,
                AntarchyForgeEntites.BOMBER,
                AntarchyForgeEntites.SCORPION,
                AntarchyForgeEntites.CATERPILLAR,
                AntarchyForgeEntites.BUTTERFLY,
                AntarchyForgeEntites.REVERIE,
                AntarchyForgeEntites.TRIFFID,
                AntarchyForgeEntites.BRUTALFLY,
                AntarchyForgeEntites.BRUTALFLY_ORB,
                AntarchyForgeEntites.HUSH_PROJECTILE,
                AntarchyForgeEntites.TORETERROR,
                AntarchyForgeEntites.WATER_BOMB,
                AntarchyForgeEntites.CREEPING_HORROR,
                AntarchyForgeEntites.LURKING_TERROR,
                AntarchyForgeEntites.STINK_BUG,
                AntarchyForgeEntites.CHEEP,
                AntarchyForgeEntites.DORRIE,
                () -> AntarchyForgeBlocks.DUPLICATOR_LOG.get(),
                () -> AntarchyForgeBlocks.DUPLICATOR_SAPLING.get(),
                () -> AntarchyForgeBlocks.DUCT_TAPE.get(),
                () -> AntarchyForgeBlocks.INFESTED_ROOTED_DIRT.get(),
                () -> AntarchyForgeBlocks.INFESTED_COARSE_DIRT.get(),
                () -> AntarchyForgeBlocks.NYXITE.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE.get(),
                () -> AntarchyForgeBlocks.POLISHED_SHELLSTONE.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_BRICKS.get(),
                () -> AntarchyForgeBlocks.CHISELED_SHELLSTONE.get(),
                () -> AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICKS.get(),
                () -> AntarchyForgeBlocks.CRACKED_SHELLSTONE_BRICKS.get(),
                () -> AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICK_STAIRS.get(),
                () -> AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICK_SLAB.get(),
                () -> AntarchyForgeBlocks.MOSSY_SHELLSTONE_BRICK_WALL.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_STAIRS.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_SLAB.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_WALL.get(),
                () -> AntarchyForgeBlocks.POLISHED_SHELLSTONE_STAIRS.get(),
                () -> AntarchyForgeBlocks.POLISHED_SHELLSTONE_SLAB.get(),
                () -> AntarchyForgeBlocks.POLISHED_SHELLSTONE_WALL.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_BRICK_STAIRS.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_BRICK_SLAB.get(),
                () -> AntarchyForgeBlocks.SHELLSTONE_BRICK_WALL.get(),
                () -> AntarchyForgeBlocks.CLOUD_BLOCK.get(),
                () -> AntarchyForgeBlocks.TRIFFID_GOO_BLOCK.get(),
                () -> AntarchyForgeBlocks.PALE_NYXITE.get(),
                () -> AntarchyForgeBlocks.NYXITE_SPIKE.get(),
                () -> AntarchyForgeBlocks.CHITIN_BLOCK.get(),
                () -> AntarchyForgeBlocks.CHITIN_SPIKE.get(),
                () -> AntarchyForgeBlocks.POTENT_NYXITE.get(),
                () -> AntarchyForgeBlocks.ANTIMETAL.get(),
                () -> AntarchyForgeBlocks.POLISHED_ANTIMETAL.get(),
                () -> AntarchyForgeBlocks.BUDDING_BLOOD_CRYSTAL.get(),
                () -> AntarchyForgeBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyForgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyForgeBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyForgeBlocks.BLOOD_CRYSTAL_CRYSTAL.get(),
                () -> AntarchyForgeItems.OURANWOOD_ACORN.get(),
                AntarchyForgeItems.KRAKEN_TOOTH,
                () -> AntarchyForgeItems.MOGGLES.get(),
                () -> AntarchyForgeItems.REVERIE_BOTTLE.get(),
                () -> AntarchyForgeItems.STINK_BUG.get(),
                () -> AntarchyForgeMisc.mobEffectHolder(AntarchyForgeMisc.DREAD),
                () -> AntarchyForgeMisc.mobEffectHolder(AntarchyForgeMisc.PARALYZED),
                () -> AntarchyForgeMisc.mobEffectHolder(AntarchyForgeMisc.INVERTED),
                () -> AntarchyForgeMisc.mobEffectHolder(AntarchyForgeMisc.STINKY),
                () -> AntarchyForgeBlocks.OURANWOOD_ACORN_BLOCK.get(),
                () -> AntarchyForgeBlocks.MOSSY_OURANWOOD_LOG.get(),
                () -> AntarchyForgeBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyForgeBlocks.UMBRAL_MOSS_BLOCK.get(),
                () -> AntarchyForgeBlocks.UMBRAL_MOSS_CARPET.get(),
                () -> AntarchyForgeBlocks.BLUSH_MOSS_BLOCK.get(),
                () -> AntarchyForgeBlocks.BLUSH_MOSS_CARPET.get(),
                () -> AntarchyForgeBlocks.ORANGE_MILKWEED.get(),
                () -> AntarchyForgeBlocks.PINK_MILKWEED.get(),
                () -> AntarchyForgeBlocks.BED_BUG_EGG.get(),
                () -> AntarchyForgeBlocks.CREEPING_HORROR_EGG.get(),
                () -> AntarchyForgeBlocks.LURKING_TERROR_EGG.get(),
                () -> AntarchyForgeBlocks.WASP_NEST.get(),
                () -> AntarchyForgeBlocks.HUSHWEED.get(),
                () -> AntarchyForgeBlocks.OURANWOOD_SQUIRREL_NEST.get(),
                () -> AntarchyForgeBlocks.BRUTALFLY_COCOON_SPAWNER.get(),
                () -> AntarchyForgeBlocks.GIANT_LILY_PAD.get(),
                () -> AntarchyForgeBlocks.SEASHELL.get(),
                () -> AntarchyForgeBlocks.ANT_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyForgeBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                () -> AntarchyForgeBlocks.WASP_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyForgeBlocks.HUSHWEED_BLOCK_ENTITY.get(),
                () -> AntarchyForgeBlocks.SEASHELL_BLOCK_ENTITY.get(),
                () -> AntarchyForgeMisc.STINKY_GAS.get(),
                () -> AntarchyForgeMisc.STINKY_FLY.get(),
                () -> AntarchyForgeMisc.PEACH_LEAVES_PARTICLE.get(),
                () -> AntarchyForgeMisc.attributeHolder(AntarchyForgeMisc.SCALE),
                () -> AntarchyForgeMisc.attributeHolder(AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE),
                () -> AntarchyForgeMisc.attributeHolder(AntarchyForgeMisc.BLOODGLASS_MAX_HEARTS),
                () -> AntarchyForgeMisc.mobEffectHolder(AntarchyForgeMisc.BLOODGLASS_WARD)
        );
        AntarchyObjects.setOctopusBomb(AntarchyForgeEntites.OCTOPUS_BOMB);
        AntarchyObjects.setTentacle(AntarchyForgeEntites.TENTACLE);
        AntarchyObjects.setNightmarePortal(AntarchyForgeEntites.NIGHTMARE_PORTAL);
        AntarchyObjects.setNightmareBite(AntarchyForgeEntites.NIGHTMARE_BITE);
        AntarchyObjects.setKrakensGraspTrident(AntarchyForgeEntites.KRAKENS_GRASP_TRIDENT);
        AntarchyObjects.setLotus(() -> AntarchyForgeBlocks.LOTUS.get());
        AntarchyObjects.setKrakenTentacle(() -> AntarchyForgeItems.KRAKEN_TENTACLE.get());
        AntarchyObjects.setKrakensGrasp(() -> AntarchyForgeItems.KRAKENS_GRASP.get());
        AntarchyObjects.setOuranwoodDeer(AntarchyForgeEntites.OURANWOOD_DEER);
        AntarchyObjects.setGlimmer(AntarchyForgeEntites.GLIMMER);
        AntarchyObjects.setRollyPolly(AntarchyForgeEntites.ROLLY_POLLY);
        AntarchyObjects.setSpiritApple(() -> AntarchyForgeItems.SPIRIT_APPLE.get());
        AntarchyObjects.setGlimmeringEffect(() -> AntarchyForgeMisc.mobEffectHolder(AntarchyForgeMisc.GLIMMERING));
        AntarchyObjects.setElka(AntarchyForgeEntites.ELKA);
        AntarchyObjects.setPeach(() -> AntarchyForgeItems.PEACH.get());
        AntarchyObjects.setCorn(() -> AntarchyForgeItems.CORN.get());
        AntarchyObjects.setCornSeeds(() -> AntarchyForgeItems.CORN_SEEDS.get());
        AntarchyObjects.setStarCoralBlock(() -> AntarchyForgeBlocks.STAR_CORAL_BLOCK.get());
        AntarchyObjects.setStarCoral(() -> AntarchyForgeBlocks.STAR_CORAL.get());
        AntarchyObjects.setStarCoralFan(() -> AntarchyForgeBlocks.STAR_CORAL_FAN.get());
        AntarchyObjects.setCookedCorndog(() -> AntarchyForgeItems.COOKED_CORNDOG.get());
        AntarchyObjects.setGlimmerBottle(() -> AntarchyForgeItems.GLIMMER_BOTTLE.get());
        AntarchyObjects.setLumen(() -> AntarchyForgeMisc.LUMEN.get());
        AntarchyObjects.setFlowingLumen(() -> AntarchyForgeMisc.FLOWING_LUMEN.get());
        AntarchyObjects.setLumenBucket(() -> AntarchyForgeItems.LUMEN_BUCKET.get());
        AntarchyObjects.setLumenBlock(() -> AntarchyForgeBlocks.LUMEN_BLOCK.get());
        AntarchyObjects.setLumenFroglight(() -> AntarchyForgeBlocks.LUMEN_FROGLIGHT.get());
        AntarchyObjects.setPeachLeavesParticle(() -> AntarchyForgeMisc.PEACH_LEAVES_PARTICLE.get());
        AntarchyObjects.setLotusPollen(() -> AntarchyForgeMisc.LOTUS_POLLEN.get());
        AntarchyObjects.setLucidBoltImpactSmall(() -> AntarchyForgeMisc.LUCID_BOLT_IMPACT_SMALL.get());
        AntarchyObjects.setLucidBoltImpactLarge(() -> AntarchyForgeMisc.LUCID_BOLT_IMPACT_LARGE.get());
        AntarchyObjects.setDorrieInventoryMenu(AntarchyForgeMisc.DORRIE_INVENTORY_MENU);
        AntarchyObjects.setCritterCage(() -> AntarchyForgeItems.CRITTER_CAGE.get());
        AntarchyObjects.setCritterCageBlock(() -> AntarchyForgeBlocks.CRITTER_CAGE_BLOCK.get());
        AntarchyObjects.setCritterCageBlockEntity(() -> AntarchyForgeBlocks.CRITTER_CAGE_BLOCK_ENTITY.get());
        AntarchyObjects.setLucidAnchor(() -> AntarchyForgeBlocks.LUCID_ANCHOR.get());
        AntarchyObjects.setLucidAnchorBlockEntity(() -> AntarchyForgeBlocks.LUCID_ANCHOR_BLOCK_ENTITY.get());
        AntarchyObjects.setCritterCageProjectile(() -> AntarchyForgeEntites.CRITTER_CAGE_PROJECTILE.get());
    }

    private static boolean isModLoaded(String modId) {
        try {
            return ModList.get().isLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isSupportedInfinityLoaded() {
        if (!isModLoaded("infinity")) {
            return false;
        }

        try {
            String version = ModList.get()
                    .getModContainerById("infinity")
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse(null);
            if (InfinityCompatVersion.isSupported(version)) {
                return true;
            }

            Antarchy.LOGGER.warn(
                    "Skipping Infinity integration because version {} is below the supported minimum {}",
                    version,
                    InfinityCompatVersion.requiredVersion()
            );
        } catch (Throwable throwable) {
            Antarchy.LOGGER.warn("Skipping Infinity integration because the installed Infinity version could not be verified", throwable);
        }
        return false;
    }
}
