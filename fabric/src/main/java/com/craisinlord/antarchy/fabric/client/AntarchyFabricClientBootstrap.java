package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.*;
import com.craisinlord.antarchy.content.client.particle.*;
import com.craisinlord.antarchy.content.client.renderer.AntiwaterFluidRenderer;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
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
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
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
                (net.minecraft.world.level.block.entity.BlockEntityType) AntarchyFabricContent.DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                (BlockEntityRendererProvider) CampfireRenderer::new
        );

        EntityRendererRegistry.register(AntarchyFabricContent.EASTER_BUNNY.get(), EasterBunnyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.FLYING_SQUIRREL.get(), FlyingSquirrelRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.CATERPILLAR.get(), CaterpillarRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BUTTERFLY.get(), ButterflyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.REVERIE.get(), ReverieRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BRUTALFLY.get(), BrutalflyRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.RED_ANT.get(), AntRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BROWN_ANT.get(), AntRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.RAINBOW_ANT.get(), AntRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.MOLEWORM.get(), MolewormRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.MANTIS.get(), MantisRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.MOLEVORE.get(), MolevoreRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.TRIFFID.get(), TriffidRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.OURANWOOD_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/boat/ouranwood.png"), false));
        EntityRendererRegistry.register(AntarchyFabricContent.OURANWOOD_CHEST_BOAT_ENTITY.get(), context -> new OuranwoodBoatRenderer<>(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/chest_boat/ouranwood.png"), true));
        EntityRendererRegistry.register(AntarchyFabricContent.APPLE_COW.get(), AppleCowRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.GOLDEN_APPLE_COW.get(), AppleCowRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.ENCHANTED_GOLDEN_APPLE_COW.get(), AppleCowRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.DIAMOND_MINECART.get(), DiamondMinecartRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.DR_TRAYAURUS.get(), context -> new DrTrayaurusRenderer(context, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/dr_trayaurus.png")));
        EntityRendererRegistry.register(AntarchyFabricContent.CLOUD_SHARK.get(), CloudSharkRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.KRAKEN.get(), KrakenRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.MISSILE_SQUID.get(), MissileSquidRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.NIGHTMARE.get(), NightmareRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.LUCID.get(), LucidRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BED_BUG.get(), BedBugRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.WASP.get(), WaspRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BOMBER.get(), BomberRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BASILISK.get(), BasiliskRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.SHRINK_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.GROWTH_RAY_PROJECTILE.get(), SizeRayProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.LUCID_BOLT.get(), LucidBoltRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.LUCID_PEARL_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.HUSH_PROJECTILE.get(), HushProjectileRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.BRUTALFLY_ORB.get(), BrutalflyOrbRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.UPWARD_FALLING_BLOCK.get(), UpwardFallingBlockRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.SCORPION.get(), ScorpionRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.EMPEROR_SCORPION.get(), EmperorScorpionRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.TORETERROR.get(), ToreterrorRenderer::new);
        EntityRendererRegistry.register(AntarchyFabricContent.WATER_BOMB.get(), WaterBombRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(OuranwoodBoatRenderer.boatLayer(), BoatModel::createBodyModel);
        EntityModelLayerRegistry.registerModelLayer(OuranwoodBoatRenderer.chestBoatLayer(), ChestBoatModel::createBodyModel);
    }

    private static void registerColors() {
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor(),
                AntarchyFabricContent.OURANWOOD_LEAVES.get()
        );
        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> 0xFF4A0000, AntarchyFabricContent.ANTIWATER_BLOCK.get());
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> FoliageColor.getDefaultColor(), AntarchyFabricContent.OURANWOOD_LEAVES_ITEM.get());
    }

    private static void registerFluids() {
        FluidRenderHandlerRegistry.INSTANCE.register(
                AntarchyFabricContent.ICHOR.get(),
                AntarchyFabricContent.FLOWING_ICHOR.get(),
                new SimpleFluidRenderHandler(
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_still"),
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_flow"),
                        ResourceLocation.withDefaultNamespace("block/water_overlay"),
                        0xFF2A0306
                )
        );

        FluidRenderHandlerRegistry.INSTANCE.register(
                AntarchyFabricContent.ANTIWATER.get(),
                AntarchyFabricContent.FLOWING_ANTIWATER.get(),
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
    }

    private static void registerParticles() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(AntarchyFabricContent.DREAM_FIRE_FLAME.get(), DreamFlameParticle.Provider::new);
        registry.register(AntarchyFabricContent.HYPNOTIC_GAS.get(), HypnoticGasParticle.Provider::new);
        registry.register(AntarchyFabricContent.HYPNOTIC_GAS_DOWN.get(), sprites -> new HypnoticGasParticle.Provider(sprites, true));
        registry.register(AntarchyFabricContent.HYPNOTIC_GAS_CLOUD.get(), new HypnoticGasCloudParticle.Provider());
        registry.register(AntarchyFabricContent.HYPNOTIC_GAS_CLOUD_DOWN.get(), new HypnoticGasCloudParticle.Provider(true));
        registry.register(AntarchyFabricContent.INVERTED_GEYSER_BASE.get(), InvertedGeyserBaseParticle.Provider::new);
        registry.register(AntarchyFabricContent.INVERTED_GEYSER_PLUME.get(), InvertedGeyserPlumeParticle.Provider::new);
        registry.register(AntarchyFabricContent.INVERTED_GEYSER_POOF.get(), InvertedGeyserBaseParticle.Provider::new);
        registry.register(AntarchyFabricContent.INVERTED_GEYSER_ERUPTION.get(), new InvertedGeyserEruptionParticle.Provider());
        registry.register(AntarchyFabricContent.FIREFLY.get(), FireflyParticle.Provider::new);
    }

    private static void registerRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.OURANWOOD_LEAVES.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.OURANWOOD_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.OURANWOOD_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.OURANWOOD_ACORN_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DUPLICATOR_SAPLING.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.ORANGE_MILKWEED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.PINK_MILKWEED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.TORCHFLOWER_BUSH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.HUSHWEED.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.CORNEA_STALK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.FALLEN_KING_CROWN_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.ANTIMETAL_SCAFFOLDING.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.UMBRAL_MOSS_CARPET.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.NYXITE_SPIKE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.SMALL_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.MEDIUM_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.LARGE_BLOOD_CRYSTAL_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.BLOOD_CRYSTAL_CRYSTAL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DREAM_TORCH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DREAM_WALL_TORCH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DREAM_LANTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DREAM_CAMPFIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DREAM_FIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.DREAM_CEILING_FIRE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.ANTIWATER_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.TRIFFID_GOO_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.URANIUM_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.TITANIUM_DOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.URANIUM_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.TITANIUM_TRAPDOOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.URANIUM_BARS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.TITANIUM_BARS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AntarchyFabricContent.CLOUD_BLOCK.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putFluids(
                RenderType.translucent(),
                AntarchyFabricContent.ICHOR.get(),
                AntarchyFabricContent.FLOWING_ICHOR.get(),
                AntarchyFabricContent.ANTIWATER.get(),
                AntarchyFabricContent.FLOWING_ANTIWATER.get()
        );
    }

    private static void registerItemProperties() {
        ItemProperties.register(
                AntarchyFabricContent.ULTIMATE_BOW.get(),
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack
                        ? 0.0F
                        : (stack.getItem().getUseDuration(stack, entity) - entity.getUseItemRemainingTicks()) / 20.0F
        );
        ItemProperties.register(
                AntarchyFabricContent.ULTIMATE_BOW.get(),
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricContent.ULTIMATE_CROSSBOW.get(),
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
                AntarchyFabricContent.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricContent.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("charged"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(
                AntarchyFabricContent.ULTIMATE_CROSSBOW.get(),
                ResourceLocation.withDefaultNamespace("firework"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) && ChargedProjectiles.of(stack).contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerEntityLayers() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            if (entityType == EntityType.PLAYER) {
                if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
                    registrationHelper.register(new ParalyzedStonePlayerLayer(playerRenderer));
                    registrationHelper.register(new BrutalflyElytraLayer(playerRenderer));
                    registrationHelper.register(new FallenKingCrownLayer(playerRenderer));
                }
                return;
            }
            if (software.bernie.geckolib.renderer.GeoEntityRenderer.class.isAssignableFrom(renderer.getClass())) {
                software.bernie.geckolib.renderer.GeoEntityRenderer geoRenderer =
                        (software.bernie.geckolib.renderer.GeoEntityRenderer) (Object) renderer;
                geoRenderer.addRenderLayer(new ParalyzedStoneGeoLayer(geoRenderer));
                return;
            }
            registrationHelper.register(new ParalyzedStoneLivingLayer(renderer));
        });
    }

    private static void registerClientCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ParalyzedClientHandler.clampPlayerInput(client.player);
            }
            BrutalflyElytraClientHandler.tick();
            DiamondMinecartClientHandler.tick();
            ParalyzedClientHandler.tick();
            DreadClientHandler.tick();
            BloodCrystalKatanaTrailHandler.tick();
            ScorpionWhipTetherRenderHandler.tick();
        });

        GravityGunClientHandler.register();

        ElythiaFireflyManager.register();
        LucidSoundHandler.register();
        MogglesClientRenderer.register();
        ReverieTrailHandler.register();

        HudRenderCallback.EVENT.register((guiGraphics, partialTick) -> {
            DreadHudRenderer.render(guiGraphics);
            ParalyzedHudRenderer.render(guiGraphics);
            BrutalflyElytraHudRenderer.render(guiGraphics);
            BloodglassHudRenderer.render(guiGraphics);
            TriffidGooHudRenderer.render(guiGraphics);
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            BloodCrystalKatanaTrailHandler.render(context);
            ScorpionWhipTetherRenderHandler.render(context);
        });
    }
}
