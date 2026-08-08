package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatVersion;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import com.craisinlord.antarchy.fabric.registry.DeferredHolder;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.item.ScorpionWhipTetherSync;
import com.craisinlord.antarchy.content.network.ImpactShakeSync;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
import com.craisinlord.antarchy.content.network.HordeIntensitySync;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.LiquidBlock;
import net.fabricmc.loader.api.FabricLoader;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricCreativeModeTabs;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricSounds;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntityAttributes;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricSpawnPlacements;

public final class AntarchyFabricContent {


    public static Holder<Potion> potionHolder(DeferredHolder<Potion, ? extends Potion> potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion.get());
    }



    private static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }



    public static net.minecraft.world.level.material.FlowingFluid lookupFlowingFluid(String path) {
        return (net.minecraft.world.level.material.FlowingFluid) BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }



    public static Item lookupItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }



    public static LiquidBlock lookupLiquidBlock(String path) {
        return (LiquidBlock) BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }



    public static void register() {
        com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity.TYPE = AntarchyFabricEntities.UPWARD_FALLING_BLOCK;
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.LUCID_EYE.get()), potionHolder(AntarchyFabricMisc.INVERSION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.INVERSION), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_INVERSION));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.STINK_BUG_ITEM.get()), potionHolder(AntarchyFabricMisc.STINKY_POTION));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.STINKY_POTION), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_STINKY));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.BASILISK_FANG.get()), potionHolder(AntarchyFabricMisc.PARALYSIS));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.PARALYSIS), Ingredient.of(Items.REDSTONE), potionHolder(AntarchyFabricMisc.LONG_PARALYSIS));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.MOLEWORM_ITEM.get()), potionHolder(AntarchyFabricMisc.HASTE));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.HASTE), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.STRONG_HASTE));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.URANIUM_NUGGET.get()), potionHolder(AntarchyFabricMisc.SHRINKING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.SHRINKING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.STRONG_SHRINKING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.STRONG_SHRINKING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.EXTREME_SHRINKING));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.TITANIUM_NUGGET.get()), potionHolder(AntarchyFabricMisc.GROWING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.GROWING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.STRONG_GROWING));
            builder.registerPotionRecipe(potionHolder(AntarchyFabricMisc.STRONG_GROWING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(AntarchyFabricMisc.EXTREME_GROWING));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.CLOUD_SHARK_FIN.get()), net.minecraft.world.item.alchemy.Potions.SLOW_FALLING);
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.JUMPY_BUG_LEG.get()), Potions.LEAPING);
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(AntarchyFabricItems.CORNEA_EAR.get()), net.minecraft.world.item.alchemy.Potions.NIGHT_VISION);
            // Corn/syrup/root-beer are plain items, not Potion holders, so vanilla's Builder#expectPotion
            // rejects them via registerItemRecipe/registerPotionRecipe. The actual output logic comes from
            // PotionBrewingMixin -> CustomBrewingRecipes; brewing-stand slot validity for these custom items
            // is handled by BrewingStandCustomIngredientMixin (Fabric doesn't expose Builder#addContainer).
        });

        AntarchyFabricSounds.SOUND_EVENTS.register();
        AntarchyFabricMisc.ARMOR_MATERIALS.register();
        AntarchyFabricMisc.FLUIDS.register();
        AntarchyFabricBlocks.BLOCKS.register();
        AntarchyFabricEntities.ENTITY_TYPES.register();
        AntarchyFabricMisc.ATTRIBUTES.register();
        AntarchyFabricEntityAttributes.register();
        AntarchyFabricSpawnPlacements.register();
        AntarchyFabricBlocks.BLOCK_ENTITY_TYPES.register();
        AntarchyFabricMisc.PARTICLE_TYPES.register();
        AntarchyFabricMisc.FEATURES.register();
        AntarchyFabricMisc.BIOME_SOURCES.register();
        AntarchyFabricMisc.DENSITY_FUNCTION_TYPES.register();
        AntarchyFabricMisc.ENTITY_SUB_PREDICATES.register();
        AntarchyFabricMisc.MOB_EFFECTS.register();
        AntarchyFabricMisc.POTIONS.register();
        AntarchyFabricItems.ITEMS.register();
        AntarchyFabricMisc.MENUS.register();
        AntarchyFabricMisc.DATA_COMPONENT_TYPES.register();
        AntarchyFabricMisc.RECIPE_SERIALIZERS.register();
        AntarchyFabricCreativeModeTabs.CREATIVE_MODE_TABS.register();

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
                AntarchyFabricSounds.SHRINK_RAY_SOUND,
                AntarchyFabricSounds.GROWTH_RAY_SOUND,
                AntarchyFabricSounds.SIZE_RAY_CHARGE,
                AntarchyFabricSounds.ANT_AMBIENT,
                AntarchyFabricSounds.ANT_IDLE,
                AntarchyFabricSounds.ANT_HURT,
                AntarchyFabricSounds.ANT_BITE,
                AntarchyFabricSounds.ANT_GATHER,
                AntarchyFabricSounds.ANT_NEST,
                AntarchyFabricSounds.CLOUD_SHARK_BITE,
                AntarchyFabricSounds.CLOUD_SHARK_IDLE,
                AntarchyFabricSounds.CLOUD_SHARK_HURT,
                AntarchyFabricSounds.CLOUD_SHARK_DEATH,
                AntarchyFabricSounds.CLOUD_SHARK_FLY,
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
                AntarchyFabricSounds.TRIFFID_ATTACK,
                AntarchyFabricSounds.TRIFFID_GRAB,
                AntarchyFabricSounds.TRIFFID_HURT,
                AntarchyFabricSounds.TRIFFID_DEATH,
                AntarchyFabricSounds.TRIFFID_HISS,
                AntarchyFabricSounds.TRIFFID_GROWL,
                AntarchyFabricSounds.MANTIS_AMBIENT,
                AntarchyFabricSounds.MANTIS_HURT,
                AntarchyFabricSounds.MANTIS_ATTACK,
                AntarchyFabricSounds.MANTIS_FLY_LOOP,
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
                AntarchyFabricSounds.CREEPING_HORROR_GROWL,
                AntarchyFabricSounds.CREEPING_HORROR_HURT,
                AntarchyFabricSounds.CREEPING_HORROR_BITE,
                AntarchyFabricSounds.LURKING_TERROR_SNARL,
                AntarchyFabricSounds.LURKING_TERROR_HURT,
                AntarchyFabricSounds.LURKING_TERROR_BITE,
                AntarchyFabricSounds.LURKING_TERROR_FLY_LOOP,
                AntarchyFabricSounds.DUCT_TAPE_USE,
                AntarchyFabricSounds.HERCULES_BEETLE_IDLE,
                AntarchyFabricSounds.HERCULES_BEETLE_HURT,
                AntarchyFabricSounds.HERCULES_BEETLE_ATTACK,
                AntarchyFabricSounds.HERCULES_BEETLE_CRY,
                AntarchyFabricSounds.HERCULES_BEETLE_CHARGE_START,
                AntarchyFabricSounds.HERCULES_BEETLE_KNOCKED_DOWN,
                AntarchyFabricSounds.JUMPY_BUG_IDLE,
                AntarchyFabricSounds.SPIT_BUG_IDLE,
                AntarchyFabricSounds.JUMPY_BUG_HURT,
                AntarchyFabricSounds.JUMPY_BUG_JUMP,
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
                AntarchyFabricSounds.JERRY_ADULT_ATTACK
        );

        AntarchyObjects.setOctopusBomb(AntarchyFabricEntities.OCTOPUS_BOMB);
        AntarchyObjects.setTentacle(AntarchyFabricEntities.TENTACLE);
        AntarchyObjects.setNightmarePortal(AntarchyFabricEntities.NIGHTMARE_PORTAL);
        AntarchyObjects.setNightmareBite(AntarchyFabricEntities.NIGHTMARE_BITE);
        AntarchyObjects.setKrakensGraspTrident(AntarchyFabricEntities.KRAKENS_GRASP_TRIDENT);
        AntarchyObjects.setLotus(() -> AntarchyFabricBlocks.LOTUS.get());
        AntarchyObjects.setKrakenTentacle(() -> AntarchyFabricItems.KRAKEN_TENTACLE.get());
        AntarchyObjects.setKrakensGrasp(() -> AntarchyFabricItems.KRAKENS_GRASP.get());
        AntarchyObjects.setOuranwoodDeer(AntarchyFabricEntities.OURANWOOD_DEER);
        AntarchyObjects.setRollyPolly(AntarchyFabricEntities.ROLLY_POLLY);
        AntarchyObjects.setGlimmer(AntarchyFabricEntities.GLIMMER);
        AntarchyObjects.setSpiritApple(() -> AntarchyFabricItems.SPIRIT_APPLE.get());
        AntarchyObjects.setGlimmeringEffect(() -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.GLIMMERING));
        AntarchyObjects.setElka(AntarchyFabricEntities.ELKA);
        AntarchyObjects.setPeach(() -> AntarchyFabricItems.PEACH.get());
        AntarchyObjects.setCorn(() -> AntarchyFabricItems.CORN.get());
        AntarchyObjects.setCornSeeds(() -> AntarchyFabricItems.CORN_SEEDS.get());
        AntarchyObjects.setWildCorn(() -> AntarchyFabricBlocks.WILD_CORN.get());
        AntarchyObjects.setStarCoralBlock(() -> AntarchyFabricBlocks.STAR_CORAL_BLOCK.get());
        AntarchyObjects.setStarCoral(() -> AntarchyFabricBlocks.STAR_CORAL.get());
        AntarchyObjects.setStarCoralFan(() -> AntarchyFabricBlocks.STAR_CORAL_FAN.get());
        AntarchyObjects.setCookedCorndog(() -> AntarchyFabricItems.COOKED_CORNDOG.get());
        AntarchyObjects.setGlimmerBottle(() -> AntarchyFabricItems.GLIMMER_BOTTLE.get());
        AntarchyObjects.setGlimmerVariantComponent(() -> AntarchyFabricMisc.GLIMMER_VARIANT.get());
        AntarchyObjects.setAmericanComponent(() -> AntarchyFabricMisc.AMERICAN.get());
        AntarchyObjects.setLumen(() -> AntarchyFabricMisc.LUMEN.get());
        AntarchyObjects.setFlowingLumen(() -> AntarchyFabricMisc.FLOWING_LUMEN.get());
        AntarchyObjects.setLumenBucket(() -> AntarchyFabricItems.LUMEN_BUCKET.get());
        AntarchyObjects.setLumenBlock(() -> AntarchyFabricBlocks.LUMEN_BLOCK.get());
        AntarchyObjects.setLumenFroglight(() -> AntarchyFabricBlocks.LUMEN_FROGLIGHT.get());
        AntarchyObjects.setPeachLeavesParticle(() -> AntarchyFabricMisc.PEACH_LEAVES_PARTICLE.get());
        AntarchyObjects.setLotusPollen(() -> AntarchyFabricMisc.LOTUS_POLLEN.get());
        AntarchyObjects.setLucidBoltImpactSmall(() -> AntarchyFabricMisc.LUCID_BOLT_IMPACT_SMALL.get());
        AntarchyObjects.setLucidBoltImpactLarge(() -> AntarchyFabricMisc.LUCID_BOLT_IMPACT_LARGE.get());
        AntarchyObjects.setDorrieInventoryMenu(AntarchyFabricMisc.DORRIE_INVENTORY_MENU);
        AntarchyObjects.setCritterCage(() -> AntarchyFabricItems.CRITTER_CAGE.get());
        AntarchyObjects.setCritterCageBlock(() -> AntarchyFabricBlocks.CRITTER_CAGE_BLOCK.get());
        AntarchyObjects.setLucidAnchor(() -> AntarchyFabricBlocks.LUCID_ANCHOR.get());
        AntarchyObjects.setCritterCageBlockEntity(() -> AntarchyFabricBlocks.CRITTER_CAGE_BLOCK_ENTITY.get());
        AntarchyObjects.setLucidAnchorBlockEntity(() -> AntarchyFabricBlocks.LUCID_ANCHOR_BLOCK_ENTITY.get());
        AntarchyObjects.setCritterCageProjectile(() -> AntarchyFabricEntities.CRITTER_CAGE_PROJECTILE.get());
        AntarchyObjects.setCritterCageEntityTypeComponent(() -> AntarchyFabricMisc.CRITTER_CAGE_ENTITY_TYPE_COMPONENT.get());
        AntarchyObjects.setCritterCagePrimaryColorComponent(() -> AntarchyFabricMisc.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.get());
        AntarchyObjects.setCritterCageSecondaryColorComponent(() -> AntarchyFabricMisc.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.get());
        AntarchyObjects.bind(
                AntarchyFabricEntities.EASTER_BUNNY,
                AntarchyFabricEntities.FLYING_SQUIRREL,
                AntarchyFabricEntities.KRAKEN,
                AntarchyFabricEntities.MISSILE_SQUID,
                AntarchyFabricEntities.MOLEWORM,
                AntarchyFabricEntities.MANTIS,
                AntarchyFabricEntities.BED_BUG,
                AntarchyFabricEntities.WASP,
                AntarchyFabricEntities.BOMBER,
                AntarchyFabricEntities.SCORPION,
                AntarchyFabricEntities.CATERPILLAR,
                AntarchyFabricEntities.BUTTERFLY,
                AntarchyFabricEntities.REVERIE,
                AntarchyFabricEntities.TRIFFID,
                AntarchyFabricEntities.BRUTALFLY,
                AntarchyFabricEntities.BRUTALFLY_ORB,
                AntarchyFabricEntities.HUSH_PROJECTILE,
                AntarchyFabricEntities.TORETERROR,
                AntarchyFabricEntities.WATER_BOMB,
                AntarchyFabricEntities.CREEPING_HORROR,
                AntarchyFabricEntities.LURKING_TERROR,
                AntarchyFabricEntities.STINK_BUG,
                AntarchyFabricEntities.CHEEP,
                AntarchyFabricEntities.DORRIE,
                () -> AntarchyFabricBlocks.DUPLICATOR_LOG.get(),
                () -> AntarchyFabricBlocks.DUPLICATOR_SAPLING.get(),
                () -> AntarchyFabricBlocks.DUCT_TAPE.get(),
                () -> AntarchyFabricBlocks.INFESTED_ROOTED_DIRT.get(),
                () -> AntarchyFabricBlocks.INFESTED_COARSE_DIRT.get(),
                () -> AntarchyFabricBlocks.NYXITE.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE.get(),
                () -> AntarchyFabricBlocks.POLISHED_SHELLSTONE.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_BRICKS.get(),
                () -> AntarchyFabricBlocks.CHISELED_SHELLSTONE.get(),
                () -> AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICKS.get(),
                () -> AntarchyFabricBlocks.CRACKED_SHELLSTONE_BRICKS.get(),
                () -> AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICK_STAIRS.get(),
                () -> AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICK_SLAB.get(),
                () -> AntarchyFabricBlocks.MOSSY_SHELLSTONE_BRICK_WALL.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_STAIRS.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_SLAB.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_WALL.get(),
                () -> AntarchyFabricBlocks.POLISHED_SHELLSTONE_STAIRS.get(),
                () -> AntarchyFabricBlocks.POLISHED_SHELLSTONE_SLAB.get(),
                () -> AntarchyFabricBlocks.POLISHED_SHELLSTONE_WALL.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_BRICK_STAIRS.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_BRICK_SLAB.get(),
                () -> AntarchyFabricBlocks.SHELLSTONE_BRICK_WALL.get(),
                () -> AntarchyFabricBlocks.CLOUD_BLOCK.get(),
                () -> AntarchyFabricBlocks.TRIFFID_GOO_BLOCK.get(),
                () -> AntarchyFabricBlocks.PALE_NYXITE.get(),
                () -> AntarchyFabricBlocks.NYXITE_SPIKE.get(),
                () -> AntarchyFabricBlocks.CHITIN_BLOCK.get(),
                () -> AntarchyFabricBlocks.CHITIN_SPIKE.get(),
                () -> AntarchyFabricBlocks.POTENT_NYXITE.get(),
                () -> AntarchyFabricBlocks.ANTIMETAL.get(),
                () -> AntarchyFabricBlocks.POLISHED_ANTIMETAL.get(),
                () -> AntarchyFabricBlocks.BUDDING_BLOOD_CRYSTAL.get(),
                () -> AntarchyFabricBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyFabricBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyFabricBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(),
                () -> AntarchyFabricBlocks.BLOOD_CRYSTAL_CRYSTAL.get(),
                () -> AntarchyFabricItems.OURANWOOD_ACORN.get(),
                AntarchyFabricItems.KRAKEN_TOOTH,
                () -> AntarchyFabricItems.MOGGLES.get(),
                () -> AntarchyFabricItems.REVERIE_BOTTLE.get(),
                () -> AntarchyFabricItems.STINK_BUG_ITEM.get(),
                () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.DREAD),
                () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.PARALYZED),
                () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.INVERTED),
                () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.STINKY),
                () -> AntarchyFabricBlocks.OURANWOOD_ACORN_BLOCK.get(),
                () -> AntarchyFabricBlocks.MOSSY_OURANWOOD_LOG.get(),
                () -> AntarchyFabricBlocks.MOSSY_OURANWOOD_WOOD.get(),
                () -> AntarchyFabricBlocks.UMBRAL_MOSS_BLOCK.get(),
                () -> AntarchyFabricBlocks.UMBRAL_MOSS_CARPET.get(),
                () -> AntarchyFabricBlocks.BLUSH_MOSS_BLOCK.get(),
                () -> AntarchyFabricBlocks.BLUSH_MOSS_CARPET.get(),
                () -> AntarchyFabricBlocks.ORANGE_MILKWEED.get(),
                () -> AntarchyFabricBlocks.PINK_MILKWEED.get(),
                () -> AntarchyFabricBlocks.BED_BUG_EGG.get(),
                () -> AntarchyFabricBlocks.CREEPING_HORROR_EGG.get(),
                () -> AntarchyFabricBlocks.LURKING_TERROR_EGG.get(),
                () -> AntarchyFabricBlocks.WASP_NEST.get(),
                () -> AntarchyFabricBlocks.HUSHWEED.get(),
                () -> AntarchyFabricBlocks.OURANWOOD_SQUIRREL_NEST.get(),
                () -> AntarchyFabricBlocks.BRUTALFLY_COCOON_SPAWNER.get(),
                () -> AntarchyFabricBlocks.GIANT_LILY_PAD.get(),
                () -> AntarchyFabricBlocks.SEASHELL.get(),
                () -> AntarchyFabricBlocks.ANT_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyFabricBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                () -> AntarchyFabricBlocks.WASP_NEST_BLOCK_ENTITY.get(),
                () -> AntarchyFabricBlocks.HUSHWEED_BLOCK_ENTITY.get(),
                () -> AntarchyFabricBlocks.SEASHELL_BLOCK_ENTITY.get(),
                () -> AntarchyFabricMisc.STINKY_GAS.get(),
                () -> AntarchyFabricMisc.STINKY_FLY.get(),
                () -> AntarchyFabricMisc.PEACH_LEAVES_PARTICLE.get(),
                () -> AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.DOUBLE_DAMAGE_CHANCE),
                () -> AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.BLOODGLASS_MAX_HEARTS),
                () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.BLOODGLASS_WARD)
        );

        LucidEyeProjectileEntity.defaultItemSupplier = () -> AntarchyFabricItems.LUCID_PEARL.get();
        LucidEntity.invertedEffectSupplier = () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.INVERTED);
        LucidEntity.boltEntityTypeSupplier = () -> AntarchyFabricEntities.LUCID_BOLT.get();
        LucidBoltEntity.invertedEffectSupplier = () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.INVERTED);
        LucidEyeProjectileEntity.invertedEffectSupplier = () -> AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.INVERTED);

        ScorpionWhipTetherSync.setSink(AntarchyFabricNetworking::syncScorpionWhipTether);
        HerculesBeetleImpactShakeSync.setSink((player, ticks) -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakePayload(ticks)));
        ImpactShakeSync.setSink((player, payload) -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload));
        HordeIntensitySync.setSink((player, payload) -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload));
        TigerEyeCamouflageSync.setSendToPlayer((player, payload) -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload));
        TigerEyeCamouflageSync.setSyncSelfAndTracking(AntarchyFabricNetworking::syncTigerEyeCamouflage);
        BloodCrystalKatanaItem.setTrailCallback(AntarchyFabricNetworking::syncKatanaTrail);
        com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.setSyncDispatcher(AntarchyFabricNetworking::syncGravityEntity);
        AntarchyFabricEvents.register();

        if (isSupportedInfinityLoaded()) {
            InfinityCompat.bind(new FabricInfinityCompat());
        }
    }



    private static boolean isSupportedInfinityLoaded() {
        if (!FabricInfinityCompat.isAvailableOnClasspath()) {
            return false;
        }

        try {
            String version = FabricLoader.getInstance()
                    .getModContainer("infinity")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
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
