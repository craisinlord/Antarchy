package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyAnthologyHooks;
import com.craisinlord.antarchy.content.block.BluestoneWireBlock;
import com.craisinlord.antarchy.content.block.Fossils;
import com.craisinlord.antarchy.content.block.RockiteTint;
import com.craisinlord.antarchy.content.client.AntarchyArchivePreviews;
import com.craisinlord.antarchy.content.client.GlimmeringHudRenderer;
import com.craisinlord.antarchy.content.client.GoopedHudRenderer;
import com.craisinlord.antarchy.content.client.HordeHudRenderer;
import com.craisinlord.antarchy.content.client.KingJudgmentFlashHudRenderer;
import com.craisinlord.antarchy.content.client.PortalGunCrosshairRenderer;
import com.craisinlord.antarchy.content.client.SnoutCompassAngle;
import com.craisinlord.antarchy.content.client.TigerEyeClientHooks;
import com.craisinlord.antarchy.content.client.game.AntarchyComputerGames;
import com.craisinlord.antarchy.content.client.hud.BloodglassHudRenderer;
import com.craisinlord.antarchy.content.client.particle.*;
import com.craisinlord.antarchy.content.client.renderer.*;
import com.craisinlord.antarchy.content.client.renderer.UnderVaultRenderer;
import com.craisinlord.antarchy.content.client.renderer.glimmer.GlimmerRenderer;
import com.craisinlord.antarchy.content.client.screen.DorrieInventoryScreen;
import com.craisinlord.antarchy.content.client.screen.GiantFryingPanScreen;
import com.craisinlord.antarchy.content.client.screen.RoyalJudgmentScreen;
import com.craisinlord.antarchy.content.item.CritterCageItem;
import com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeBlocks;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeEntities;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeItems;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@EventBusSubscriber(modid = Antarchy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AntarchyNeoForgeClient {
    private static final ResourceLocation DR_TRAYAURUS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dr_trayaurus.png");
    private static final ResourceLocation OURANWOOD_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/ouranwood.png");
    private static final ResourceLocation OURANWOOD_CHEST_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png");
    private static final ResourceLocation PEACH_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/peach.png");
    private static final ResourceLocation PEACH_CHEST_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/peach.png");
    private static final ResourceLocation NADIR_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/nadir.png");
    private static final ResourceLocation NADIR_CHEST_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/nadir.png");
    private static final ResourceLocation ROYAL_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/royal.png");
    private static final ResourceLocation ROYAL_CHEST_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/royal.png");
    private static final ResourceLocation TRUFFALO_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/truffalo.png");
    private static final ResourceLocation TRUFFALO_CHEST_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/truffalo.png");
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final ResourceLocation UNDERWATER_OVERLAY = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    private static final ResourceLocation ANTIWATER_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_still");
    private static final ResourceLocation ANTIWATER_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_flow");
    private static final ResourceLocation BILE_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still");
    private static final ResourceLocation BILE_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing");
    private static final ResourceLocation ICHOR_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/ichor/ichor_still");
    private static final ResourceLocation ICHOR_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/ichor/ichor_flowing");
    private AntarchyNeoForgeClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Antarchy.physicalClient = true;
        AntarchyAnthologyHooks.registerClient();
        event.enqueueWork(() -> {
            AntarchyArchivePreviews.register();
            AntarchyComputerGames.register();
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(), CampfireRenderer::new);
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.SEASHELL_BLOCK_ENTITY.get(), SeashellRenderer::new);
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.LUCID_ANCHOR_BLOCK_ENTITY.get(), LucidAnchorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.CRITTER_CAGE_BLOCK_ENTITY.get(), CritterCageRenderer::new);
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.UNDERTRIAL_SPAWNER_BLOCK_ENTITY.get(), UndertrialSpawnerRenderer::new);
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.UNDERVAULT_BLOCK_ENTITY.get(), UnderVaultRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.EASTER_BUNNY.get(), context -> withParalyzedGeoLayer(new EasterBunnyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.FLYING_SQUIRREL.get(), context -> withParalyzedGeoLayer(new FlyingSquirrelRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.CATERPILLAR.get(), context -> withParalyzedGeoLayer(new CaterpillarRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BUTTERFLY.get(), context -> withParalyzedGeoLayer(new ButterflyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.REVERIE.get(), context -> withParalyzedGeoLayer(new ReverieRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BRUTALFLY.get(), context -> withParalyzedGeoLayer(new BrutalflyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.RED_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BROWN_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.RAINBOW_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.MOLEWORM.get(), context -> withParalyzedGeoLayer(new MolewormRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.WORM.get(), context -> withParalyzedGeoLayer(new WormRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.MANTIS.get(), context -> withParalyzedGeoLayer(new MantisRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ALPHA_MANTIS.get(), context -> withParalyzedGeoLayer(new AlphaMantisRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.HOVERBOARD.get(), HoverboardRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.MOLEVORE.get(), context -> withParalyzedGeoLayer(new MolevoreRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.FLYTRAP.get(), context -> withParalyzedGeoLayer(new FlytrapRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.OURANWOOD_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, OURANWOOD_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.OURANWOOD_CHEST_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, OURANWOOD_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.PEACH_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, PEACH_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.PEACH_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, PEACH_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.NADIR_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, NADIR_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.NADIR_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, NADIR_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, ROYAL_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, ROYAL_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.TRUFFALO_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, TRUFFALO_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.TRUFFALO_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, TRUFFALO_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.GOLDEN_APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.DIAMOND_MINECART.get(), DiamondMinecartRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.DR_TRAYAURUS.get(), context -> new DrTrayaurusRenderer(context, DR_TRAYAURUS_TEXTURE));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.STRATOSHARK.get(), context -> withParalyzedGeoLayer(new StratosharkRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.KRAKEN.get(), context -> withParalyzedGeoLayer(new KrakenRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.TENTACLE.get(), TentacleRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.KRAKENS_GRASP_TRIDENT.get(), KrakensGraspThrownTridentRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.MISSILE_SQUID.get(), context -> withParalyzedGeoLayer(new MissileSquidRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.OCTOPUS_BOMB.get(), context -> withParalyzedGeoLayer(new OctopusBombRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.NIGHTMARE.get(), context -> withParalyzedGeoLayer(new NightmareRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.NIGHTMARE_PORTAL.get(), NightmarePortalRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.DIMENSIONAL_TEAR.get(), DimensionalTearRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.PORTAL_GUN_PORTAL.get(), PortalGunPortalRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.PORTAL_GUN_PROJECTILE.get(), PortalGunProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.NIGHTMARE_BITE.get(), NightmareBiteRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.LUCID.get(), context -> withParalyzedGeoLayer(new LucidRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.VORTEX.get(), context -> withParalyzedGeoLayer(new VortexRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BED_BUG.get(), context -> withParalyzedGeoLayer(new BedBugRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.WASP.get(), context -> withParalyzedGeoLayer(new WaspRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BOMBER.get(), context -> withParalyzedGeoLayer(new BomberRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BASILISK.get(), context -> withParalyzedGeoLayer(new BasiliskRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.SHRINK_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.GROWTH_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.LUCID_BOLT.get(), LucidBoltRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.LUCID_PEARL_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.VORTEX_CHARGE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.WIND_VORTEX.get(), WindVortexRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_BLACK_HOLE.get(), RoyalBlackHoleRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_ASSAILANT_BLACK_HOLE.get(), RoyalAssailantBlackHoleRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.CRITTER_CAGE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.LAUNCHED_ANVIL.get(), LaunchedAnvilRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.WORM_HOOK_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.HUSH_PROJECTILE.get(), HushProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.BRUTALFLY_ORB.get(), BrutalflyOrbRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.UPWARD_FALLING_BLOCK.get(), UpwardFallingBlockRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.SCORPION.get(), context -> withParalyzedGeoLayer(new ScorpionRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.EMPEROR_SCORPION.get(), context -> withParalyzedGeoLayer(new EmperorScorpionRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.TORETERROR.get(), context -> withParalyzedGeoLayer(new ToreterrorRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.WATER_BOMB.get(), WaterBombRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.CHEEP.get(), context -> withParalyzedGeoLayer(new CheepRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.DORRIE.get(), context -> withParalyzedGeoLayer(new DorrieRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.OURANWOOD_DEER.get(), context -> withParalyzedGeoLayer(new OuranwoodDeerRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.GLIMMER.get(), context -> withParalyzedGeoLayer(new GlimmerRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ELKA.get(), context -> withParalyzedGeoLayer(new ElkaRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROLLY_POLLY.get(), context -> withParalyzedGeoLayer(new RollyPollyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.TERMITE.get(), context -> withParalyzedGeoLayer(new TermiteRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.CRAWLING_BLIGHT.get(), context -> withParalyzedGeoLayer(new CrawlingBlightRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.SKULKING_FRIGHT.get(), context -> withParalyzedGeoLayer(new SkulkingFrightRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.MANTICORE.get(), context -> withParalyzedGeoLayer(new ManticoreRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.HERCULES_BEETLE.get(), context -> withParalyzedGeoLayer(new HerculesBeetleRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.PRINCE.get(), RoyalMountRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.PRINCESS.get(), RoyalMountRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.KING.get(), RoyalBossRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.QUEEN.get(), RoyalBossRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_BOLT.get(), RoyalBoltRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_ELEMENTAL_PROJECTILE.get(), RoyalElementalProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.ROYAL_ICE_SPIKE.get(), RoyalIceSpikeRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.JERRY.get(), context -> withParalyzedGeoLayer(new JerryRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.SPRINGBUG.get(), context -> withParalyzedGeoLayer(new SpringbugRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.SPIT_BUG.get(), context -> withParalyzedGeoLayer(new SpitBugRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntities.SPIT_BUG_PROJECTILE.get(), SpitBugProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntities.STINK_BUG.get(), context -> withParalyzedGeoLayer(new StinkBugRenderer(context)));
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer =
                    (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new ParalyzedStonePlayerLayer(renderer));
                renderer.addLayer(new TigerEyeCamouflagePlayerLayer(renderer));
                renderer.addLayer(new GoopedLivingLayer(renderer));
                renderer.addLayer(new GlimmeringLivingLayer(renderer));
                renderer.addLayer(new LumenSoakedLivingLayer(renderer));
                renderer.addLayer(new RoyalIndicatorLivingLayer<>(renderer));
                renderer.addLayer(new BrutalflyElytraLayer(renderer));
                renderer.addLayer(new ManticoreWingsLayer(renderer));
                renderer.addLayer(new FallenKingCrownLayer(renderer));
            }
        }
        LivingEntityRenderer<ArmorStand, ArmorStandModel> armorStandRenderer =
                (LivingEntityRenderer<ArmorStand, ArmorStandModel>) event.getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null) {
            armorStandRenderer.addLayer(new FallenKingCrownArmorStandLayer(armorStandRenderer));
        }
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> addStatusLayersToEntity(event, entityType));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OuranwoodBoatRenderer.boatLayer(), BoatModel::createBodyModel);
        event.registerLayerDefinition(OuranwoodBoatRenderer.chestBoatLayer(), ChestBoatModel::createBodyModel);
        event.registerLayerDefinition(PeachBoatRenderer.boatLayer(), BoatModel::createBodyModel);
        event.registerLayerDefinition(PeachBoatRenderer.chestBoatLayer(), ChestBoatModel::createBodyModel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends Entity> void addStatusLayersToEntity(EntityRenderersEvent.AddLayers event, EntityType<E> entityType) {
        if (entityType == EntityType.PLAYER) {
            return;
        }
        var renderer = event.getRenderer(entityType);
        if (renderer == null) {
            return;
        }
        if (!(renderer instanceof LivingEntityRenderer<?, ?> livingRenderer)) {
            return;
        }
        if (renderer instanceof GeoEntityRenderer<?>) {
            return;
        }
        ((LivingEntityRenderer) livingRenderer).addLayer(new ParalyzedStoneLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new GoopedLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new GlimmeringLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new LumenSoakedLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new RoyalIndicatorLivingLayer<>(livingRenderer));
    }

    private static <T extends LivingEntity & GeoAnimatable> GeoEntityRenderer<T> withParalyzedGeoLayer(GeoEntityRenderer<T> renderer) {
        renderer.addRenderLayer(new ParalyzedStoneGeoLayer<>(renderer));
        renderer.addRenderLayer(new GoopedGeoLayer<>(renderer));
        renderer.addRenderLayer(new LumenSoakedGeoLayer<>(renderer));
        renderer.addRenderLayer(new RoyalIndicatorGeoLayer<>(renderer));
        renderer.addRenderLayer(new TemporalContractionGeoLayer<>(renderer));
        return renderer;
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? Fossils.color(Fossils.entityAt(level, pos), tintIndex)
                        : -1,
                AntarchyNeoforgeBlocks.MYRMITE_FOSSIL.get(),
                AntarchyNeoforgeBlocks.BROODSTONE_FOSSIL.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0 ? bluestoneWireColor(state.getValue(BluestoneWireBlock.POWER)) : -1,
                AntarchyNeoforgeBlocks.BLUESTONE_WIRE.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> RockiteTint.color(pos, tintIndex),
                RockiteTint.blocks()
        );
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor(),
                AntarchyNeoforgeBlocks.OURANWOOD_LEAVES.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> 0xFF4A0000,
                AntarchyNeoforgeBlocks.ANTIWATER_BLOCK.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.getDefaultColor(),
                AntarchyNeoforgeBlocks.SPIDER_LILY.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.getDefaultColor(),
                AntarchyNeoforgeBlocks.POTTED_SPIDER_LILY.get()
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> Fossils.color(Fossils.entityOf(stack), tintIndex),
                AntarchyNeoforgeItems.MYRMITE_FOSSIL_ITEM.get(),
                AntarchyNeoforgeItems.BROODSTONE_FOSSIL_ITEM.get()
        );
        event.register(
                (stack, tintIndex) -> FoliageColor.getDefaultColor(),
                AntarchyNeoforgeItems.OURANWOOD_LEAVES_ITEM.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? GrassColor.getDefaultColor() : -1,
                AntarchyNeoforgeItems.SPIDER_LILY_ITEM.get()
        );
        event.register(
                (stack, tintIndex) -> RockiteTint.itemColor(tintIndex),
                RockiteTint.blocks()
        );
        event.register(
                (stack, tintIndex) -> {
                    if (!(stack.getItem() instanceof CritterCageItem cage)) {
                        return -1;
                    }
                    if (cage.getItemState(stack) != 2) {
                        return -1;
                    }
                    if (tintIndex == 1) {
                        return cage.getPrimaryColor(stack);
                    }
                    if (tintIndex == 2) {
                        return cage.getSecondaryColor(stack);
                    }
                    return -1;
                },
                AntarchyNeoforgeItems.CRITTER_CAGE.get()
        );
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyNeoforgeItems.ICHOR_BUCKET.get());
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyNeoforgeItems.LUMEN_BUCKET.get());
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyNeoforgeItems.ANTIWATER_BUCKET.get());
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(AntarchyNeoforgeMisc.REMEMBRANCE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.REMEMBRANCE_MOTE.get(), RemembranceMoteParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.NIGHTMARE_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.STINKY_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.STINKY_FLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.PEACH_LEAVES_PARTICLE.get(), PeachLeavesParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.BUG_SPRAY_PARTICLE.get(), BugSprayParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LOTUS_POLLEN.get(), LotusPollenParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.TEMPORAL_GLINT_SLOW.get(), TemporalGlintParticle::slow);
        event.registerSpriteSet(AntarchyNeoforgeMisc.TEMPORAL_GLINT_FAST.get(), TemporalGlintParticle::fast);
        event.registerSpriteSet(AntarchyNeoforgeMisc.HYPNOTIC_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.HYPNOTIC_GAS_DOWN.get(), sprites -> new HypnoticGasParticle.Provider(sprites, true));
        event.registerSpecial(AntarchyNeoforgeMisc.HYPNOTIC_GAS_CLOUD.get(), new HypnoticGasCloudParticle.Provider());
        event.registerSpecial(AntarchyNeoforgeMisc.HYPNOTIC_GAS_CLOUD_DOWN.get(), new HypnoticGasCloudParticle.Provider(true));
        event.registerSpriteSet(AntarchyNeoforgeMisc.INVERTED_GEYSER_BASE.get(), InvertedGeyserBaseParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.INVERTED_GEYSER_PLUME.get(), InvertedGeyserPlumeParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.INVERTED_GEYSER_POOF.get(), InvertedGeyserBaseParticle.Provider::new);
        event.registerSpecial(AntarchyNeoforgeMisc.INVERTED_GEYSER_ERUPTION.get(), new InvertedGeyserEruptionParticle.Provider());
        event.registerSpriteSet(AntarchyNeoforgeMisc.FIREFLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.GROUND_MIST.get(), GroundMistParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.WIND_LINE.get(), WindLineParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.ORANGE_ASH.get(), OrangeAshParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LUCID_BOLT_IMPACT_SMALL.get(), LucidBoltImpactParticle.SmallProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LUCID_BOLT_IMPACT_LARGE.get(), LucidBoltImpactParticle.LargeProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.UNDERTRIAL_SPAWNER_DETECTION.get(), UndertrialParticle.DetectionProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.UNDERTRIAL_OMEN.get(), UndertrialParticle.OmenProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.UNDERVAULT_CONNECTION.get(), UndertrialParticle.OmenProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LUMEN_DRIP.get(), LumenDripParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(AntarchyNeoforgeMisc.DORRIE_INVENTORY_MENU.get(), DorrieInventoryScreen::new);
        event.register(AntarchyNeoforgeMisc.ROYAL_JUDGMENT_MENU.get(), RoyalJudgmentScreen::new);
        event.register(AntarchyNeoforgeMisc.GIANT_FRYING_PAN_MENU.get(), GiantFryingPanScreen::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.CROSSHAIR,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_crosshair"),
                (guiGraphics, partialTick) -> PortalGunCrosshairRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dread_darkness"),
                (guiGraphics, partialTick) -> DreadHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "horde_intensity"),
                (guiGraphics, partialTick) -> HordeHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "paralyzed_stone"),
                (guiGraphics, partialTick) -> ParalyzedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gooped_tint"),
                (guiGraphics, partialTick) -> GoopedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "glimmering_tint"),
                (guiGraphics, partialTick) -> GlimmeringHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "brutalfly_elytra"),
                (guiGraphics, partialTick) -> BrutalflyElytraHudRenderer.render(guiGraphics)
        );
        event.registerBelow(
                VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bloodglass_hearts"),
                (guiGraphics, partialTick) -> BloodglassHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flytrap_goo"),
                (guiGraphics, partialTick) -> FlytrapGooHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "springy_boots"),
                (guiGraphics, partialTick) -> SpringyBootsHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dorrie_jump"),
                (guiGraphics, partialTick) -> DorrieJumpHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle_charge"),
                (guiGraphics, partialTick) -> HerculesBeetleChargeHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "king_judgment_flash"),
                (guiGraphics, partialTick) -> KingJudgmentFlashHudRenderer.render(guiGraphics)
        );
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Antarchy.LOGGER.info("[Antiwater] Registering antiwater client fluid textures still={} flow={}", ANTIWATER_STILL, ANTIWATER_FLOW);
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_flow");
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return 0xFFFFFFFF;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.58F, 0.84F, 0.88F);
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.4F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 12.0F));
                RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            }
        }, AntarchyNeoForgeFluidTypes.LUMEN_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ICHOR_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ICHOR_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return 0xFFFFFFFF;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.16F, 0.02F, 0.03F);
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.5F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 10.0F));
                RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            }
        }, AntarchyNeoForgeFluidTypes.ICHOR_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return BILE_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return BILE_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return 0xFFFFFFFF;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.38F, 0.42F, 0.10F);
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.25F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 4.0F));
                RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            }
        }, AntarchyNeoForgeFluidTypes.BILE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public int getTintColor() {
                return 0xFFFF1A1A;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return 0xFFFF1A1A;
            }

            @Override
            public boolean renderFluid(FluidState fluidState, BlockAndTintGetter getter, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState) {
                var sprites = FluidSpriteCache.getFluidSprites(getter, pos, fluidState);
                int tint = IClientFluidTypeExtensions.of(fluidState).getTintColor(fluidState, getter, pos);
                AntiwaterFluidRenderer.render(getter, pos, vertexConsumer, blockState, fluidState, sprites[0], sprites[1], sprites[2], tint);
                return true;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.52F, 0.06F, 0.06F);
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.5F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 12.0F));
                RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            }
        }, AntarchyNeoForgeFluidTypes.ANTIWATER_TYPE.get());

        registerGeoItemExtensionSizeRay(event, AntarchyNeoforgeItems.SHRINK_RAY.get());
        registerGeoItemExtensionSizeRay(event, AntarchyNeoforgeItems.GROWTH_RAY.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.GRAVITY_GUN.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.PORTAL_GUN.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.PORTAL_GUN_ATLAS.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.PORTAL_GUN_P_BODY.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.PORTAL_GUN_CRAISIN.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.PORTAL_GUN_LAYNCE.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.SQUIDZOOKA.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.RPO_LAUNCHER.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.ANVIL_CANNON.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.WATER_CANNON.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.BATTLE_AXE.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.BIG_BERTHA.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.BIG_BETSY.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.ROYAL_GUARDIAN_SWORD.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.GIANT_FRYING_PAN.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TigerEyeClientHooks.setCamouflageKeyTextSupplier(() -> AntarchyKeyBindings.TIGERS_EYE_CAMOUFLAGE.getTranslatedKeyMessage());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PRIMORDIAL_GRASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_PRIMORDIAL_GRASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.UNDERTRIAL_SPAWNER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.UNDERVAULT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIMETAL_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIMETAL_POWERED_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIMETAL_DETECTOR_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIMETAL_ACTIVATOR_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_ACORN_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_OURANWOOD_ACORN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CRANBERRY_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CRANBERRY_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CRANBERRY_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CRANBERRY_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PEACH_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PEACH_HANGING_PEACH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PEACH_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PEACH_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PEACH_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_PEACH_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NADIR_VEIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NADIR_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NADIR_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NADIR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_NADIR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ROYAL_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ROYAL_FLOWERING_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ROYAL_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ROYAL_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ROYAL_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_ROYAL_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TRUFFALO_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TRUFFALO_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TRUFFALO_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_TRUFFALO_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DUPLICATOR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ORANGE_MILKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PINK_MILKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.GLIMMERING_REED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.AMBER_LICHEN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.MUCUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CREEPVINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CAMELLIA.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.SPIDER_LILY.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_SPIDER_LILY.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.LOTUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_LOTUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.GIANT_LILY_PAD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.HUSHWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CRITTER_CAGE_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.MYRMITE_FOSSIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BROODSTONE_FOSSIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CORNEA_STALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CORN_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.WILD_CORN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIMETAL_SCAFFOLDING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.UMBRAL_MOSS_CARPET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NYXITE_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CHITIN_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TYPHONITE_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.SPIRALING_VINES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.GOREVINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.WHIRLFLOWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NADIR_FERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.LARGE_NADIR_FERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DUSKBELL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NADIR_ROOTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.VERDANT_ROOTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.STAR_CORAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.STAR_CORAL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.STAR_CORAL_WALL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DEAD_STAR_CORAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DEAD_STAR_CORAL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DEAD_STAR_CORAL_WALL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BLOOD_CRYSTAL_CRYSTAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.REMEMBRANCE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.REMEMBRANCE_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_CEILING_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BLUESTONE_WIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BLUESTONE_REPEATER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BLUESTONE_COMPARATOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BLUESTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_CAMPFIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_CEILING_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIWATER_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.LUMEN_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.LUCID_ANCHOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ELYTHIA_PORTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.THORAXIS_PORTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.FLYTRAP_GOO_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.JELLIED_CRANBERRY_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.VORTEX_LENS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.URANIUM_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TITANIUM_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.URANIUM_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TITANIUM_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.URANIUM_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TITANIUM_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CLOUD_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.SPRINGBUG_EGG.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.SPIT_BUG_EGG.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.JERRY_EGG.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BIOWART_TENDRILS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.HANGING_CREEPROOTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.MOLTING_VINES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.POTTED_GLOWCAP_MUSHROOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.GLOWCAP_MUSHROOM.get(), RenderType.cutout());
            registerUltimateBowProperties();
            registerUltimateCrossbowProperties();
        });
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                TigerEyeCamouflageClientHandler.clearClientCaches();
            }
        });
    }

    private static void registerUltimateBowProperties() {
        ItemProperties.register(AntarchyNeoforgeItems.SNOUT_COMPASS.get(),
                ResourceLocation.fromNamespaceAndPath("antarchy", "angle"),
                SnoutCompassAngle::angle);
        ItemProperties.register(
                AntarchyNeoforgeItems.ULTIMATE_BOW.get(),
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || entity.getUseItem() != stack) {
                        return 0.0F;
                    }

                    return (stack.getItem().getUseDuration(stack, entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                }
        );
        ItemProperties.register(
                AntarchyNeoforgeItems.ULTIMATE_BOW.get(),
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
    }

    private static void registerUltimateCrossbowProperties() {
        ItemProperties.register(
                AntarchyNeoforgeItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || CrossbowItem.isCharged(stack) || entity.getUseItem() != stack) {
                        return 0.0F;
                    }

                    float adjustedChargeDuration = Math.max(1.0F, stack.getItem().getUseDuration(stack, entity) - 3.0F);
                    return Mth.clamp((stack.getItem().getUseDuration(stack, entity) - entity.getUseItemRemainingTicks()) / adjustedChargeDuration, 0.0F, 1.0F);
                }
        );
        ItemProperties.register(
                AntarchyNeoforgeItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyNeoforgeItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("charged"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyNeoforgeItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("firework"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) && ChargedProjectiles.of(stack).contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyNeoforgeItems.CRITTER_CAGE.get(),
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "critter_cage_state"),
                (stack, level, entity, seed) -> {
                    if (!(stack.getItem() instanceof CritterCageItem cage)) {
                        return 0.0F;
                    }
                    return cage.getItemState(stack);
                }
        );
    }

    private static void registerGeoItemExtension(RegisterClientExtensionsEvent event, Item item) {
        registerGeoItemExtension(event, item, null);
    }

    private static void registerGeoItemExtension(RegisterClientExtensionsEvent event, Item item, HumanoidModel.ArmPose armPose) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return GeoRenderProvider.of(item).getGeoItemRenderer();
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return armPose;
            }
        }, item);
    }

    private static void registerGeoItemExtensionSizeRay(RegisterClientExtensionsEvent event, Item item) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return GeoRenderProvider.of(item).getGeoItemRenderer();
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (entityLiving.isUsingItem() && entityLiving.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
                return null;
            }
        }, item);
    }

    private static int bluestoneWireColor(int power) {
        float normalized = power / 15.0F;
        int red = (int) (6.0F + normalized * 54.0F);
        int green = (int) (22.0F + normalized * 132.0F);
        int blue = (int) (64.0F + normalized * 191.0F);
        return red << 16 | green << 8 | blue;
    }
}
