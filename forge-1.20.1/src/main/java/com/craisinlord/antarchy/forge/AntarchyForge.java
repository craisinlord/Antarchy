package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatVersion;
import com.craisinlord.antarchy.content.AntarchyGameRules;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancementTriggers;
import com.craisinlord.antarchy.content.effect.AntarchySizeScaling;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkBridge;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;
import com.craisinlord.antarchy.forge.registry.*;
import com.craisinlord.anthology.effect.AnthologyEffects;
import net.minecraft.world.level.GameRules;
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
        AntarchyAdvancementTriggers.register();
        bootstrapGameRules();
        AntarchyConfigModuleForge.init();
        AntarchySoundEvents.bind(
                AntarchyForgeSounds.SQUIDZOOKA_FIRE,
                AntarchyForgeSounds.SHRINK_RAY_CHARGE,
                AntarchyForgeSounds.SHRINK_RAY_LOOP,
                AntarchyForgeSounds.GROWTH_RAY_CHARGE,
                AntarchyForgeSounds.GROWTH_RAY_LOOP,
                AntarchyForgeSounds.SIZE_RAY_WEAKSHOT,
                AntarchyForgeSounds.SIZE_RAY_MEDIUMSHOT,
                AntarchyForgeSounds.SIZE_RAY_CRITSHOT,
                AntarchyForgeSounds.ANT_AMBIENT,
                AntarchyForgeSounds.ANT_IDLE,
                AntarchyForgeSounds.ANT_HURT,
                AntarchyForgeSounds.ANT_BITE,
                AntarchyForgeSounds.ANT_GATHER,
                AntarchyForgeSounds.ANT_NEST,
                AntarchyForgeSounds.STRATOSHARK_BITE,
                AntarchyForgeSounds.STRATOSHARK_IDLE,
                AntarchyForgeSounds.STRATOSHARK_HURT,
                AntarchyForgeSounds.STRATOSHARK_DEATH,
                AntarchyForgeSounds.STRATOSHARK_FLY,
                AntarchyForgeSounds.CATERPILLAR_IDLE,
                AntarchyForgeSounds.CATERPILLAR_HURT,
                AntarchyForgeSounds.CATERPILLAR_CRAWL,
                AntarchyForgeSounds.BUTTERFLY_HURT,
                AntarchyForgeSounds.BRUTALFLY_IDLE,
                AntarchyForgeSounds.BRUTALFLY_DEATH,
                AntarchyForgeSounds.ELYTHIA_FIREFLY_AMBIENT,
                AntarchyForgeSounds.ELYTHIA_OURANWOOD_FOREST_AMBIENT,
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
                AntarchyForgeSounds.FLYTRAP_ATTACK,
                AntarchyForgeSounds.FLYTRAP_GRAB,
                AntarchyForgeSounds.FLYTRAP_HURT,
                AntarchyForgeSounds.FLYTRAP_DEATH,
                AntarchyForgeSounds.FLYTRAP_HISS,
                AntarchyForgeSounds.FLYTRAP_GROWL,
                AntarchyForgeSounds.MANTIS_AMBIENT,
                AntarchyForgeSounds.MANTIS_HURT,
                AntarchyForgeSounds.MANTIS_ATTACK,
                AntarchyForgeSounds.MANTIS_FLY_LOOP,
                AntarchyForgeSounds.MANTICORE_IDLE,
                AntarchyForgeSounds.MANTICORE_WALK,
                AntarchyForgeSounds.MANTICORE_FLY,
                AntarchyForgeSounds.MANTICORE_BITE,
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
                AntarchyForgeSounds.CRAWLING_BLIGHT_GROWL,
                AntarchyForgeSounds.CRAWLING_BLIGHT_HURT,
                AntarchyForgeSounds.CRAWLING_BLIGHT_BITE,
                AntarchyForgeSounds.SKULKING_FRIGHT_SNARL,
                AntarchyForgeSounds.SKULKING_FRIGHT_HURT,
                AntarchyForgeSounds.SKULKING_FRIGHT_BITE,
                AntarchyForgeSounds.SKULKING_FRIGHT_FLY_LOOP,
                AntarchyForgeSounds.DUCT_TAPE_USE,
                AntarchyForgeSounds.HERCULES_BEETLE_IDLE,
                AntarchyForgeSounds.HERCULES_BEETLE_HURT,
                AntarchyForgeSounds.HERCULES_BEETLE_ATTACK,
                AntarchyForgeSounds.HERCULES_BEETLE_CRY,
                AntarchyForgeSounds.HERCULES_BEETLE_CHARGE_START,
                AntarchyForgeSounds.HERCULES_BEETLE_KNOCKED_DOWN,
                AntarchyForgeSounds.SPRINGBUG_IDLE,
                AntarchyForgeSounds.SPIT_BUG_IDLE,
                AntarchyForgeSounds.SPRINGBUG_HURT,
                AntarchyForgeSounds.SPRINGBUG_JUMP,
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
                AntarchyForgeSounds.JERRY_ADULT_ATTACK,
                AntarchyForgeSounds.WORM_HOOK_FLYING,
                AntarchyForgeSounds.BLOOD_CRYSTAL_KATANA_DASH,
                AntarchyForgeSounds.BLOOD_CRYSTAL_ARMOR_EQUIP,
                AntarchyForgeSounds.BLOODGLASS_WARD_HEART_BREAK,
                AntarchyForgeSounds.BLOODGLASS_WARD_HEART_REGEN,
                AntarchyForgeSounds.KING_IDLE,
                AntarchyForgeSounds.KING_HURT,
                AntarchyForgeSounds.KING_DEATH,
                AntarchyForgeSounds.KING_BITE,
                AntarchyForgeSounds.QUEEN_IDLE,
                AntarchyForgeSounds.QUEEN_HURT,
                AntarchyForgeSounds.QUEEN_DEATH,
                AntarchyForgeSounds.QUEEN_BITE,
                AntarchyForgeSounds.QUEEN_ROAR,
                AntarchyForgeSounds.KING_BEAM_SHOOT,
                AntarchyForgeSounds.KING_FIREBALL_SHOOT,
                AntarchyForgeSounds.KING_ICE_SPIKES,
                AntarchyForgeSounds.KING_DECREE_CAST,
                AntarchyForgeSounds.QUEEN_BEAM_SHOOT,
                AntarchyForgeSounds.QUEEN_BEAM_START,
                AntarchyForgeSounds.QUEEN_BEAM_LOOP,
                AntarchyForgeSounds.QUEEN_BEAM_END,
                AntarchyForgeSounds.KING_STOMP,
                AntarchyForgeSounds.KING_ROAR,
                AntarchyForgeSounds.KING_WING_FLAP,
                AntarchyForgeSounds.KING_FLY_LOOP,
                AntarchyForgeSounds.QUEEN_FLY_LOOP,
                AntarchyForgeSounds.PRINCE_IDLE,
                AntarchyForgeSounds.PRINCE_BITE,
                AntarchyForgeSounds.PRINCE_SHOOT,
                AntarchyForgeSounds.PRINCE_FLY,
                AntarchyForgeSounds.PRINCE_STEP,
                AntarchyForgeSounds.PRINCESS_IDLE,
                AntarchyForgeSounds.PRINCESS_BITE,
                AntarchyForgeSounds.PRINCESS_SHOOT,
                AntarchyForgeSounds.PRINCESS_FLY,
                AntarchyForgeSounds.PRINCESS_STEP,
                AntarchyForgeSounds.KING_DECREE,
                AntarchyForgeSounds.KING_SUCCESS,
                AntarchyForgeSounds.KING_JUDGEMENT
        );
        AntarchySoundEvents.bindDimensionalTearSounds(
                AntarchyForgeSounds.DIMENSIONAL_TEAR_OPEN,
                AntarchyForgeSounds.DIMENSIONAL_TEAR_IDLE,
                AntarchyForgeSounds.DIMENSIONAL_TEAR_CREATURE_LEAVES,
                AntarchyForgeSounds.DIMENSIONAL_TEAR_CLOSE
        );
        AntarchySoundEvents.bindHoverboardSounds(AntarchyForgeSounds.HOVERBOARD_MOUNT, AntarchyForgeSounds.HOVERBOARD_DISMOUNT, AntarchyForgeSounds.HOVERBOARD_IDLE);
        AntarchySoundEvents.bindThrowbackSounds(AntarchyForgeSounds.MUSIC_THROWBACK, AntarchyForgeSounds.PLAYER_HURT_CLASSIC);
        bindCommonObjects();
        if (isSupportedInfinityLoaded()) {
            InfinityCompat.bind(new ForgeInfinityCompat());
        }
        AntarchyForgeEvents.register(modEventBus);
        AntarchyForgeSounds.register(modEventBus);
        AntarchyForgeEntities.register(modEventBus);
        AntarchyForgeBlocks.register(modEventBus);
        AntarchyForgeItems.register(modEventBus);
        AntarchyForgeFluidTypes.register(modEventBus);
        AntarchyForgeMisc.register(modEventBus);
        AntarchyForgeSpawnPlacements.register(modEventBus);
        AntarchyForgeCreativeModeTabs.register(modEventBus);
        AntarchyForgeEntityAttributes.register(modEventBus);
        AntarchyForgePayloadHandlers.register();
        AntarchyNetwork.bindServer(new AntarchyForgeNetworkBridge());
        AntarchyNetwork.bindClient(AntarchyForgeNetworkCore::sendToServer);
        Antarchy.init();
    }

    private static void bootstrapGameRules() {
        AntarchyGameRules.bootstrap((name, category, defaultValue) -> {
            GameRules.Type<GameRules.BooleanValue> type =
                    GameRules.BooleanValue.create(defaultValue);
            return GameRules.register(name, category, type);
        });
    }

    private static void bindCommonObjects() {
        UpwardFallingBlockEntity.TYPE = AntarchyForgeEntities.UPWARD_FALLING_BLOCK;
        PermanentPortalType.bindBlocks(
                () -> AntarchyForgeBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyForgeBlocks.ELYTHIA_PORTAL.get(),
                () -> AntarchyForgeBlocks.NYXITE.get(),
                () -> AntarchyForgeBlocks.THORAXIS_PORTAL.get(),
                () -> AntarchyForgeBlocks.MYRMITE.get(),
                () -> AntarchyForgeBlocks.CAVARYN_PORTAL.get()
        );
        AntarchyObjects.setDread(() -> AntarchyForgeMisc.DREAD.get());
        AntarchyObjects.setParalyzedEffect(() -> AntarchyForgeMisc.PARALYZED.get());
        AntarchyObjects.setInvertedEffect(AnthologyEffects::inverted);
        AntarchyObjects.setStinkyEffect(() -> AntarchyForgeMisc.STINKY.get());
        AntarchyObjects.setDoubleDamageChance(() -> AntarchyForgeMisc.attributeHolder(AntarchyForgeMisc.DOUBLE_DAMAGE_CHANCE));
        AntarchyObjects.setBloodglassMaxHearts(() -> AntarchyForgeMisc.attributeHolder(AntarchyForgeMisc.BLOODGLASS_MAX_HEARTS));
        AntarchyObjects.setBloodglassWard(() -> AntarchyForgeMisc.BLOODGLASS_WARD.get());
        AntarchyEnchantments.bind(() -> AntarchyForgeMisc.FEATHER_RISING.get(), () -> AntarchyForgeMisc.CHRONOSPHERE.get());
        AntarchySizeScaling.register(AntarchyForgeMisc.SHRINKING_EFFECT, AntarchyForgeMisc.GROWTH_EFFECT);
        AntarchyObjects.setGlimmeringEffect(() -> AntarchyForgeMisc.GLIMMERING.get());
        AntarchyObjects.setRemembranceEffect(() -> AntarchyForgeMisc.REMEMBRANCE.get());
        AntarchyObjects.setRoyalVitalityPenaltyEffect(() -> AntarchyForgeMisc.ROYAL_VITALITY_PENALTY.get());
        AntarchyObjects.setBleedingEffect(() -> AntarchyForgeMisc.BLEEDING.get());
        AntarchyObjects.setBloodMoonEffect(() -> AntarchyForgeMisc.BLOOD_MOON.get());
        AntarchyObjects.setSaucedEffect(() -> AntarchyForgeMisc.SAUCED.get());
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
