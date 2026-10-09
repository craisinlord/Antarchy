package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.PortalGunEntityTransformationStack;
import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectionUtil;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import com.craisinlord.antarchy.mixins.client.CameraBasisAccessor;
import com.craisinlord.antarchy.mixins.client.MinecraftMainRenderTargetAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public final class PortalGunPortalViewRenderer {
    private static final int MAX_VIEW_LEVELS = 2;
    private static final double PORTAL_SEARCH_RADIUS = 128.0D;
    private static final int PORTAL_TARGET_FILTER = 9729;
    private static final double SURFACE_OFFSET = 0.04D;
    private static final double EXIT_CLIP_OFFSET = 0.01D;
    private static final int APERTURE_SEGMENTS = 32;
    private static final double APERTURE_RIM_FRACTION = 0.92D;
    private static TextureTarget offscreenTarget;
    private static ShaderInstance portalViewShader;
    private static boolean renderingPortalView;
    private static boolean loggedMissingPortalViewShader;
    private static long lastRenderFailureTick = Long.MIN_VALUE;

    private PortalGunPortalViewRenderer() {
    }

    public static void setPortalViewShader(ShaderInstance shader) {
        portalViewShader = shader;
    }

    public static void clearPortalViewShader() {
        portalViewShader = null;
    }

    public static boolean isEnabled() {
        return AntarchySettings.portalGunSeeThroughPortals();
    }

    public static void releaseTargets() {
        PortalGunPortalVisibilityQueries.clear();
        PortalGunPortalSectionViews.clear();
        if (offscreenTarget != null) {
            offscreenTarget.destroyBuffers();
            offscreenTarget = null;
        }
    }

    public static void render(Camera camera, Matrix4f poseMatrix, Matrix4f projectionMatrix, DeltaTracker tickCounter) {
        if (renderingPortalView) {
            return;
        }
        if (isEnabled()) {
            renderViews(camera, poseMatrix, projectionMatrix, tickCounter);
        }
        renderHiddenOutlines(camera, poseMatrix, projectionMatrix, tickCounter);
    }

    private static void renderHiddenOutlines(Camera camera, Matrix4f poseMatrix, Matrix4f projectionMatrix, DeltaTracker tickCounter) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || camera == null || poseMatrix == null || projectionMatrix == null || tickCounter == null) {
            return;
        }
        try {
            minecraft.getMainRenderTarget().bindWrite(true);
            PortalGunPortalRim.drawHiddenOutlines(minecraft, camera.getPosition(), new Matrix4f(poseMatrix), new Matrix4f(projectionMatrix),
                    tickCounter.getGameTimeDeltaPartialTick(true));
        } catch (RuntimeException exception) {
            logRenderFailure(minecraft, exception);
        }
    }

    private static void renderViews(Camera camera, Matrix4f poseMatrix, Matrix4f projectionMatrix, DeltaTracker tickCounter) {
        PortalRenderWatchdog.frame();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gameRenderer == null || camera == null || poseMatrix == null || projectionMatrix == null || tickCounter == null) {
            return;
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Matrix4f rootViewMatrix = new Matrix4f(poseMatrix);
        Matrix4f rootProjection = new Matrix4f(projectionMatrix);
        float partialTick = tickCounter.getGameTimeDeltaPartialTick(true);
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        int width = mainTarget.width;
        int height = mainTarget.height;

        List<PortalGunPortalEntity> portals = collectPortals(minecraft, cameraPos);
        if (portals.isEmpty()) {
            return;
        }
        List<RootCandidate> candidates = new ArrayList<>();
        for (PortalGunPortalEntity portal : portals) {
            boolean moon = portal.isMoonPortal();
            PortalGunPortalEntity linked = moon ? null : portal.getLinkedPortal();
            if (!moon && (linked == null || !linked.isAlive()) || !portal.shouldRenderFront(cameraPos)) {
                continue;
            }
            ScreenClip clip = computeScreenClip(cameraPos, rootViewMatrix, rootProjection, portal, partialTick, width, height);
            if (clip != null) {
                candidates.add(new RootCandidate(portal, linked, clip));
            }
        }
        candidates.sort((left, right) -> Integer.compare(right.clip().area(), left.clip().area()));
        int maxRootViews = AntarchySettings.portalGunMaxLiveViews();
        if (candidates.size() > maxRootViews) {
            candidates = candidates.subList(0, maxRootViews);
        }
        if (candidates.isEmpty()) {
            return;
        }

        boolean directStencil = PortalStencilTarget.supportsDirect(mainTarget);
        if (!directStencil && portalViewShader == null) {
            if (!loggedMissingPortalViewShader) {
                loggedMissingPortalViewShader = true;
                Antarchy.LOGGER.error("Portal gun destination view shader is unavailable; skipping offscreen portal rendering");
            }
            return;
        }
        Matrix4f cameraToWorld = new Matrix4f(rootViewMatrix).invert();
        Vector3f rootLook = cameraToWorld.transformDirection(new Vector3f(0.0F, 0.0F, -1.0F)).normalize();
        Vector3f rootUp = cameraToWorld.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F)).normalize();
        RenderContext rootContext = new RenderContext(cameraPos, new Vec3(rootLook), new Vec3(rootUp), rootViewMatrix);
        ViewBudget budget = new ViewBudget(maxRootViews * 2);
        PortalRenderWatchdog.portalViewDrawn();
        renderingPortalView = true;
        RenderSystem.backupProjectionMatrix();
        try {
            if (directStencil) {
                PortalStencilTarget.beginFrame(mainTarget);
            }
            for (RootCandidate candidate : candidates) {
                if (!budget.tryReserve()) {
                    break;
                }
                try {
                    if (candidate.linked() == null) {
                        if (directStencil) {
                            renderMoonViewDirect(minecraft, partialTick, rootContext, rootProjection, candidate.portal(), candidate.clip());
                        } else {
                            renderMoonViewOffscreen(minecraft, partialTick, rootContext, rootProjection, candidate.portal(), candidate.clip());
                        }
                    } else if (directStencil) {
                        renderViewDirect(minecraft, cameraEntity, partialTick, rootContext, rootProjection, rootProjection,
                                candidate.portal(), candidate.linked(), 0, candidate.clip(), null, budget);
                    } else {
                        renderViewOffscreen(minecraft, cameraEntity, partialTick, rootContext, rootProjection,
                                candidate.portal(), candidate.linked(), candidate.clip());
                    }
                } catch (RuntimeException exception) {
                    logRenderFailure(minecraft, exception);
                    if (directStencil) {
                        PortalStencilTarget.beginFrame(mainTarget);
                    }
                }
            }
        } catch (RuntimeException exception) {
            logRenderFailure(minecraft, exception);
        } finally {
            try {
                if (directStencil) {
                    PortalStencilTarget.endFrame();
                }
                restorePortalDrawState();
                mainTarget.bindWrite(true);
                minecraft.getEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.crosshairPickEntity);
                minecraft.getBlockEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.hitResult);
            } catch (RuntimeException exception) {
                logRenderFailure(minecraft, exception);
            } finally {
                RenderSystem.restoreProjectionMatrix();
                renderingPortalView = false;
            }
        }
    }

    private static void renderViewDirect(Minecraft minecraft, Entity cameraEntity, float partialTick, RenderContext context,
                                         Matrix4f rootProjection, Matrix4f parentProjection,
                                         PortalGunPortalEntity source, PortalGunPortalEntity destination,
                                         int parentLevel, ScreenClip clip, ScreenClip parentClip, ViewBudget budget) {
        PortalGunWorldPortalShape exit = destination.getWorldPortalShape();
        if (!PortalTerrain.canView(minecraft, exit.center())) {
            return;
        }
        boolean root = parentLevel == 0;
        boolean visible = !root || PortalGunPortalVisibilityQueries.shouldRender(source.getUUID(), destination.getUUID());
        RenderTarget target = minecraft.getMainRenderTarget();
        PortalStencilTarget.pushAperture(parentLevel, () -> {
            boolean queryStarted = root && PortalGunPortalVisibilityQueries.begin(source.getUUID(), destination.getUUID());
            try {
                drawPortalMask(context.cameraPos(), context.viewMatrix(), parentProjection, source, partialTick);
            } finally {
                if (root) {
                    PortalGunPortalVisibilityQueries.end(source.getUUID(), destination.getUUID(), queryStarted);
                }
            }
        });
        try {
            if (!visible) {
                return;
            }
            RenderContext transformed = transformContext(context, source, destination);
            Matrix4f clippedProjection = applyPortalClipPlane(rootProjection, transformed, destination);
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            renderScene(minecraft, cameraEntity, partialTick, transformed, rootProjection, clippedProjection,
                    source, destination, clip, target.width, target.height, parentLevel + 1);
            if (parentLevel + 1 < MAX_VIEW_LEVELS) {
                renderNestedViews(minecraft, cameraEntity, partialTick, transformed, rootProjection, clippedProjection,
                        destination, parentLevel + 1, clip, budget, target.width, target.height);
            }
        } finally {
            try {
                if (visible) {
                    PortalStencilTarget.drawInside(parentLevel + 1);
                    PortalGunPortalRim.drawOverView(source, context.cameraPos(), context.viewMatrix(), parentProjection, partialTick);
                }
            } finally {
                PortalStencilTarget.popAperture(parentLevel,
                        () -> restorePortalApertureDepth(context.cameraPos(), context.viewMatrix(), parentProjection, source, partialTick));
            }
            if (parentClip == null) {
                RenderSystem.disableScissor();
            } else {
                RenderSystem.enableScissor(parentClip.x(), parentClip.y(), parentClip.width(), parentClip.height());
            }
        }
    }

    private static void renderMoonViewDirect(Minecraft minecraft, float partialTick, RenderContext context,
                                             Matrix4f rootProjection, PortalGunPortalEntity source, ScreenClip clip) {
        PortalStencilTarget.pushAperture(0, () -> drawPortalMask(context.cameraPos(), context.viewMatrix(), rootProjection, source, partialTick));
        try {
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            PortalGunMoonViewRenderer.render(minecraft, source.getWorldPortalShape(), context.cameraPos(), context.look(), context.up(),
                    rootProjection, partialTick);
        } finally {
            try {
                PortalStencilTarget.drawInside(1);
                PortalGunPortalRim.drawOverView(source, context.cameraPos(), context.viewMatrix(), rootProjection, partialTick);
            } finally {
                PortalStencilTarget.popAperture(0,
                        () -> restorePortalApertureDepth(context.cameraPos(), context.viewMatrix(), rootProjection, source, partialTick));
                RenderSystem.disableScissor();
            }
        }
    }

    private static void renderMoonViewOffscreen(Minecraft minecraft, float partialTick, RenderContext context,
                                                Matrix4f rootProjection, PortalGunPortalEntity source, ScreenClip clip) {
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        TextureTarget target = ensureOffscreenTarget(mainTarget.width, mainTarget.height);
        MinecraftMainRenderTargetAccessor targetAccessor = (MinecraftMainRenderTargetAccessor) minecraft;
        targetAccessor.antarchy$setMainRenderTarget(target);
        try {
            target.bindWrite(true);
            RenderSystem.disableScissor();
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            PortalGunMoonViewRenderer.render(minecraft, source.getWorldPortalShape(), context.cameraPos(), context.look(), context.up(),
                    rootProjection, partialTick);
        } finally {
            RenderSystem.disableScissor();
            targetAccessor.antarchy$setMainRenderTarget(mainTarget);
            mainTarget.bindWrite(true);
        }
        RenderSystem.setProjectionMatrix(rootProjection, VertexSorting.DISTANCE_TO_ORIGIN);
        drawPortalComposite(context.cameraPos(), context.viewMatrix(), rootProjection, source, partialTick, target.getColorTextureId());
    }

    private static void renderNestedViews(Minecraft minecraft, Entity cameraEntity, float partialTick, RenderContext viewContext,
                                          Matrix4f rootProjection, Matrix4f viewProjection, PortalGunPortalEntity exit,
                                          int level, ScreenClip viewClip, ViewBudget budget, int width, int height) {
        PortalGunWorldPortalShape exitShape = exit.getWorldPortalShape();
        for (PortalGunPortalEntity child : collectPortals(minecraft, viewContext.cameraPos())) {
            if (!budget.hasRemaining()) {
                return;
            }
            PortalGunPortalEntity childLinked = child.getLinkedPortal();
            if (child == exit || childLinked == null || !childLinked.isAlive()
                    || !child.shouldRenderFront(viewContext.cameraPos())
                    || !exitShape.intersectsFront(child.getBoundingBox(), 0.0D)) {
                continue;
            }
            ScreenClip childClip = computeScreenClip(viewContext.cameraPos(), viewContext.viewMatrix(), viewProjection,
                    child, partialTick, width, height);
            childClip = childClip == null ? null : childClip.intersect(viewClip);
            if (childClip == null || !budget.tryReserve()) {
                continue;
            }
            renderViewDirect(minecraft, cameraEntity, partialTick, viewContext, rootProjection, viewProjection,
                    child, childLinked, level, childClip, viewClip, budget);
        }
    }

    private static void renderViewOffscreen(Minecraft minecraft, Entity cameraEntity, float partialTick, RenderContext context,
                                            Matrix4f rootProjection, PortalGunPortalEntity source,
                                            PortalGunPortalEntity destination, ScreenClip clip) {
        if (!PortalTerrain.canView(minecraft, destination.getWorldPortalShape().center())) {
            return;
        }
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        TextureTarget target = ensureOffscreenTarget(mainTarget.width, mainTarget.height);
        RenderContext transformed = transformContext(context, source, destination);
        Matrix4f clippedProjection = applyPortalClipPlane(rootProjection, transformed, destination);
        MinecraftMainRenderTargetAccessor targetAccessor = (MinecraftMainRenderTargetAccessor) minecraft;
        targetAccessor.antarchy$setMainRenderTarget(target);
        try {
            target.bindWrite(true);
            RenderSystem.disableScissor();
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            renderScene(minecraft, cameraEntity, partialTick, transformed, rootProjection, clippedProjection,
                    source, destination, clip, target.width, target.height, 1);
        } finally {
            RenderSystem.disableScissor();
            targetAccessor.antarchy$setMainRenderTarget(mainTarget);
            mainTarget.bindWrite(true);
        }
        RenderSystem.setProjectionMatrix(rootProjection, VertexSorting.DISTANCE_TO_ORIGIN);
        drawPortalComposite(context.cameraPos(), context.viewMatrix(), rootProjection, source, partialTick, target.getColorTextureId());
    }

    private static void renderScene(Minecraft minecraft, Entity cameraEntity, float partialTick, RenderContext transformed,
                                    Matrix4f rootProjection, Matrix4f clippedProjection,
                                    PortalGunPortalEntity source, PortalGunPortalEntity destination,
                                    ScreenClip clip, int width, int height, int level) {
        PortalGunWorldPortalShape exit = destination.getWorldPortalShape();
        PortalCamera portalCamera = new PortalCamera();
        portalCamera.setup(minecraft.level, cameraEntity, partialTick, transformed.cameraPos(), transformed.look(), transformed.up());
        Frustum frustum = new Frustum(transformed.viewMatrix(), cropProjectionToScreenClip(rootProjection, clip, width, height));
        frustum.prepare(transformed.cameraPos().x, transformed.cameraPos().y, transformed.cameraPos().z);
        PortalGunPortalRenderState.pushPortalView(new PortalGunPortalRenderState.PortalRenderContext(
                source.getWorldPortalShape(), exit, transformed.cameraPos(), transformed.look(), transformed.up(),
                transformed.viewMatrix(), clippedProjection, level, false, destination.getUUID()));
        try {
            PortalTerrain terrain = PortalTerrain.prepare(minecraft, destination.getUUID(), exit.center(), exit.normal(), frustum);
            PortalGunPortalSceneRenderer.render(minecraft, portalCamera, partialTick, transformed.viewMatrix(), clippedProjection,
                    rootProjection, frustum, terrain,
                    poseStack -> renderCrossingEntities(minecraft, transformed.cameraPos(), partialTick, source, destination, poseStack));
        } finally {
            PortalGunPortalRenderState.popPortalView(false);
        }
    }

    private static List<PortalGunPortalEntity> collectPortals(Minecraft minecraft, Vec3 cameraPos) {
        List<PortalGunPortalEntity> portals = new ArrayList<>();
        for (PortalGunPortalEntity portal : com.craisinlord.antarchy.content.portalgun.PortalGunPortalRegistry.all(minecraft.level)) {
            if (portal.isAlive() && portal.distanceToSqr(cameraPos) <= PORTAL_SEARCH_RADIUS * PORTAL_SEARCH_RADIUS) {
                portals.add(portal);
            }
        }
        portals.sort((left, right) -> Double.compare(left.distanceToSqr(cameraPos), right.distanceToSqr(cameraPos)));
        return portals;
    }

    private static void logRenderFailure(Minecraft minecraft, RuntimeException exception) {
        long gameTime = minecraft.level == null ? Long.MIN_VALUE : minecraft.level.getGameTime();
        if (lastRenderFailureTick == Long.MIN_VALUE || gameTime == Long.MIN_VALUE || gameTime - lastRenderFailureTick >= 100L) {
            lastRenderFailureTick = gameTime;
            Antarchy.LOGGER.error("Portal gun portal view render failed", exception);
        }
    }

    private static TextureTarget ensureOffscreenTarget(int width, int height) {
        if (offscreenTarget == null) {
            offscreenTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            offscreenTarget.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        } else if (offscreenTarget.width != width || offscreenTarget.height != height) {
            offscreenTarget.resize(width, height, Minecraft.ON_OSX);
        }
        offscreenTarget.setFilterMode(PORTAL_TARGET_FILTER);
        return offscreenTarget;
    }

    private static RenderContext transformContext(RenderContext context, PortalGunPortalEntity source, PortalGunPortalEntity destination) {
        PortalGunWorldPortalShape sourceShape = source.getWorldPortalShape();
        PortalGunWorldPortalShape destinationShape = destination.getWorldPortalShape();
        Vec3 relativeEye = context.cameraPos().subtract(sourceShape.center());
        Vec3 destinationEye = destinationShape.center().add(PortalGunTransformUtil.transformPosition(sourceShape, destinationShape, relativeEye));
        Vec3 look = PortalGunTransformUtil.transformVector(sourceShape, destinationShape, context.look()).normalize();
        Vec3 up = PortalGunTransformUtil.transformVector(sourceShape, destinationShape, context.up()).normalize();
        Matrix4f viewMatrix = new Matrix4f().rotation(PortalGunTransformUtil.orientationQuaternion(look, up).conjugate(new Quaternionf()));
        return new RenderContext(destinationEye, look, up, viewMatrix);
    }

    private static Matrix4f applyPortalClipPlane(Matrix4f projection, RenderContext context, PortalGunPortalEntity destination) {
        PortalGunWorldPortalShape exit = destination.getWorldPortalShape();
        return PortalGunProjectionUtil.clipAtExit(projection, context.cameraPos(), context.look(), context.up(),
                exit.center().add(exit.normal().scale(EXIT_CLIP_OFFSET)), exit.normal());
    }

    private static ScreenClip computeScreenClip(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                                                PortalGunPortalEntity portal, float partialTick, int width, int height) {
        List<Vector4f> clipVertices = projectPortalPolygon(new Matrix4f(projectionMatrix).mul(viewMatrix), cameraPos, portal, partialTick);
        if (clipVertices.size() < 3) {
            return null;
        }
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (Vector4f projected : clipVertices) {
            float invW = 1.0F / projected.w;
            float screenX = (projected.x * invW * 0.5F + 0.5F) * width;
            float screenY = (projected.y * invW * 0.5F + 0.5F) * height;
            minX = Math.min(minX, screenX);
            minY = Math.min(minY, screenY);
            maxX = Math.max(maxX, screenX);
            maxY = Math.max(maxY, screenY);
        }
        if (maxX <= 0.0F || minX >= width || maxY <= 0.0F || minY >= height) {
            return null;
        }
        int x = Mth.clamp((int) Math.floor(minX), 0, width - 1);
        int maxClipX = Mth.clamp((int) Math.ceil(maxX), x + 1, width);
        int y = Mth.clamp((int) Math.floor(minY), 0, height - 1);
        int maxClipY = Mth.clamp((int) Math.ceil(maxY), y + 1, height);
        return new ScreenClip(x, y, maxClipX - x, maxClipY - y);
    }

    private static Matrix4f cropProjectionToScreenClip(Matrix4f projectionMatrix, ScreenClip clip, int width, int height) {
        float left = (float) (clip.x() * 2.0D / width - 1.0D);
        float right = (float) ((clip.x() + clip.width()) * 2.0D / width - 1.0D);
        float bottom = (float) (clip.y() * 2.0D / height - 1.0D);
        float top = (float) ((clip.y() + clip.height()) * 2.0D / height - 1.0D);
        float widthNdc = Math.max(1.0E-5F, right - left);
        float heightNdc = Math.max(1.0E-5F, top - bottom);
        return new Matrix4f()
                .translate(-(right + left) / widthNdc, -(top + bottom) / heightNdc, 0.0F)
                .scale(2.0F / widthNdc, 2.0F / heightNdc, 1.0F)
                .mul(projectionMatrix);
    }

    private static List<Vector4f> clipPolygon(List<Vector4f> vertices, boolean nearPlane) {
        if (vertices.isEmpty()) {
            return vertices;
        }
        List<Vector4f> clipped = new ArrayList<>(vertices.size() + 2);
        Vector4f previous = vertices.get(vertices.size() - 1);
        float previousDistance = clipPlaneDistance(previous, nearPlane);
        for (Vector4f current : vertices) {
            float currentDistance = clipPlaneDistance(current, nearPlane);
            boolean previousInside = previousDistance >= 0.0F;
            boolean currentInside = currentDistance >= 0.0F;
            if (previousInside != currentInside) {
                float amount = previousDistance / (previousDistance - currentDistance);
                clipped.add(new Vector4f(
                        previous.x + (current.x - previous.x) * amount,
                        previous.y + (current.y - previous.y) * amount,
                        previous.z + (current.z - previous.z) * amount,
                        previous.w + (current.w - previous.w) * amount
                ));
            }
            if (currentInside) {
                clipped.add(new Vector4f(current));
            }
            previous = current;
            previousDistance = currentDistance;
        }
        return clipped;
    }

    private static float clipPlaneDistance(Vector4f vertex, boolean nearPlane) {
        return nearPlane ? vertex.z + vertex.w : vertex.w - 0.001F;
    }

    private static List<Vector4f> projectPortalPolygon(Matrix4f combined, Vec3 cameraPos, PortalGunPortalEntity portal, float partialTick) {
        Vec3[] corners = apertureOutline(portal, partialTick);
        List<Vector4f> polygon = new ArrayList<>(corners.length);
        for (Vec3 corner : corners) {
            Vec3 relative = corner.subtract(cameraPos);
            polygon.add(new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1.0F).mul(combined));
        }
        polygon = clipPolygon(polygon, false);
        return clipPolygon(polygon, true);
    }

    static Vec3[] apertureOutline(PortalGunPortalEntity portal, float partialTick) {
        float scale = portal.getPortalVisualScale(partialTick);
        PortalGunWorldPortalShape shape = portal.getWorldPortalShape().scaleAperture(scale);
        double radiusHorizontal = Math.min(shape.halfWidth(), (shape.halfWidth() + PortalGunPortalRim.EXTRA_WIDTH * scale) * APERTURE_RIM_FRACTION);
        double radiusVertical = Math.min(shape.halfHeight(), (shape.halfHeight() + PortalGunPortalRim.EXTRA_HEIGHT * scale) * APERTURE_RIM_FRACTION);
        return shape.getOutline(SURFACE_OFFSET, APERTURE_SEGMENTS, radiusHorizontal, radiusVertical);
    }

    private static MeshData buildAperture(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                                          PortalGunPortalEntity portal, float partialTick, boolean withScreenUv) {
        List<Vector4f> polygon = projectPortalPolygon(new Matrix4f(projectionMatrix).mul(viewMatrix), cameraPos, portal, partialTick);
        if (polygon.size() < 3) {
            return null;
        }
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                withScreenUv ? DefaultVertexFormat.POSITION_TEX : DefaultVertexFormat.POSITION);
        for (int index = 1; index + 1 < polygon.size(); index++) {
            addClipVertex(buffer, polygon.get(0), withScreenUv);
            addClipVertex(buffer, polygon.get(index), withScreenUv);
            addClipVertex(buffer, polygon.get(index + 1), withScreenUv);
        }
        return buffer.build();
    }

    private static void addClipVertex(BufferBuilder buffer, Vector4f clipPosition, boolean withScreenUv) {
        float inverseW = 1.0F / clipPosition.w();
        float ndcX = clipPosition.x() * inverseW;
        float ndcY = clipPosition.y() * inverseW;
        float ndcZ = clipPosition.z() * inverseW;
        if (withScreenUv) {
            buffer.addVertex(ndcX, ndcY, ndcZ).setUv(ndcX * 0.5F + 0.5F, ndcY * 0.5F + 0.5F);
        } else {
            buffer.addVertex(ndcX, ndcY, ndcZ);
        }
    }

    private static void drawPortalMask(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix, PortalGunPortalEntity portal, float partialTick) {
        drawClipSpaceMesh(buildAperture(cameraPos, viewMatrix, projectionMatrix, portal, partialTick, false));
    }

    private static void restorePortalApertureDepth(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                                                   PortalGunPortalEntity portal, float partialTick) {
        MeshData mesh = buildAperture(cameraPos, viewMatrix, projectionMatrix, portal, partialTick, false);
        int previousDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        try {
            drawClipSpaceMesh(mesh);
        } finally {
            GL11.glDepthFunc(previousDepthFunction);
        }
    }

    private static void drawClipSpaceMesh(MeshData mesh) {
        if (mesh == null) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        modelViewStack.pushMatrix();
        try {
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            modelViewStack.identity();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f(), previousSorting);
            RenderSystem.setShader(GameRenderer::getPositionShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferUploader.drawWithShader(mesh);
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableCull();
        }
    }

    private static void drawPortalComposite(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                                            PortalGunPortalEntity portal, float partialTick, int textureId) {
        MeshData mesh = buildAperture(cameraPos, viewMatrix, projectionMatrix, portal, partialTick, true);
        if (mesh == null || portalViewShader == null) {
            if (mesh != null) {
                mesh.close();
            }
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        modelViewStack.pushMatrix();
        try {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            modelViewStack.identity();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f(), previousSorting);
            portalViewShader.setSampler("Sampler0", textureId);
            RenderSystem.setShader(() -> portalViewShader);
            RenderSystem.setShaderTexture(0, textureId);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferUploader.drawWithShader(mesh);
        } finally {
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            restorePortalDrawState();
        }
    }

    private static void renderCrossingEntities(Minecraft minecraft, Vec3 cameraPos, float partialTick,
                                               PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, PoseStack poseStack) {
        MultiBufferSource.BufferSource bufferSource = PortalGunPortalSceneRenderer.bufferSource();
        List<Entity> entities = minecraft.level.getEntities((Entity) null, sourcePortal.getWorldPortalShape().getBoundsForCulling(),
                entity -> isCrossing(entity, sourcePortal, destinationPortal));
        Set<Entity> rendered = Collections.newSetFromMap(new IdentityHashMap<>());
        try {
            for (Entity entity : entities) {
                renderCrossingEntityTree(minecraft, cameraPos, partialTick, sourcePortal, destinationPortal, entity, poseStack, bufferSource, rendered);
            }
        } finally {
            bufferSource.endBatch();
        }
    }

    private static void renderCrossingEntityTree(Minecraft minecraft, Vec3 cameraPos, float partialTick, PortalGunPortalEntity sourcePortal,
                                                 PortalGunPortalEntity destinationPortal, Entity entity, PoseStack poseStack,
                                                 MultiBufferSource.BufferSource bufferSource, Set<Entity> rendered) {
        if (!isCrossing(entity, sourcePortal, destinationPortal) || !rendered.add(entity)) {
            return;
        }
        PortalGunEntityTransformationStack transformationStack = new PortalGunEntityTransformationStack(entity);
        transformationStack.push();
        try {
            Vec3 position = transformationStack.moveEntity(sourcePortal, destinationPortal, partialTick);
            float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            int light = minecraft.getEntityRenderDispatcher().getPackedLightCoords(entity, partialTick);
            PortalGunPortalRenderState.pushDestinationShape(destinationPortal.getWorldPortalShape());
            try {
                minecraft.getEntityRenderDispatcher().render(entity, position.x - cameraPos.x, position.y - cameraPos.y,
                        position.z - cameraPos.z, yaw, partialTick, poseStack, bufferSource, light);
            } finally {
                PortalGunPortalRenderState.popDestinationShape();
            }
        } finally {
            transformationStack.pop();
        }
        for (Entity passenger : entity.getPassengers()) {
            renderCrossingEntityTree(minecraft, cameraPos, partialTick, sourcePortal, destinationPortal, passenger, poseStack, bufferSource, rendered);
        }
    }

    private static boolean isCrossing(Entity entity, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal) {
        return entity != null
                && entity.isAlive()
                && !(entity instanceof PortalGunPortalEntity)
                && entity != sourcePortal
                && entity != destinationPortal
                && entity != Minecraft.getInstance().getCameraEntity()
                && sourcePortal.intersectsEntityBounds(entity);
    }

    private static void restorePortalDrawState() {
        RenderSystem.disableScissor();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static final class PortalCamera extends Camera {
        private void setup(net.minecraft.world.level.BlockGetter level, Entity cameraEntity, float partialTick, Vec3 position, Vec3 look, Vec3 up) {
            super.setup(level, cameraEntity, true, false, partialTick);
            Vec3 normalizedLook = look.normalize();
            Vec3 normalizedUp = up.subtract(normalizedLook.scale(up.dot(normalizedLook))).normalize();
            this.setPosition(position);
            this.setRotation(PortalGunTransformUtil.yawFromLook(normalizedLook), PortalGunTransformUtil.pitchFromLook(normalizedLook));
            Quaternionf orientation = PortalGunTransformUtil.orientationQuaternion(normalizedLook, normalizedUp);
            this.rotation().set(orientation);
            CameraBasisAccessor basis = (CameraBasisAccessor) (Object) this;
            basis.antarchy$getForwards().set(0.0F, 0.0F, -1.0F).rotate(orientation);
            basis.antarchy$getUp().set(0.0F, 1.0F, 0.0F).rotate(orientation);
            basis.antarchy$getLeft().set(-1.0F, 0.0F, 0.0F).rotate(orientation);
        }
    }

    private record RenderContext(Vec3 cameraPos, Vec3 look, Vec3 up, Matrix4f viewMatrix) {
    }

    private record RootCandidate(PortalGunPortalEntity portal, PortalGunPortalEntity linked, ScreenClip clip) {
    }

    private static final class ViewBudget {
        private int remaining;

        private ViewBudget(int maximumViews) {
            this.remaining = maximumViews;
        }

        private boolean tryReserve() {
            if (this.remaining <= 0) {
                return false;
            }
            this.remaining--;
            return true;
        }

        private boolean hasRemaining() {
            return this.remaining > 0;
        }
    }

    private record ScreenClip(int x, int y, int width, int height) {
        private int area() {
            return this.width * this.height;
        }

        private ScreenClip intersect(ScreenClip other) {
            int minX = Math.max(this.x, other.x);
            int minY = Math.max(this.y, other.y);
            int maxX = Math.min(this.x + this.width, other.x + other.width);
            int maxY = Math.min(this.y + this.height, other.y + other.height);
            return maxX <= minX || maxY <= minY ? null : new ScreenClip(minX, minY, maxX - minX, maxY - minY);
        }
    }
}
