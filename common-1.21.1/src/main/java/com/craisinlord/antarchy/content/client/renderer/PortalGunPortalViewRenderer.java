package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Quaternionf;

public final class PortalGunPortalViewRenderer {
    private static final int MAX_RECURSION_DEPTH = 3;
    private static final double SURFACE_OFFSET = 0.03125D;
    private static final double CAMERA_OFFSET = 0.0625D;
    private static final double OVERLAY_OFFSET = 0.03135D;
    private static final ResourceLocation BLUE_OVERLAY = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_blue.png");
    private static final ResourceLocation ORANGE_OVERLAY = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_orange.png");
    private static final TextureTarget[] PORTAL_TARGETS = new TextureTarget[MAX_RECURSION_DEPTH + 1];
    private static boolean renderingPortalView;

    private PortalGunPortalViewRenderer() {
    }

    public static void render(Camera camera, Matrix4f poseMatrix, DeltaTracker tickCounter) {
        if (renderingPortalView) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gameRenderer == null || camera == null || poseMatrix == null || tickCounter == null) {
            return;
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        List<PortalGunPortalEntity> portals = collectVisiblePortals(minecraft, camera);
        if (portals.isEmpty()) {
            return;
        }
        ensureTargets(minecraft);
        if (PORTAL_TARGETS[0] == null) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Vec3 rootLook = new Vec3(camera.getLookVector()).normalize();
        Vec3 rootUp = new Vec3(camera.getUpVector()).normalize();
        Matrix4f rootViewMatrix = new Matrix4f().rotation(PortalGunTransformUtil.orientationQuaternion(rootLook, rootUp).conjugate(new Quaternionf()));
        RenderContext rootContext = new RenderContext(cameraPos, rootLook, rootUp, rootViewMatrix);
        renderingPortalView = true;
        RenderSystem.backupProjectionMatrix();
        for (PortalGunPortalEntity portal : portals) {
            PortalGunPortalEntity linkedPortal = portal.getLinkedPortal();
            if (linkedPortal == null || !linkedPortal.isAlive()) {
                continue;
            }
            int textureId = renderPortalSurface(minecraft, cameraEntity, tickCounter, rootContext, portal, linkedPortal, 0);
            minecraft.getMainRenderTarget().bindWrite(true);
            drawPortalQuad(cameraPos, poseMatrix, portal, textureId);
            drawPortalOverlay(minecraft, cameraPos, poseMatrix, portal);
        }
        RenderSystem.restoreProjectionMatrix();
        renderingPortalView = false;
    }

    private static List<PortalGunPortalEntity> collectVisiblePortals(Minecraft minecraft, Camera camera) {
        return collectVisiblePortals(minecraft, camera.getPosition());
    }

    private static List<PortalGunPortalEntity> collectVisiblePortals(Minecraft minecraft, Vec3 cameraPos) {
        List<PortalGunPortalEntity> portals = new ArrayList<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof PortalGunPortalEntity portal) || !portal.isAlive()) {
                continue;
            }
            if (portal.getLinkedPortalId() == null) {
                continue;
            }
            if (!portal.shouldRenderFront(cameraPos)) {
                continue;
            }
            portals.add(portal);
        }
        portals.sort((left, right) -> Double.compare(left.distanceToSqr(cameraPos), right.distanceToSqr(cameraPos)));
        return portals;
    }

    private static void ensureTargets(Minecraft minecraft) {
        int width = minecraft.getWindow().getWidth();
        int height = minecraft.getWindow().getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        for (int i = 0; i < PORTAL_TARGETS.length; i++) {
            TextureTarget target = PORTAL_TARGETS[i];
            if (target == null) {
                target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
                target.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
                PORTAL_TARGETS[i] = target;
                continue;
            }
            if (target.width != width || target.height != height) {
                target.resize(width, height, Minecraft.ON_OSX);
            }
        }
    }

    private static int renderPortalSurface(
            Minecraft minecraft,
            Entity cameraEntity,
            DeltaTracker tickCounter,
            RenderContext currentContext,
            PortalGunPortalEntity sourcePortal,
            PortalGunPortalEntity destinationPortal,
            int depth
    ) {
        TextureTarget target = PORTAL_TARGETS[depth];
        RenderContext transformedContext = transformContext(currentContext, sourcePortal, destinationPortal);
        PortalCamera portalCamera = new PortalCamera();
        portalCamera.setup(minecraft.level, cameraEntity, tickCounter.getGameTimeDeltaPartialTick(true), transformedContext.cameraPos(), transformedContext.look(), transformedContext.up());
        target.bindWrite(true);
        target.clear(Minecraft.ON_OSX);
        double fov = minecraft.options.fov().get();
        Matrix4f baseProjectionMatrix = minecraft.gameRenderer.getProjectionMatrix(fov);
        Matrix4f clippedProjectionMatrix = applyPortalClipPlane(baseProjectionMatrix, transformedContext, destinationPortal);
        minecraft.gameRenderer.resetProjectionMatrix(clippedProjectionMatrix);
        ScreenClip clip = computeScreenClip(minecraft, currentContext.cameraPos(), currentContext.viewMatrix(), baseProjectionMatrix, sourcePortal);
        boolean clipEnabled = clip != null;
        boolean renderAll = clip == null || clip.area() >= widthTimesHeight(minecraft);
        if (clipEnabled) {
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
        }
        PortalGunPortalRenderState.pushPortalView(sourcePortal.getWorldPortalShape(), destinationPortal.getWorldPortalShape(), renderAll);
        try {
            minecraft.levelRenderer.prepareCullFrustum(portalCamera.getPosition(), transformedContext.viewMatrix(), clippedProjectionMatrix);
            minecraft.levelRenderer.renderLevel(tickCounter, false, portalCamera, minecraft.gameRenderer, minecraft.gameRenderer.lightTexture(), transformedContext.viewMatrix(), clippedProjectionMatrix);
            if (depth < MAX_RECURSION_DEPTH) {
                List<PortalGunPortalEntity> nestedPortals = collectVisiblePortals(minecraft, transformedContext.cameraPos());
                Matrix4f nestedPose = new Matrix4f().identity();
                for (PortalGunPortalEntity nestedPortal : nestedPortals) {
                    PortalGunPortalEntity nestedLinked = nestedPortal.getLinkedPortal();
                    if (nestedLinked == null || !nestedLinked.isAlive() || nestedPortal == destinationPortal || nestedPortal == sourcePortal) {
                        continue;
                    }
                    if (clip != null && clip.area() < 6400) {
                        continue;
                    }
                    int nestedTextureId = renderPortalSurface(minecraft, cameraEntity, tickCounter, transformedContext, nestedPortal, nestedLinked, depth + 1);
                    target.bindWrite(true);
                    drawPortalQuad(transformedContext.cameraPos(), nestedPose, nestedPortal, nestedTextureId);
                    drawPortalOverlay(minecraft, transformedContext.cameraPos(), nestedPose, nestedPortal);
                }
            }
        } finally {
            PortalGunPortalRenderState.popPortalView(renderAll);
        }
        if (clipEnabled) {
            RenderSystem.disableScissor();
        }
        return target.getColorTextureId();
    }

    private static RenderContext transformContext(RenderContext currentContext, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal) {
        Quaternionf transform = PortalGunTransformUtil.createTransform(sourcePortal, destinationPortal);
        Vec3 destinationNormal = destinationPortal.getNormalVec().normalize();
        Vec3 relativeEye = currentContext.cameraPos().subtract(sourcePortal.position());
        double depthOffset = Math.max(CAMERA_OFFSET, relativeEye.dot(sourcePortal.getNormalVec().normalize()));
        Vec3 transformedEyeOffset = PortalGunTransformUtil.transformRelativePosition(sourcePortal, destinationPortal, relativeEye);
        Vec3 destinationEye = destinationPortal.position()
                .add(transformedEyeOffset)
                .add(destinationNormal.scale(depthOffset + CAMERA_OFFSET - transformedEyeOffset.dot(destinationNormal)));
        Vec3 transformedLook = PortalGunTransformUtil.transform(currentContext.look(), transform).normalize();
        Vec3 transformedUp = PortalGunTransformUtil.transform(currentContext.up(), transform).normalize();
        Matrix4f viewMatrix = new Matrix4f().rotation(PortalGunTransformUtil.orientationQuaternion(transformedLook, transformedUp).conjugate(new Quaternionf()));
        return new RenderContext(destinationEye, transformedLook, transformedUp, viewMatrix);
    }

    private static Matrix4f applyPortalClipPlane(Matrix4f projectionMatrix, RenderContext renderContext, PortalGunPortalEntity destinationPortal) {
        Vec3 planePointWorld = destinationPortal.position().add(destinationPortal.getNormalVec().normalize().scale(0.01D));
        Vec3 planeNormalWorld = destinationPortal.getNormalVec().normalize();
        Vector4f clipPlane = portalPlaneToCameraSpace(renderContext, planePointWorld, planeNormalWorld);
        if (clipPlane.lengthSquared() <= 1.0E-6F) {
            return projectionMatrix;
        }
        Matrix4f clippedProjection = new Matrix4f(projectionMatrix);
        Vector4f q = new Vector4f(
                signNonZero(clipPlane.x()) / clippedProjection.m00(),
                signNonZero(clipPlane.y()) / clippedProjection.m11(),
                -1.0F,
                (1.0F + clippedProjection.m22()) / clippedProjection.m32()
        );
        float scale = 2.0F / clipPlane.dot(q);
        Vector4f c = clipPlane.mul(scale, new Vector4f());
        clippedProjection.m02(c.x());
        clippedProjection.m12(c.y());
        clippedProjection.m22(c.z() + 1.0F);
        clippedProjection.m32(c.w());
        return clippedProjection;
    }

    private static Vector4f portalPlaneToCameraSpace(RenderContext renderContext, Vec3 planePointWorld, Vec3 planeNormalWorld) {
        Vec3 look = renderContext.look().normalize();
        Vec3 up = renderContext.up().normalize();
        Vec3 right = up.cross(look).normalize();
        Vec3 relativePoint = planePointWorld.subtract(renderContext.cameraPos());
        Vector4f point = new Vector4f(
                (float) relativePoint.dot(right),
                (float) relativePoint.dot(up),
                (float) -relativePoint.dot(look),
                1.0F
        );
        Vector4f normal = new Vector4f(
                (float) planeNormalWorld.dot(right),
                (float) planeNormalWorld.dot(up),
                (float) -planeNormalWorld.dot(look),
                0.0F
        );
        if (normal.z() > 0.0F) {
            normal.mul(-1.0F);
        }
        return new Vector4f(normal.x(), normal.y(), normal.z(), -(normal.x() * point.x() + normal.y() * point.y() + normal.z() * point.z()));
    }

    private static float signNonZero(float value) {
        return value >= 0.0F ? 1.0F : -1.0F;
    }

    private static ScreenClip computeScreenClip(Minecraft minecraft, Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix, PortalGunPortalEntity portal) {
        Vec3[] worldCorners = portal.getWorldPortalShape().getCorners(SURFACE_OFFSET);
        int width = minecraft.getWindow().getWidth();
        int height = minecraft.getWindow().getHeight();
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean visible = false;
        Matrix4f combined = new Matrix4f(projectionMatrix).mul(viewMatrix);
        for (Vec3 worldCorner : worldCorners) {
            Vec3 corner = worldCorner.subtract(cameraPos);
            Vector4f projected = new Vector4f((float) corner.x, (float) corner.y, (float) corner.z, 1.0F).mul(combined);
            if (projected.w <= 0.001F) {
                continue;
            }
            float invW = 1.0F / projected.w;
            float ndcX = projected.x * invW;
            float ndcY = projected.y * invW;
            if (ndcX < -1.25F || ndcX > 1.25F || ndcY < -1.25F || ndcY > 1.25F) {
                continue;
            }
            visible = true;
            float screenX = (ndcX * 0.5F + 0.5F) * width;
            float screenY = (ndcY * 0.5F + 0.5F) * height;
            minX = Math.min(minX, screenX);
            minY = Math.min(minY, screenY);
            maxX = Math.max(maxX, screenX);
            maxY = Math.max(maxY, screenY);
        }
        if (!visible) {
            return null;
        }
        int scissorX = Mth.clamp((int) Math.floor(minX), 0, width - 1);
        int scissorMaxX = Mth.clamp((int) Math.ceil(maxX), scissorX + 1, width);
        int scissorYTop = Mth.clamp((int) Math.floor(minY), 0, height - 1);
        int scissorYBottom = Mth.clamp((int) Math.ceil(maxY), scissorYTop + 1, height);
        int scissorY = height - scissorYBottom;
        int scissorWidth = Math.max(1, scissorMaxX - scissorX);
        int scissorHeight = Math.max(1, scissorYBottom - scissorYTop);
        return new ScreenClip(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    private static void drawPortalQuad(Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal, int textureId) {
        drawTexturedPortal(cameraPos, poseMatrix, portal, textureId, SURFACE_OFFSET, 1.0F, 1.0F, 1.0F, 1.0F, false);
    }

    private static int widthTimesHeight(Minecraft minecraft) {
        return minecraft.getWindow().getWidth() * minecraft.getWindow().getHeight();
    }

    private static void drawPortalOverlay(Minecraft minecraft, Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal) {
        float time = (minecraft.level.getGameTime() + minecraft.getTimer().getGameTimeDeltaPartialTick(false)) * 0.075F;
        float pulse = 0.72F + 0.18F * (float) Math.sin(time);
        float edge = 0.38F + 0.08F * (float) Math.cos(time * 1.7F);
        if (portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE) {
            drawTexturedPortal(cameraPos, poseMatrix, portal, BLUE_OVERLAY, OVERLAY_OFFSET, 0.78F, 0.94F, 1.0F, pulse, false);
            drawTexturedPortal(cameraPos, poseMatrix, portal, BLUE_OVERLAY, OVERLAY_OFFSET + 0.0008D, 0.26F, 0.62F, 1.0F, edge, true);
            return;
        }
        drawTexturedPortal(cameraPos, poseMatrix, portal, ORANGE_OVERLAY, OVERLAY_OFFSET, 1.0F, 0.84F, 0.46F, pulse, false);
        drawTexturedPortal(cameraPos, poseMatrix, portal, ORANGE_OVERLAY, OVERLAY_OFFSET + 0.0008D, 1.0F, 0.48F, 0.16F, edge, true);
    }

    private static void drawTexturedPortal(Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal, ResourceLocation texture, double offset, float red, float green, float blue, float alpha, boolean additive) {
        Vec3 widthVec = portal.getWidthVec().normalize().scale(0.5D);
        Vec3 upVec = portal.getUpVec().normalize().scale(1.0D);
        Vec3 normalVec = portal.getNormalVec().normalize().scale(offset);
        Vec3 center = portal.position().add(normalVec);
        Vec3 bottomLeft = center.subtract(widthVec).subtract(upVec).subtract(cameraPos);
        Vec3 topLeft = center.subtract(widthVec).add(upVec).subtract(cameraPos);
        Vec3 topRight = center.add(widthVec).add(upVec).subtract(cameraPos);
        Vec3 bottomRight = center.add(widthVec).subtract(upVec).subtract(cameraPos);

        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(poseMatrix, (float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).setUv(0.0F, 1.0F);
        buffer.addVertex(poseMatrix, (float) topLeft.x, (float) topLeft.y, (float) topLeft.z).setUv(0.0F, 0.0F);
        buffer.addVertex(poseMatrix, (float) topRight.x, (float) topRight.y, (float) topRight.z).setUv(1.0F, 0.0F);
        buffer.addVertex(poseMatrix, (float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).setUv(1.0F, 1.0F);
        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawTexturedPortal(Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal, int textureId, double offset, float red, float green, float blue, float alpha, boolean additive) {
        Vec3 widthVec = portal.getWidthVec().normalize().scale(0.5D);
        Vec3 upVec = portal.getUpVec().normalize().scale(1.0D);
        Vec3 normalVec = portal.getNormalVec().normalize().scale(offset);
        Vec3 center = portal.position().add(normalVec);
        Vec3 bottomLeft = center.subtract(widthVec).subtract(upVec).subtract(cameraPos);
        Vec3 topLeft = center.subtract(widthVec).add(upVec).subtract(cameraPos);
        Vec3 topRight = center.add(widthVec).add(upVec).subtract(cameraPos);
        Vec3 bottomRight = center.add(widthVec).subtract(upVec).subtract(cameraPos);

        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(poseMatrix, (float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).setUv(0.0F, 1.0F);
        buffer.addVertex(poseMatrix, (float) topLeft.x, (float) topLeft.y, (float) topLeft.z).setUv(0.0F, 0.0F);
        buffer.addVertex(poseMatrix, (float) topRight.x, (float) topRight.y, (float) topRight.z).setUv(1.0F, 0.0F);
        buffer.addVertex(poseMatrix, (float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).setUv(1.0F, 1.0F);
        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static final class PortalCamera extends Camera {
        private void setup(net.minecraft.world.level.BlockGetter level, Entity cameraEntity, float partialTick, Vec3 position, Vec3 look, Vec3 up) {
            super.setup(level, cameraEntity, false, false, partialTick);
            Vec3 normalizedLook = look.normalize();
            Vec3 normalizedUp = up.subtract(normalizedLook.scale(up.dot(normalizedLook))).normalize();
            float yaw = PortalGunTransformUtil.yawFromLook(normalizedLook);
            float pitch = PortalGunTransformUtil.pitchFromLook(normalizedLook);
            this.setPosition(position);
            this.setRotation(yaw, pitch);
            Vec3 currentUp = new Vec3(this.getUpVector()).normalize();
            if (currentUp.dot(normalizedUp) < 0.0D) {
                this.setRotation(yaw + 180.0F, -pitch);
            }
        }
    }

    private record RenderContext(Vec3 cameraPos, Vec3 look, Vec3 up, Matrix4f viewMatrix) {
    }

    private record ScreenClip(int x, int y, int width, int height) {
        private int area() {
            return this.width * this.height;
        }
    }
}
