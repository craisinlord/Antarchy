package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatVersion;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.CreativeTabOrder;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.item.BloodCrystalArmorItem;
import com.craisinlord.antarchy.content.item.BloodCrystalAppleItem;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.fluid.BileLiquidBlock;
import com.craisinlord.antarchy.content.fluid.LumenLiquidBlock;
import com.craisinlord.antarchy.content.worldgen.ants.BrownAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RainbowAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RedAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.TermiteNestFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileCystFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileVeinFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCreepvineFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWallAmberMossFeature;
import com.craisinlord.antarchy.content.worldgen.overworld.CornPatchFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NyxiteSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.AntiwaterSpringsConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.AntiwaterSpringsFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NyxiteSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.PotentNyxiteFeature;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterFluid;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterLiquidBlock;
import com.craisinlord.antarchy.fabric.item.DeferredSpawnEggItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import com.craisinlord.antarchy.fabric.registry.DeferredBlock;
import com.craisinlord.antarchy.fabric.registry.DeferredHolder;
import com.craisinlord.antarchy.fabric.registry.DeferredItem;
import com.craisinlord.antarchy.fabric.registry.DeferredRegister;
import com.craisinlord.antarchy.content.effect.DreadMobEffect;
import com.craisinlord.antarchy.content.effect.GrowthMobEffect;
import com.craisinlord.antarchy.content.effect.InvertedMobEffect;
import com.craisinlord.antarchy.content.effect.ParalyzedMobEffect;
import com.craisinlord.antarchy.content.effect.ShrinkMobEffect;
import com.craisinlord.antarchy.content.effect.StinkyMobEffect;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.AppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.EnchantedGoldenAppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.GoldenAppleCow;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.entity.DrTrayaurusEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodBoatEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodChestBoatEntity;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.RollyPollyEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.item.LucidEyeItem;
import com.craisinlord.antarchy.content.item.LucidPearlItem;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.craisinlord.antarchy.content.entity.ant.BrownAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RedAntEntity;
import com.craisinlord.antarchy.content.item.BattleAxeItem;
import com.craisinlord.antarchy.content.item.BasiliskDaggerItem;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.AntimetalBlockItem;
import com.craisinlord.antarchy.fabric.item.AntimetalScaffoldingItem;
import com.craisinlord.antarchy.content.item.CloudSharkFinSoupItem;
import com.craisinlord.antarchy.content.item.CorneaEarItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.MinersDreamItem;
import com.craisinlord.antarchy.content.item.DuctTapeBlockItem;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.MobComingSoonTooltipItem;
import com.craisinlord.antarchy.content.item.MogglesItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateAxeItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateHoeItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimatePickaxeItem;
import com.craisinlord.antarchy.content.item.ultimate.UtlimateShovelItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateSwordItem;
import com.craisinlord.antarchy.content.item.DiamondMinecartItem;
import com.craisinlord.antarchy.content.item.ReverieBottleItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipTetherSync;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import com.craisinlord.antarchy.content.item.SquidzookaItem;
import com.craisinlord.antarchy.content.item.SimpleToolTier;
import com.craisinlord.antarchy.content.item.ultimate.UltimateArmorItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateBowItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateCrossbowItem;
import com.craisinlord.antarchy.content.item.NightmareArmorItem;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import com.craisinlord.antarchy.content.item.OuranwoodBoatOnlyItem;
import com.craisinlord.antarchy.content.item.OuranwoodChestBoatItem;
import com.craisinlord.antarchy.content.item.RainbowSugarItem;
import com.craisinlord.antarchy.content.worldgen.elythia.CoralSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaBiomeSource;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverCarveFunction;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaFloraFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaLargeTuffBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachForestMossyBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachForestPondConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachForestPondFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaPondFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaSurfaceCoverFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaTuffBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaUndergroundFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormCaveEntranceFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormSurfaceMoundsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.BrutalflyCocoonFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenPoolFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenLilyPadFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenStreamFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormTunnelsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormWarrensFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodCocoonTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.TriffidPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynEggPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitenSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitenSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.BedBugNestFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.BedBugSurfaceClusterFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.CloudSeaCalciteFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.LucidAntiwaterPoolFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBiomeSource;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisDuneConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisDuneFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisFissureConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisFissureFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBloodCrystalConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBloodCrystalFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisAntiwaterPoolConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisAntiwaterPoolFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisRibColumnsConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisRibColumnsFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisSpikeFeature;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserBaseParticleOptions;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;

public final class AntarchyFabricEntityAttributes {


    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }



    public static void register() {
        FabricDefaultAttributeRegistry.register(
                net.minecraft.world.entity.EntityType.PLAYER,
                net.minecraft.world.entity.player.Player.createAttributes()
                        .add(AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.DOUBLE_DAMAGE_CHANCE))
                        .add(AntarchyFabricMisc.attributeHolder(AntarchyFabricMisc.BLOODGLASS_MAX_HEARTS))
                        .build()
        );

        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.EASTER_BUNNY.get(), rabbitAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.REVERIE.get(), ReverieEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.GOLDEN_APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.DR_TRAYAURUS.get(), Villager.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CLOUD_SHARK.get(), CloudSharkEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.KRAKEN.get(), KrakenEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.OCTOPUS_BOMB.get(), OctopusBombEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TENTACLE.get(), TentacleEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BED_BUG.get(), BedBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.STINK_BUG.get(), StinkBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.WASP.get(), WaspEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BOMBER.get(), BomberEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.JUMPY_BUG.get(), JumpyBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SPIT_BUG.get(), com.craisinlord.antarchy.content.entity.SpitBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MANTIS.get(), MantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ALPHA_MANTIS.get(), AlphaMantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ROLLY_POLLY.get(), RollyPollyEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TRIFFID.get(), TriffidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.LUCID.get(), LucidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.SCORPION.get(), ScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BASILISK.get(), BasiliskEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CREEPING_HORROR.get(), com.craisinlord.antarchy.content.entity.CreepingHorrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.LURKING_TERROR.get(), com.craisinlord.antarchy.content.entity.LurkingTerrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.JERRY.get(), com.craisinlord.antarchy.content.entity.JerryEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.CHEEP.get(), com.craisinlord.antarchy.content.entity.CheepEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.DORRIE.get(), com.craisinlord.antarchy.content.entity.DorrieEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.HERCULES_BEETLE.get(), com.craisinlord.antarchy.content.entity.HerculesBeetleEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.OURANWOOD_DEER.get(), com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.GLIMMER.get(), com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.ELKA.get(), com.craisinlord.antarchy.content.entity.ElkaEntity.createAttributes().build());

        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.RED_ANT.get(), buildAntAttributes(
                AntarchySettings.redAntHealth(),
                AntarchySettings.redAntMovementSpeed(),
                AntarchySettings.redAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.BROWN_ANT.get(), buildAntAttributes(
                AntarchySettings.brownAntHealth(),
                AntarchySettings.brownAntMovementSpeed(),
                AntarchySettings.brownAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.RAINBOW_ANT.get(), buildAntAttributes(
                AntarchySettings.rainbowAntHealth(),
                AntarchySettings.rainbowAntMovementSpeed(),
                AntarchySettings.rainbowAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.TERMITE.get(), buildAntAttributes(
                AntarchySettings.termiteHealth(),
                AntarchySettings.termiteMovementSpeed(),
                AntarchySettings.termiteAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MOLEWORM.get(), MolewormEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AntarchyFabricEntities.MOLEVORE.get(), MolevoreEntity.createAttributes().build());
    }

}
