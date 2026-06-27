package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.hud.BloodglassHudRenderer;
import com.craisinlord.antarchy.content.client.particle.*;
import com.craisinlord.antarchy.content.client.renderer.*;
import com.craisinlord.antarchy.content.client.renderer.CreepingHorrorRenderer;
import com.craisinlord.antarchy.content.client.renderer.LurkingTerrorRenderer;
import com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeBlocks;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeEntites;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeItems;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

@EventBusSubscriber(modid = Antarchy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AntarchyNeoForgeClient {
    private static final ResourceLocation DR_TRAYAURUS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dr_trayaurus.png");
    private static final ResourceLocation OURANWOOD_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/ouranwood.png");
    private static final ResourceLocation OURANWOOD_CHEST_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png");
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final ResourceLocation UNDERWATER_OVERLAY = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    private static final ResourceLocation ANTIWATER_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_still");
    private static final ResourceLocation ANTIWATER_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_flow");
    private static final ResourceLocation BILE_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still");
    private static final ResourceLocation BILE_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing");
    private AntarchyNeoForgeClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AntarchyNeoforgeBlocks.DREAM_CAMPFIRE_BLOCK_ENTITY.get(), CampfireRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.EASTER_BUNNY.get(), context -> withParalyzedGeoLayer(new EasterBunnyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.FLYING_SQUIRREL.get(), context -> withParalyzedGeoLayer(new FlyingSquirrelRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.CATERPILLAR.get(), context -> withParalyzedGeoLayer(new CaterpillarRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BUTTERFLY.get(), context -> withParalyzedGeoLayer(new ButterflyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.REVERIE.get(), context -> withParalyzedGeoLayer(new ReverieRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BRUTALFLY.get(), context -> withParalyzedGeoLayer(new BrutalflyRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.RED_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BROWN_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.RAINBOW_ANT.get(), context -> withParalyzedGeoLayer(new AntRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.TERMITE.get(), context -> withParalyzedGeoLayer(new TermiteRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.MOLEWORM.get(), context -> withParalyzedGeoLayer(new MolewormRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.MANTIS.get(), context -> withParalyzedGeoLayer(new MantisRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.MOLEVORE.get(), context -> withParalyzedGeoLayer(new MolevoreRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.TRIFFID.get(), context -> withParalyzedGeoLayer(new TriffidRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.OURANWOOD_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, OURANWOOD_BOAT_TEXTURE, false));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.OURANWOOD_CHEST_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, OURANWOOD_CHEST_BOAT_TEXTURE, true));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.GOLDEN_APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.ENCHANTED_GOLDEN_APPLE_COW.get(), context -> withParalyzedGeoLayer(new AppleCowRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.DIAMOND_MINECART.get(), DiamondMinecartRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.DR_TRAYAURUS.get(), context -> new DrTrayaurusRenderer(context, DR_TRAYAURUS_TEXTURE));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.CLOUD_SHARK.get(), context -> withParalyzedGeoLayer(new CloudSharkRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.KRAKEN.get(), context -> withParalyzedGeoLayer(new KrakenRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.MISSILE_SQUID.get(), context -> withParalyzedGeoLayer(new MissileSquidRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.OCTOPUS_BOMB.get(), context -> withParalyzedGeoLayer(new OctopusBombRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.NIGHTMARE.get(), context -> withParalyzedGeoLayer(new NightmareRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.LUCID.get(), context -> withParalyzedGeoLayer(new LucidRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BED_BUG.get(), context -> withParalyzedGeoLayer(new BedBugRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.STINK_BUG.get(), context -> withParalyzedGeoLayer(new StinkBugRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.WASP.get(), context -> withParalyzedGeoLayer(new WaspRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BOMBER.get(), context -> withParalyzedGeoLayer(new BomberRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.JUMPY_BUG.get(), context -> withParalyzedGeoLayer(new JumpyBugRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BASILISK.get(), context -> withParalyzedGeoLayer(new BasiliskRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.SHRINK_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.GROWTH_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.LUCID_BOLT.get(), LucidBoltRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.LUCID_PEARL_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.HUSH_PROJECTILE.get(), HushProjectileRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.BRUTALFLY_ORB.get(), BrutalflyOrbRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.UPWARD_FALLING_BLOCK.get(), com.craisinlord.antarchy.content.client.renderer.UpwardFallingBlockRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.SCORPION.get(), context -> withParalyzedGeoLayer(new ScorpionRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.EMPEROR_SCORPION.get(), context -> withParalyzedGeoLayer(new EmperorScorpionRenderer(context)));
        event.registerEntityRenderer(AntarchyNeoforgeEntites.TORETERROR.get(), ToreterrorRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.WATER_BOMB.get(), WaterBombRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.CREEPING_HORROR.get(), CreepingHorrorRenderer::new);
        event.registerEntityRenderer(AntarchyNeoforgeEntites.LURKING_TERROR.get(), LurkingTerrorRenderer::new);
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer =
                    (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new ParalyzedStonePlayerLayer(renderer));
                renderer.addLayer(new BrutalflyElytraLayer(renderer));
                renderer.addLayer(new FallenKingCrownLayer(renderer));
            }
        }
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> addParalyzedLayerToEntity(event, entityType));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OuranwoodBoatRenderer.boatLayer(), BoatModel::createBodyModel);
        event.registerLayerDefinition(OuranwoodBoatRenderer.chestBoatLayer(), ChestBoatModel::createBodyModel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends Entity> void addParalyzedLayerToEntity(EntityRenderersEvent.AddLayers event, EntityType<E> entityType) {
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
    }

    private static <T extends LivingEntity & GeoAnimatable> GeoEntityRenderer<T> withParalyzedGeoLayer(GeoEntityRenderer<T> renderer) {
        renderer.addRenderLayer(new ParalyzedStoneGeoLayer<>(renderer));
        return renderer;
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
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
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> FoliageColor.getDefaultColor(),
                AntarchyNeoforgeItems.OURANWOOD_LEAVES_ITEM.get()
        );
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyNeoforgeItems.ICHOR_BUCKET.get());
        event.register(new DynamicFluidContainerModel.Colors(), AntarchyNeoforgeItems.ANTIWATER_BUCKET.get());
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(AntarchyNeoforgeMisc.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.STINKY_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.STINKY_FLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.HYPNOTIC_GAS.get(), HypnoticGasParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.HYPNOTIC_GAS_DOWN.get(), sprites -> new HypnoticGasParticle.Provider(sprites, true));
        event.registerSpecial(AntarchyNeoforgeMisc.HYPNOTIC_GAS_CLOUD.get(), new HypnoticGasCloudParticle.Provider());
        event.registerSpecial(AntarchyNeoforgeMisc.HYPNOTIC_GAS_CLOUD_DOWN.get(), new HypnoticGasCloudParticle.Provider(true));
        event.registerSpriteSet(AntarchyNeoforgeMisc.INVERTED_GEYSER_BASE.get(), InvertedGeyserBaseParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.INVERTED_GEYSER_PLUME.get(), InvertedGeyserPlumeParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.INVERTED_GEYSER_POOF.get(), InvertedGeyserBaseParticle.Provider::new);
        event.registerSpecial(AntarchyNeoforgeMisc.INVERTED_GEYSER_ERUPTION.get(), new InvertedGeyserEruptionParticle.Provider());
        event.registerSpriteSet(AntarchyNeoforgeMisc.FIREFLY.get(), FireflyParticle.Provider::new);
        event.registerSpriteSet(AntarchyNeoforgeMisc.ORANGE_ASH.get(), OrangeAshParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dread_darkness"),
                (guiGraphics, partialTick) -> DreadHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "paralyzed_stone"),
                (guiGraphics, partialTick) -> ParalyzedHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "brutalfly_elytra"),
                (guiGraphics, partialTick) -> BrutalflyElytraHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bloodglass_hearts"),
                (guiGraphics, partialTick) -> BloodglassHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "triffid_goo"),
                (guiGraphics, partialTick) -> TriffidGooHudRenderer.render(guiGraphics)
        );
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_boots"),
                (guiGraphics, partialTick) -> JumpyBootsHudRenderer.render(guiGraphics)
        );
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Antarchy.LOGGER.info("[Antiwater] Registering antiwater client fluid textures still={} flow={}", ANTIWATER_STILL, ANTIWATER_FLOW);
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ANTIWATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ANTIWATER_FLOW;
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
                return 0xFF2A0306;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, net.minecraft.core.BlockPos pos) {
                return 0xFF2A0306;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.16F, 0.02F, 0.03F);
            }

            @Override
            public void modifyFogRender(Camera camera, net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
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
            public int getTintColor(FluidState state, BlockAndTintGetter getter, net.minecraft.core.BlockPos pos) {
                return 0xFFFFFFFF;
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
            public int getTintColor(FluidState state, BlockAndTintGetter getter, net.minecraft.core.BlockPos pos) {
                return 0xFFFF1A1A;
            }

            @Override
            public boolean renderFluid(FluidState fluidState, BlockAndTintGetter getter, net.minecraft.core.BlockPos pos, VertexConsumer vertexConsumer, net.minecraft.world.level.block.state.BlockState blockState) {
                var sprites = net.neoforged.neoforge.client.textures.FluidSpriteCache.getFluidSprites(getter, pos, fluidState);
                int tint = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluidState).getTintColor(fluidState, getter, pos);
                AntiwaterFluidRenderer.render(getter, pos, vertexConsumer, blockState, fluidState, sprites[0], sprites[1], sprites[2], tint);
                return true;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.52F, 0.06F, 0.06F);
            }

            @Override
            public void modifyFogRender(Camera camera, net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.5F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 12.0F));
                RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            }
        }, AntarchyNeoForgeFluidTypes.ANTIWATER_TYPE.get());

        registerGeoItemExtensionSizeRay(event, AntarchyNeoforgeItems.SHRINK_RAY.get());
        registerGeoItemExtensionSizeRay(event, AntarchyNeoforgeItems.GROWTH_RAY.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.GRAVITY_GUN.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.SQUIDZOOKA.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.WATER_CANNON.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        registerGeoItemExtension(event, AntarchyNeoforgeItems.BATTLE_AXE.get());
        registerGeoItemExtension(event, AntarchyNeoforgeItems.BIG_BERTHA.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.OURANWOOD_ACORN_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DUPLICATOR_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ORANGE_MILKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.PINK_MILKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TORCHFLOWER_BUSH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.HUSHWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CORNEA_STALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIMETAL_SCAFFOLDING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.UMBRAL_MOSS_CARPET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.AMBER_MOSS_CARPET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.AMBER_LICHEN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BILE_VEIN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CREEPVINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.NYXITE_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CHITEN_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.BLOOD_CRYSTAL_CRYSTAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_CAMPFIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.DREAM_CEILING_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.ANTIWATER_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TRIFFID_GOO_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.URANIUM_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TITANIUM_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.URANIUM_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TITANIUM_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.URANIUM_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.TITANIUM_BARS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(AntarchyNeoforgeBlocks.CLOUD_BLOCK.get(), RenderType.translucent());
            registerUltimateBowProperties();
            registerUltimateCrossbowProperties();
        });
    }

    private static void registerUltimateBowProperties() {
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
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack itemStack) {
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
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack itemStack) {
                if (entityLiving.isUsingItem() && entityLiving.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
                return null;
            }
        }, item);
    }
}
