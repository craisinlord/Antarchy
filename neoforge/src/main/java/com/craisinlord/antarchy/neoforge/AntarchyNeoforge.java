package com.craisinlord.antarchy.neoforge;

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
import com.craisinlord.antarchy.neoforge.registry.*;
import com.craisinlord.antarchy.neoforge.network.AntarchyGravityNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Mod(Antarchy.MODID)
public class AntarchyNeoforge {
    public static IEventBus modEventBusTempHolder = null;

    public AntarchyNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBusTempHolder = modEventBus;
        bootstrapGameRules();
        AntarchyConfigModuleNeoforge.init(modContainer);
        AntarchySoundEvents.bind(
                AntarchyNeoforgeSounds.SQUIDZOOKA_FIRE,
                AntarchyNeoforgeSounds.SHRINK_RAY_SOUND,
                AntarchyNeoforgeSounds.GROWTH_RAY_SOUND,
                AntarchyNeoforgeSounds.SIZE_RAY_CHARGE,
                AntarchyNeoforgeSounds.ANT_AMBIENT,
                AntarchyNeoforgeSounds.ANT_IDLE,
                AntarchyNeoforgeSounds.ANT_HURT,
                AntarchyNeoforgeSounds.ANT_BITE,
                AntarchyNeoforgeSounds.ANT_GATHER,
                AntarchyNeoforgeSounds.ANT_NEST,
                AntarchyNeoforgeSounds.CLOUD_SHARK_BITE,
                AntarchyNeoforgeSounds.CLOUD_SHARK_IDLE,
                AntarchyNeoforgeSounds.CLOUD_SHARK_HURT,
                AntarchyNeoforgeSounds.CLOUD_SHARK_DEATH,
                AntarchyNeoforgeSounds.CLOUD_SHARK_FLY,
                AntarchyNeoforgeSounds.CATERPILLAR_IDLE,
                AntarchyNeoforgeSounds.CATERPILLAR_HURT,
                AntarchyNeoforgeSounds.CATERPILLAR_CRAWL,
                AntarchyNeoforgeSounds.BUTTERFLY_HURT,
                AntarchyNeoforgeSounds.BRUTALFLY_IDLE,
                AntarchyNeoforgeSounds.BRUTALFLY_DEATH,
                AntarchyNeoforgeSounds.ELYTHIA_FIREFLY_AMBIENT,
                AntarchyNeoforgeSounds.MISSILE_SQUID_AMBIENT,
                AntarchyNeoforgeSounds.MISSILE_SQUID_HURT,
                AntarchyNeoforgeSounds.MISSILE_SQUID_DEATH,
                AntarchyNeoforgeSounds.MISSILE_SQUID_ATTACK,
                AntarchyNeoforgeSounds.KRAKEN_FLYING_LOOP,
                AntarchyNeoforgeSounds.KRAKEN_FLYING_SIDEWAYS_LOOP,
                AntarchyNeoforgeSounds.KRAKEN_ATTACK,
                AntarchyNeoforgeSounds.KRAKEN_SPIN,
                AntarchyNeoforgeSounds.KRAKEN_ROAR,
                AntarchyNeoforgeSounds.KRAKEN_SUMMON,
                AntarchyNeoforgeSounds.KRAKEN_HURT,
                AntarchyNeoforgeSounds.KRAKEN_DEATH,
                AntarchyNeoforgeSounds.BASILISK_IDLE_LOOP,
                AntarchyNeoforgeSounds.BASILISK_SLITHER_LOOP,
                AntarchyNeoforgeSounds.BASILISK_BITE,
                AntarchyNeoforgeSounds.BASILISK_HISS,
                AntarchyNeoforgeSounds.BASILISK_HURT,
                AntarchyNeoforgeSounds.BASILISK_DEATH,
                AntarchyNeoforgeSounds.THORAXIS_NIGHTMARE_WASTES_AMBIENT,
                AntarchyNeoforgeSounds.THORAXIS_NIGHTMARE_WASTES_ADDITIONS,
                AntarchyNeoforgeSounds.THORAXIS_NIGHTMARE_WASTES_MOOD,
                AntarchyNeoforgeSounds.THORAXIS_DREAM_DUNES_AMBIENT,
                AntarchyNeoforgeSounds.THORAXIS_DREAM_DUNES_ADDITIONS,
                AntarchyNeoforgeSounds.THORAXIS_DREAM_DUNES_MOOD,
                AntarchyNeoforgeSounds.THORAXIS_LUCID_POOLS_AMBIENT,
                AntarchyNeoforgeSounds.THORAXIS_LUCID_POOLS_ADDITIONS,
                AntarchyNeoforgeSounds.THORAXIS_LUCID_POOLS_MOOD,
                AntarchyNeoforgeSounds.LUCID_AMBIENT,
                AntarchyNeoforgeSounds.LUCID_FLYING,
                AntarchyNeoforgeSounds.LUCID_ATTACK,
                AntarchyNeoforgeSounds.LUCID_BOLT_SOUND,
                AntarchyNeoforgeSounds.LUCID_HURT,
                AntarchyNeoforgeSounds.LUCID_DEATH,
                AntarchyNeoforgeSounds.REVERIE_IDLE,
                AntarchyNeoforgeSounds.REVERIE_HURT,
                AntarchyNeoforgeSounds.REVERIE_WORRY,
                AntarchyNeoforgeSounds.REVERIE_SAVE,
                AntarchyNeoforgeSounds.REVERIE_JOIN_PLAYER,
                AntarchyNeoforgeSounds.REVERIE_ALERT,
                AntarchyNeoforgeSounds.FLYING_SQUIRREL_IDLE,
                AntarchyNeoforgeSounds.FLYING_SQUIRREL_BEG,
                AntarchyNeoforgeSounds.FLYING_SQUIRREL_NUT,
                AntarchyNeoforgeSounds.FLYING_SQUIRREL_GLIDE_LOOP,
                AntarchyNeoforgeSounds.FLYING_SQUIRREL_HURT,
                AntarchyNeoforgeSounds.FLYING_SQUIRREL_DEATH,
                AntarchyNeoforgeSounds.NIGHTMARE_IDLE,
                AntarchyNeoforgeSounds.NIGHTMARE_HURT,
                AntarchyNeoforgeSounds.NIGHTMARE_ROAR,
                AntarchyNeoforgeSounds.NIGHTMARE_DEATH,
                AntarchyNeoforgeSounds.NIGHTMARE_BITE,
                AntarchyNeoforgeSounds.NIGHTMARE_FLAP,
                AntarchyNeoforgeSounds.TRIFFID_ATTACK,
                AntarchyNeoforgeSounds.TRIFFID_GRAB,
                AntarchyNeoforgeSounds.TRIFFID_HURT,
                AntarchyNeoforgeSounds.TRIFFID_DEATH,
                AntarchyNeoforgeSounds.TRIFFID_HISS,
                AntarchyNeoforgeSounds.TRIFFID_GROWL,
                AntarchyNeoforgeSounds.MANTIS_AMBIENT,
                AntarchyNeoforgeSounds.MANTIS_HURT,
                AntarchyNeoforgeSounds.MANTIS_ATTACK,
                AntarchyNeoforgeSounds.MANTIS_FLY_LOOP,
                AntarchyNeoforgeSounds.GRAVITY_GUN_PICKUP,
                AntarchyNeoforgeSounds.GRAVITY_GUN_DROP,
                AntarchyNeoforgeSounds.GRAVITY_GUN_HOLD_LOOP,
                AntarchyNeoforgeSounds.GRAVITY_GUN_LAUNCH,
                AntarchyNeoforgeSounds.GRAVITY_GUN_DRYFIRE,
                AntarchyNeoforgeSounds.BED_BUG_AMBIENT,
                AntarchyNeoforgeSounds.BED_BUG_HURT,
                AntarchyNeoforgeSounds.BED_BUG_ATTACK,
                AntarchyNeoforgeSounds.WASP_IDLE,
                AntarchyNeoforgeSounds.WASP_HURT,
                AntarchyNeoforgeSounds.WASP_ATTACK,
                AntarchyNeoforgeSounds.WASP_DEATH,
                AntarchyNeoforgeSounds.SCORPION_AMBIENT,
                AntarchyNeoforgeSounds.SCORPION_HURT,
                AntarchyNeoforgeSounds.SCORPION_ATTACK,
                AntarchyNeoforgeSounds.EMPEROR_SCORPION_AMBIENT,
                AntarchyNeoforgeSounds.EMPEROR_SCORPION_HURT,
                AntarchyNeoforgeSounds.EMPEROR_SCORPION_ATTACK,
                AntarchyNeoforgeSounds.EMPEROR_SCORPION_ROAR,
                AntarchyNeoforgeSounds.MOLEWORM_AMBIENT,
                AntarchyNeoforgeSounds.MOLEWORM_HURT,
                AntarchyNeoforgeSounds.MOLEWORM_ATTACK,
                AntarchyNeoforgeSounds.MOLEWORM_DIG,
                AntarchyNeoforgeSounds.MOLEVORE_AMBIENT,
                AntarchyNeoforgeSounds.MOLEVORE_HURT,
                AntarchyNeoforgeSounds.MOLEVORE_ATTACK,
                AntarchyNeoforgeSounds.MOLEVORE_DIG,
                AntarchyNeoforgeSounds.BOMBER_WALK,
                AntarchyNeoforgeSounds.BOMBER_KNOCK,
                AntarchyNeoforgeSounds.BOMBER_EXPLODE,
                AntarchyNeoforgeSounds.TORETERROR_IDLE,
                AntarchyNeoforgeSounds.TORETERROR_HURT,
                AntarchyNeoforgeSounds.TORETERROR_DEATH,
                AntarchyNeoforgeSounds.TORETERROR_BOMBER_FIRE,
                AntarchyNeoforgeSounds.TORETERROR_SPIN,
                AntarchyNeoforgeSounds.TORETERROR_RICOCHET,
                AntarchyNeoforgeSounds.WATER_CANNON_FIRE,
                AntarchyNeoforgeSounds.TORETERROR_JUMP_UP,
                AntarchyNeoforgeSounds.TORETERROR_JUMP_LAND,
                AntarchyNeoforgeSounds.STINKY_FLY,
                AntarchyNeoforgeSounds.STINK_BUG_FART,
                AntarchyNeoforgeSounds.STINK_BUG_IDLE,
                AntarchyNeoforgeSounds.CAVARYN_HEARTBEAT,
                AntarchyNeoforgeSounds.ROLLY_POLLY_IDLE,
                AntarchyNeoforgeSounds.CREEPING_HORROR_GROWL,
                AntarchyNeoforgeSounds.CREEPING_HORROR_HURT,
                AntarchyNeoforgeSounds.CREEPING_HORROR_BITE,
                AntarchyNeoforgeSounds.LURKING_TERROR_SNARL,
                AntarchyNeoforgeSounds.LURKING_TERROR_HURT,
                AntarchyNeoforgeSounds.LURKING_TERROR_BITE,
                AntarchyNeoforgeSounds.LURKING_TERROR_FLY_LOOP,
                AntarchyNeoforgeSounds.DUCT_TAPE_USE,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_IDLE,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_HURT,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_ATTACK,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_CRY,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_CHARGE_START,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_KNOCKED_DOWN,
                AntarchyNeoforgeSounds.JUMPY_BUG_IDLE,
                AntarchyNeoforgeSounds.SPIT_BUG_IDLE,
                AntarchyNeoforgeSounds.JUMPY_BUG_HURT,
                AntarchyNeoforgeSounds.JUMPY_BUG_JUMP,
                AntarchyNeoforgeSounds.SPIT_BUG_HURT,
                AntarchyNeoforgeSounds.SPIT_BUG_SPIT,
                AntarchyNeoforgeSounds.BRUTALFLY_SPIT,
                AntarchyNeoforgeSounds.ROLLY_POLLY_WHEEL_MODE,
                AntarchyNeoforgeSounds.ROLLY_POLLY_NORMAL_MODE,
                AntarchyNeoforgeSounds.ROLLY_POLLY_ROLL,
                AntarchyNeoforgeSounds.JERRY_YOUNG_IDLE,
                AntarchyNeoforgeSounds.JERRY_YOUNG_HURT,
                AntarchyNeoforgeSounds.JERRY_YOUNG_DEATH,
                AntarchyNeoforgeSounds.JERRY_YOUNG_ATTACK,
                AntarchyNeoforgeSounds.JERRY_ADULT_IDLE,
                AntarchyNeoforgeSounds.JERRY_ADULT_HURT,
                AntarchyNeoforgeSounds.JERRY_ADULT_DEATH,
                AntarchyNeoforgeSounds.JERRY_ADULT_ATTACK
        );
        bindCommonObjects();
        ScorpionWhipTetherSync.setSink((player, targetId) -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload(player.getId(), targetId)
        ));
        BloodCrystalKatanaItem.setTrailCallback((player, durationTicks) -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new BloodCrystalKatanaTrailPayload(player.getId(), durationTicks)
        ));
        com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.setSyncDispatcher(AntarchyGravityNetworking::syncEntity);
        if (isSupportedInfinityLoaded()) {
            InfinityCompat.bind(new NeoForgeInfinityCompat());
        }
        AntarchyNeoForgeEvents.register(modEventBus);
        AntarchyNeoforgeSounds.register(modEventBus);
        AntarchyNeoforgeEntites.register(modEventBus);
        AntarchyNeoforgeBlocks.register(modEventBus);
        AntarchyNeoforgeItems.register(modEventBus);
        AntarchyNeoForgeFluidTypes.register(modEventBus);
        AntarchyNeoforgeMisc.register(modEventBus);
        AntarchyNeoforgeSpawnPlacements.register(modEventBus);
        AntarchyNeoforgeCreativeModeTabs.register(modEventBus);
        AntarchyNeoforgeEntityAttributes.register(modEventBus);
        AntarchyNeoforgePayloadHandlers.register(modEventBus);
        Antarchy.init();
    }

    @SuppressWarnings("unchecked")
    private static void bootstrapGameRules() {
        AntarchyGameRules.bootstrap((name, category, defaultValue) -> {
            try {
                Method create = net.minecraft.world.level.GameRules.BooleanValue.class.getDeclaredMethod("create", boolean.class);
                create.setAccessible(true);
                net.minecraft.world.level.GameRules.Type<net.minecraft.world.level.GameRules.BooleanValue> type =
                        (net.minecraft.world.level.GameRules.Type<net.minecraft.world.level.GameRules.BooleanValue>) create.invoke(null, defaultValue);
                Method register = net.minecraft.world.level.GameRules.class.getDeclaredMethod(
                        "register",
                        String.class,
                        net.minecraft.world.level.GameRules.Category.class,
                        net.minecraft.world.level.GameRules.Type.class
                );
                register.setAccessible(true);
                return (net.minecraft.world.level.GameRules.Key<net.minecraft.world.level.GameRules.BooleanValue>) register.invoke(null, name, category, type);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to register boolean gamerule " + name, e);
            }
        });
    }

    private static void bindCommonObjects() {
        com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity.TYPE = AntarchyNeoforgeEntites.UPWARD_FALLING_BLOCK;
        PermanentPortalType.bindBlocks(
                () -> AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyNeoforgeBlocks.ELYTHIA_PORTAL.get(),
                () -> AntarchyNeoforgeBlocks.NYXITE.get(),
                () -> AntarchyNeoforgeBlocks.THORAXIS_PORTAL.get(),
                () -> AntarchyNeoforgeBlocks.MYRMITE.get(),
                () -> AntarchyNeoforgeBlocks.CAVARYN_PORTAL.get()
        );
        AntarchyObjects.bind(
                AntarchyNeoforgeEntites.EASTER_BUNNY,
                AntarchyNeoforgeEntites.FLYING_SQUIRREL,
                AntarchyNeoforgeEntites.KRAKEN,
                AntarchyNeoforgeEntites.MISSILE_SQUID,
                AntarchyNeoforgeEntites.MOLEWORM,
                AntarchyNeoforgeEntites.MANTIS,
                AntarchyNeoforgeEntites.BED_BUG,
                AntarchyNeoforgeEntites.WASP,
                AntarchyNeoforgeEntites.BOMBER,
                AntarchyNeoforgeEntites.SCORPION,
                AntarchyNeoforgeEntites.CATERPILLAR,
                AntarchyNeoforgeEntites.BUTTERFLY,
                AntarchyNeoforgeEntites.REVERIE,
                AntarchyNeoforgeEntites.TRIFFID,
                AntarchyNeoforgeEntites.BRUTALFLY,
                AntarchyNeoforgeEntites.BRUTALFLY_ORB,
                AntarchyNeoforgeEntites.HUSH_PROJECTILE,
                AntarchyNeoforgeEntites.TORETERROR,
                AntarchyNeoforgeEntites.WATER_BOMB,
                AntarchyNeoforgeEntites.CREEPING_HORROR,
                AntarchyNeoforgeEntites.LURKING_TERROR,
                AntarchyNeoforgeEntites.STINK_BUG,
                AntarchyNeoforgeEntites.CHEEP,
                AntarchyNeoforgeEntites.DORRIE,
                () -> AntarchyNeoforgeBlocks.DUPLICATOR_LOG.get(),
                () -> AntarchyNeoforgeBlocks.DUPLICATOR_SAPLING.get(),
                () -> AntarchyNeoforgeBlocks.DUCT_TAPE.get(),
                () -> AntarchyNeoforgeBlocks.INFESTED_ROOTED_DIRT.get(),
                () -> AntarchyNeoforgeBlocks.INFESTED_COARSE_DIRT.get(),
                () -> AntarchyNeoforgeBlocks.NYXITE.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE.get(),
                () -> AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_BRICKS.get(),
                () -> AntarchyNeoforgeBlocks.CHISELED_SHELLSTONE.get(),
                () -> AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICKS.get(),
                () -> AntarchyNeoforgeBlocks.CRACKED_SHELLSTONE_BRICKS.get(),
                () -> AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICK_STAIRS.get(),
                () -> AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICK_SLAB.get(),
                () -> AntarchyNeoforgeBlocks.MOSSY_SHELLSTONE_BRICK_WALL.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_STAIRS.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_SLAB.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_WALL.get(),
                () -> AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE_STAIRS.get(),
                () -> AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE_SLAB.get(),
                () -> AntarchyNeoforgeBlocks.POLISHED_SHELLSTONE_WALL.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_BRICK_STAIRS.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_BRICK_SLAB.get(),
                () -> AntarchyNeoforgeBlocks.SHELLSTONE_BRICK_WALL.get(),
                () -> AntarchyNeoforgeBlocks.CLOUD_BLOCK.get(),
                () -> AntarchyNeoforgeBlocks.TRIFFID_GOO_BLOCK.get(),
                () -> AntarchyNeoforgeBlocks.PALE_NYXITE.get(),
                () -> AntarchyNeoforgeBlocks.NYXITE_SPIKE.get(),
                () -> AntarchyNeoforgeBlocks.CHITEN_BLOCK.get(),
                () -> AntarchyNeoforgeBlocks.CHITEN_SPIKE.get(),
                () -> AntarchyNeoforgeBlocks.POTENT_NYXITE.get(),
                () -> AntarchyNeoforgeBlocks.ANTIMETAL.get(),
                () -> AntarchyNeoforgeBlocks.POLISHED_ANTIMETAL.get(),
                () -> AntarchyNeoforgeBlocks.BUDDING_BLOOD_CRYSTAL.get(),
                () -> AntarchyNeoforgeBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyNeoforgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyNeoforgeBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyNeoforgeBlocks.BLOOD_CRYSTAL_CRYSTAL.get(),
                () -> AntarchyNeoforgeItems.OURANWOOD_ACORN.get(),
                AntarchyNeoforgeItems.KRAKEN_TOOTH,
                () -> AntarchyNeoforgeItems.MOGGLES.get(),
                () -> AntarchyNeoforgeItems.REVERIE_BOTTLE.get(),
                () -> AntarchyNeoforgeItems.STINK_BUG.get(),
                () -> AntarchyNeoforgeMisc.DREAD,
                () -> AntarchyNeoforgeMisc.PARALYZED,
                () -> AntarchyNeoforgeMisc.INVERTED,
                () -> AntarchyNeoforgeMisc.STINKY,
                () -> AntarchyNeoforgeBlocks.OURANWOOD_ACORN_BLOCK.get(),
                () -> AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_LOG.get(),
                () -> AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyNeoforgeBlocks.UMBRAL_MOSS_BLOCK.get(),
                () -> AntarchyNeoforgeBlocks.UMBRAL_MOSS_CARPET.get(),
                () -> AntarchyNeoforgeBlocks.BLUSH_MOSS_BLOCK.get(),
                () -> AntarchyNeoforgeBlocks.BLUSH_MOSS_CARPET.get(),
                () -> AntarchyNeoforgeBlocks.ORANGE_MILKWEED.get(),
                () -> AntarchyNeoforgeBlocks.PINK_MILKWEED.get(),
                () -> AntarchyNeoforgeBlocks.BED_BUG_EGG.get(),
                () -> AntarchyNeoforgeBlocks.CREEPING_HORROR_EGG.get(),
                () -> AntarchyNeoforgeBlocks.LURKING_TERROR_EGG.get(),
                () -> AntarchyNeoforgeBlocks.WASP_NEST.get(),
                () -> AntarchyNeoforgeBlocks.HUSHWEED.get(),
                () -> AntarchyNeoforgeBlocks.OURANWOOD_SQUIRREL_NEST.get(),
                () -> AntarchyNeoforgeBlocks.BRUTALFLY_COCOON_SPAWNER.get(),
                () -> AntarchyNeoforgeBlocks.GIANT_LILY_PAD.get(),
                () -> AntarchyNeoforgeBlocks.SEASHELL.get(),
                () -> AntarchyNeoforgeBlocks.ANT_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.WASP_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.HUSHWEED_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.SEASHELL_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeMisc.STINKY_GAS.get(),
                () -> AntarchyNeoforgeMisc.STINKY_FLY.get(),
                () -> AntarchyNeoforgeMisc.PEACH_LEAVES_PARTICLE.get(),
                () -> AntarchyNeoforgeMisc.DOUBLE_DAMAGE_CHANCE,
                () -> AntarchyNeoforgeMisc.BLOODGLASS_MAX_HEARTS,
                () -> AntarchyNeoforgeMisc.BLOODGLASS_WARD
        );
        AntarchyObjects.setOctopusBomb(AntarchyNeoforgeEntites.OCTOPUS_BOMB);
        AntarchyObjects.setTentacle(AntarchyNeoforgeEntites.TENTACLE);
        AntarchyObjects.setKrakensGraspTrident(AntarchyNeoforgeEntites.KRAKENS_GRASP_TRIDENT);
        AntarchyObjects.setLotus(() -> AntarchyNeoforgeBlocks.LOTUS.get());
        AntarchyObjects.setKrakenTentacle(() -> AntarchyNeoforgeItems.KRAKEN_TENTACLE.get());
        AntarchyObjects.setKrakensGrasp(() -> AntarchyNeoforgeItems.KRAKENS_GRASP.get());
        AntarchyObjects.setOuranwoodDeer(AntarchyNeoforgeEntites.OURANWOOD_DEER);
        AntarchyObjects.setGlimmer(AntarchyNeoforgeEntites.GLIMMER);
        AntarchyObjects.setSpiritApple(() -> AntarchyNeoforgeItems.SPIRIT_APPLE.get());
        AntarchyObjects.setElka(AntarchyNeoforgeEntites.ELKA);
        AntarchyObjects.setPeach(() -> AntarchyNeoforgeItems.PEACH.get());
        AntarchyObjects.setCorn(() -> AntarchyNeoforgeItems.CORN.get());
        AntarchyObjects.setCornSeeds(() -> AntarchyNeoforgeItems.CORN_SEEDS.get());
        AntarchyObjects.setStarCoralBlock(() -> AntarchyNeoforgeBlocks.STAR_CORAL_BLOCK.get());
        AntarchyObjects.setStarCoral(() -> AntarchyNeoforgeBlocks.STAR_CORAL.get());
        AntarchyObjects.setStarCoralFan(() -> AntarchyNeoforgeBlocks.STAR_CORAL_FAN.get());
        AntarchyObjects.setCookedCorndog(() -> AntarchyNeoforgeItems.COOKED_CORNDOG.get());
        AntarchyObjects.setGlimmerBottle(() -> AntarchyNeoforgeItems.GLIMMER_BOTTLE.get());
        AntarchyObjects.setGlimmerVariantComponent(() -> AntarchyNeoforgeMisc.GLIMMER_VARIANT.get());
        AntarchyObjects.setAmericanComponent(() -> AntarchyNeoforgeMisc.AMERICAN.get());
        AntarchyObjects.setLumen(() -> AntarchyNeoforgeMisc.LUMEN.get());
        AntarchyObjects.setFlowingLumen(() -> AntarchyNeoforgeMisc.FLOWING_LUMEN.get());
        AntarchyObjects.setLumenBucket(() -> AntarchyNeoforgeItems.LUMEN_BUCKET.get());
        AntarchyObjects.setLumenBlock(() -> AntarchyNeoforgeBlocks.LUMEN_BLOCK.get());
        AntarchyObjects.setLumenFroglight(() -> AntarchyNeoforgeBlocks.LUMEN_FROGLIGHT.get());
        AntarchyObjects.setPeachLeavesParticle(() -> AntarchyNeoforgeMisc.PEACH_LEAVES_PARTICLE.get());
        AntarchyObjects.setLotusPollen(() -> AntarchyNeoforgeMisc.LOTUS_POLLEN.get());
        AntarchyObjects.setDorrieInventoryMenu(AntarchyNeoforgeMisc.DORRIE_INVENTORY_MENU);
        AntarchyObjects.setCritterCage(() -> AntarchyNeoforgeItems.CRITTER_CAGE.get());
        AntarchyObjects.setCritterCageBlock(() -> AntarchyNeoforgeBlocks.CRITTER_CAGE_BLOCK.get());
        AntarchyObjects.setCritterCageBlockEntity(() -> AntarchyNeoforgeBlocks.CRITTER_CAGE_BLOCK_ENTITY.get());
        AntarchyObjects.setCritterCageProjectile(() -> AntarchyNeoforgeEntites.CRITTER_CAGE_PROJECTILE.get());
        AntarchyObjects.setCritterCageEntityTypeComponent(() -> AntarchyNeoforgeMisc.CRITTER_CAGE_ENTITY_TYPE_COMPONENT.get());
        AntarchyObjects.setCritterCagePrimaryColorComponent(() -> AntarchyNeoforgeMisc.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.get());
        AntarchyObjects.setCritterCageSecondaryColorComponent(() -> AntarchyNeoforgeMisc.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.get());
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
