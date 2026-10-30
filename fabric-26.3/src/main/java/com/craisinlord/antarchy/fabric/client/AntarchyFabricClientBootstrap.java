package com.craisinlord.antarchy.fabric.client;
import net.minecraft.world.entity.EntityTypes;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.AntarchyArchivePreviews;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.client.KingJudgmentFlashClientState;
import com.craisinlord.antarchy.content.client.SizeRaySoundHandler;
import com.craisinlord.antarchy.content.client.TigerEyeClientHooks;
import com.craisinlord.antarchy.content.client.renderer.*;
import com.craisinlord.antarchy.content.client.particle.*;
import com.craisinlord.antarchy.content.client.renderer.AntiwaterFluidRenderer;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import com.craisinlord.antarchy.content.client.renderer.UnderVaultRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import java.util.List;
import java.util.Set;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.GeoEntityRenderer;

public final class AntarchyFabricClientBootstrap {
    private AntarchyFabricClientBootstrap() {
    }

    public static void register() {
        AntarchyArchivePreviews.register();
        AntarchyKeyBindings.register();
        MenuScreens.register(AntarchyFabricMisc.DORRIE_INVENTORY_MENU.get(), com.craisinlord.antarchy.content.client.screen.DorrieInventoryScreen::new);
        MenuScreens.register(AntarchyFabricMisc.ROYAL_JUDGMENT_MENU.get(), com.craisinlord.antarchy.content.client.screen.RoyalJudgmentScreen::new);
        MenuScreens.register(AntarchyFabricMisc.GIANT_FRYING_PAN_MENU.get(), com.craisinlord.antarchy.content.client.screen.GiantFryingPanScreen::new);
        registerRenderers();
        registerEntityLayers();
        registerColors();
        registerParticles();
        registerFluids();
        registerItemProperties();
        registerClientCallbacks();
    }

    private static void registerRenderers() {
        BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) CampfireRenderer::new
        );
        BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricBlocks.SEASHELL_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) SeashellRenderer::new
        );
        BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricBlocks.LUCID_ANCHOR_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) LucidAnchorBlockEntityRenderer::new
        );
        BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricBlocks.CRITTER_CAGE_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) CritterCageRenderer::new
        );
        BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricBlocks.UNDERTRIAL_SPAWNER_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) UndertrialSpawnerRenderer::new
        );
        BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricBlocks.UNDERVAULT_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) UnderVaultRenderer::new
        );

        EntityRendererRegistry.register(AntarchyFabricEntities.EASTER_BUNNY.get(), context -> withTemporalGeoLayer(new EasterBunnyRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.FLYING_SQUIRREL.get(), context -> withTemporalGeoLayer(new FlyingSquirrelRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.CATERPILLAR.get(), context -> withTemporalGeoLayer(new CaterpillarRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.BUTTERFLY.get(), context -> withTemporalGeoLayer(new ButterflyRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.REVERIE.get(), context -> withTemporalGeoLayer(new ReverieRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.BRUTALFLY.get(), context -> withTemporalGeoLayer(new BrutalflyRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.RED_ANT.get(), context -> withTemporalGeoLayer(new AntRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.BROWN_ANT.get(), context -> withTemporalGeoLayer(new AntRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.RAINBOW_ANT.get(), context -> withTemporalGeoLayer(new AntRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.MOLEWORM.get(), context -> withTemporalGeoLayer(new MolewormRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.WORM.get(), context -> withTemporalGeoLayer(new WormRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.MANTIS.get(), context -> withTemporalGeoLayer(new MantisRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.ALPHA_MANTIS.get(), context -> withTemporalGeoLayer(new AlphaMantisRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.MOLEVORE.get(), context -> withTemporalGeoLayer(new MolevoreRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.FLYTRAP.get(), context -> withTemporalGeoLayer(new FlytrapRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.OURANWOOD_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/ouranwood.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.OURANWOOD_CHEST_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.PEACH_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/peach.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.PEACH_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/peach.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.NADIR_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/nadir.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.NADIR_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/nadir.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/royal.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/royal.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.TRUFFALO_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/truffalo.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.TRUFFALO_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/truffalo.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.APPLE_COW.get(), context -> withTemporalGeoLayer(new AppleCowRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.GOLDEN_APPLE_COW.get(), context -> withTemporalGeoLayer(new AppleCowRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), context -> withTemporalGeoLayer(new AppleCowRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.DIAMOND_MINECART.get(), DiamondMinecartRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.DR_TRAYAURUS.get(), context -> new DrTrayaurusRenderer(context, Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dr_trayaurus.png")));
        EntityRendererRegistry.register(AntarchyFabricEntities.STRATOSHARK.get(), context -> withTemporalGeoLayer(new StratosharkRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.KRAKEN.get(), context -> withTemporalGeoLayer(new KrakenRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.TENTACLE.get(), com.craisinlord.antarchy.content.client.renderer.TentacleRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.KRAKENS_GRASP_TRIDENT.get(), com.craisinlord.antarchy.content.client.renderer.KrakensGraspThrownTridentRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.MISSILE_SQUID.get(), context -> withTemporalGeoLayer(new MissileSquidRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.OCTOPUS_BOMB.get(), context -> withTemporalGeoLayer(new OctopusBombRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.NIGHTMARE.get(), context -> withTemporalGeoLayer(new NightmareRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.NIGHTMARE_PORTAL.get(), NightmarePortalRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.DIMENSIONAL_TEAR.get(), DimensionalTearRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.PORTAL_GUN_PORTAL.get(), PortalGunPortalRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.HOVERBOARD.get(), com.craisinlord.antarchy.content.client.renderer.HoverboardRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.PORTAL_GUN_PROJECTILE.get(), com.craisinlord.antarchy.content.client.renderer.PortalGunProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.NIGHTMARE_BITE.get(), NightmareBiteRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LUCID.get(), context -> withTemporalGeoLayer(new LucidRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.VORTEX.get(), context -> withTemporalGeoLayer(new VortexRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.BED_BUG.get(), context -> withTemporalGeoLayer(new BedBugRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.WASP.get(), context -> withTemporalGeoLayer(new WaspRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.BOMBER.get(), context -> withTemporalGeoLayer(new BomberRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.BASILISK.get(), context -> withTemporalGeoLayer(new BasiliskRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.SHRINK_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.GROWTH_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LUCID_BOLT.get(), LucidBoltRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LUCID_PEARL_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.VORTEX_CHARGE_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.WIND_VORTEX.get(), WindVortexRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_BLACK_HOLE.get(), com.craisinlord.antarchy.content.client.renderer.RoyalBlackHoleRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_ASSAILANT_BLACK_HOLE.get(), com.craisinlord.antarchy.content.client.renderer.RoyalAssailantBlackHoleRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.CRITTER_CAGE_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LAUNCHED_ANVIL.get(), com.craisinlord.antarchy.content.client.renderer.LaunchedAnvilRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.WORM_HOOK_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.HUSH_PROJECTILE.get(), HushProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BRUTALFLY_ORB.get(), BrutalflyOrbRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.UPWARD_FALLING_BLOCK.get(), UpwardFallingBlockRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.SCORPION.get(), context -> withTemporalGeoLayer(new ScorpionRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.EMPEROR_SCORPION.get(), context -> withTemporalGeoLayer(new EmperorScorpionRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.TORETERROR.get(), context -> withTemporalGeoLayer(new ToreterrorRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.WATER_BOMB.get(), WaterBombRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.CHEEP.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.CheepRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.DORRIE.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.DorrieRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.OURANWOOD_DEER.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.OuranwoodDeerRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.GLIMMER.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.glimmer.GlimmerRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.ELKA.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.ElkaRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.ROLLY_POLLY.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.RollyPollyRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.TERMITE.get(), context -> withTemporalGeoLayer(new TermiteRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.CRAWLING_BLIGHT.get(), context -> withTemporalGeoLayer(new CrawlingBlightRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.SKULKING_FRIGHT.get(), context -> withTemporalGeoLayer(new SkulkingFrightRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.MANTICORE.get(), context -> withTemporalGeoLayer(new com.craisinlord.antarchy.content.client.renderer.ManticoreRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.HERCULES_BEETLE.get(), context -> withTemporalGeoLayer(new HerculesBeetleRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.PRINCE.get(), com.craisinlord.antarchy.content.client.renderer.RoyalMountRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.PRINCESS.get(), com.craisinlord.antarchy.content.client.renderer.RoyalMountRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.KING.get(), com.craisinlord.antarchy.content.client.renderer.RoyalBossRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.QUEEN.get(), com.craisinlord.antarchy.content.client.renderer.RoyalBossRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_BOLT.get(), com.craisinlord.antarchy.content.client.renderer.RoyalBoltRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_ELEMENTAL_PROJECTILE.get(), com.craisinlord.antarchy.content.client.renderer.RoyalElementalProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ROYAL_ICE_SPIKE.get(), com.craisinlord.antarchy.content.client.renderer.RoyalIceSpikeRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.JERRY.get(), context -> withTemporalGeoLayer(new JerryRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.SPRINGBUG.get(), context -> withTemporalGeoLayer(new SpringbugRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.SPIT_BUG.get(), context -> withTemporalGeoLayer(new SpitBugRenderer(context)));
        EntityRendererRegistry.register(AntarchyFabricEntities.SPIT_BUG_PROJECTILE.get(), SpitBugProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.STINK_BUG.get(), StinkBugRenderer::new);

        ModelLayerRegistry.registerModelLayer(OuranwoodBoatRenderer.boatLayer(), BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(OuranwoodBoatRenderer.chestBoatLayer(), BoatModel::createChestBoatModel);
        ModelLayerRegistry.registerModelLayer(PeachBoatRenderer.boatLayer(), BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(PeachBoatRenderer.chestBoatLayer(), BoatModel::createChestBoatModel);
    }

    private static <T extends Entity & GeoAnimatable, S extends EntityRenderState, R extends GeoEntityRenderer<T, S>> R withTemporalGeoLayer(R renderer) {
        renderer.withRenderLayer(new TemporalContractionGeoLayer<>(renderer));
        return renderer;
    }

    private static void registerColors() {
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return bluestoneWireColor(state.getValue(com.craisinlord.antarchy.content.block.BluestoneWireBlock.POWER));
            }

            @Override
            public Set<Property<?>> relevantProperties() {
                return Set.of(com.craisinlord.antarchy.content.block.BluestoneWireBlock.POWER);
            }
        }), AntarchyFabricBlocks.BLUESTONE_WIRE.get());
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return FoliageColor.FOLIAGE_DEFAULT;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageFoliageColor(level, pos);
            }
        }), AntarchyFabricBlocks.OURANWOOD_LEAVES.get());
        BlockColorRegistry.register(List.of(BlockTintSources.constant(0xFF4A0000)), AntarchyFabricBlocks.ANTIWATER_BLOCK.get());
        BlockColorRegistry.register(List.of(BlockTintSources.grass()), AntarchyFabricBlocks.SPIDER_LILY.get());
        BlockColorRegistry.register(List.of(BlockTintSources.grass()), AntarchyFabricBlocks.POTTED_SPIDER_LILY.get());
        ItemTintSources.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antarchy.MODID, "critter_cage"), CritterCageTintSource.MAP_CODEC);
    }

    private static void registerFluids() {
        FluidRenderingRegistry.register(
                AntarchyFabricMisc.BILE.get(),
                AntarchyFabricMisc.FLOWING_BILE.get(),
                translucentFluidModel(
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still"),
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing"),
                        Identifier.withDefaultNamespace("block/water_overlay"),
                        null
                )
        );

        FluidRenderingRegistry.register(
                AntarchyFabricMisc.ICHOR.get(),
                AntarchyFabricMisc.FLOWING_ICHOR.get(),
                translucentFluidModel(
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/ichor/ichor_still"),
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/ichor/ichor_flowing"),
                        Identifier.withDefaultNamespace("block/water_overlay"),
                        null
                )
        );

        FluidRenderingRegistry.register(
                AntarchyFabricMisc.ANTIWATER.get(),
                AntarchyFabricMisc.FLOWING_ANTIWATER.get(),
                translucentFluidModel(
                        Identifier.withDefaultNamespace("block/water_still"),
                        Identifier.withDefaultNamespace("block/water_flow"),
                        Identifier.withDefaultNamespace("block/water_overlay"),
                        BlockTintSources.constant(0xFFFF1A1A)
                ),
                new FluidRenderHandler() {
                    @Override
                    public void renderFluid(FluidRenderer fluidRenderer, BlockPos pos, BlockAndTintGetter world, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
                        FluidModel model = fluidRenderer.fluidModels.get(fluidState);
                        int color = model.tintSource() != null ? model.tintSource().colorInWorld(blockState, world, pos) : -1;
                        AntiwaterFluidRenderer.render(
                                world,
                                pos,
                                output.getBuilder(model.layer()),
                                blockState,
                                fluidState,
                                model.stillMaterial().sprite(),
                                model.flowingMaterial().sprite(),
                                model.overlayMaterial() != null ? model.overlayMaterial().sprite() : null,
                                color
                        );
                    }
                }
        );

        FluidRenderingRegistry.register(
                AntarchyFabricMisc.LUMEN.get(),
                AntarchyFabricMisc.FLOWING_LUMEN.get(),
                translucentFluidModel(
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_still"),
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_flow"),
                        Identifier.withDefaultNamespace("block/water_overlay"),
                        null
                )
        );
    }

    private static FluidModel.Unbaked translucentFluidModel(Identifier still, Identifier flowing, Identifier overlay, BlockTintSource tint) {
        return new FluidModel.Unbaked(new Material(still, true), new Material(flowing, true), new Material(overlay, true), tint);
    }

    private static void registerParticles() {
        ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
        registry.register(AntarchyFabricMisc.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        registry.register(AntarchyFabricMisc.NIGHTMARE_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        registry.register(AntarchyFabricMisc.STINKY_GAS.get(), HypnoticGasParticle.Provider::new);
        registry.register(AntarchyFabricMisc.STINKY_FLY.get(), FireflyParticle.Provider::new);
        registry.register(AntarchyFabricMisc.PEACH_LEAVES_PARTICLE.get(), PeachLeavesParticle.Provider::new);
        registry.register(AntarchyFabricMisc.BUG_SPRAY_PARTICLE.get(), com.craisinlord.antarchy.content.client.particle.BugSprayParticle.Provider::new);
        registry.register(AntarchyFabricMisc.LOTUS_POLLEN.get(), com.craisinlord.antarchy.content.client.particle.LotusPollenParticle.Provider::new);
        registry.register(AntarchyFabricMisc.HYPNOTIC_GAS.get(), HypnoticGasParticle.Provider::new);
        registry.register(AntarchyFabricMisc.HYPNOTIC_GAS_DOWN.get(), sprites -> new HypnoticGasParticle.Provider(sprites, true));
        registry.register(AntarchyFabricMisc.HYPNOTIC_GAS_CLOUD.get(), new HypnoticGasCloudParticle.Provider());
        registry.register(AntarchyFabricMisc.HYPNOTIC_GAS_CLOUD_DOWN.get(), new HypnoticGasCloudParticle.Provider(true));
        registry.register(AntarchyFabricMisc.INVERTED_GEYSER_BASE.get(), InvertedGeyserBaseParticle.Provider::new);
        registry.register(AntarchyFabricMisc.INVERTED_GEYSER_PLUME.get(), InvertedGeyserPlumeParticle.Provider::new);
        registry.register(AntarchyFabricMisc.INVERTED_GEYSER_POOF.get(), InvertedGeyserBaseParticle.Provider::new);
        registry.register(AntarchyFabricMisc.INVERTED_GEYSER_ERUPTION.get(), new InvertedGeyserEruptionParticle.Provider());
        registry.register(AntarchyFabricMisc.FIREFLY.get(), FireflyParticle.Provider::new);
        registry.register(AntarchyFabricMisc.GROUND_MIST.get(), com.craisinlord.antarchy.content.client.particle.GroundMistParticle.Provider::new);
        registry.register(AntarchyFabricMisc.ORANGE_ASH.get(), OrangeAshParticle.Provider::new);
        registry.register(AntarchyFabricMisc.LUCID_BOLT_IMPACT_SMALL.get(), LucidBoltImpactParticle.SmallProvider::new);
        registry.register(AntarchyFabricMisc.LUCID_BOLT_IMPACT_LARGE.get(), LucidBoltImpactParticle.LargeProvider::new);
        registry.register(AntarchyFabricMisc.UNDERTRIAL_SPAWNER_DETECTION.get(), UndertrialParticle.DetectionProvider::new);
        registry.register(AntarchyFabricMisc.UNDERTRIAL_OMEN.get(), UndertrialParticle.OmenProvider::new);
        registry.register(AntarchyFabricMisc.UNDERVAULT_CONNECTION.get(), UndertrialParticle.OmenProvider::new);
    }

    private static void registerItemProperties() {
        RangeSelectItemModelProperties.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antarchy.MODID, "critter_cage_state"), CritterCageStateProperty.MAP_CODEC);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerEntityLayers() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            RenderLayerParent parent = (RenderLayerParent) renderer;
            if (entityType == EntityTypes.PLAYER) {
                if (renderer instanceof AvatarRenderer<?> playerRenderer) {
                    RenderLayerParent playerParent = (RenderLayerParent) playerRenderer;
                    registrationHelper.register((RenderLayer) new ParalyzedStonePlayerLayer(playerParent));
                    registrationHelper.register((RenderLayer) new TigerEyeCamouflagePlayerLayer(playerParent));
                    registrationHelper.register((RenderLayer) new GoopedLivingLayer(playerParent));
                    registrationHelper.register((RenderLayer) new GlimmeringLivingLayer(playerParent));
                    registrationHelper.register((RenderLayer) new com.craisinlord.antarchy.content.client.renderer.RoyalIndicatorLivingLayer(playerParent));
                    registrationHelper.register((RenderLayer) new BrutalflyElytraLayer(playerParent));
                    registrationHelper.register((RenderLayer) new com.craisinlord.antarchy.content.client.renderer.ManticoreWingsLayer(playerParent));
                    registrationHelper.register((RenderLayer) new FallenKingCrownLayer(playerParent, context.getBlockModelResolver()));
                }
                return;
            }
            if (entityType == EntityTypes.ARMOR_STAND && renderer instanceof ArmorStandRenderer armorStandRenderer) {
                registrationHelper.register(new FallenKingCrownArmorStandLayer(armorStandRenderer, context.getBlockModelResolver()));
                return;
            }
            if (GeoEntityRenderer.class.isAssignableFrom(renderer.getClass())) {
                GeoEntityRenderer geoRenderer = (GeoEntityRenderer) (Object) renderer;
                geoRenderer.withRenderLayer(new ParalyzedStoneGeoLayer(geoRenderer));
                geoRenderer.withRenderLayer(new GoopedGeoLayer(geoRenderer));
                geoRenderer.withRenderLayer(new com.craisinlord.antarchy.content.client.renderer.RoyalIndicatorGeoLayer(geoRenderer));
                return;
            }
            registrationHelper.register((RenderLayer) new ParalyzedStoneLivingLayer(parent));
            registrationHelper.register((RenderLayer) new GoopedLivingLayer(parent));
            registrationHelper.register((RenderLayer) new GlimmeringLivingLayer(parent));
            registrationHelper.register((RenderLayer) new com.craisinlord.antarchy.content.client.renderer.RoyalIndicatorLivingLayer(parent));
        });
    }

    private static void registerClientCallbacks() {
        TigerEyeClientHooks.setCamouflageKeyTextSupplier(() -> AntarchyKeyBindings.TIGERS_EYE_CAMOUFLAGE.getTranslatedKeyMessage());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HerculesBeetleImpactShakeClientState.tick();
            KingJudgmentFlashClientState.tick();
            HordeClientState.tick();
            com.craisinlord.antarchy.content.client.ElythiaEventClient.tick();
            if (client.player != null) {
                ParalyzedClientHandler.clampPlayerInput(client.player);
                com.craisinlord.antarchy.content.item.BigBerthaItem.clientTick(client.player);
                com.craisinlord.antarchy.content.item.AnvilCannonItem.clientTick(client.player);
            }
            BrutalflyElytraClientHandler.tick();
            SpringyBootsClientHandler.tick();
            DiamondMinecartClientHandler.tick();
            ParalyzedClientHandler.tick();
            DreadClientHandler.tick();
            TigerEyeCamouflageClientHandler.tick();
            RoyalInversionClientHandler.tick();
            RoyalBoundaryClientHandler.tick();
            BloodCrystalKatanaTrailHandler.tick();
            ScorpionWhipTetherRenderHandler.tick();
            WormHookTetherRenderHandler.tick();
            DorrieJumpClientHandler.tick();
            HerculesBeetleClientHandler.tick();
            RoyalMountClientHandler.tick();
            GiantFryingPanClientHandler.tick();
            SizeRaySoundHandler.tick(client);
        });

        BigBerthaClientHandler.register();
        GravityGunClientHandler.register();
        PortalGunClientHandler.register();
        EyeOfStormClientHandler.register();

        ElythiaFireflyManager.register();
        LucidSoundHandler.register();
        TearIdleSoundHandler.register();
        MogglesClientRenderer.register();
        StinkySoundHandler.register();
        ReverieTrailHandler.register();
        HoverboardTrailHandler.register();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(Antarchy.MODID, "hud"), (guiGraphics, deltaTracker) -> {
            DreadHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.HordeHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.GoopedHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.GlimmeringHudRenderer.render(guiGraphics);
            ParalyzedHudRenderer.render(guiGraphics);
            BrutalflyElytraHudRenderer.render(guiGraphics);
            SpringyBootsHudRenderer.render(guiGraphics);
            FlytrapGooHudRenderer.render(guiGraphics);
            DorrieJumpHudRenderer.render(guiGraphics);
            HerculesBeetleChargeHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.KingJudgmentFlashHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.PortalGunCrosshairRenderer.render(guiGraphics);
        });

        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            BloodCrystalKatanaTrailHandler.render(context);
            ScorpionWhipTetherRenderHandler.render(context);
            WormHookTetherRenderHandler.render(context);
        });
    }

    private static int bluestoneWireColor(int power) {
        float normalized = power / 15.0F;
        int red = (int) (6.0F + normalized * 54.0F);
        int green = (int) (22.0F + normalized * 132.0F);
        int blue = (int) (64.0F + normalized * 191.0F);
        return ARGB.color(255, red, green, blue);
    }
}
