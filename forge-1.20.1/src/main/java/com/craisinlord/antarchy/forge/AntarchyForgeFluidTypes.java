package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.AntiwaterFluidRenderer;
import com.craisinlord.antarchy.forge.content.fluid.AntiwaterFluidType;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import com.craisinlord.antarchy.forge.registry.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.function.Consumer;

public final class AntarchyForgeFluidTypes {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final ResourceLocation UNDERWATER_OVERLAY = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    private static final ResourceLocation ANTIWATER_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_still");
    private static final ResourceLocation ANTIWATER_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/antiwater_flow");
    private static final ResourceLocation BILE_STILL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_still");
    private static final ResourceLocation BILE_FLOW = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "block/bile/bile_flowing");

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Antarchy.MODID);

    public static final RegistryObject<FluidType> ANTIWATER_TYPE = FLUID_TYPES.register("antiwater",
            () -> new AntiwaterFluidType(FluidType.Properties.create()
                    .descriptionId("block.antarchy.antiwater")
                    .fallDistanceModifier(0.0F)
                    .canSwim(true)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1000)
                    .viscosity(1000)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
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
                        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                            return new Vector3f(0.52F, 0.06F, 0.06F);
                        }

                        @Override
                        public void modifyFogRender(Camera camera, net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                            RenderSystem.setShaderFogStart(0.5F);
                            RenderSystem.setShaderFogEnd(Math.min(farDistance, 12.0F));
                            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        }
                    });
                }
            });

    public static final RegistryObject<FluidType> BILE_TYPE = FLUID_TYPES.register("bile",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.antarchy.bile")
                    .fallDistanceModifier(0.0F)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1100)
                    .viscosity(1200)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
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
                        public void modifyFogRender(Camera camera, net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                            RenderSystem.setShaderFogStart(0.25F);
                            RenderSystem.setShaderFogEnd(Math.min(farDistance, 4.0F));
                            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        }
                    });
                }
            });

    public static final RegistryObject<FluidType> ICHOR_TYPE = FLUID_TYPES.register("ichor",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.antarchy.ichor")
                    .fallDistanceModifier(0.0F)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1200)
                    .viscosity(1400)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
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
                        public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
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
                    });
                }
            });

    public static final RegistryObject<FluidType> LUMEN_TYPE = FLUID_TYPES.register("lumen",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.antarchy.lumen")
                    .fallDistanceModifier(0.0F)
                    .canSwim(true)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1000)
                    .viscosity(1000)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
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
                        public void modifyFogRender(Camera camera, net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                            RenderSystem.setShaderFogStart(0.4F);
                            RenderSystem.setShaderFogEnd(Math.min(farDistance, 12.0F));
                            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        }
                    });
                }
            });

    private AntarchyForgeFluidTypes() {
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}
