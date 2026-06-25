package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipTetherSync;
import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.neoforge.registry.*;
import com.craisinlord.antarchy.neoforge.network.AntarchyGravityNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;


@Mod(Antarchy.MODID)
public class AntarchyNeoforge {
    public static IEventBus modEventBusTempHolder = null;

    public AntarchyNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBusTempHolder = modEventBus;
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
                AntarchyNeoforgeSounds.DUCT_TAPE_USE
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
        if (isModLoaded("infinity")) {
            InfinityCompat.bind(new NeoForgeInfinityCompat());
        }
        AntarchyNeoForgeEvents.register(modEventBus);
        AntarchyNeoforgeSounds.register(modEventBus);
        AntarchyNeoforgeEntites.register(modEventBus);
        AntarchyNeoforgeBlocks.register(modEventBus);
        AntarchyNeoforgeItems.register(modEventBus);
        AntarchyNeoforgeMisc.register(modEventBus);
        AntarchyNeoforgeSpawnPlacements.register(modEventBus);
        AntarchyNeoforgeCreativeModeTabs.register(modEventBus);
        AntarchyNeoforgeEntityAttributes.register(modEventBus);
        AntarchyNeoforgePayloadHandlers.register(modEventBus);
        Antarchy.init();
    }

    private static void bindCommonObjects() {
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
                () -> AntarchyNeoforgeBlocks.ORANGE_MILKWEED.get(),
                () -> AntarchyNeoforgeBlocks.PINK_MILKWEED.get(),
                () -> AntarchyNeoforgeBlocks.TORCHFLOWER_BUSH.get(),
                () -> AntarchyNeoforgeBlocks.BED_BUG_EGG.get(),
                () -> AntarchyNeoforgeBlocks.CREEPING_HORROR_EGG.get(),
                () -> AntarchyNeoforgeBlocks.LURKING_TERROR_EGG.get(),
                () -> AntarchyNeoforgeBlocks.WASP_NEST.get(),
                () -> AntarchyNeoforgeBlocks.HUSHWEED.get(),
                () -> AntarchyNeoforgeBlocks.OURANWOOD_SQUIRREL_NEST.get(),
                () -> AntarchyNeoforgeBlocks.ANT_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.WASP_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeBlocks.HUSHWEED_BLOCK_ENTITY.get(),
                () -> AntarchyNeoforgeMisc.STINKY_GAS.get(),
                () -> AntarchyNeoforgeMisc.DOUBLE_DAMAGE_CHANCE,
                () -> AntarchyNeoforgeMisc.BLOODGLASS_MAX_HEARTS,
                () -> AntarchyNeoforgeMisc.BLOODGLASS_WARD
        );
    }

    private static boolean isModLoaded(String modId) {
        try {
            return ModList.get().isLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
