package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.mixins.client.LevelRendererPortalSceneInvoker;
import com.craisinlord.antarchy.mixins.client.LevelRendererPortalViewAreaAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class PortalGunProxySceneRenderer {
    private PortalGunProxySceneRenderer() {
    }

    public static void render(Minecraft minecraft, LevelRenderer renderer, Camera camera, float partialTick, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, String tracePair) {
        Vec3 cameraPosition = camera.getPosition();
        double cameraX = cameraPosition.x;
        double cameraY = cameraPosition.y;
        double cameraZ = cameraPosition.z;
        Frustum frustum = new Frustum(modelViewMatrix, projectionMatrix);
        frustum.prepare(cameraX, cameraY, cameraZ);
        LevelRendererPortalSceneInvoker scene = (LevelRendererPortalSceneInvoker) renderer;
        float renderDistance = minecraft.gameRenderer.getRenderDistance();
        BlockPos cameraBlockPos = BlockPos.containing(cameraX, cameraY, cameraZ);
        boolean foggy = minecraft.level.effects().isFoggyAt(cameraBlockPos.getX(), cameraBlockPos.getY())
                || minecraft.gui.getBossOverlay().shouldCreateWorldFog();

        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "sky", "camera=" + cameraPosition)) {
            RenderSystem.setShaderGameTime(minecraft.level.getGameTime(), partialTick);
            minecraft.getBlockEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.hitResult);
            minecraft.getEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.crosshairPickEntity);
            FogRenderer.setupColor(camera, partialTick, minecraft.level, minecraft.options.getEffectiveRenderDistance(), minecraft.gameRenderer.getDarkenWorldAmount(partialTick));
            FogRenderer.levelFogColor();
            FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, foggy, partialTick);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionShader);
            renderer.renderSky(modelViewMatrix, projectionMatrix, partialTick, camera, foggy,
                    () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, foggy, partialTick));
        }

        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "frustum-and-visibility", "renderDistance=" + rendererAccessDistance(renderer))) {
            FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDistance, 32.0F), foggy, partialTick);
            scene.antarchy$setupPortalScene(camera, frustum, false, minecraft.player != null && minecraft.player.isSpectator());
        }
        LevelRendererPortalViewAreaAccessor viewAreaAccessor = (LevelRendererPortalViewAreaAccessor) renderer;
        ObjectArrayList<SectionRenderDispatcher.RenderSection> fullVisibleSections = viewAreaAccessor.antarchy$getVisibleSections();
        ObjectArrayList<SectionRenderDispatcher.RenderSection> budgetedVisibleSections = new ObjectArrayList<>();
        PortalGunPortalRendererPool.beginSceneBudget();
        int scannedVisibleSections = 0;
        while (scannedVisibleSections < fullVisibleSections.size() && PortalGunPortalRendererPool.remainingSectionBudgetNanos() > 0L) {
            budgetedVisibleSections.add(fullVisibleSections.get(scannedVisibleSections++));
        }
        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "compile-visible-sections", "visibleTotal=" + fullVisibleSections.size() + " visibleScanned=" + scannedVisibleSections + " rebuildBudgetNanos=" + PortalGunPortalRendererPool.remainingSectionBudgetNanos())) {
            viewAreaAccessor.antarchy$setVisibleSections(budgetedVisibleSections);
            try {
                scene.antarchy$compilePortalSections(camera);
            } finally {
                viewAreaAccessor.antarchy$setVisibleSections(fullVisibleSections);
            }
        }
        PortalSceneRenderTrace.event(tracePair, "visible-sections-ready", "count=" + viewAreaAccessor.antarchy$getVisibleSections().size() + " scheduledRebuilds=" + PortalGunPortalRendererPool.scheduledSectionRebuilds());

        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "opaque-terrain-layers", "sections=" + viewAreaAccessor.antarchy$getVisibleSections().size())) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            scene.antarchy$renderPortalSectionLayer(RenderType.solid(), cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
            scene.antarchy$renderPortalSectionLayer(RenderType.cutoutMipped(), cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
            scene.antarchy$renderPortalSectionLayer(RenderType.cutout(), cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
        }

        if (minecraft.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel();
        } else {
            Lighting.setupLevel();
        }

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "entity-scene-state", "enter-model-view")) {
            modelViewStack.pushMatrix();
            modelViewStack.mul(modelViewMatrix);
            RenderSystem.applyModelViewMatrix();
        }
        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        try {
            int entityCount = 0;
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "entities", "dispatch-start")) {
                for (Entity entity : minecraft.level.entitiesForRendering()) {
                    if (!minecraft.getEntityRenderDispatcher().shouldRender(entity, frustum, cameraX, cameraY, cameraZ)
                            && !entity.hasIndirectPassenger(minecraft.player)) {
                        continue;
                    }
                    if (entity == camera.getEntity() && !camera.isDetached()
                            && (!(entity instanceof LivingEntity living) || !living.isSleeping())) {
                        continue;
                    }
                    if (entity instanceof Player player && player == minecraft.player && !player.isSpectator() && entity != camera.getEntity()) {
                        continue;
                    }
                    scene.antarchy$renderPortalEntity(entity, cameraX, cameraY, cameraZ, partialTick, poseStack, buffers);
                    entityCount++;
                }
            }
            buffers.endBatch();
            PortalSceneRenderTrace.event(tracePair, "entities-dispatched", "count=" + entityCount);

            int blockEntityCount = 0;
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "block-entities", "visibleSections=" + viewAreaAccessor.antarchy$getVisibleSections().size())) {
                for (var section : viewAreaAccessor.antarchy$getVisibleSections()) {
                    for (BlockEntity blockEntity : section.getCompiled().getRenderableBlockEntities()) {
                        renderBlockEntity(minecraft, blockEntity, cameraX, cameraY, cameraZ, partialTick, poseStack, buffers);
                        blockEntityCount++;
                    }
                }
                synchronized (viewAreaAccessor.antarchy$getGlobalBlockEntities()) {
                    for (BlockEntity blockEntity : viewAreaAccessor.antarchy$getGlobalBlockEntities()) {
                        if (frustum.isVisible(new AABB(blockEntity.getBlockPos()).inflate(1.0D))) {
                            renderBlockEntity(minecraft, blockEntity, cameraX, cameraY, cameraZ, partialTick, poseStack, buffers);
                            blockEntityCount++;
                        }
                    }
                }
            }
            buffers.endBatch();
            PortalSceneRenderTrace.event(tracePair, "block-entities-dispatched", "count=" + blockEntityCount);

            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "translucent-and-particles", "rendering")) {
                minecraft.particleEngine.render(minecraft.gameRenderer.lightTexture(), camera, partialTick, frustum, type -> !type.isTranslucent());
                scene.antarchy$renderPortalSectionLayer(RenderType.translucent(), cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
                buffers.endBatch(RenderType.lines());
                buffers.endBatch();
                scene.antarchy$renderPortalSectionLayer(RenderType.tripwire(), cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
                minecraft.particleEngine.render(minecraft.gameRenderer.lightTexture(), camera, partialTick, frustum, ParticleRenderType::isTranslucent);
            }

            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(tracePair, "clouds", minecraft.options.getCloudsType().toString())) {
                if (minecraft.options.getCloudsType() != CloudStatus.OFF) {
                    renderer.renderClouds(poseStack, modelViewMatrix, projectionMatrix, partialTick, cameraX, cameraY, cameraZ);
                }
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
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
            }
        }
    }

    private static int rendererAccessDistance(LevelRenderer renderer) {
        return ((LevelRendererPortalViewAreaAccessor) renderer).antarchy$getViewArea().getViewDistance();
    }

    private static void renderBlockEntity(Minecraft minecraft, BlockEntity blockEntity, double cameraX, double cameraY, double cameraZ,
                                         float partialTick, PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
        minecraft.getBlockEntityRenderDispatcher().render(blockEntity, partialTick, poseStack, buffers);
        poseStack.popPose();
    }
}
