package com.craisinlord.antarchy.fabric.client;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.client.TigerEyeClientHooks;
import com.craisinlord.antarchy.content.client.renderer.*;
import com.craisinlord.antarchy.content.client.particle.*;
import com.craisinlord.antarchy.content.client.renderer.AntiwaterFluidRenderer;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import com.craisinlord.antarchy.fabric.client.renderer.MultipartPartRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.world.entity.EntityType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.FoliageColor;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.BlockAndTintGetter;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;

public final class AntarchyFabricClientBootstrap {
    private AntarchyFabricClientBootstrap() {
    }

    public static void register() {
        AntarchyKeyBindings.register();
        MenuScreens.register(AntarchyFabricMisc.DORRIE_INVENTORY_MENU.get(), com.craisinlord.antarchy.content.client.screen.DorrieInventoryScreen::new);
        registerRenderers();
        registerEntityLayers();
        registerColors();
        registerParticles();
        registerFluids();
        registerRenderLayers();
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

        EntityRendererRegistry.register(AntarchyFabricEntities.EASTER_BUNNY.get(), EasterBunnyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.FLYING_SQUIRREL.get(), FlyingSquirrelRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.CATERPILLAR.get(), CaterpillarRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BUTTERFLY.get(), ButterflyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.REVERIE.get(), ReverieRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BRUTALFLY.get(), BrutalflyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.RED_ANT.get(), AntRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BROWN_ANT.get(), AntRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.RAINBOW_ANT.get(), AntRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.MOLEWORM.get(), MolewormRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.MANTIS.get(), MantisRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ALPHA_MANTIS.get(), AlphaMantisRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.MOLEVORE.get(), MolevoreRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.TRIFFID.get(), TriffidRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.OURANWOOD_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/ouranwood.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.OURANWOOD_CHEST_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.PEACH_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/peach.png"), false));
        EntityRendererRegistry.register(AntarchyFabricEntities.PEACH_CHEST_BOAT_ENTITY.get(), context -> new PeachBoatRenderer<>(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/peach.png"), true));
        EntityRendererRegistry.register(AntarchyFabricEntities.APPLE_COW.get(), AppleCowRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.GOLDEN_APPLE_COW.get(), AppleCowRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), AppleCowRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.DIAMOND_MINECART.get(), DiamondMinecartRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.DR_TRAYAURUS.get(), context -> new DrTrayaurusRenderer(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dr_trayaurus.png")));
        EntityRendererRegistry.register(AntarchyFabricEntities.CLOUD_SHARK.get(), CloudSharkRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.KRAKEN.get(), KrakenRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.KRAKEN_PART.get(), MultipartPartRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.TENTACLE.get(), com.craisinlord.antarchy.content.client.renderer.TentacleRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.KRAKENS_GRASP_TRIDENT.get(), com.craisinlord.antarchy.content.client.renderer.KrakensGraspThrownTridentRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.MISSILE_SQUID.get(), MissileSquidRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.OCTOPUS_BOMB.get(), OctopusBombRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.NIGHTMARE.get(), NightmareRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.NIGHTMARE_PORTAL.get(), NightmarePortalRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.NIGHTMARE_BITE.get(), NightmareBiteRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LUCID.get(), LucidRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BED_BUG.get(), BedBugRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.WASP.get(), WaspRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BOMBER.get(), BomberRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BASILISK.get(), BasiliskRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.SHRINK_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.GROWTH_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LUCID_BOLT.get(), LucidBoltRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LUCID_PEARL_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.CRITTER_CAGE_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.HUSH_PROJECTILE.get(), HushProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.BRUTALFLY_ORB.get(), BrutalflyOrbRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.UPWARD_FALLING_BLOCK.get(), UpwardFallingBlockRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.SCORPION.get(), ScorpionRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.EMPEROR_SCORPION.get(), EmperorScorpionRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.TORETERROR.get(), ToreterrorRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.WATER_BOMB.get(), WaterBombRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.CHEEP.get(), com.craisinlord.antarchy.content.client.renderer.CheepRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.DORRIE.get(), com.craisinlord.antarchy.content.client.renderer.DorrieRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.OURANWOOD_DEER.get(), com.craisinlord.antarchy.content.client.renderer.OuranwoodDeerRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.GLIMMER.get(), com.craisinlord.antarchy.content.client.renderer.glimmer.GlimmerRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ELKA.get(), com.craisinlord.antarchy.content.client.renderer.ElkaRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.ROLLY_POLLY.get(), com.craisinlord.antarchy.content.client.renderer.RollyPollyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.TERMITE.get(), TermiteRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.CREEPING_HORROR.get(), CreepingHorrorRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.LURKING_TERROR.get(), LurkingTerrorRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.HERCULES_BEETLE.get(), HerculesBeetleRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.JERRY.get(), JerryRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.JUMPY_BUG.get(), JumpyBugRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.SPIT_BUG.get(), SpitBugRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.SPIT_BUG_PROJECTILE.get(), SpitBugProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricEntities.STINK_BUG.get(), StinkBugRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(OuranwoodBoatRenderer.boatLayer(), BoatModel::createBodyModel);
        EntityModelLayerRegistry.registerModelLayer(OuranwoodBoatRenderer.chestBoatLayer(), ChestBoatModel::createBodyModel);
        EntityModelLayerRegistry.registerModelLayer(PeachBoatRenderer.boatLayer(), BoatModel::createBodyModel);
        EntityModelLayerRegistry.registerModelLayer(PeachBoatRenderer.chestBoatLayer(), ChestBoatModel::createBodyModel);
    }

    private static void registerColors() {
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> tintIndex == 0 ? bluestoneWireColor(state.getValue(com.craisinlord.antarchy.content.block.BluestoneWireBlock.POWER)) : -1,
                AntarchyFabricBlocks.BLUESTONE_WIRE.get()
        );
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor(),
                AntarchyFabricBlocks.OURANWOOD_LEAVES.get()
        );
        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> 0xFF4A0000, AntarchyFabricBlocks.ANTIWATER_BLOCK.get());
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : net.minecraft.world.level.GrassColor.getDefaultColor(),
                AntarchyFabricBlocks.SPIDER_LILY.get()
        );
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : net.minecraft.world.level.GrassColor.getDefaultColor(),
                AntarchyFabricBlocks.POTTED_SPIDER_LILY.get()
        );
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> FoliageColor.getDefaultColor(), AntarchyFabricItems.OURANWOOD_LEAVES_ITEM.get());
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> tintIndex == 0 ? net.minecraft.world.level.GrassColor.getDefaultColor() : -1,
                AntarchyFabricItems.SPIDER_LILY_ITEM.get()
        );
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> {
                    if (!(stack.getItem() instanceof com.craisinlord.antarchy.content.item.CritterCageItem cage)) {
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
                AntarchyFabricItems.CRITTER_CAGE.get()
        );
    }

    private static void registerFluids() {
        FluidRenderHandlerRegistry.INSTANCE.register(
                AntarchyFabricMisc.BILE.get(),
                AntarchyFabricMisc.FLOWING_BILE.get(),
                new SimpleFluidRenderHandler(
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still"),
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing"),
                        ResourceLocation.withDefaultNamespace("block/water_overlay"),
                        0xFFFFFFFF
                )
        );

        FluidRenderHandlerRegistry.INSTANCE.register(
                AntarchyFabricMisc.ICHOR.get(),
                AntarchyFabricMisc.FLOWING_ICHOR.get(),
                new SimpleFluidRenderHandler(
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still"),
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing"),
                        ResourceLocation.withDefaultNamespace("block/water_overlay"),
                        0xFF3D0408
                )
        );

        FluidRenderHandlerRegistry.INSTANCE.register(
                AntarchyFabricMisc.ANTIWATER.get(),
                AntarchyFabricMisc.FLOWING_ANTIWATER.get(),
                new SimpleFluidRenderHandler(
                        ResourceLocation.withDefaultNamespace("block/water_still"),
                        ResourceLocation.withDefaultNamespace("block/water_flow"),
                        ResourceLocation.withDefaultNamespace("block/water_overlay"),
                        0xFFFF1A1A
                ) {
                    @Override
                    public void renderFluid(BlockPos pos, BlockAndTintGetter world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
                        TextureAtlasSprite[] sprites = this.getFluidSprites(world, pos, fluidState);
                        AntiwaterFluidRenderer.render(
                                world,
                                pos,
                                vertexConsumer,
                                blockState,
                                fluidState,
                                sprites[0],
                                sprites[1],
                                sprites.length > 2 ? sprites[2] : null,
                                this.getFluidColor(world, pos, fluidState)
                        );
                    }
                }
        );

        FluidRenderHandlerRegistry.INSTANCE.register(
                AntarchyFabricMisc.LUMEN.get(),
                AntarchyFabricMisc.FLOWING_LUMEN.get(),
                new SimpleFluidRenderHandler(
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_still"),
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/lumen_flow"),
                        ResourceLocation.withDefaultNamespace("block/water_overlay"),
                        0xFFFFFFFF
                )
        );
    }

    private static void registerParticles() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(AntarchyFabricMisc.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        registry.register(AntarchyFabricMisc.STINKY_GAS.get(), HypnoticGasParticle.Provider::new);
        registry.register(AntarchyFabricMisc.STINKY_FLY.get(), FireflyParticle.Provider::new);
        registry.register(AntarchyFabricMisc.PEACH_LEAVES_PARTICLE.get(), PeachLeavesParticle.Provider::new);
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
        registry.register(AntarchyFabricMisc.ORANGE_ASH.get(), OrangeAshParticle.Provider::new);
    }

    private static void registerRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ANTIMETAL_RAIL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ANTIMETAL_POWERED_RAIL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ANTIMETAL_DETECTOR_RAIL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ANTIMETAL_ACTIVATOR_RAIL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.OURANWOOD_LEAVES.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.OURANWOOD_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.OURANWOOD_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.OURANWOOD_ACORN_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.POTTED_OURANWOOD_ACORN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.PEACH_LEAVES.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.PEACH_HANGING_PEACH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.PEACH_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.PEACH_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.PEACH_SAPLING.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.POTTED_PEACH_SAPLING.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DUPLICATOR_SAPLING.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ORANGE_MILKWEED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.PINK_MILKWEED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.AMBER_LICHEN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.MUCUS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CREEPVINE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CAMELLIA.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.SPIDER_LILY.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.POTTED_SPIDER_LILY.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.LOTUS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.POTTED_LOTUS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.GIANT_LILY_PAD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.HUSHWEED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CRITTER_CAGE_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.POTTED_GLOWCAP_MUSHROOM.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CORNEA_STALK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CORN_CROP.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.WILD_CORN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.FALLEN_KING_CROWN_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ANTIMETAL_SCAFFOLDING.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.UMBRAL_MOSS_CARPET.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.NYXITE_SPIKE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CHITIN_SPIKE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.STAR_CORAL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.STAR_CORAL_FAN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.STAR_CORAL_WALL_FAN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DEAD_STAR_CORAL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DEAD_STAR_CORAL_FAN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DEAD_STAR_CORAL_WALL_FAN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.SMALL_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.MEDIUM_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.LARGE_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.BLOOD_CRYSTAL_CRYSTAL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_TORCH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_WALL_TORCH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_CEILING_TORCH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.BLUESTONE_WIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.BLUESTONE_REPEATER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.BLUESTONE_COMPARATOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.BLUESTONE_TORCH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_LANTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_CAMPFIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_FIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.DREAM_CEILING_FIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ANTIWATER_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.LUMEN_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.ELYTHIA_PORTAL.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.THORAXIS_PORTAL.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.TRIFFID_GOO_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.URANIUM_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.TITANIUM_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.URANIUM_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.TITANIUM_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.URANIUM_BARS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.TITANIUM_BARS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.CLOUD_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.LUCID_ANCHOR.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.JUMPY_BUG_EGG.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.SPIT_BUG_EGG.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.JERRY_EGG.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.BIOWART_TENDRILS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.HANGING_CREEPROOTS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.MOLTING_VINES.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricBlocks.GLOWCAP_MUSHROOM.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putFluids(
                RenderType.translucent(),
                AntarchyFabricMisc.BILE.get(),
                AntarchyFabricMisc.FLOWING_BILE.get(),
                AntarchyFabricMisc.ICHOR.get(),
                AntarchyFabricMisc.FLOWING_ICHOR.get(),
                AntarchyFabricMisc.LUMEN.get(),
                AntarchyFabricMisc.FLOWING_LUMEN.get(),
                AntarchyFabricMisc.ANTIWATER.get(),
                AntarchyFabricMisc.FLOWING_ANTIWATER.get()
        );
    }

    private static void registerItemProperties() {
        ItemProperties.register(
                AntarchyFabricItems.ULTIMATE_BOW.get(),
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack
                        ? 0.0F
                        : (stack.getItem().getUseDuration(stack, entity) - entity.getUseItemRemainingTicks()) / 20.0F
        );
        ItemProperties.register(
                AntarchyFabricItems.ULTIMATE_BOW.get(),
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricItems.ULTIMATE_CROSSBOW.get(),
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
                AntarchyFabricItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("charged"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricItems.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("firework"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) && ChargedProjectiles.of(stack).contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricItems.CRITTER_CAGE.get(),
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "critter_cage_state"),
                (stack, level, entity, seed) -> {
                    if (!(stack.getItem() instanceof com.craisinlord.antarchy.content.item.CritterCageItem cage)) {
                        return 0.0F;
                    }
                    return cage.getItemState(stack);
                }
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerEntityLayers() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            if (entityType == EntityType.PLAYER) {
                if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
                    registrationHelper.register(new ParalyzedStonePlayerLayer(playerRenderer));
                    registrationHelper.register(new TigerEyeCamouflagePlayerLayer(playerRenderer));
                    registrationHelper.register(new GoopedLivingLayer(playerRenderer));
                    registrationHelper.register(new GlimmeringLivingLayer(playerRenderer));
                    registrationHelper.register(new BrutalflyElytraLayer(playerRenderer));
                    registrationHelper.register(new FallenKingCrownLayer(playerRenderer));
                }
                return;
            }
            if (entityType == EntityType.ARMOR_STAND && renderer instanceof ArmorStandRenderer armorStandRenderer) {
                registrationHelper.register(new FallenKingCrownArmorStandLayer((net.minecraft.client.renderer.entity.RenderLayerParent) armorStandRenderer));
                return;
            }
            if (software.bernie.geckolib.renderer.GeoEntityRenderer.class.isAssignableFrom(renderer.getClass())) {
                software.bernie.geckolib.renderer.GeoEntityRenderer geoRenderer =
                        (software.bernie.geckolib.renderer.GeoEntityRenderer) (Object) renderer;
                geoRenderer.addRenderLayer(new ParalyzedStoneGeoLayer(geoRenderer));
                geoRenderer.addRenderLayer(new GoopedGeoLayer(geoRenderer));
                return;
            }
            registrationHelper.register(new ParalyzedStoneLivingLayer(renderer));
            registrationHelper.register(new GoopedLivingLayer(renderer));
            registrationHelper.register(new GlimmeringLivingLayer(renderer));
        });
    }

    private static void registerClientCallbacks() {
        TigerEyeClientHooks.setCamouflageKeyTextSupplier(() -> AntarchyKeyBindings.TIGERS_EYE_CAMOUFLAGE.getTranslatedKeyMessage());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CameraShakeClientState.tick();
            HerculesBeetleImpactShakeClientState.tick();
            HordeClientState.tick();
            if (client.player != null) {
                ParalyzedClientHandler.clampPlayerInput(client.player);
            }
            BrutalflyElytraClientHandler.tick();
            JumpyBootsClientHandler.tick();
            DiamondMinecartClientHandler.tick();
            ParalyzedClientHandler.tick();
            DreadClientHandler.tick();
            TigerEyeCamouflageClientHandler.tick();
            BloodCrystalKatanaTrailHandler.tick();
            ScorpionWhipTetherRenderHandler.tick();
            DorrieJumpClientHandler.tick();
            HerculesBeetleClientHandler.tick();
        });

        BigBerthaClientHandler.register();
        GravityGunClientHandler.register();

        ElythiaFireflyManager.register();
        LucidSoundHandler.register();
        MogglesClientRenderer.register();
        StinkySoundHandler.register();
        ReverieTrailHandler.register();
        HudRenderCallback.EVENT.register((guiGraphics, partialTick) -> {
            DreadHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.HordeHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.GoopedHudRenderer.render(guiGraphics);
            com.craisinlord.antarchy.content.client.GlimmeringHudRenderer.render(guiGraphics);
            ParalyzedHudRenderer.render(guiGraphics);
            BrutalflyElytraHudRenderer.render(guiGraphics);
            JumpyBootsHudRenderer.render(guiGraphics);
            TriffidGooHudRenderer.render(guiGraphics);
            DorrieJumpHudRenderer.render(guiGraphics);
            HerculesBeetleChargeHudRenderer.render(guiGraphics);
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            BloodCrystalKatanaTrailHandler.render(context);
            ScorpionWhipTetherRenderHandler.render(context);
        });
    }

    private static int bluestoneWireColor(int power) {
        float normalized = power / 15.0F;
        int red = (int) (6.0F + normalized * 54.0F);
        int green = (int) (22.0F + normalized * 132.0F);
        int blue = (int) (64.0F + normalized * 191.0F);
        return red << 16 | green << 8 | blue;
    }
}
