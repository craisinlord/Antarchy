package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.mixins.client.LevelRendererPortalSceneInvoker;
import com.craisinlord.antarchy.mixins.client.LevelRendererPortalViewAreaAccessor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

public final class PortalGunPortalSceneRenderer {
    private static RenderBuffers portalBuffers;
    private static boolean renderingScene;

    private PortalGunPortalSceneRenderer() {
    }

    public static MultiBufferSource.BufferSource bufferSource() {
        if (portalBuffers == null) {
            portalBuffers = new RenderBuffers(1);
        }
        return portalBuffers.bufferSource();
    }

    public static boolean isRenderingScene() {
        return renderingScene;
    }

    public static void render(Minecraft minecraft, Camera camera, float partialTick, Matrix4f viewMatrix,
                              Matrix4f projection, Matrix4f skyProjection, Frustum frustum,
                              PortalTerrain terrain, Consumer<PoseStack> crossingEntities, String tracePair) {
        LevelRenderer renderer = minecraft.levelRenderer;
        LevelRendererPortalViewAreaAccessor state = (LevelRendererPortalViewAreaAccessor) renderer;
        LevelRendererPortalSceneInvoker scene = (LevelRendererPortalSceneInvoker) renderer;
        Vec3 cameraPosition = camera.getPosition();
        double cameraX = cameraPosition.x;
        double cameraY = cameraPosition.y;
        double cameraZ = cameraPosition.z;

        int previousRainSoundTime = state.antarchy$getRainSoundTime();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        float previousFogStart = RenderSystem.getShaderFogStart();
        float previousFogEnd = RenderSystem.getShaderFogEnd();
        float[] previousFogColor = RenderSystem.getShaderFogColor().clone();
        FogShape previousFogShape = RenderSystem.getShaderFogShape();
        Camera previousBlockEntityCamera = minecraft.getBlockEntityRenderDispatcher().camera;
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        RenderSystem.applyModelViewMatrix();
        renderingScene = true;
        terrain.begin(minecraft);
        try {
            float renderDistance = minecraft.gameRenderer.getRenderDistance();
            BlockPos cameraBlockPos = BlockPos.containing(cameraX, cameraY, cameraZ);
            boolean foggy = minecraft.level.effects().isFoggyAt(cameraBlockPos.getX(), cameraBlockPos.getY())
                    || minecraft.gui.getBossOverlay().shouldCreateWorldFog();

            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "background-and-sky", "sections=" + terrain.sectionCount())) {
                RenderSystem.setShaderGameTime(minecraft.level.getGameTime(), partialTick);
                minecraft.getBlockEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.hitResult);
                minecraft.getEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.crosshairPickEntity);
                FogRenderer.setupColor(camera, partialTick, minecraft.level, minecraft.options.getEffectiveRenderDistance(),
                        minecraft.gameRenderer.getDarkenWorldAmount(partialTick));
                FogRenderer.levelFogColor();
                fillBackground(RenderSystem.getShaderFogColor());
                RenderSystem.setProjectionMatrix(skyProjection, VertexSorting.DISTANCE_TO_ORIGIN);
                FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, foggy, partialTick);
                RenderSystem.setShader(GameRenderer::getPositionShader);
                renderer.renderSky(viewMatrix, skyProjection, partialTick, camera, foggy,
                        () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, foggy, partialTick));
            }

            RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
            FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDistance, 32.0F), foggy, partialTick);
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "opaque-terrain", "sections=" + terrain.sectionCount())) {
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                terrain.drawLayer(minecraft, RenderType.solid(), cameraPosition, viewMatrix, projection);
                terrain.drawLayer(minecraft, RenderType.cutoutMipped(), cameraPosition, viewMatrix, projection);
                terrain.drawLayer(minecraft, RenderType.cutout(), cameraPosition, viewMatrix, projection);
            }

            if (minecraft.level.effects().constantAmbientLight()) {
                Lighting.setupNetherLevel();
            } else {
                Lighting.setupLevel();
            }
            modelViewStack.pushMatrix();
            modelViewStack.mul(viewMatrix);
            RenderSystem.applyModelViewMatrix();
            PoseStack poseStack = new PoseStack();
            MultiBufferSource.BufferSource buffers = bufferSource();
            try {
                try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "entities", "rendering")) {
                    for (Entity entity : minecraft.level.entitiesForRendering()) {
                        if (!minecraft.getEntityRenderDispatcher().shouldRender(entity, frustum, cameraX, cameraY, cameraZ)
                                && !entity.hasIndirectPassenger(minecraft.player)) {
                            continue;
                        }
                        if (entity instanceof PortalGunPortalEntity portal && !portal.shouldRenderFront(cameraPosition)) {
                            continue;
                        }
                        if (entity == camera.getEntity()
                                && entity.getBoundingBox().inflate(0.1D).contains(cameraPosition)
                                && (!(entity instanceof LivingEntity living) || !living.isSleeping())) {
                            continue;
                        }
                        scene.antarchy$renderPortalEntity(entity, cameraX, cameraY, cameraZ, partialTick, poseStack, buffers);
                    }
                    crossingEntities.accept(poseStack);
                }
                buffers.endBatch();

                try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "block-entities", "rendering")) {
                    terrain.forEachBlockEntity(minecraft, frustum, blockEntity ->
                            renderBlockEntity(minecraft, blockEntity, cameraX, cameraY, cameraZ, partialTick, poseStack, buffers));
                }
                buffers.endBatch();

                try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "translucent-and-particles", "rendering")) {
                    terrain.drawLayer(minecraft, RenderType.translucent(), cameraPosition, viewMatrix, projection);
                    buffers.endBatch();
                    terrain.drawLayer(minecraft, RenderType.tripwire(), cameraPosition, viewMatrix, projection);
                    minecraft.particleEngine.render(minecraft.gameRenderer.lightTexture(), camera, partialTick);
                }

                try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "weather-and-border", "rendering")) {
                    RenderSystem.depthMask(false);
                    scene.antarchy$renderPortalWeather(minecraft.gameRenderer.lightTexture(), partialTick, cameraX, cameraY, cameraZ);
                    scene.antarchy$renderPortalWorldBorder(camera);
                }
            } finally {
                try {
                    buffers.endBatch();
                } finally {
                    modelViewStack.popMatrix();
                    RenderSystem.applyModelViewMatrix();
                }
            }
        } finally {
            renderingScene = false;
            terrain.end(minecraft);
            state.antarchy$setRainSoundTime(previousRainSoundTime);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            RenderSystem.setShaderFogStart(previousFogStart);
            RenderSystem.setShaderFogEnd(previousFogEnd);
            RenderSystem.setShaderFogColor(previousFogColor[0], previousFogColor[1], previousFogColor[2], previousFogColor[3]);
            RenderSystem.setShaderFogShape(previousFogShape);
            if (previousBlockEntityCamera != null) {
                minecraft.getBlockEntityRenderDispatcher().prepare(minecraft.level, previousBlockEntityCamera, minecraft.hitResult);
            }
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void fillBackground(float[] fogColor) {
        int previousDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        buffer.addVertex(-1.0F, -1.0F, 0.99999F);
        buffer.addVertex(3.0F, -1.0F, 0.99999F);
        buffer.addVertex(-1.0F, 3.0F, 0.99999F);
        MeshData mesh = buffer.build();
        try {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.setProjectionMatrix(new Matrix4f(), previousSorting);
            RenderSystem.setShader(GameRenderer::getPositionShader);
            RenderSystem.setShaderColor(fogColor[0], fogColor[1], fogColor[2], 1.0F);
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        } finally {
            GL11.glDepthFunc(previousDepthFunction);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            RenderSystem.enableCull();
        }
    }

    private static void renderBlockEntity(Minecraft minecraft, BlockEntity blockEntity, double cameraX, double cameraY, double cameraZ,
                                          float partialTick, PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        try {
            poseStack.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
            minecraft.getBlockEntityRenderDispatcher().render(blockEntity, partialTick, poseStack, buffers);
        } finally {
            poseStack.popPose();
        }
    }
}
