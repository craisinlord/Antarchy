package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.anthology.effect.AnthologyEffects;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyGameRules;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancementTriggers;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.neoforge.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;


@Mod(Antarchy.MODID)
public class AntarchyNeoforge {
    public static IEventBus modEventBusTempHolder = null;

    public AntarchyNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBusTempHolder = modEventBus;
        modEventBus.addListener(AntarchyNeoforge::registerAdvancementTriggers);
        modEventBus.addListener(AntarchyNeoforge::registerGameRules);
        AntarchyConfigModuleNeoforge.init(modContainer);
        AntarchySoundEvents.bind(
                AntarchyNeoforgeSounds.SQUIDZOOKA_FIRE,
                AntarchyNeoforgeSounds.SHRINK_RAY_CHARGE,
                AntarchyNeoforgeSounds.SHRINK_RAY_LOOP,
                AntarchyNeoforgeSounds.GROWTH_RAY_CHARGE,
                AntarchyNeoforgeSounds.GROWTH_RAY_LOOP,
                AntarchyNeoforgeSounds.SIZE_RAY_WEAKSHOT,
                AntarchyNeoforgeSounds.SIZE_RAY_MEDIUMSHOT,
                AntarchyNeoforgeSounds.SIZE_RAY_CRITSHOT,
                AntarchyNeoforgeSounds.ANT_AMBIENT,
                AntarchyNeoforgeSounds.ANT_IDLE,
                AntarchyNeoforgeSounds.ANT_HURT,
                AntarchyNeoforgeSounds.ANT_BITE,
                AntarchyNeoforgeSounds.ANT_GATHER,
                AntarchyNeoforgeSounds.ANT_NEST,
                AntarchyNeoforgeSounds.STRATOSHARK_BITE,
                AntarchyNeoforgeSounds.STRATOSHARK_IDLE,
                AntarchyNeoforgeSounds.STRATOSHARK_HURT,
                AntarchyNeoforgeSounds.STRATOSHARK_DEATH,
                AntarchyNeoforgeSounds.STRATOSHARK_FLY,
                AntarchyNeoforgeSounds.CATERPILLAR_IDLE,
                AntarchyNeoforgeSounds.CATERPILLAR_HURT,
                AntarchyNeoforgeSounds.CATERPILLAR_CRAWL,
                AntarchyNeoforgeSounds.BUTTERFLY_HURT,
                AntarchyNeoforgeSounds.BRUTALFLY_IDLE,
                AntarchyNeoforgeSounds.BRUTALFLY_DEATH,
                AntarchyNeoforgeSounds.ELYTHIA_FIREFLY_AMBIENT,
                AntarchyNeoforgeSounds.ELYTHIA_OURANWOOD_FOREST_AMBIENT,
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
                AntarchyNeoforgeSounds.FLYTRAP_ATTACK,
                AntarchyNeoforgeSounds.FLYTRAP_GRAB,
                AntarchyNeoforgeSounds.FLYTRAP_HURT,
                AntarchyNeoforgeSounds.FLYTRAP_DEATH,
                AntarchyNeoforgeSounds.FLYTRAP_HISS,
                AntarchyNeoforgeSounds.FLYTRAP_GROWL,
                AntarchyNeoforgeSounds.MANTIS_AMBIENT,
                AntarchyNeoforgeSounds.MANTIS_HURT,
                AntarchyNeoforgeSounds.MANTIS_ATTACK,
                AntarchyNeoforgeSounds.MANTIS_FLY_LOOP,
                AntarchyNeoforgeSounds.MANTICORE_IDLE,
                AntarchyNeoforgeSounds.MANTICORE_WALK,
                AntarchyNeoforgeSounds.MANTICORE_FLY,
                AntarchyNeoforgeSounds.MANTICORE_BITE,
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
                AntarchyNeoforgeSounds.CRAWLING_BLIGHT_GROWL,
                AntarchyNeoforgeSounds.CRAWLING_BLIGHT_HURT,
                AntarchyNeoforgeSounds.CRAWLING_BLIGHT_BITE,
                AntarchyNeoforgeSounds.SKULKING_FRIGHT_SNARL,
                AntarchyNeoforgeSounds.SKULKING_FRIGHT_HURT,
                AntarchyNeoforgeSounds.SKULKING_FRIGHT_BITE,
                AntarchyNeoforgeSounds.SKULKING_FRIGHT_FLY_LOOP,
                AntarchyNeoforgeSounds.DUCT_TAPE_USE,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_IDLE,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_HURT,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_ATTACK,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_CRY,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_CHARGE_START,
                AntarchyNeoforgeSounds.HERCULES_BEETLE_KNOCKED_DOWN,
                AntarchyNeoforgeSounds.SPRINGBUG_IDLE,
                AntarchyNeoforgeSounds.SPIT_BUG_IDLE,
                AntarchyNeoforgeSounds.SPRINGBUG_HURT,
                AntarchyNeoforgeSounds.SPRINGBUG_JUMP,
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
                AntarchyNeoforgeSounds.JERRY_ADULT_ATTACK,
                AntarchyNeoforgeSounds.WORM_HOOK_FLYING,
                AntarchyNeoforgeSounds.BLOOD_CRYSTAL_KATANA_DASH,
                AntarchyNeoforgeSounds.BLOOD_CRYSTAL_ARMOR_EQUIP,
                AntarchyNeoforgeSounds.BLOODGLASS_WARD_HEART_BREAK,
                AntarchyNeoforgeSounds.BLOODGLASS_WARD_HEART_REGEN,
                AntarchyNeoforgeSounds.KING_IDLE,
                AntarchyNeoforgeSounds.KING_HURT,
                AntarchyNeoforgeSounds.KING_DEATH,
                AntarchyNeoforgeSounds.KING_BITE,
                AntarchyNeoforgeSounds.QUEEN_IDLE,
                AntarchyNeoforgeSounds.QUEEN_HURT,
                AntarchyNeoforgeSounds.QUEEN_DEATH,
                AntarchyNeoforgeSounds.QUEEN_BITE,
                AntarchyNeoforgeSounds.QUEEN_ROAR,
                AntarchyNeoforgeSounds.KING_BEAM_SHOOT,
                AntarchyNeoforgeSounds.KING_FIREBALL_SHOOT,
                AntarchyNeoforgeSounds.KING_ICE_SPIKES,
                AntarchyNeoforgeSounds.KING_DECREE_CAST,
                AntarchyNeoforgeSounds.QUEEN_BEAM_SHOOT,
                AntarchyNeoforgeSounds.QUEEN_BEAM_START,
                AntarchyNeoforgeSounds.QUEEN_BEAM_LOOP,
                AntarchyNeoforgeSounds.QUEEN_BEAM_END,
                AntarchyNeoforgeSounds.KING_STOMP,
                AntarchyNeoforgeSounds.KING_ROAR,
                AntarchyNeoforgeSounds.KING_WING_FLAP,
                AntarchyNeoforgeSounds.KING_FLY_LOOP,
                AntarchyNeoforgeSounds.QUEEN_FLY_LOOP,
                AntarchyNeoforgeSounds.PRINCE_IDLE,
                AntarchyNeoforgeSounds.PRINCE_BITE,
                AntarchyNeoforgeSounds.PRINCE_SHOOT,
                AntarchyNeoforgeSounds.PRINCE_FLY,
                AntarchyNeoforgeSounds.PRINCE_STEP,
                AntarchyNeoforgeSounds.PRINCESS_IDLE,
                AntarchyNeoforgeSounds.PRINCESS_BITE,
                AntarchyNeoforgeSounds.PRINCESS_SHOOT,
                AntarchyNeoforgeSounds.PRINCESS_FLY,
                AntarchyNeoforgeSounds.PRINCESS_STEP,
                AntarchyNeoforgeSounds.KING_DECREE,
                AntarchyNeoforgeSounds.KING_SUCCESS,
                AntarchyNeoforgeSounds.KING_JUDGEMENT
        );
        AntarchySoundEvents.bindDimensionalTearSounds(
                AntarchyNeoforgeSounds.DIMENSIONAL_TEAR_OPEN,
                AntarchyNeoforgeSounds.DIMENSIONAL_TEAR_IDLE,
                AntarchyNeoforgeSounds.DIMENSIONAL_TEAR_CREATURE_LEAVES,
                AntarchyNeoforgeSounds.DIMENSIONAL_TEAR_CLOSE
        );
        AntarchySoundEvents.bindHoverboardSounds(AntarchyNeoforgeSounds.HOVERBOARD_MOUNT, AntarchyNeoforgeSounds.HOVERBOARD_DISMOUNT, AntarchyNeoforgeSounds.HOVERBOARD_IDLE);
        AntarchySoundEvents.bindThrowbackSounds(AntarchyNeoforgeSounds.THROWBACK_MUSIC, AntarchyNeoforgeSounds.PLAYER_HURT_CLASSIC);
        bindCommonObjects();
        AntarchyNeoForgeAttachments.register(modEventBus);
        AntarchyNeoForgeEvents.register(modEventBus);
        AntarchyNeoforgeSounds.register(modEventBus);
        AntarchyNeoforgeEntities.register(modEventBus);
        AntarchyNeoforgeBlocks.register(modEventBus);
        AntarchyNeoforgeItems.register(modEventBus);
        AntarchyNeoForgeFluidTypes.register(modEventBus);
        AntarchyNeoforgeMisc.register(modEventBus);
        AntarchyNeoforgeSpawnPlacements.register(modEventBus);
        AntarchyNeoforgeCreativeModeTabs.register(modEventBus);
        AntarchyNeoforgeEntityAttributes.register(modEventBus);
        AntarchyNeoforgePayloadHandlers.register(modEventBus);
        AntarchyNetwork.bindServer(new AntarchyNeoforgeNetworkBridge());
        Antarchy.init();
    }

    private static void registerAdvancementTriggers(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.TRIGGER_TYPE)) {
            AntarchyAdvancementTriggers.register((id, trigger) ->
                    event.register(Registries.TRIGGER_TYPE, id, () -> trigger));
        }
    }

    private static void registerGameRules(RegisterEvent event) {
        event.register(Registries.GAME_RULE, helper -> AntarchyGameRules.bootstrap((name, rule) -> {
            helper.register(Identifier.fromNamespaceAndPath(Antarchy.MODID, name), rule);
            return rule;
        }));
    }

    private static void bindCommonObjects() {
        com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity.TYPE = AntarchyNeoforgeEntities.UPWARD_FALLING_BLOCK;
        PermanentPortalType.bindBlocks(
                () -> AntarchyNeoforgeBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyNeoforgeBlocks.ELYTHIA_PORTAL.get(),
                () -> AntarchyNeoforgeBlocks.NYXITE.get(),
                () -> AntarchyNeoforgeBlocks.THORAXIS_PORTAL.get(),
                () -> AntarchyNeoforgeBlocks.MYRMITE.get(),
                () -> AntarchyNeoforgeBlocks.CAVARYN_PORTAL.get()
        );
        AntarchyObjects.setDread(() -> AntarchyNeoforgeMisc.DREAD);
        AntarchyObjects.setParalyzedEffect(() -> AntarchyNeoforgeMisc.PARALYZED);
        AntarchyObjects.setInvertedEffect(AnthologyEffects::inverted);
        AntarchyObjects.setStinkyEffect(() -> AntarchyNeoforgeMisc.STINKY);
        AntarchyObjects.setDoubleDamageChance(() -> AntarchyNeoforgeMisc.DOUBLE_DAMAGE_CHANCE);
        AntarchyObjects.setBloodglassMaxHearts(() -> AntarchyNeoforgeMisc.BLOODGLASS_MAX_HEARTS);
        AntarchyObjects.setBloodglassWard(() -> AntarchyNeoforgeMisc.BLOODGLASS_WARD);
        AntarchyObjects.setGlimmeringEffect(() -> AntarchyNeoforgeMisc.GLIMMERING);
        AntarchyObjects.setRemembranceEffect(() -> AntarchyNeoforgeMisc.REMEMBRANCE);
        AntarchyObjects.setRoyalVitalityPenaltyEffect(() -> AntarchyNeoforgeMisc.ROYAL_VITALITY_PENALTY);
        AntarchyObjects.setBleedingEffect(() -> AntarchyNeoforgeMisc.BLEEDING);
        AntarchyObjects.setBloodMoonEffect(() -> AntarchyNeoforgeMisc.BLOOD_MOON);
        AntarchyObjects.setSaucedEffect(() -> AntarchyNeoforgeMisc.SAUCED);
    }

    private static boolean isModLoaded(String modId) {
        try {
            return ModList.get().isLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
