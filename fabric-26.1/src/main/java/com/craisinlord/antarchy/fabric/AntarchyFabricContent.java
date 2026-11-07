package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.dispenser.EyeOfTheStormDispenseBehavior;
import com.craisinlord.antarchy.content.dispenser.VortexChargeDispenseBehavior;
import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexChargeProjectileEntity;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricRegistries;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricSounds;
import com.craisinlord.antarchy.fabric.registry.DeferredHolder;
import com.craisinlord.anthology.effect.AnthologyEffects;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

public final class AntarchyFabricContent {

    public static Holder<Potion> potionHolder(DeferredHolder<Potion, ? extends Potion> potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion.get());
    }

    private static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static FlowingFluid lookupFlowingFluid(String path) {
        return (FlowingFluid) BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    public static Item lookupItem(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    public static LiquidBlock lookupLiquidBlock(String path) {
        return (LiquidBlock) BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    public static void register() {
        UpwardFallingBlockEntity.TYPE = AntarchyFabricEntities.UPWARD_FALLING_BLOCK;
        registerBrewingRecipes();
        AntarchyFabricRegistries.registerAll();
        bindCommonContent();
        AntarchyFabricEvents.register();
    }

    private static void registerBrewingRecipes() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.FADED_MEMORY.get()), potionHolder(AntarchyFabricMisc.REMEMBRANCE_POTION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.REMEMBRANCE_POTION), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_REMEMBRANCE));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.LUCID_EYE.get()), potionHolder(AntarchyFabricMisc.INVERSION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.INVERSION), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_INVERSION));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.STINK_BUG_ITEM.get()), potionHolder(AntarchyFabricMisc.STINKY_POTION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.STINKY_POTION), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_STINKY));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.BASILISK_FANG.get()), potionHolder(AntarchyFabricMisc.PARALYSIS));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.PARALYSIS), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_PARALYSIS));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.MOLEWORM_ITEM.get()), potionHolder(AntarchyFabricMisc.HASTE));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.HASTE), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.STRONG_HASTE));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.URANIUM_NUGGET.get()), potionHolder(AntarchyFabricMisc.SHRINKING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.SHRINKING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.STRONG_SHRINKING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.STRONG_SHRINKING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.EXTREME_SHRINKING));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.TITANIUM_NUGGET.get()), potionHolder(AntarchyFabricMisc.GROWING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.GROWING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.STRONG_GROWING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.STRONG_GROWING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.EXTREME_GROWING));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.KING_SCALE.get()), potionHolder(AntarchyFabricMisc.COMMAND));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.COMMAND), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_COMMAND));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.QUEEN_SCALE.get()), potionHolder(AntarchyFabricMisc.TIME_DILATION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.TIME_DILATION), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.LONG_TIME_DILATION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.TIME_DILATION), Ingredient.of(Items.FERMENTED_SPIDER_EYE), potionHolder(AntarchyFabricMisc.TIME_CONTRACTION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.TIME_CONTRACTION), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.LONG_TIME_CONTRACTION));
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.STRATOSHARK_FIN.get()), Potions.SLOW_FALLING);
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.SPRINGBUG_LEG.get()), Potions.LEAPING);
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.CORNEA_EAR.get()), Potions.NIGHT_VISION);
        });
    }

    private static void bindCommonContent() {
        DispenserBlock.registerBehavior(AntarchyFabricItems.VORTEX_CHARGE.get(), new VortexChargeDispenseBehavior());
        DispenserBlock.registerBehavior(AntarchyFabricItems.EYE_OF_THE_STORM.get(), new EyeOfTheStormDispenseBehavior());

        PermanentPortalType.bindBlocks(
                () -> AntarchyFabricBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyFabricBlocks.ELYTHIA_PORTAL.get(),
                () -> AntarchyFabricBlocks.NYXITE.get(),
                () -> AntarchyFabricBlocks.THORAXIS_PORTAL.get(),
                () -> AntarchyFabricBlocks.MYRMITE.get(),
                () -> AntarchyFabricBlocks.CAVARYN_PORTAL.get()
        );

        AntarchySoundEvents.bind(
                AntarchyFabricSounds.SQUIDZOOKA_FIRE,
                AntarchyFabricSounds.SHRINK_RAY_CHARGE,
                AntarchyFabricSounds.SHRINK_RAY_LOOP,
                AntarchyFabricSounds.GROWTH_RAY_CHARGE,
                AntarchyFabricSounds.GROWTH_RAY_LOOP,
                AntarchyFabricSounds.SIZE_RAY_WEAKSHOT,
                AntarchyFabricSounds.SIZE_RAY_MEDIUMSHOT,
                AntarchyFabricSounds.SIZE_RAY_CRITSHOT,
                AntarchyFabricSounds.ANT_AMBIENT,
                AntarchyFabricSounds.ANT_IDLE,
                AntarchyFabricSounds.ANT_HURT,
                AntarchyFabricSounds.ANT_BITE,
                AntarchyFabricSounds.ANT_GATHER,
                AntarchyFabricSounds.ANT_NEST,
                AntarchyFabricSounds.STRATOSHARK_BITE,
                AntarchyFabricSounds.STRATOSHARK_IDLE,
                AntarchyFabricSounds.STRATOSHARK_HURT,
                AntarchyFabricSounds.STRATOSHARK_DEATH,
                AntarchyFabricSounds.STRATOSHARK_FLY,
                AntarchyFabricSounds.CATERPILLAR_IDLE,
                AntarchyFabricSounds.CATERPILLAR_HURT,
                AntarchyFabricSounds.CATERPILLAR_CRAWL,
                AntarchyFabricSounds.BUTTERFLY_HURT,
                AntarchyFabricSounds.BRUTALFLY_IDLE,
                AntarchyFabricSounds.BRUTALFLY_DEATH,
                AntarchyFabricSounds.ELYTHIA_FIREFLY_AMBIENT,
                AntarchyFabricSounds.ELYTHIA_OURANWOOD_FOREST_AMBIENT,
                AntarchyFabricSounds.MISSILE_SQUID_AMBIENT,
                AntarchyFabricSounds.MISSILE_SQUID_HURT,
                AntarchyFabricSounds.MISSILE_SQUID_DEATH,
                AntarchyFabricSounds.MISSILE_SQUID_ATTACK,
                AntarchyFabricSounds.KRAKEN_FLYING_LOOP,
                AntarchyFabricSounds.KRAKEN_FLYING_SIDEWAYS_LOOP,
                AntarchyFabricSounds.KRAKEN_ATTACK,
                AntarchyFabricSounds.KRAKEN_SPIN,
                AntarchyFabricSounds.KRAKEN_ROAR,
                AntarchyFabricSounds.KRAKEN_SUMMON,
                AntarchyFabricSounds.KRAKEN_HURT,
                AntarchyFabricSounds.KRAKEN_DEATH,
                AntarchyFabricSounds.BASILISK_IDLE_LOOP,
                AntarchyFabricSounds.BASILISK_SLITHER_LOOP,
                AntarchyFabricSounds.BASILISK_BITE,
                AntarchyFabricSounds.BASILISK_HISS,
                AntarchyFabricSounds.BASILISK_HURT,
                AntarchyFabricSounds.BASILISK_DEATH,
                AntarchyFabricSounds.THORAXIS_NIGHTMARE_WASTES_AMBIENT,
                AntarchyFabricSounds.THORAXIS_NIGHTMARE_WASTES_ADDITIONS,
                AntarchyFabricSounds.THORAXIS_NIGHTMARE_WASTES_MOOD,
                AntarchyFabricSounds.THORAXIS_DREAM_DUNES_AMBIENT,
                AntarchyFabricSounds.THORAXIS_DREAM_DUNES_ADDITIONS,
                AntarchyFabricSounds.THORAXIS_DREAM_DUNES_MOOD,
                AntarchyFabricSounds.THORAXIS_LUCID_POOLS_AMBIENT,
                AntarchyFabricSounds.THORAXIS_LUCID_POOLS_ADDITIONS,
                AntarchyFabricSounds.THORAXIS_LUCID_POOLS_MOOD,
                AntarchyFabricSounds.LUCID_AMBIENT,
                AntarchyFabricSounds.LUCID_FLYING,
                AntarchyFabricSounds.LUCID_ATTACK,
                AntarchyFabricSounds.LUCID_BOLT_SOUND,
                AntarchyFabricSounds.LUCID_HURT,
                AntarchyFabricSounds.LUCID_DEATH,
                AntarchyFabricSounds.REVERIE_IDLE,
                AntarchyFabricSounds.REVERIE_HURT,
                AntarchyFabricSounds.REVERIE_WORRY,
                AntarchyFabricSounds.REVERIE_SAVE,
                AntarchyFabricSounds.REVERIE_JOIN_PLAYER,
                AntarchyFabricSounds.REVERIE_ALERT,
                AntarchyFabricSounds.FLYING_SQUIRREL_IDLE,
                AntarchyFabricSounds.FLYING_SQUIRREL_BEG,
                AntarchyFabricSounds.FLYING_SQUIRREL_NUT,
                AntarchyFabricSounds.FLYING_SQUIRREL_GLIDE_LOOP,
                AntarchyFabricSounds.FLYING_SQUIRREL_HURT,
                AntarchyFabricSounds.FLYING_SQUIRREL_DEATH,
                AntarchyFabricSounds.NIGHTMARE_IDLE,
                AntarchyFabricSounds.NIGHTMARE_HURT,
                AntarchyFabricSounds.NIGHTMARE_ROAR,
                AntarchyFabricSounds.NIGHTMARE_DEATH,
                AntarchyFabricSounds.NIGHTMARE_BITE,
                AntarchyFabricSounds.NIGHTMARE_FLAP,
                AntarchyFabricSounds.FLYTRAP_ATTACK,
                AntarchyFabricSounds.FLYTRAP_GRAB,
                AntarchyFabricSounds.FLYTRAP_HURT,
                AntarchyFabricSounds.FLYTRAP_DEATH,
                AntarchyFabricSounds.FLYTRAP_HISS,
                AntarchyFabricSounds.FLYTRAP_GROWL,
                AntarchyFabricSounds.MANTIS_AMBIENT,
                AntarchyFabricSounds.MANTIS_HURT,
                AntarchyFabricSounds.MANTIS_ATTACK,
                AntarchyFabricSounds.MANTIS_FLY_LOOP,
                AntarchyFabricSounds.MANTICORE_IDLE,
                AntarchyFabricSounds.MANTICORE_WALK,
                AntarchyFabricSounds.MANTICORE_FLY,
                AntarchyFabricSounds.MANTICORE_BITE,
                AntarchyFabricSounds.GRAVITY_GUN_PICKUP,
                AntarchyFabricSounds.GRAVITY_GUN_DROP,
                AntarchyFabricSounds.GRAVITY_GUN_HOLD_LOOP,
                AntarchyFabricSounds.GRAVITY_GUN_LAUNCH,
                AntarchyFabricSounds.GRAVITY_GUN_DRYFIRE,
                AntarchyFabricSounds.BED_BUG_AMBIENT,
                AntarchyFabricSounds.BED_BUG_HURT,
                AntarchyFabricSounds.BED_BUG_ATTACK,
                AntarchyFabricSounds.WASP_IDLE,
                AntarchyFabricSounds.WASP_HURT,
                AntarchyFabricSounds.WASP_ATTACK,
                AntarchyFabricSounds.WASP_DEATH,
                AntarchyFabricSounds.SCORPION_AMBIENT,
                AntarchyFabricSounds.SCORPION_HURT,
                AntarchyFabricSounds.SCORPION_ATTACK,
                AntarchyFabricSounds.EMPEROR_SCORPION_AMBIENT,
                AntarchyFabricSounds.EMPEROR_SCORPION_HURT,
                AntarchyFabricSounds.EMPEROR_SCORPION_ATTACK,
                AntarchyFabricSounds.EMPEROR_SCORPION_ROAR,
                AntarchyFabricSounds.MOLEWORM_AMBIENT,
                AntarchyFabricSounds.MOLEWORM_HURT,
                AntarchyFabricSounds.MOLEWORM_ATTACK,
                AntarchyFabricSounds.MOLEWORM_DIG,
                AntarchyFabricSounds.MOLEVORE_AMBIENT,
                AntarchyFabricSounds.MOLEVORE_HURT,
                AntarchyFabricSounds.MOLEVORE_ATTACK,
                AntarchyFabricSounds.MOLEVORE_DIG,
                AntarchyFabricSounds.BOMBER_WALK,
                AntarchyFabricSounds.BOMBER_KNOCK,
                AntarchyFabricSounds.BOMBER_EXPLODE,
                AntarchyFabricSounds.TORETERROR_IDLE,
                AntarchyFabricSounds.TORETERROR_HURT,
                AntarchyFabricSounds.TORETERROR_DEATH,
                AntarchyFabricSounds.TORETERROR_BOMBER_FIRE,
                AntarchyFabricSounds.TORETERROR_SPIN,
                AntarchyFabricSounds.TORETERROR_RICOCHET,
                AntarchyFabricSounds.WATER_CANNON_FIRE,
                AntarchyFabricSounds.TORETERROR_JUMP_UP,
                AntarchyFabricSounds.TORETERROR_JUMP_LAND,
                AntarchyFabricSounds.STINKY_FLY_SOUND,
                AntarchyFabricSounds.STINK_BUG_FART,
                AntarchyFabricSounds.STINK_BUG_IDLE,
                AntarchyFabricSounds.CAVARYN_HEARTBEAT,
                AntarchyFabricSounds.ROLLY_POLLY_IDLE,
                AntarchyFabricSounds.CRAWLING_BLIGHT_GROWL,
                AntarchyFabricSounds.CRAWLING_BLIGHT_HURT,
                AntarchyFabricSounds.CRAWLING_BLIGHT_BITE,
                AntarchyFabricSounds.SKULKING_FRIGHT_SNARL,
                AntarchyFabricSounds.SKULKING_FRIGHT_HURT,
                AntarchyFabricSounds.SKULKING_FRIGHT_BITE,
                AntarchyFabricSounds.SKULKING_FRIGHT_FLY_LOOP,
                AntarchyFabricSounds.DUCT_TAPE_USE,
                AntarchyFabricSounds.HERCULES_BEETLE_IDLE,
                AntarchyFabricSounds.HERCULES_BEETLE_HURT,
                AntarchyFabricSounds.HERCULES_BEETLE_ATTACK,
                AntarchyFabricSounds.HERCULES_BEETLE_CRY,
                AntarchyFabricSounds.HERCULES_BEETLE_CHARGE_START,
                AntarchyFabricSounds.HERCULES_BEETLE_KNOCKED_DOWN,
                AntarchyFabricSounds.SPRINGBUG_IDLE,
                AntarchyFabricSounds.SPIT_BUG_IDLE,
                AntarchyFabricSounds.SPRINGBUG_HURT,
                AntarchyFabricSounds.SPRINGBUG_JUMP,
                AntarchyFabricSounds.SPIT_BUG_HURT,
                AntarchyFabricSounds.SPIT_BUG_SPIT,
                AntarchyFabricSounds.BRUTALFLY_SPIT,
                AntarchyFabricSounds.ROLLY_POLLY_WHEEL_MODE,
                AntarchyFabricSounds.ROLLY_POLLY_NORMAL_MODE,
                AntarchyFabricSounds.ROLLY_POLLY_ROLL,
                AntarchyFabricSounds.JERRY_YOUNG_IDLE,
                AntarchyFabricSounds.JERRY_YOUNG_HURT,
                AntarchyFabricSounds.JERRY_YOUNG_DEATH,
                AntarchyFabricSounds.JERRY_YOUNG_ATTACK,
                AntarchyFabricSounds.JERRY_ADULT_IDLE,
                AntarchyFabricSounds.JERRY_ADULT_HURT,
                AntarchyFabricSounds.JERRY_ADULT_DEATH,
                AntarchyFabricSounds.JERRY_ADULT_ATTACK,
                AntarchyFabricSounds.WORM_HOOK_FLYING,
                AntarchyFabricSounds.BLOOD_CRYSTAL_KATANA_DASH,
                AntarchyFabricSounds.BLOOD_CRYSTAL_ARMOR_EQUIP,
                AntarchyFabricSounds.BLOODGLASS_WARD_HEART_BREAK,
                AntarchyFabricSounds.BLOODGLASS_WARD_HEART_REGEN,
                AntarchyFabricSounds.KING_IDLE,
                AntarchyFabricSounds.KING_HURT,
                AntarchyFabricSounds.KING_DEATH,
                AntarchyFabricSounds.KING_BITE,
                AntarchyFabricSounds.QUEEN_IDLE,
                AntarchyFabricSounds.QUEEN_HURT,
                AntarchyFabricSounds.QUEEN_DEATH,
                AntarchyFabricSounds.QUEEN_BITE,
                AntarchyFabricSounds.QUEEN_ROAR,
                AntarchyFabricSounds.KING_BEAM_SHOOT,
                AntarchyFabricSounds.KING_FIREBALL_SHOOT,
                AntarchyFabricSounds.KING_ICE_SPIKES,
                AntarchyFabricSounds.KING_DECREE_CAST,
                AntarchyFabricSounds.QUEEN_BEAM_SHOOT,
                AntarchyFabricSounds.QUEEN_BEAM_START,
                AntarchyFabricSounds.QUEEN_BEAM_LOOP,
                AntarchyFabricSounds.QUEEN_BEAM_END,
                AntarchyFabricSounds.KING_STOMP,
                AntarchyFabricSounds.KING_ROAR,
                AntarchyFabricSounds.KING_WING_FLAP,
                AntarchyFabricSounds.KING_FLY_LOOP,
                AntarchyFabricSounds.QUEEN_FLY_LOOP,
                AntarchyFabricSounds.PRINCE_IDLE,
                AntarchyFabricSounds.PRINCE_BITE,
                AntarchyFabricSounds.PRINCE_SHOOT,
                AntarchyFabricSounds.PRINCE_FLY,
                AntarchyFabricSounds.PRINCE_STEP,
                AntarchyFabricSounds.PRINCESS_IDLE,
                AntarchyFabricSounds.PRINCESS_BITE,
                AntarchyFabricSounds.PRINCESS_SHOOT,
                AntarchyFabricSounds.PRINCESS_FLY,
                AntarchyFabricSounds.PRINCESS_STEP,
                AntarchyFabricSounds.KING_DECREE,
                AntarchyFabricSounds.KING_SUCCESS,
                AntarchyFabricSounds.KING_JUDGEMENT
        );
        AntarchySoundEvents.bindDimensionalTearSounds(
                AntarchyFabricSounds.DIMENSIONAL_TEAR_OPEN,
                AntarchyFabricSounds.DIMENSIONAL_TEAR_IDLE,
                AntarchyFabricSounds.DIMENSIONAL_TEAR_CREATURE_LEAVES,
                AntarchyFabricSounds.DIMENSIONAL_TEAR_CLOSE
        );
        AntarchySoundEvents.bindHoverboardSounds(AntarchyFabricSounds.HOVERBOARD_MOUNT, AntarchyFabricSounds.HOVERBOARD_DISMOUNT, AntarchyFabricSounds.HOVERBOARD_IDLE);
        AntarchySoundEvents.bindThrowbackSounds(AntarchyFabricSounds.THROWBACK_MUSIC, AntarchyFabricSounds.PLAYER_HURT_CLASSIC);

        AntarchyObjects.setGlimmeringEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.GLIMMERING));
        AntarchyObjects.setRemembranceEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.REMEMBRANCE));
        AntarchyObjects.setRoyalVitalityPenaltyEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.ROYAL_VITALITY_PENALTY));
        AntarchyObjects.setBleedingEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.BLEEDING));
        AntarchyObjects.setBloodMoonEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.BLOOD_MOON));
        AntarchyObjects.setSaucedEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.SAUCED));
        AntarchyObjects.setDread(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.DREAD));
        AntarchyObjects.setParalyzedEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.PARALYZED));
        AntarchyObjects.setInvertedEffect(AnthologyEffects::inverted);
        AntarchyObjects.setStinkyEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.STINKY));
        AntarchyObjects.setDoubleDamageChance(() -> AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.DOUBLE_DAMAGE_CHANCE));
        AntarchyObjects.setBloodglassMaxHearts(() -> AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.BLOODGLASS_MAX_HEARTS));
        AntarchyObjects.setBloodglassWard(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.BLOODGLASS_WARD));

        LucidEyeProjectileEntity.defaultItemSupplier = () -> AntarchyFabricItems.LUCID_PEARL.get();
        LucidEntity.invertedEffectSupplier = AnthologyEffects::inverted;
        LucidEntity.boltEntityTypeSupplier = () -> AntarchyFabricEntities.LUCID_BOLT.get();
        LucidBoltEntity.invertedEffectSupplier = AnthologyEffects::inverted;
        LucidEyeProjectileEntity.invertedEffectSupplier = AnthologyEffects::inverted;
        VortexEntity.windVortexTypeSupplier = () -> AntarchyFabricEntities.WIND_VORTEX.get();
        VortexChargeProjectileEntity.defaultItemSupplier = () -> AntarchyFabricItems.VORTEX_CHARGE.get();
        VortexChargeProjectileEntity.projectileTypeSupplier = () -> AntarchyFabricEntities.VORTEX_CHARGE_PROJECTILE.get();
        VortexChargeProjectileEntity.windVortexTypeSupplier = () -> AntarchyFabricEntities.WIND_VORTEX.get();

    }

}
