package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AntarchyNeoforgeSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Antarchy.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SQUIDZOOKA_FIRE = registerSoundEvent("squidzooka_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHRINK_RAY_SOUND = registerSoundEvent("shrink_ray");
    public static final DeferredHolder<SoundEvent, SoundEvent> GROWTH_RAY_SOUND = registerSoundEvent("growth_ray");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIZE_RAY_CHARGE = registerSoundEvent("size_ray_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_AMBIENT = registerSoundEvent("ant_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_IDLE = registerSoundEvent("ant_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_HURT = registerSoundEvent("ant_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_BITE = registerSoundEvent("ant_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_GATHER = registerSoundEvent("ant_gather");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_NEST = registerSoundEvent("ant_nest");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_BITE = registerSoundEvent("cloud_shark_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_IDLE = registerSoundEvent("cloud_shark_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_HURT = registerSoundEvent("cloud_shark_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_DEATH = registerSoundEvent("cloud_shark_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_FLY = registerSoundEvent("cloud_shark_fly");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERPILLAR_IDLE = registerSoundEvent("caterpillar_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERPILLAR_HURT = registerSoundEvent("caterpillar_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERPILLAR_CRAWL = registerSoundEvent("caterpillar_crawl");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTERFLY_HURT = registerSoundEvent("butterfly_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRUTALFLY_IDLE = registerSoundEvent("brutalfly_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRUTALFLY_DEATH = registerSoundEvent("brutalfly_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELYTHIA_FIREFLY_AMBIENT = registerSoundEvent("ambient.elythia.firefly");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_AMBIENT = registerSoundEvent("missile_squid_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_HURT = registerSoundEvent("missile_squid_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_DEATH = registerSoundEvent("missile_squid_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_ATTACK = registerSoundEvent("missile_squid_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_FLYING_LOOP = registerSoundEvent("kraken_flying_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_FLYING_SIDEWAYS_LOOP = registerSoundEvent("kraken_flying_sideways_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_ATTACK = registerSoundEvent("kraken_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SPIN = registerSoundEvent("kraken_spin");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_ROAR = registerSoundEvent("kraken_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SUMMON = registerSoundEvent("kraken_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_HURT = registerSoundEvent("kraken_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_DEATH = registerSoundEvent("kraken_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_IDLE_LOOP = registerSoundEvent("basilisk_idle_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_SLITHER_LOOP = registerSoundEvent("basilisk_slither_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_BITE = registerSoundEvent("basilisk_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_HISS = registerSoundEvent("basilisk_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_HURT = registerSoundEvent("basilisk_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_DEATH = registerSoundEvent("basilisk_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_NIGHTMARE_WASTES_AMBIENT = registerSoundEvent("thoraxis_nightmare_wastes_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_NIGHTMARE_WASTES_ADDITIONS = registerSoundEvent("thoraxis_nightmare_wastes_additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_NIGHTMARE_WASTES_MOOD = registerSoundEvent("thoraxis_nightmare_wastes_mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_DREAM_DUNES_AMBIENT = registerSoundEvent("thoraxis_dream_dunes_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_DREAM_DUNES_ADDITIONS = registerSoundEvent("thoraxis_dream_dunes_additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_DREAM_DUNES_MOOD = registerSoundEvent("thoraxis_dream_dunes_mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_LUCID_POOLS_AMBIENT = registerSoundEvent("thoraxis_lucid_pools_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_LUCID_POOLS_ADDITIONS = registerSoundEvent("thoraxis_lucid_pools_additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_LUCID_POOLS_MOOD = registerSoundEvent("thoraxis_lucid_pools_mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_AMBIENT = registerSoundEvent("lucid_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_FLYING = registerSoundEvent("lucid_flying");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_ATTACK = registerSoundEvent("lucid_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_BOLT_SOUND = registerSoundEvent("lucid_bolt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_HURT = registerSoundEvent("lucid_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_DEATH = registerSoundEvent("lucid_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_IDLE = registerSoundEvent("reverie_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_HURT = registerSoundEvent("reverie_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_WORRY = registerSoundEvent("reverie_worry");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_SAVE = registerSoundEvent("reverie_save");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_JOIN_PLAYER = registerSoundEvent("reverie_join_player");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_ALERT = registerSoundEvent("reverie_alert");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_IDLE = registerSoundEvent("flying_squirrel_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_BEG = registerSoundEvent("flying_squirrel_beg");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_NUT = registerSoundEvent("flying_squirrel_nut");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_GLIDE_LOOP = registerSoundEvent("flying_squirrel_glide_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_HURT = registerSoundEvent("flying_squirrel_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_DEATH = registerSoundEvent("flying_squirrel_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_IDLE = registerSoundEvent("nightmare_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_HURT = registerSoundEvent("nightmare_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_ROAR = registerSoundEvent("nightmare_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_DEATH = registerSoundEvent("nightmare_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_BITE = registerSoundEvent("nightmare_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_FLAP = registerSoundEvent("nightmare_flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_ATTACK = registerSoundEvent("triffid_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_GRAB = registerSoundEvent("triffid_grab");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_HURT = registerSoundEvent("triffid_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_DEATH = registerSoundEvent("triffid_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_HISS = registerSoundEvent("triffid_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_GROWL = registerSoundEvent("triffid_growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_AMBIENT = registerSoundEvent("mantis_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_HURT = registerSoundEvent("mantis_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_ATTACK = registerSoundEvent("mantis_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_FLY_LOOP = registerSoundEvent("mantis_fly_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_PICKUP = registerSoundEvent("gravity_gun_pickup");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_DROP = registerSoundEvent("gravity_gun_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_HOLD_LOOP = registerSoundEvent("gravity_gun_hold_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_LAUNCH = registerSoundEvent("gravity_gun_launch");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_DRYFIRE = registerSoundEvent("gravity_gun_dryfire");
    public static final DeferredHolder<SoundEvent, SoundEvent> BED_BUG_AMBIENT = registerSoundEvent("bed_bug_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BED_BUG_HURT = registerSoundEvent("bed_bug_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BED_BUG_ATTACK = registerSoundEvent("bed_bug_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_IDLE = registerSoundEvent("wasp_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_HURT = registerSoundEvent("wasp_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_ATTACK = registerSoundEvent("wasp_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_DEATH = registerSoundEvent("wasp_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_AMBIENT = registerSoundEvent("scorpion_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_HURT = registerSoundEvent("scorpion_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_ATTACK = registerSoundEvent("scorpion_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_AMBIENT = registerSoundEvent("emperor_scorpion_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_HURT = registerSoundEvent("emperor_scorpion_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_ATTACK = registerSoundEvent("emperor_scorpion_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_ROAR = registerSoundEvent("emperor_scorpion_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_AMBIENT = registerSoundEvent("moleworm_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_HURT = registerSoundEvent("moleworm_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_ATTACK = registerSoundEvent("moleworm_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_DIG = registerSoundEvent("moleworm_dig");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_AMBIENT = registerSoundEvent("molevore_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_HURT = registerSoundEvent("molevore_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_ATTACK = registerSoundEvent("molevore_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_DIG = registerSoundEvent("molevore_dig");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCT_TAPE_USE = registerSoundEvent("duct_tape_use");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_HYPNOTIC_GAS = registerSoundEvent("potent_nyxite_hypnotic_gas");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_ERUPTION_START = registerSoundEvent("potent_nyxite_geyser_eruption_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_ERUPTION_ACTIVE = registerSoundEvent("potent_nyxite_geyser_eruption_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_CONTINUOUS_START = registerSoundEvent("potent_nyxite_geyser_continuous_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_CONTINUOUS_ACTIVE = registerSoundEvent("potent_nyxite_geyser_continuous_active");

    private AntarchyNeoforgeSounds() {}

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String path) {
        return SOUND_EVENTS.register(path,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path)));
    }
}
