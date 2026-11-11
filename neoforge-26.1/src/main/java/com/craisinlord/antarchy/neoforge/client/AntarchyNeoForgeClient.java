package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyAnthologyHooks;
import com.craisinlord.antarchy.content.block.BluestoneWireBlock;
import com.craisinlord.antarchy.content.block.Fossils;
import com.craisinlord.antarchy.content.block.RockiteTint;
import com.craisinlord.antarchy.content.block.entity.FossilBlockEntity;
import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.craisinlord.antarchy.content.client.AntarchyArchivePreviews;
import com.craisinlord.antarchy.content.client.ElythiaEchoes;
import com.craisinlord.antarchy.content.client.GlimmeringHudRenderer;
import com.craisinlord.antarchy.content.client.GoopedHudRenderer;
import com.craisinlord.antarchy.content.client.HordeHudRenderer;
import com.craisinlord.antarchy.content.client.KingJudgmentFlashHudRenderer;
import com.craisinlord.antarchy.content.client.PortalGunCrosshairRenderer;
import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
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
import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeBlocks;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeEntities;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeItems;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.GeoEntityRenderer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.CustomFluidRenderer;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class AntarchyNeoForgeClient {
    @SubscribeEvent
    public static void tickThrowbackState(ClientTickEvent.Post event) {
        AntTransitClientState.tick();
        PrimordialGrassRenderer.tick(Minecraft.getInstance());
        ElythiaEchoes.tick();
        ThoraxisEventClient.tick();
        ThrowbackClientState.tick();
    }

    @SubscribeEvent
    public static void renderAntTransitGround(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft minecraft = Minecraft.getInstance();
        AntTransitGroundRenderer.render(event.getPoseStack(), minecraft.renderBuffers().bufferSource(),
                event.getLevelRenderState().cameraRenderState.pos, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        ElythiaEchoes.render(event.getPoseStack(), minecraft.renderBuffers().bufferSource(),
                event.getLevelRenderState().cameraRenderState.pos, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        ThoraxisRainRenderer.render(event.getPoseStack(), minecraft.renderBuffers().bufferSource(),
                event.getLevelRenderState().cameraRenderState.pos, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        ThoraxisBubbleRenderer.render(event.getPoseStack(), minecraft.renderBuffers().bufferSource(),
                event.getLevelRenderState().cameraRenderState.pos, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }
    private static final Identifier DR_TRAYAURUS_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dr_trayaurus.png");
    private static final Identifier OURANWOOD_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/ouranwood.png");
    private static final Identifier OURANWOOD_CHEST_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png");
    private static final Identifier PEACH_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/peach.png");
    private static final Identifier PEACH_CHEST_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/peach.png");
    private static final Identifier NADIR_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/nadir.png");
    private static final Identifier NADIR_CHEST_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/nadir.png");
    private static final Identifier ROYAL_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/royal.png");
    private static final Identifier ROYAL_CHEST_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/royal.png");
    private static final Identifier TRUFFALO_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/truffalo.png");
    private static final Identifier TRUFFALO_CHEST_BOAT_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/truffalo.png");
    private static final Identifier WATER_STILL = Identifier.withDefaultNamespace("block/water_still");
    private static final Identifier WATER_FLOW = Identifier.withDefaultNamespace("block/water_flow");
    private static final Identifier WATER_OVERLAY = Identifier.withDefaultNamespace("block/water_overlay");
    private static final Identifier UNDERWATER_OVERLAY = Identifier.withDefaultNamespace("textures/misc/underwater.png");
    private static final Identifier LUMEN_STILL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_still");
    private static final Identifier LUMEN_FLOW = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_flow");
    private static final Identifier ANTIWATER_STILL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_still");
    private static final Identifier ANTIWATER_FLOW = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_flow");
    private static final Identifier BILE_STILL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still");
    private static final Identifier BILE_FLOW = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing");
    private static final Identifier ICHOR_STILL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/ichor/ichor_still");
    private static final Identifier ICHOR_FLOW = Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/ichor/ichor_flowing");
    private AntarchyNeoForgeClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Antarchy.physicalClient = true;
        AntarchyNetwork.bindClient(ClientPacketDistributor::sendToServer);
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
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new ParalyzedStonePlayerLayer(renderer));
                renderer.addLayer(new TigerEyeCamouflagePlayerLayer(renderer));
                renderer.addLayer(new GoopedLivingLayer<>(renderer));
                renderer.addLayer(new GlimmeringLivingLayer<>(renderer));
                renderer.addLayer(new RoyalIndicatorLivingLayer<>(renderer));
                renderer.addLayer(new TemporalContractionAvatarLayer(renderer));
                renderer.addLayer(new BrutalflyElytraLayer(renderer));
                renderer.addLayer(new ManticoreWingsLayer(renderer));
                renderer.addLayer(new FallenKingCrownLayer(renderer));
            }
        }
        ArmorStandRenderer armorStandRenderer = event.getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null) {
            armorStandRenderer.addLayer(new FallenKingCrownArmorStandLayer(armorStandRenderer));
        }
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> addStatusLayersToEntity(event, entityType));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OuranwoodBoatRenderer.boatLayer(), BoatModel::createBoatModel);
        event.registerLayerDefinition(OuranwoodBoatRenderer.chestBoatLayer(), BoatModel::createChestBoatModel);
        event.registerLayerDefinition(PeachBoatRenderer.boatLayer(), BoatModel::createBoatModel);
        event.registerLayerDefinition(PeachBoatRenderer.chestBoatLayer(), BoatModel::createChestBoatModel);
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
        if (!(renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer)) {
            return;
        }
        if (renderer instanceof GeoEntityRenderer<?, ?>) {
            return;
        }
        ((LivingEntityRenderer) livingRenderer).addLayer(new ParalyzedStoneLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new GoopedLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new GlimmeringLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new RoyalIndicatorLivingLayer<>(livingRenderer));
        ((LivingEntityRenderer) livingRenderer).addLayer(new TemporalContractionAfterimageLayer<>(livingRenderer));
    }

    private static <T extends Entity & GeoAnimatable, S extends EntityRenderState, R extends GeoEntityRenderer<T, S>> R withParalyzedGeoLayer(R renderer) {
        renderer.withRenderLayer(new ParalyzedStoneGeoLayer<>(renderer));
        renderer.withRenderLayer(new GoopedGeoLayer<>(renderer));
        renderer.withRenderLayer(new RoyalIndicatorGeoLayer<>(renderer));
        renderer.withRenderLayer(new TemporalContractionGeoLayer<>(renderer));
        return renderer;
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(
                new BlockTintSource() {
                    @Override
                    public int color(BlockState state) {
                        return -1;
                    }

                    @Override
                    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                        return Fossils.color(
                                level.getBlockEntity(pos) instanceof FossilBlockEntity entity ? entity.entityId() : Fossils.entityAt(pos), 0);
                    }
                },
                new BlockTintSource() {
                    @Override
                    public int color(BlockState state) {
                        return -1;
                    }

                    @Override
                    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                        return Fossils.color(
                                level.getBlockEntity(pos) instanceof FossilBlockEntity entity ? entity.entityId() : Fossils.entityAt(pos), 1);
                    }
                }
        ), AntarchyNeoforgeBlocks.MYRMITE_FOSSIL.get(), AntarchyNeoforgeBlocks.BROODSTONE_FOSSIL.get());
        event.register(
                List.of(new BlockTintSource() {
                    @Override
                    public int color(BlockState state) {
                        return ARGB.opaque(bluestoneWireColor(state.getValue(BluestoneWireBlock.POWER)));
                    }

                    @Override
                    public Set<Property<?>> relevantProperties() {
                        return Set.of(BluestoneWireBlock.POWER);
                    }
                }),
                AntarchyNeoforgeBlocks.BLUESTONE_WIRE.get()
        );
        event.register(
                List.of(BlockTintSources.foliage()),
                AntarchyNeoforgeBlocks.OURANWOOD_LEAVES.get()
        );
        event.register(
                List.of(BlockTintSources.constant(0xFF4A0000)),
                AntarchyNeoforgeBlocks.ANTIWATER_BLOCK.get()
        );
        event.register(
                List.of(BlockTintSources.grass()),
                AntarchyNeoforgeBlocks.SPIDER_LILY.get()
        );
        event.register(
                List.of(BlockTintSources.grass()),
                AntarchyNeoforgeBlocks.POTTED_SPIDER_LILY.get()
        );
        event.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return RockiteTint.DEFAULT_COLOR;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return RockiteTint.color(pos.getY());
            }
        }), RockiteTint.blocks());
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Antarchy.MODID, "critter_cage"), CritterCageTintSource.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Antarchy.MODID, "fossil"), FossilTintSource.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Antarchy.MODID, "critter_cage_state"), CritterCageStateProperty.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Antarchy.MODID, "snout_compass_angle"), SnoutCompassAngleProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(AntarchyNeoforgeMisc.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.WIND_LINE.get(), WindLineParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.REMEMBRANCE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.REMEMBRANCE_MOTE.get(), RemembranceMoteParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.NIGHTMARE_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.STINKY_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.STINKY_FLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.PEACH_LEAVES_PARTICLE.get(), PeachLeavesParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.BUG_SPRAY_PARTICLE.get(), BugSprayParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LOTUS_POLLEN.get(), LotusPollenParticle.Provider::new);
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
        event.registerSpriteSet(AntarchyNeoforgeMisc.TEMPORAL_GLINT_SLOW.get(), TemporalGlintParticle::slow);
        event.registerSpriteSet(AntarchyNeoforgeMisc.TEMPORAL_GLINT_FAST.get(), TemporalGlintParticle::fast);
        event.registerSpriteSet(AntarchyNeoforgeMisc.ORANGE_ASH.get(), OrangeAshParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LUCID_BOLT_IMPACT_SMALL.get(), LucidBoltImpactParticle.SmallProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.LUCID_BOLT_IMPACT_LARGE.get(), LucidBoltImpactParticle.LargeProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.UNDERTRIAL_SPAWNER_DETECTION.get(), UndertrialParticle.DetectionProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.UNDERTRIAL_OMEN.get(), UndertrialParticle.OmenProvider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.UNDERVAULT_CONNECTION.get(), UndertrialParticle.OmenProvider::new);
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
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_crosshair"),
                (guiGraphics, partialTick) -> PortalGunCrosshairRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "dread_darkness"),
                (guiGraphics, partialTick) -> DreadHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "horde_intensity"),
                (guiGraphics, partialTick) -> HordeHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "paralyzed_stone"),
                (guiGraphics, partialTick) -> ParalyzedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "gooped_tint"),
                (guiGraphics, partialTick) -> GoopedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "glimmering_tint"),
                (guiGraphics, partialTick) -> GlimmeringHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "brutalfly_elytra"),
                (guiGraphics, partialTick) -> BrutalflyElytraHudRenderer.render(guiGraphics)
        );
        event.registerBelow(
                VanillaGuiLayers.CHAT,
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "bloodglass_hearts"),
                (guiGraphics, partialTick) -> BloodglassHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "flytrap_goo"),
                (guiGraphics, partialTick) -> FlytrapGooHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "springy_boots"),
                (guiGraphics, partialTick) -> SpringyBootsHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "dorrie_jump"),
                (guiGraphics, partialTick) -> DorrieJumpHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle_charge"),
                (guiGraphics, partialTick) -> HerculesBeetleChargeHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "king_judgment_flash"),
                (guiGraphics, partialTick) -> KingJudgmentFlashHudRenderer.render(guiGraphics)
        );
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        Antarchy.LOGGER.info("[Antiwater] Registering antiwater client fluid textures still={} flow={}", ANTIWATER_STILL, ANTIWATER_FLOW);
        event.register(new FluidModel.Unbaked(
                new Material(LUMEN_STILL, true),
                new Material(LUMEN_FLOW, true),
                new Material(WATER_OVERLAY),
                FluidTintSources.constant(0xFFFFFFFF)
        ), AntarchyNeoforgeMisc.LUMEN, AntarchyNeoforgeMisc.FLOWING_LUMEN);
        event.register(new FluidModel.Unbaked(
                new Material(ICHOR_STILL),
                new Material(ICHOR_FLOW),
                new Material(WATER_OVERLAY),
                FluidTintSources.constant(0xFFFFFFFF)
        ), AntarchyNeoforgeMisc.ICHOR, AntarchyNeoforgeMisc.FLOWING_ICHOR);
        event.register(new FluidModel.Unbaked(
                new Material(BILE_STILL),
                new Material(BILE_FLOW),
                new Material(WATER_OVERLAY),
                FluidTintSources.constant(0xFFFFFFFF)
        ), AntarchyNeoforgeMisc.BILE, AntarchyNeoforgeMisc.FLOWING_BILE);
        event.register(new FluidModel.Unbaked(
                new Material(WATER_STILL, true),
                new Material(WATER_FLOW, true),
                new Material(WATER_OVERLAY),
                FluidTintSources.constant(0xFFFF1A1A, 0xFF4A0000),
                ANTIWATER_RENDERER
        ), AntarchyNeoforgeMisc.ANTIWATER, AntarchyNeoforgeMisc.FLOWING_ANTIWATER);
    }

    private static final CustomFluidRenderer ANTIWATER_RENDERER = (fluidRenderer, fluidState, getter, pos, output, blockState) -> {
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        int tint = model.fluidTintSource() != null ? model.fluidTintSource().colorInWorld(fluidState, blockState, getter, pos) : -1;
        AntiwaterFluidRenderer.render(
                getter,
                pos,
                output.getBuilder(model.layer()),
                blockState,
                fluidState,
                model.stillMaterial().sprite(),
                model.flowingMaterial().sprite(),
                model.overlayMaterial() != null ? model.overlayMaterial().sprite() : model.flowingMaterial().sprite(),
                tint
        );
        return true;
    };

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                fluidFogColor.set(0.58F, 0.84F, 0.88F, fluidFogColor.w);
            }

            @Override
            public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                applyFluidFog(fogData, 0.4F, 12.0F);
            }
        }, AntarchyNeoForgeFluidTypes.LUMEN_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                fluidFogColor.set(0.16F, 0.02F, 0.03F, fluidFogColor.w);
            }

            @Override
            public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                applyFluidFog(fogData, 0.5F, 10.0F);
            }
        }, AntarchyNeoForgeFluidTypes.ICHOR_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                fluidFogColor.set(0.38F, 0.42F, 0.10F, fluidFogColor.w);
            }

            @Override
            public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                applyFluidFog(fogData, 0.25F, 4.0F);
            }
        }, AntarchyNeoForgeFluidTypes.BILE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_OVERLAY;
            }

            @Override
            public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                fluidFogColor.set(0.52F, 0.06F, 0.06F, fluidFogColor.w);
            }

            @Override
            public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                applyFluidFog(fogData, 0.5F, 12.0F);
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

    private static void applyFluidFog(FogData fogData, float start, float end) {
        fogData.environmentalStart = start;
        fogData.environmentalEnd = Math.min(fogData.renderDistanceEnd, end);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> TigerEyeClientHooks.setCamouflageKeyTextSupplier(() -> AntarchyKeyBindings.TIGERS_EYE_CAMOUFLAGE.getTranslatedKeyMessage()));
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Antarchy.MODID, "tiger_eye_camouflage_caches"), new SimplePreparableReloadListener<Void>() {
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

    private static void registerGeoItemExtension(RegisterClientExtensionsEvent event, Item item) {
        registerGeoItemExtension(event, item, null);
    }

    private static void registerGeoItemExtension(RegisterClientExtensionsEvent event, Item item, HumanoidModel.@Nullable ArmPose armPose) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return armPose;
            }
        }, item);
    }

    private static void registerGeoItemExtensionSizeRay(RegisterClientExtensionsEvent event, Item item) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
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
