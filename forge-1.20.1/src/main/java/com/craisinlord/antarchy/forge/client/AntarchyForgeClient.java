package com.craisinlord.antarchy.forge.client;

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
import com.craisinlord.antarchy.forge.registry.AntarchyForgeBlocks;
import com.craisinlord.antarchy.forge.registry.AntarchyForgeEntities;
import com.craisinlord.antarchy.forge.registry.AntarchyForgeItems;
import com.craisinlord.antarchy.forge.registry.AntarchyForgeMisc;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AntarchyForgeClient {
    private static final ResourceLocation DR_TRAYAURUS_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/dr_trayaurus.png");
    private static final ResourceLocation OURANWOOD_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/boat/ouranwood.png");
    private static final ResourceLocation OURANWOOD_CHEST_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png");
    private static final ResourceLocation PEACH_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/boat/peach.png");
    private static final ResourceLocation PEACH_CHEST_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/chest_boat/peach.png");
    private static final ResourceLocation NADIR_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/boat/nadir.png");
    private static final ResourceLocation NADIR_CHEST_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/chest_boat/nadir.png");
    private static final ResourceLocation ROYAL_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/boat/royal.png");
    private static final ResourceLocation ROYAL_CHEST_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/chest_boat/royal.png");
    private static final ResourceLocation TRUFFALO_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/boat/truffalo.png");
    private static final ResourceLocation TRUFFALO_CHEST_BOAT_TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/chest_boat/truffalo.png");
    private static final ResourceLocation WATER_STILL = new ResourceLocation("block/water_still");
    private static final ResourceLocation WATER_FLOW = new ResourceLocation("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = new ResourceLocation("block/water_overlay");
    private static final ResourceLocation UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    private static final ResourceLocation ANTIWATER_STILL = new ResourceLocation(Antarchy.MODID, "block/antiwater_still");
    private static final ResourceLocation ANTIWATER_FLOW = new ResourceLocation(Antarchy.MODID, "block/antiwater_flow");
    private static final ResourceLocation BILE_STILL = new ResourceLocation(Antarchy.MODID, "block/bile/bile_still");
    private static final ResourceLocation BILE_FLOW = new ResourceLocation(Antarchy.MODID, "block/bile/bile_flowing");
    private static final ResourceLocation ICHOR_STILL = new ResourceLocation(Antarchy.MODID, "block/ichor/ichor_still");
    private static final ResourceLocation ICHOR_FLOW = new ResourceLocation(Antarchy.MODID, "block/ichor/ichor_flowing");
    private AntarchyForgeClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Antarchy.physicalClient = true;
        AntarchyAnthologyHooks.registerClient();
        event.enqueueWork(() -> {
            AntarchyArchivePreviews.register();
            AntarchyComputerGames.register();
            MenuScreens.register(AntarchyForgeMisc.DORRIE_INVENTORY_MENU.get(), DorrieInventoryScreen::new);
            MenuScreens.register(AntarchyForgeMisc.ROYAL_JUDGMENT_MENU.get(), RoyalJudgmentScreen::new);
            MenuScreens.register(AntarchyForgeMisc.GIANT_FRYING_PAN_MENU.get(), GiantFryingPanScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AntarchyForgeBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(), CampfireRenderer::new);
        event.registerBlockEntityRenderer(AntarchyForgeBlocks.SEASHELL_BLOCK_ENTITY.get(), SeashellRenderer::new);
        event.registerBlockEntityRenderer(AntarchyForgeBlocks.LUCID_ANCHOR_BLOCK_ENTITY.get(), LucidAnchorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(AntarchyForgeBlocks.CRITTER_CAGE_BLOCK_ENTITY.get(), CritterCageRenderer::new);
        event.registerBlockEntityRenderer(AntarchyForgeBlocks.UNDERTRIAL_SPAWNER_BLOCK_ENTITY.get(), UndertrialSpawnerRenderer::new);
        event.registerBlockEntityRenderer(AntarchyForgeBlocks.UNDERVAULT_BLOCK_ENTITY.get(), UnderVaultRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.EASTER_BUNNY.get(), context -> withParalyzedGeoLayer(new EasterBunnyRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.FLYING_SQUIRREL.get(), context -> withParalyzedGeoLayer(new FlyingSquirrelRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.CATERPILLAR.get(), context -> withParalyzedGeoLayer(new CaterpillarRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.BUTTERFLY.get(), context -> withParalyzedGeoLayer(new ButterflyRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.REVERIE.get(), context -> withParalyzedGeoLayer(new ReverieRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.BRUTALFLY.get(), context -> withParalyzedGeoLayer(new BrutalflyRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.RED_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.BROWN_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.RAINBOW_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.MOLEWORM.get(), context -> withParalyzedGeoLayer(new MolewormRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.WORM.get(), context -> withParalyzedGeoLayer(new WormRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.MANTIS.get(), context -> withParalyzedGeoLayer(new MantisRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.ALPHA_MANTIS.get(), context -> withParalyzedGeoLayer(new AlphaMantisRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.HOVERBOARD.get(), HoverboardRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.MOLEVORE.get(), context -> withParalyzedGeoLayer(new MolevoreRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.FLYTRAP.get(), context -> withParalyzedGeoLayer(new FlytrapRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.OURANWOOD_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, OURANWOOD_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyForgeEntities.OURANWOOD_CHEST_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, OURANWOOD_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyForgeEntities.PEACH_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, PEACH_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyForgeEntities.PEACH_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, PEACH_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyForgeEntities.NADIR_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, NADIR_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyForgeEntities.NADIR_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, NADIR_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, ROYAL_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, ROYAL_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyForgeEntities.TRUFFALO_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, TRUFFALO_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyForgeEntities.TRUFFALO_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, TRUFFALO_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyForgeEntities.APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.GOLDEN_APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.DIAMOND_MINECART.get(), DiamondMinecartRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.DR_TRAYAURUS.get(), context -> new DrTrayaurusRenderer(context, DR_TRAYAURUS_TEXTURE));
        event.registerEntityRenderer(AntarchyForgeEntities.STRATOSHARK.get(), context -> withParalyzedGeoLayer(new StratosharkRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.KRAKEN.get(), context -> withParalyzedGeoLayer(new KrakenRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.TENTACLE.get(), TentacleRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.KRAKENS_GRASP_TRIDENT.get(), KrakensGraspThrownTridentRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.MISSILE_SQUID.get(), context -> withParalyzedGeoLayer(new MissileSquidRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.OCTOPUS_BOMB.get(), context -> withParalyzedGeoLayer(new OctopusBombRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.NIGHTMARE.get(), context -> withParalyzedGeoLayer(new NightmareRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.NIGHTMARE_PORTAL.get(), NightmarePortalRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.DIMENSIONAL_TEAR.get(), DimensionalTearRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.PORTAL_GUN_PORTAL.get(), PortalGunPortalRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.PORTAL_GUN_PROJECTILE.get(), PortalGunProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.NIGHTMARE_BITE.get(), NightmareBiteRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.LUCID.get(), context -> withParalyzedGeoLayer(new LucidRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.VORTEX.get(), context -> withParalyzedGeoLayer(new VortexRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.BED_BUG.get(), context -> withParalyzedGeoLayer(new BedBugRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.WASP.get(), context -> withParalyzedGeoLayer(new WaspRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.BOMBER.get(), context -> withParalyzedGeoLayer(new BomberRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.BASILISK.get(), context -> withParalyzedGeoLayer(new BasiliskRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.SHRINK_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.GROWTH_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.LUCID_BOLT.get(), LucidBoltRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.LUCID_PEARL_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.VORTEX_CHARGE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.WIND_VORTEX.get(), WindVortexRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_BLACK_HOLE.get(), RoyalBlackHoleRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_ASSAILANT_BLACK_HOLE.get(), RoyalAssailantBlackHoleRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.CRITTER_CAGE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.LAUNCHED_ANVIL.get(), LaunchedAnvilRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.WORM_HOOK_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.HUSH_PROJECTILE.get(), HushProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.BRUTALFLY_ORB.get(), BrutalflyOrbRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.UPWARD_FALLING_BLOCK.get(), UpwardFallingBlockRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.SCORPION.get(), context -> withParalyzedGeoLayer(new ScorpionRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.EMPEROR_SCORPION.get(), context -> withParalyzedGeoLayer(new EmperorScorpionRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.TORETERROR.get(), context -> withParalyzedGeoLayer(new ToreterrorRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.WATER_BOMB.get(), WaterBombRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.CHEEP.get(), context -> withParalyzedGeoLayer(new CheepRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.DORRIE.get(), context -> withParalyzedGeoLayer(new DorrieRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.OURANWOOD_DEER.get(), context -> withParalyzedGeoLayer(new OuranwoodDeerRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.GLIMMER.get(), context -> withParalyzedGeoLayer(new GlimmerRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.ELKA.get(), context -> withParalyzedGeoLayer(new ElkaRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.ROLLY_POLLY.get(), context -> withParalyzedGeoLayer(new RollyPollyRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.TERMITE.get(), context -> withParalyzedGeoLayer(new TermiteRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.CRAWLING_BLIGHT.get(), context -> withParalyzedGeoLayer(new CrawlingBlightRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.SKULKING_FRIGHT.get(), context -> withParalyzedGeoLayer(new SkulkingFrightRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.MANTICORE.get(), context -> withParalyzedGeoLayer(new ManticoreRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.HERCULES_BEETLE.get(), context -> withParalyzedGeoLayer(new HerculesBeetleRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.PRINCE.get(), RoyalMountRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.PRINCESS.get(), RoyalMountRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.KING.get(), RoyalBossRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.QUEEN.get(), RoyalBossRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_BOLT.get(), RoyalBoltRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_ELEMENTAL_PROJECTILE.get(), RoyalElementalProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.ROYAL_ICE_SPIKE.get(), RoyalIceSpikeRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.JERRY.get(), context -> withParalyzedGeoLayer(new JerryRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.SPRINGBUG.get(), context -> withParalyzedGeoLayer(new SpringbugRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.SPIT_BUG.get(), context -> withParalyzedGeoLayer(new SpitBugRenderer(context)));
        event.registerEntityRenderer(AntarchyForgeEntities.SPIT_BUG_PROJECTILE.get(), SpitBugProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyForgeEntities.STINK_BUG.get(), context -> withParalyzedGeoLayer(new StinkBugRenderer(context)));
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer =
                    (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new ParalyzedStonePlayerLayer(renderer));
                renderer.addLayer(new TigerEyeCamouflagePlayerLayer(renderer));
                renderer.addLayer(new GoopedLivingLayer(renderer));
                renderer.addLayer(new GlimmeringLivingLayer(renderer));
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
        Object renderer;
        try {
            renderer = event.getRenderer((EntityType) entityType);
        } catch (ClassCastException notLiving) {
            return;
        }
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
        ((LivingEntityRenderer) livingRenderer).addLayer(new RoyalIndicatorLivingLayer<>(livingRenderer));
    }

    private static <T extends LivingEntity & GeoAnimatable> GeoEntityRenderer<T> withParalyzedGeoLayer(GeoEntityRenderer<T> renderer) {
        renderer.addRenderLayer(new ParalyzedStoneGeoLayer<>(renderer));
        renderer.addRenderLayer(new GoopedGeoLayer<>(renderer));
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
                AntarchyForgeBlocks.MYRMITE_FOSSIL.get(),
                AntarchyForgeBlocks.BROODSTONE_FOSSIL.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0 ? bluestoneWireColor(state.getValue(BluestoneWireBlock.POWER)) : -1,
                AntarchyForgeBlocks.BLUESTONE_WIRE.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> RockiteTint.color(pos, tintIndex),
                RockiteTint.blocks()
        );
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor(),
                AntarchyForgeBlocks.OURANWOOD_LEAVES.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> 0xFF4A0000,
                AntarchyForgeBlocks.ANTIWATER_BLOCK.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.getDefaultColor(),
                AntarchyForgeBlocks.SPIDER_LILY.get()
        );
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : GrassColor.getDefaultColor(),
                AntarchyForgeBlocks.POTTED_SPIDER_LILY.get()
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> Fossils.color(Fossils.entityOf(stack), tintIndex),
                AntarchyForgeItems.MYRMITE_FOSSIL_ITEM.get(),
                AntarchyForgeItems.BROODSTONE_FOSSIL_ITEM.get()
        );
        event.register(
                (stack, tintIndex) -> FoliageColor.getDefaultColor(),
                AntarchyForgeItems.OURANWOOD_LEAVES_ITEM.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? GrassColor.getDefaultColor() : -1,
                AntarchyForgeItems.SPIDER_LILY_ITEM.get()
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
                AntarchyForgeItems.CRITTER_CAGE.get()
        );
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyForgeItems.ICHOR_BUCKET.get());
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyForgeItems.LUMEN_BUCKET.get());
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyForgeItems.ANTIWATER_BUCKET.get());
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(AntarchyForgeMisc.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.WIND_LINE.get(), WindLineParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.REMEMBRANCE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.REMEMBRANCE_MOTE.get(), RemembranceMoteParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.NIGHTMARE_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.STINKY_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.STINKY_FLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.PEACH_LEAVES_PARTICLE.get(), PeachLeavesParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.BUG_SPRAY_PARTICLE.get(), BugSprayParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.LOTUS_POLLEN.get(), LotusPollenParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.TEMPORAL_GLINT_SLOW.get(), TemporalGlintParticle::slow);
        event.registerSpriteSet(AntarchyForgeMisc.TEMPORAL_GLINT_FAST.get(), TemporalGlintParticle::fast);
        event.registerSpriteSet(AntarchyForgeMisc.HYPNOTIC_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.HYPNOTIC_GAS_DOWN.get(), sprites -> new HypnoticGasParticle.Provider(sprites, true));
        event.registerSpecial(AntarchyForgeMisc.HYPNOTIC_GAS_CLOUD.get(), new HypnoticGasCloudParticle.Provider());
        event.registerSpecial(AntarchyForgeMisc.HYPNOTIC_GAS_CLOUD_DOWN.get(), new HypnoticGasCloudParticle.Provider(true));
        event.registerSpriteSet(AntarchyForgeMisc.INVERTED_GEYSER_BASE.get(), InvertedGeyserBaseParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.INVERTED_GEYSER_PLUME.get(), InvertedGeyserPlumeParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.INVERTED_GEYSER_POOF.get(), InvertedGeyserBaseParticle.Provider::new);
        event.registerSpecial(AntarchyForgeMisc.INVERTED_GEYSER_ERUPTION.get(), new InvertedGeyserEruptionParticle.Provider());
        event.registerSpriteSet(AntarchyForgeMisc.FIREFLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.GROUND_MIST.get(), GroundMistParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.ORANGE_ASH.get(), OrangeAshParticle.Provider::new);
        event.registerSpriteSet(AntarchyForgeMisc.LUCID_BOLT_IMPACT_SMALL.get(), LucidBoltImpactParticle.SmallProvider::new);
        event.registerSpriteSet(AntarchyForgeMisc.LUCID_BOLT_IMPACT_LARGE.get(), LucidBoltImpactParticle.LargeProvider::new);
        event.registerSpriteSet(AntarchyForgeMisc.UNDERTRIAL_SPAWNER_DETECTION.get(), UndertrialParticle.DetectionProvider::new);
        event.registerSpriteSet(AntarchyForgeMisc.UNDERTRIAL_OMEN.get(), UndertrialParticle.OmenProvider::new);
        event.registerSpriteSet(AntarchyForgeMisc.UNDERVAULT_CONNECTION.get(), UndertrialParticle.OmenProvider::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                VanillaGuiOverlay.CROSSHAIR.id(),
                "portal_gun_crosshair",
                (gui, guiGraphics, partialTick, width, height) -> PortalGunCrosshairRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "dread_darkness",
                (gui, guiGraphics, partialTick, width, height) -> DreadHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "horde_intensity",
                (gui, guiGraphics, partialTick, width, height) -> HordeHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "paralyzed_stone",
                (gui, guiGraphics, partialTick, width, height) -> ParalyzedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "gooped_tint",
                (gui, guiGraphics, partialTick, width, height) -> GoopedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "glimmering_tint",
                (gui, guiGraphics, partialTick, width, height) -> GlimmeringHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "brutalfly_elytra",
                (gui, guiGraphics, partialTick, width, height) -> BrutalflyElytraHudRenderer.render(guiGraphics)
        );
        event.registerBelow(
                VanillaGuiOverlay.CHAT_PANEL.id(),
                "bloodglass_hearts",
                (gui, guiGraphics, partialTick, width, height) -> BloodglassHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "flytrap_goo",
                (gui, guiGraphics, partialTick, width, height) -> FlytrapGooHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "springy_boots",
                (gui, guiGraphics, partialTick, width, height) -> SpringyBootsHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "dorrie_jump",
                (gui, guiGraphics, partialTick, width, height) -> DorrieJumpHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "hercules_beetle_charge",
                (gui, guiGraphics, partialTick, width, height) -> HerculesBeetleChargeHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                "king_judgment_flash",
                (gui, guiGraphics, partialTick, width, height) -> KingJudgmentFlashHudRenderer.render(guiGraphics)
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TigerEyeClientHooks.setCamouflageKeyTextSupplier(() -> AntarchyKeyBindings.TIGERS_EYE_CAMOUFLAGE.getTranslatedKeyMessage());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.UNDERTRIAL_SPAWNER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.UNDERVAULT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.MYRMITE_FOSSIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BROODSTONE_FOSSIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ANTIMETAL_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ANTIMETAL_POWERED_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ANTIMETAL_DETECTOR_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ANTIMETAL_ACTIVATOR_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.OURANWOOD_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.OURANWOOD_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.OURANWOOD_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.OURANWOOD_ACORN_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_OURANWOOD_ACORN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PEACH_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PEACH_HANGING_PEACH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PEACH_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PEACH_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PEACH_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CRANBERRY_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CRANBERRY_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CRANBERRY_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CRANBERRY_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.JELLIED_CRANBERRY_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.REMEMBRANCE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.REMEMBRANCE_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_PEACH_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NADIR_VEIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NADIR_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NADIR_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NADIR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_NADIR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ROYAL_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ROYAL_FLOWERING_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ROYAL_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ROYAL_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ROYAL_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_ROYAL_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TRUFFALO_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TRUFFALO_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TRUFFALO_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_TRUFFALO_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DUPLICATOR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ORANGE_MILKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PINK_MILKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.GLIMMERING_REED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.AMBER_LICHEN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.MUCUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CREEPVINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CAMELLIA.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.SPIDER_LILY.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.PRIMORDIAL_GRASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_PRIMORDIAL_GRASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_SPIDER_LILY.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.LOTUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_LOTUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.GIANT_LILY_PAD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.HUSHWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CRITTER_CAGE_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CORNEA_STALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CORN_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.WILD_CORN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ANTIMETAL_SCAFFOLDING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.UMBRAL_MOSS_CARPET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NYXITE_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CHITIN_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TYPHONITE_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.SPIRALING_VINES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.GOREVINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.WHIRLFLOWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NADIR_FERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.LARGE_NADIR_FERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DUSKBELL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.NADIR_ROOTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.VERDANT_ROOTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.STAR_CORAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.STAR_CORAL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.STAR_CORAL_WALL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DEAD_STAR_CORAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DEAD_STAR_CORAL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DEAD_STAR_CORAL_WALL_FAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BLOOD_CRYSTAL_CRYSTAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_CEILING_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BLUESTONE_WIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BLUESTONE_REPEATER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BLUESTONE_COMPARATOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BLUESTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_CAMPFIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.DREAM_CEILING_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ANTIWATER_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeMisc.ANTIWATER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeMisc.FLOWING_ANTIWATER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.LUMEN_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeMisc.LUMEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeMisc.FLOWING_LUMEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.LUCID_ANCHOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.ELYTHIA_PORTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.THORAXIS_PORTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.FLYTRAP_GOO_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.VORTEX_LENS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.URANIUM_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TITANIUM_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.URANIUM_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TITANIUM_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.URANIUM_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.TITANIUM_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.CLOUD_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.SPRINGBUG_EGG.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.SPIT_BUG_EGG.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.JERRY_EGG.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.BIOWART_TENDRILS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.HANGING_CREEPROOTS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.MOLTING_VINES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.POTTED_GLOWCAP_MUSHROOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyForgeBlocks.GLOWCAP_MUSHROOM.get(), RenderType.cutout());
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
        ItemProperties.register(
                AntarchyForgeItems.ULTIMATE_BOW.get(),
                new ResourceLocation("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || entity.getUseItem() != stack) {
                        return 0.0F;
                    }

                    return (stack.getItem().getUseDuration(stack) - entity.getUseItemRemainingTicks()) / 20.0F;
                }
        );
        ItemProperties.register(
                AntarchyForgeItems.ULTIMATE_BOW.get(),
                new ResourceLocation("pulling"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
    }

    private static void registerUltimateCrossbowProperties() {
        ItemProperties.register(
                AntarchyForgeItems.ULTIMATE_CROSSBOW.get(),
                new ResourceLocation("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || CrossbowItem.isCharged(stack) || entity.getUseItem() != stack) {
                        return 0.0F;
                    }

                    float adjustedChargeDuration = Math.max(1.0F, stack.getItem().getUseDuration(stack) - 3.0F);
                    return Mth.clamp((stack.getItem().getUseDuration(stack) - entity.getUseItemRemainingTicks()) / adjustedChargeDuration, 0.0F, 1.0F);
                }
        );
        ItemProperties.register(
                AntarchyForgeItems.ULTIMATE_CROSSBOW.get(),
                new ResourceLocation("pulling"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyForgeItems.ULTIMATE_CROSSBOW.get(),
                new ResourceLocation("charged"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyForgeItems.ULTIMATE_CROSSBOW.get(),
                new ResourceLocation("firework"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyForgeItems.CRITTER_CAGE.get(),
                new ResourceLocation(Antarchy.MODID, "critter_cage_state"),
                (stack, level, entity, seed) -> {
                    if (!(stack.getItem() instanceof CritterCageItem cage)) {
                        return 0.0F;
                    }
                    return cage.getItemState(stack);
                }
        );
        ItemProperties.register(
                AntarchyForgeItems.SNOUT_COMPASS.get(),
                new ResourceLocation(Antarchy.MODID, "angle"),
                (stack, level, entity, seed) -> SnoutCompassAngle.angle(
                        stack,
                        level,
                        entity,
                        seed)
        );
    }

    private static int bluestoneWireColor(int power) {
        float normalized = power / 15.0F;
        int red = (int) (6.0F + normalized * 54.0F);
        int green = (int) (22.0F + normalized * 132.0F);
        int blue = (int) (64.0F + normalized * 191.0F);
        return red << 16 | green << 8 | blue;
    }
}
