package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.PortalGunEntityTransformationStack;
import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.mixins.client.MinecraftMainRenderTargetAccessor;
import com.craisinlord.antarchy.mixins.client.LevelRendererPortalTargetsAccessor;
import com.craisinlord.antarchy.mixins.client.PostChainTargetAccessor;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import com.craisinlord.antarchy.mixins.client.CameraBasisAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.joml.Quaternionf;

public final class PortalGunPortalViewRenderer {
    private static final int MAX_RECURSION_DEPTH = 1;
    private static final int MAX_PORTAL_VIEWS_PER_FRAME = 2;
    private static final double RENDER_TARGET_SCALE = 1.0D;
    private static final boolean ENABLE_DESTINATION_SCENE_RENDERING = true;
    private static final int PORTAL_TARGET_FILTER = 9729;
    private static final double PORTAL_APERTURE_SCALE = 1.0D;
    private static final double SURFACE_OFFSET = 0.04D;
    private static final double OVERLAY_OFFSET = 0.03135D;
    private static final ResourceLocation BLUE_OVERLAY = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_blue.png");
    private static final ResourceLocation ORANGE_OVERLAY = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_orange.png");
    private static final TextureTarget[] PORTAL_TARGETS = new TextureTarget[MAX_RECURSION_DEPTH + 1];
    private static ShaderInstance portalViewShader;
    private static boolean renderingPortalView;
    private static boolean loggedRendererActivation;
    private static String lastPortalCandidateState = "";
    private static final Set<String> loggedPortalSceneRenders = new HashSet<>();
    private static final Set<String> loggedPortalViewComposites = new HashSet<>();
    private static boolean loggedMissingPortalViewShader;
    private static final TargetAllocationFailure[] TARGET_ALLOCATION_FAILURES = new TargetAllocationFailure[MAX_RECURSION_DEPTH + 1];
    private static long lastRenderFailureTick = Long.MIN_VALUE;

    private PortalGunPortalViewRenderer() {
    }

    public static void setPortalViewShader(ShaderInstance shader) {
        portalViewShader = shader;
        Antarchy.LOGGER.info("Portal gun view shader registered");
    }

    public static void clearPortalViewShader() {
        portalViewShader = null;
    }

    public static boolean isEnabled() {
        return ENABLE_DESTINATION_SCENE_RENDERING && AntarchySettings.portalGunSeeThroughPortals();
    }

    public static void releaseTargets() {
        PortalSceneRenderTrace.reset();
        PortalGunPortalVisibilityQueries.clear();
        for (int i = 0; i < PORTAL_TARGETS.length; i++) {
            TextureTarget target = PORTAL_TARGETS[i];
            if (target != null) {
                PortalStencilTarget.release(target);
                target.destroyBuffers();
                PORTAL_TARGETS[i] = null;
            }
        }
        java.util.Arrays.fill(TARGET_ALLOCATION_FAILURES, null);
    }

    public static void render(Camera camera, Matrix4f poseMatrix, Matrix4f projectionMatrix, DeltaTracker tickCounter) {
        if (!isEnabled()) {
            return;
        }
        if (renderingPortalView) {
            return;
        }
        PortalGunPortalRendererPool.beginFrame();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gameRenderer == null || camera == null || poseMatrix == null || projectionMatrix == null || tickCounter == null) {
            return;
        }
        if (!loggedRendererActivation) {
            loggedRendererActivation = true;
            Antarchy.LOGGER.info("Portal gun view renderer hook active");
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Vec3 rootLook = new Vec3(camera.getLookVector()).normalize();
        Vec3 rootUp = new Vec3(camera.getUpVector()).normalize();
        Matrix4f rootViewMatrix = new Matrix4f(poseMatrix);
        List<PortalGunPortalEntity> portals = collectVisiblePortals(minecraft, cameraPos);
        if (portals.isEmpty()) {
            return;
        }
        if (portalViewShader == null) {
            if (!loggedMissingPortalViewShader) {
                loggedMissingPortalViewShader = true;
                Antarchy.LOGGER.error("Portal gun destination view shader is unavailable; skipping destination render");
            }
            return;
        }
        Vec3 effectiveCameraPos = cameraPos;
        Matrix4f rootProjectionMatrix = new Matrix4f(projectionMatrix);
        float partialTick = tickCounter.getGameTimeDeltaPartialTick(true);
        int targetWidth = getPortalTargetWidth(minecraft);
        int targetHeight = getPortalTargetHeight(minecraft);
        IdentityHashMap<PortalGunPortalEntity, ScreenClip> rootScreenClips = new IdentityHashMap<>();
        for (PortalGunPortalEntity portal : portals) {
            if (!portal.shouldRenderFront(effectiveCameraPos)) {
                continue;
            }
            ScreenClip clip = computeScreenClip(cameraPos, rootViewMatrix, rootProjectionMatrix, portal, partialTick, targetWidth, targetHeight);
            if (clip != null) {
                rootScreenClips.put(portal, clip);
            }
        }
        List<PortalGunPortalEntity> renderablePortals = new ArrayList<>();
        for (PortalGunPortalEntity portal : portals) {
            if (rootScreenClips.containsKey(portal)) {
                renderablePortals.add(portal);
            }
        }
        List<PortalGunPortalEntity> selectedPortals = renderablePortals.stream()
                .filter(portal -> {
                    PortalGunPortalEntity linked = portal.getLinkedPortal();
                    return linked != null && linked.isAlive();
                })
                .sorted((left, right) -> Integer.compare(rootScreenClips.get(right).area(), rootScreenClips.get(left).area()))
                .limit(MAX_PORTAL_VIEWS_PER_FRAME)
                .toList();
        long linkedPortalCount = renderablePortals.stream()
                .filter(portal -> portal.getLinkedPortal() != null && portal.getLinkedPortal().isAlive())
                .count();
        String candidateState = portals.stream()
                .map(portal -> portal.getUUID() + ":" + (portal.getLinkedPortal() != null && portal.getLinkedPortal().isAlive() ? portal.getLinkedPortal().getUUID() : "unlinked") + ":" + rootScreenClips.containsKey(portal))
                .sorted()
                .toList()
                + "|selected=" + selectedPortals.stream().map(portal -> portal.getUUID().toString()).sorted().toList()
                + "|shader=" + (portalViewShader != null)
                + "|target=" + targetWidth + "x" + targetHeight;
        if (!candidateState.equals(lastPortalCandidateState)) {
            lastPortalCandidateState = candidateState;
            if (!selectedPortals.isEmpty()) {
                String selectedPairKey = selectedPortals.stream()
                        .map(portal -> portal.getUUID() + "->" + portal.getLinkedPortal().getUUID())
                        .sorted()
                        .toList()
                        .toString();
                PortalSceneRenderTrace.arm(selectedPairKey);
            }
            Antarchy.LOGGER.info(
                    "Portal gun view candidate state changed portals={} frontFacing={} linked={} selected={} shader={} target={}x{}",
                    portals.size(),
                    renderablePortals.size(),
                    linkedPortalCount,
                    selectedPortals.size(),
                    portalViewShader != null,
                    targetWidth,
                    targetHeight
            );
        }
        if (selectedPortals.isEmpty()) {
            return;
        }
        RenderContext rootContext = new RenderContext(effectiveCameraPos, rootLook, rootUp, rootViewMatrix);
        boolean directStencil = PortalStencilTarget.supportsDirect(minecraft.getMainRenderTarget());
        PortalViewBudget fallbackBudget = directStencil ? null : new PortalViewBudget(MAX_PORTAL_VIEWS_PER_FRAME);
        renderingPortalView = true;
        boolean projectionBackedUp = false;
        try {
            RenderSystem.backupProjectionMatrix();
            projectionBackedUp = true;
            for (PortalGunPortalEntity portal : selectedPortals) {
                PortalGunPortalEntity linkedPortal = portal.getLinkedPortal();
                if (linkedPortal == null || !linkedPortal.isAlive()) {
                    continue;
                }
                boolean rendered;
                try {
                    if (directStencil) {
                        rendered = renderPortalSurfaceDirect(minecraft, cameraEntity, tickCounter, rootContext, rootProjectionMatrix, portal, linkedPortal, rootScreenClips.get(portal));
                    } else {
                        int textureId = renderPortalSurface(minecraft, cameraEntity, tickCounter, fallbackBudget, rootContext,
                                rootProjectionMatrix, portal, linkedPortal, 0, rootScreenClips.get(portal));
                        minecraft.gameRenderer.resetProjectionMatrix(rootProjectionMatrix);
                        minecraft.getMainRenderTarget().bindWrite(true);
                        rendered = textureId >= 0 && drawPortalQuad(cameraPos, rootViewMatrix, rootProjectionMatrix,
                                minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height,
                                portal, partialTick, textureId, rootScreenClips.get(portal));
                    }
                } catch (RuntimeException exception) {
                    logRenderFailure(minecraft, exception);
                    RenderSystem.restoreProjectionMatrix();
                    projectionBackedUp = false;
                    RenderSystem.backupProjectionMatrix();
                    projectionBackedUp = true;
                    continue;
                }
                if (!rendered) {
                    continue;
                }
                RenderSystem.restoreProjectionMatrix();
                projectionBackedUp = false;
                RenderSystem.backupProjectionMatrix();
                projectionBackedUp = true;
                minecraft.getMainRenderTarget().bindWrite(true);
                String portalPairKey = portal.getUUID() + "->" + linkedPortal.getUUID();
                if (loggedPortalViewComposites.add(portalPairKey)) {
                    Antarchy.LOGGER.info("Portal gun destination view rendered directly source={} destination={} target={}x{}", portal.getId(), linkedPortal.getId(), targetWidth, targetHeight);
                }
            }
        } catch (RuntimeException exception) {
            logRenderFailure(minecraft, exception);
        } finally {
            try {
                try {
                    restorePortalDrawState();
                } catch (RuntimeException exception) {
                    logRenderFailure(minecraft, exception);
                }
                try {
                    minecraft.getMainRenderTarget().bindWrite(true);
                } catch (RuntimeException exception) {
                    logRenderFailure(minecraft, exception);
                }
                try {
                    minecraft.getEntityRenderDispatcher().prepare(minecraft.level, camera, minecraft.crosshairPickEntity);
                } catch (RuntimeException exception) {
                    logRenderFailure(minecraft, exception);
                }
                try {
                    minecraft.levelRenderer.prepareCullFrustum(cameraPos, rootViewMatrix, rootProjectionMatrix);
                } catch (RuntimeException exception) {
                    logRenderFailure(minecraft, exception);
                }
                if (projectionBackedUp) {
                    try {
                        RenderSystem.restoreProjectionMatrix();
                    } catch (RuntimeException exception) {
                        logRenderFailure(minecraft, exception);
                    }
                }
            } finally {
                renderingPortalView = false;
            }
        }
    }

    private static List<PortalGunPortalEntity> collectVisiblePortals(Minecraft minecraft, Vec3 cameraPos) {
        AABB searchBounds = new AABB(cameraPos, cameraPos).inflate(128.0D);
        List<PortalGunPortalEntity> portals = minecraft.level.getEntitiesOfClass(
                PortalGunPortalEntity.class,
                searchBounds,
                Entity::isAlive
        );
        portals.removeIf(portal -> portal.distanceToSqr(cameraPos) > 16384.0D);
        portals.sort((left, right) -> Double.compare(left.distanceToSqr(cameraPos), right.distanceToSqr(cameraPos)));
        return portals;
    }


    private static void ensureTarget(Minecraft minecraft, int depth) {
        int width = getPortalTargetWidth(minecraft);
        int height = getPortalTargetHeight(minecraft);
        if (width <= 0 || height <= 0) {
            return;
        }
        TextureTarget target = PORTAL_TARGETS[depth];
        if (target == null) {
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            target.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            PORTAL_TARGETS[depth] = target;
        } else if (target.width != width || target.height != height) {
            target.resize(width, height, Minecraft.ON_OSX);
        }
        target.setFilterMode(PORTAL_TARGET_FILTER);
    }

    private static boolean ensureTargetSafely(Minecraft minecraft, int depth) {
        int width = getPortalTargetWidth(minecraft);
        int height = getPortalTargetHeight(minecraft);
        TargetAllocationFailure failure = TARGET_ALLOCATION_FAILURES[depth];
        if (failure != null && failure.width() == width && failure.height() == height) {
            return false;
        }
        try {
            TARGET_ALLOCATION_FAILURES[depth] = null;
            ensureTarget(minecraft, depth);
            return PORTAL_TARGETS[depth] != null;
        } catch (RuntimeException exception) {
            TARGET_ALLOCATION_FAILURES[depth] = new TargetAllocationFailure(width, height);
            Antarchy.LOGGER.error("Portal gun render target allocation failed at recursion depth {}", depth, exception);
            return false;
        }
    }

    private static int getPortalTargetWidth(Minecraft minecraft) {
        return Math.max(1, (int) Math.ceil(minecraft.getMainRenderTarget().width * RENDER_TARGET_SCALE));
    }

    private static int getPortalTargetHeight(Minecraft minecraft) {
        return Math.max(1, (int) Math.ceil(minecraft.getMainRenderTarget().height * RENDER_TARGET_SCALE));
    }

    private static void logRenderFailure(Minecraft minecraft, RuntimeException exception) {
        long gameTime = minecraft.level == null ? Long.MIN_VALUE : minecraft.level.getGameTime();
        if (lastRenderFailureTick == Long.MIN_VALUE || gameTime == Long.MIN_VALUE || gameTime - lastRenderFailureTick >= 100L) {
            lastRenderFailureTick = gameTime;
            Antarchy.LOGGER.error("Portal gun portal view render failed", exception);
        }
    }

    private static boolean renderPortalSurfaceDirect(
            Minecraft minecraft,
            Entity cameraEntity,
            DeltaTracker tickCounter,
            RenderContext currentContext,
            Matrix4f baseProjectionMatrix,
            PortalGunPortalEntity sourcePortal,
            PortalGunPortalEntity destinationPortal,
            ScreenClip clip
    ) {
        RenderTarget target = minecraft.getMainRenderTarget();
        if (clip == null || portalViewShader == null) {
            return false;
        }
        int width = target.width;
        int height = target.height;
        RenderContext transformedContext = transformContext(currentContext, sourcePortal, destinationPortal);
        float partialTick = tickCounter.getGameTimeDeltaPartialTick(true);
        Matrix4f clippedProjection = applyPortalClipPlane(baseProjectionMatrix, transformedContext, destinationPortal);
        Matrix4f apertureCullProjection = cropProjectionToScreenClip(clippedProjection, clip, width, height);
        PortalCamera portalCamera = new PortalCamera();
        portalCamera.setup(minecraft.level, cameraEntity, partialTick, transformedContext.cameraPos(), transformedContext.look(), transformedContext.up());
        String portalPairKey = sourcePortal.getUUID() + "->" + destinationPortal.getUUID();
        LevelRenderer proxyRenderer = PortalGunPortalRendererPool.acquire(minecraft, destinationPortal.getUUID(), width, height);
        LevelRendererPortalTargetsAccessor levelRendererTargets = (LevelRendererPortalTargetsAccessor) proxyRenderer;
        PostChain entityEffect = levelRendererTargets.antarchy$getEntityEffect();
        PostChain transparencyChain = levelRendererTargets.antarchy$getTransparencyChain();
        PostChainTargetAccessor entityEffectTarget = entityEffect == null ? null : (PostChainTargetAccessor) entityEffect;
        PostChainTargetAccessor transparencyTarget = transparencyChain == null ? null : (PostChainTargetAccessor) transparencyChain;
        RenderTarget previousEntityEffectTarget = entityEffectTarget == null ? null : entityEffectTarget.antarchy$getScreenTarget();
        RenderTarget previousTransparencyTarget = transparencyTarget == null ? null : transparencyTarget.antarchy$getScreenTarget();
        PortalStencilTarget.Scope stencilScope = null;
        boolean contextPushed = false;
        boolean renderDestination = PortalGunPortalVisibilityQueries.shouldRender(sourcePortal.getUUID(), destinationPortal.getUUID());
        PortalSceneRenderTrace.event(portalPairKey, "destination-render-decision", "render=" + renderDestination + " clip=" + clip.width() + "x" + clip.height());
        try {
            if (entityEffectTarget != null) {
                entityEffectTarget.antarchy$setScreenTarget(target);
            }
            if (transparencyTarget != null) {
                transparencyTarget.antarchy$setScreenTarget(target);
            }
            minecraft.gameRenderer.resetProjectionMatrix(clippedProjection);
            int maskTexture = minecraft.getTextureManager().getTexture(BLUE_OVERLAY).getId();
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "direct-stencil-mask", "clip=" + clip.width() + "x" + clip.height())) {
                stencilScope = PortalStencilTarget.beginDirect(target, () -> {
                    boolean queryStarted = PortalGunPortalVisibilityQueries.begin(sourcePortal.getUUID(), destinationPortal.getUUID());
                    try {
                        drawPortalQuad(currentContext.cameraPos(), currentContext.viewMatrix(), baseProjectionMatrix, width, height, sourcePortal, partialTick, maskTexture, clip);
                    } finally {
                        PortalGunPortalVisibilityQueries.end(sourcePortal.getUUID(), destinationPortal.getUUID(), queryStarted);
                    }
                },
                        () -> clearDepthInPortalStencil(minecraft),
                        () -> restorePortalApertureDepth(minecraft, currentContext.cameraPos(), currentContext.viewMatrix(), baseProjectionMatrix, sourcePortal, partialTick));
            }
            if (!renderDestination) {
                return false;
            }
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            PortalGunPortalRenderState.pushPortalView(new PortalGunPortalRenderState.PortalRenderContext(
                    sourcePortal.getWorldPortalShape(), destinationPortal.getWorldPortalShape(), transformedContext.cameraPos(),
                    transformedContext.look(), transformedContext.up(), transformedContext.viewMatrix(), clippedProjection,
                    0, false, destinationPortal.getUUID()));
            contextPushed = true;
            proxyRenderer.prepareCullFrustum(portalCamera.getPosition(), transformedContext.viewMatrix(), apertureCullProjection);
            PortalGunPortalViewAreaManager.Scope areaScope = PortalGunPortalViewAreaManager.enter(
                    minecraft, proxyRenderer, destinationPortal.getUUID(), transformedContext.cameraPos(), portalCamera);
            try {
                try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "direct-manual-scene", "target=" + width + "x" + height)) {
                    PortalGunProxySceneRenderer.render(minecraft, proxyRenderer, portalCamera, partialTick,
                            transformedContext.viewMatrix(), clippedProjection, apertureCullProjection, portalPairKey);
                }
            } finally {
                areaScope.close();
            }
            PortalStencilTarget.activate(target);
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            minecraft.getEntityRenderDispatcher().prepare(minecraft.level, portalCamera, cameraEntity);
            renderPortalTransitionEntities(minecraft, transformedContext.cameraPos(), partialTick, sourcePortal, destinationPortal);
            return true;
        } finally {
            try {
                if (stencilScope != null) {
                    stencilScope.close();
                }
                if (contextPushed) {
                    PortalGunPortalRenderState.popPortalView(false);
                }
            } finally {
                if (transparencyTarget != null) {
                    transparencyTarget.antarchy$setScreenTarget(previousTransparencyTarget);
                }
                if (entityEffectTarget != null) {
                    entityEffectTarget.antarchy$setScreenTarget(previousEntityEffectTarget);
                }
                restorePortalDrawState();
                target.bindWrite(true);
                minecraft.gameRenderer.resetProjectionMatrix(baseProjectionMatrix);
            }
        }
    }

    private static int renderPortalSurface(
            Minecraft minecraft,
            Entity cameraEntity,
            DeltaTracker tickCounter,
            PortalViewBudget viewBudget,
            RenderContext currentContext,
            Matrix4f baseProjectionMatrix,
            PortalGunPortalEntity sourcePortal,
            PortalGunPortalEntity destinationPortal,
            int depth,
            ScreenClip precomputedClip
    ) {
        String portalPairKey = sourcePortal.getUUID() + "->" + destinationPortal.getUUID();
        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "ensure-target", depth + ":" + getPortalTargetWidth(minecraft) + "x" + getPortalTargetHeight(minecraft))) {
            if (!ensureTargetSafely(minecraft, depth)) {
                return -1;
            }
        }
        TextureTarget target = PORTAL_TARGETS[depth];
        RenderContext transformedContext = transformContext(currentContext, sourcePortal, destinationPortal);
        float partialTick = tickCounter.getGameTimeDeltaPartialTick(true);
        ScreenClip clip = precomputedClip == null
                ? computeScreenClip(currentContext.cameraPos(), currentContext.viewMatrix(), baseProjectionMatrix, sourcePortal, partialTick, target.width, target.height)
                : precomputedClip;
        if (clip == null) {
            return -1;
        }
        if (!viewBudget.tryReserve()) {
            return -1;
        }
        Matrix4f clippedProjectionMatrix = applyPortalClipPlane(baseProjectionMatrix, transformedContext, destinationPortal);
        Matrix4f apertureCullProjection = cropProjectionToScreenClip(clippedProjectionMatrix, clip, target.width, target.height);
        boolean renderAll = true;
        PortalCamera portalCamera = new PortalCamera();
        portalCamera.setup(minecraft.level, cameraEntity, tickCounter.getGameTimeDeltaPartialTick(true), transformedContext.cameraPos(), transformedContext.look(), transformedContext.up());
        RenderTarget previousMainTarget = minecraft.getMainRenderTarget();
        MinecraftMainRenderTargetAccessor targetAccessor = (MinecraftMainRenderTargetAccessor) minecraft;
        LevelRenderer proxyRenderer;
        try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "acquire-proxy", "target=" + target.width + "x" + target.height + " renderDistance=" + minecraft.options.getEffectiveRenderDistance())) {
            proxyRenderer = PortalGunPortalRendererPool.acquire(minecraft, destinationPortal.getUUID(), target.width, target.height);
        }
        LevelRendererPortalTargetsAccessor levelRendererTargets = (LevelRendererPortalTargetsAccessor) proxyRenderer;
        PostChain entityEffect = levelRendererTargets.antarchy$getEntityEffect();
        PostChain transparencyChain = levelRendererTargets.antarchy$getTransparencyChain();
        PostChainTargetAccessor entityEffectTarget = entityEffect == null ? null : (PostChainTargetAccessor) entityEffect;
        PostChainTargetAccessor transparencyTarget = transparencyChain == null ? null : (PostChainTargetAccessor) transparencyChain;
        RenderTarget previousEntityEffectTarget = entityEffectTarget == null ? null : entityEffectTarget.antarchy$getScreenTarget();
        RenderTarget previousTransparencyTarget = transparencyTarget == null ? null : transparencyTarget.antarchy$getScreenTarget();
        targetAccessor.antarchy$setMainRenderTarget(target);
        boolean portalRenderStatePushed = false;
        PortalStencilTarget.Scope stencilScope = null;
        try {
            if (entityEffectTarget != null) {
                entityEffectTarget.antarchy$setScreenTarget(target);
            }
            if (transparencyTarget != null) {
                transparencyTarget.antarchy$setScreenTarget(target);
            }
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "clear-and-stencil", "target=" + target.width + "x" + target.height + " clip=" + clip.width() + "x" + clip.height())) {
                target.bindWrite(true);
                RenderSystem.disableScissor();
                RenderSystem.depthMask(true);
                RenderSystem.colorMask(true, true, true, true);
                target.clear(Minecraft.ON_OSX);
                minecraft.gameRenderer.resetProjectionMatrix(clippedProjectionMatrix);
                int maskTextureId = minecraft.getTextureManager().getTexture(BLUE_OVERLAY).getId();
                stencilScope = PortalStencilTarget.begin(
                        target,
                        () -> drawPortalQuad(currentContext.cameraPos(), currentContext.viewMatrix(), baseProjectionMatrix, target.width, target.height, sourcePortal, partialTick, maskTextureId, clip)
                );
            }
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            PortalGunPortalRenderState.PortalRenderContext portalRenderContext = new PortalGunPortalRenderState.PortalRenderContext(
                    sourcePortal.getWorldPortalShape(),
                    destinationPortal.getWorldPortalShape(),
                    transformedContext.cameraPos(),
                    transformedContext.look(),
                    transformedContext.up(),
                    transformedContext.viewMatrix(),
                    clippedProjectionMatrix,
                    depth,
                    renderAll,
                    destinationPortal.getUUID()
            );
            PortalGunPortalRenderState.pushPortalView(portalRenderContext);
            portalRenderStatePushed = true;
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "prepare-portal-view", "camera=" + transformedContext.cameraPos())) {
            proxyRenderer.prepareCullFrustum(portalCamera.getPosition(), transformedContext.viewMatrix(), apertureCullProjection);
            }
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "activate-view-area", "destination=" + destinationPortal.getUUID())) {
                PortalGunPortalViewAreaManager.Scope scope = PortalGunPortalViewAreaManager.enter(minecraft, proxyRenderer, destinationPortal.getUUID(), transformedContext.cameraPos(), portalCamera);
                ignored.close();
                try {
                    try (PortalSceneRenderTrace.Phase sceneTrace = PortalSceneRenderTrace.begin(portalPairKey, "manual-scene-subpass", "target=" + target.width + "x" + target.height)) {
                        PortalGunProxySceneRenderer.render(minecraft, proxyRenderer, portalCamera, partialTick, transformedContext.viewMatrix(), clippedProjectionMatrix, apertureCullProjection, portalPairKey);
                    }
                } finally {
                    try (PortalSceneRenderTrace.Phase releaseTrace = PortalSceneRenderTrace.begin(portalPairKey, "release-view-area", "destination=" + destinationPortal.getUUID())) {
                        scope.close();
                    }
                }
            }
            PortalStencilTarget.activate(target);
            RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "transition-entities", "source=" + sourcePortal.getId())) {
                target.bindWrite(true);
                minecraft.getEntityRenderDispatcher().prepare(minecraft.level, portalCamera, cameraEntity);
                renderPortalTransitionEntities(minecraft, transformedContext.cameraPos(), tickCounter.getGameTimeDeltaPartialTick(true), sourcePortal, destinationPortal);
            } catch (RuntimeException exception) {
                logRenderFailure(minecraft, exception);
            }
            if (loggedPortalSceneRenders.add(portalPairKey)) {
                Antarchy.LOGGER.info("Portal gun destination scene render pass completed source={} destination={} depth={} target={}x{} texture={} scissor={}x{}+{},{} camera={}", sourcePortal.getId(), destinationPortal.getId(), depth, target.width, target.height, target.getColorTextureId(), clip.width(), clip.height(), clip.x(), clip.y(), transformedContext.cameraPos());
            }
            if (depth < MAX_RECURSION_DEPTH) {
                List<PortalGunPortalEntity> nestedPortals = collectVisiblePortals(minecraft, transformedContext.cameraPos());
                for (PortalGunPortalEntity nestedPortal : nestedPortals) {
                    if (!viewBudget.hasRemaining()) {
                        break;
                    }
                    PortalGunPortalEntity nestedLinked = nestedPortal.getLinkedPortal();
                    if (nestedLinked == null || !nestedLinked.isAlive() || nestedPortal == destinationPortal || nestedPortal == sourcePortal) {
                        continue;
                    }
                    ScreenClip nestedClip = computeScreenClip(transformedContext.cameraPos(), transformedContext.viewMatrix(), baseProjectionMatrix, nestedPortal, partialTick, target.width, target.height);
                    if (nestedClip == null) {
                        continue;
                    }
                    int nestedTextureId;
                    try {
                        nestedTextureId = renderPortalSurface(minecraft, cameraEntity, tickCounter, viewBudget, transformedContext, baseProjectionMatrix, nestedPortal, nestedLinked, depth + 1, nestedClip);
                    } catch (RuntimeException exception) {
                        logRenderFailure(minecraft, exception);
                        continue;
                    }
                    if (nestedTextureId < 0) {
                        continue;
                    }
                    try {
                        RenderSystem.restoreProjectionMatrix();
                        RenderSystem.backupProjectionMatrix();
                        target.bindWrite(true);
                        PortalStencilTarget.activate(target);
                        RenderSystem.enableScissor(clip.x(), clip.y(), clip.width(), clip.height());
                        drawPortalQuad(transformedContext.cameraPos(), transformedContext.viewMatrix(), baseProjectionMatrix, target.width, target.height, nestedPortal, partialTick, nestedTextureId, nestedClip);
                    } catch (RuntimeException exception) {
                        logRenderFailure(minecraft, exception);
                    }
                }
            }
        } finally {
            try (PortalSceneRenderTrace.Phase ignored = PortalSceneRenderTrace.begin(portalPairKey, "restore-render-state", "depth=" + depth)) {
                try {
                    if (stencilScope != null) {
                        stencilScope.close();
                    }
                    if (portalRenderStatePushed) {
                        PortalGunPortalRenderState.popPortalView(renderAll);
                    }
                } finally {
                    try {
                        restorePortalDrawState();
                    } finally {
                        try {
                            if (transparencyTarget != null) {
                                transparencyTarget.antarchy$setScreenTarget(previousTransparencyTarget);
                            }
                        } finally {
                            try {
                                if (entityEffectTarget != null) {
                                    entityEffectTarget.antarchy$setScreenTarget(previousEntityEffectTarget);
                                }
                            } finally {
                                targetAccessor.antarchy$setMainRenderTarget(previousMainTarget);
                                previousMainTarget.bindWrite(true);
                            }
                        }
                    }
                }
            }
        }
        return target.getColorTextureId();
    }

    private static RenderContext transformContext(RenderContext currentContext, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal) {
        Vec3 relativeEye = currentContext.cameraPos().subtract(sourcePortal.position());
        Vec3 transformedEyeOffset = PortalGunTransformUtil.transformRelativePosition(sourcePortal, destinationPortal, relativeEye);
        Vec3 destinationEye = destinationPortal.position().add(transformedEyeOffset);
        Vec3 transformedLook = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, currentContext.look()).normalize();
        Vec3 transformedUp = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, currentContext.up()).normalize();
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
        Vector4f q = new Vector4f(signNonZero(clipPlane.x()), signNonZero(clipPlane.y()), 1.0F, 1.0F)
                .mul(new Matrix4f(projectionMatrix).invert());
        float planeCornerDot = clipPlane.dot(q);
        if (!Float.isFinite(planeCornerDot) || Math.abs(planeCornerDot) <= 1.0E-6F) {
            return projectionMatrix;
        }
        float scale = 2.0F / planeCornerDot;
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
        Vec3 right = look.cross(up).normalize();
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

    private static ScreenClip computeScreenClip(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix, PortalGunPortalEntity portal, float partialTick, int width, int height) {
        PortalGunWorldPortalShape renderShape = portal.getWorldPortalShape().scaleAperture(portal.getPortalVisualScale(partialTick));
        Vec3[] worldCorners = renderShape.getCorners(SURFACE_OFFSET);
        List<Vector4f> clipVertices = new java.util.ArrayList<>(worldCorners.length);
        Matrix4f combined = new Matrix4f(projectionMatrix).mul(viewMatrix);
        for (Vec3 worldCorner : worldCorners) {
            Vec3 corner = worldCorner.subtract(cameraPos);
            clipVertices.add(new Vector4f((float) corner.x, (float) corner.y, (float) corner.z, 1.0F).mul(combined));
        }
        clipVertices = clipPolygon(clipVertices, false);
        clipVertices = clipPolygon(clipVertices, true);
        if (clipVertices.size() < 3) {
            return null;
        }
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (Vector4f projected : clipVertices) {
            float invW = 1.0F / projected.w;
            float ndcX = projected.x * invW;
            float ndcY = projected.y * invW;
            float screenX = (ndcX * 0.5F + 0.5F) * width;
            float screenY = (ndcY * 0.5F + 0.5F) * height;
            minX = Math.min(minX, screenX);
            minY = Math.min(minY, screenY);
            maxX = Math.max(maxX, screenX);
            maxY = Math.max(maxY, screenY);
        }
        if (maxX <= 0.0F || minX >= width || maxY <= 0.0F || minY >= height) {
            return null;
        }
        int scissorX = Mth.clamp((int) Math.floor(minX), 0, width - 1);
        int scissorMaxX = Mth.clamp((int) Math.ceil(maxX), scissorX + 1, width);
        int scissorYTop = Mth.clamp((int) Math.floor(minY), 0, height - 1);
        int scissorYBottom = Mth.clamp((int) Math.ceil(maxY), scissorYTop + 1, height);
        int scissorY = scissorYTop;
        int scissorWidth = Math.max(1, scissorMaxX - scissorX);
        int scissorHeight = Math.max(1, scissorYBottom - scissorYTop);
        return new ScreenClip(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    private static Matrix4f cropProjectionToScreenClip(Matrix4f projectionMatrix, ScreenClip clip, int width, int height) {
        float left = (float) (clip.x() * 2.0D / width - 1.0D);
        float right = (float) ((clip.x() + clip.width()) * 2.0D / width - 1.0D);
        float bottom = (float) (clip.y() * 2.0D / height - 1.0D);
        float top = (float) ((clip.y() + clip.height()) * 2.0D / height - 1.0D);
        float widthNdc = Math.max(1.0E-5F, right - left);
        float heightNdc = Math.max(1.0E-5F, top - bottom);
        float scaleX = 2.0F / widthNdc;
        float scaleY = 2.0F / heightNdc;
        float offsetX = -(right + left) / widthNdc;
        float offsetY = -(top + bottom) / heightNdc;
        return new Matrix4f().translate(offsetX, offsetY, 0.0F).scale(scaleX, scaleY, 1.0F).mul(projectionMatrix);
    }

    private static List<Vector4f> clipPolygon(List<Vector4f> vertices, boolean nearPlane) {
        if (vertices.isEmpty()) {
            return vertices;
        }
        List<Vector4f> clipped = new java.util.ArrayList<>(vertices.size() + 2);
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

    private static boolean drawPortalQuad(Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix, int width, int height, PortalGunPortalEntity portal, float partialTick, int textureId, ScreenClip clip) {
        if (portalViewShader == null) {
            if (!loggedMissingPortalViewShader) {
                loggedMissingPortalViewShader = true;
                Antarchy.LOGGER.error("Portal gun destination view shader became unavailable; skipping destination composite");
            }
            return false;
        }
        if (clip == null || width <= 0 || height <= 0) {
            return false;
        }
        PortalGunWorldPortalShape shape = portal.getWorldPortalShape().scaleAperture(portal.getPortalVisualScale(partialTick) * PORTAL_APERTURE_SCALE);
        Matrix4f combined = new Matrix4f(projectionMatrix).mul(viewMatrix);
        List<Vector4f> polygon = projectPortalPolygon(combined, cameraPos, shape);
        if (polygon.size() < 3) {
            return false;
        }
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        for (int index = 1; index + 1 < polygon.size(); index++) {
            addPortalViewVertex(buffer, polygon.get(0));
            addPortalViewVertex(buffer, polygon.get(index));
            addPortalViewVertex(buffer, polygon.get(index + 1));
        }
        MeshData mesh = buffer.build();
        if (mesh == null) {
            return false;
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
        return true;
    }

    private static void clearDepthInPortalStencil(Minecraft minecraft) {
        int previousDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        buffer.addVertex(-1.0F, -1.0F, 1.0F);
        buffer.addVertex(3.0F, -1.0F, 1.0F);
        buffer.addVertex(-1.0F, 3.0F, 1.0F);
        MeshData mesh = buffer.build();
        try {
            drawClipSpaceMesh(minecraft, mesh);
        } finally {
            GL11.glDepthFunc(previousDepthFunction);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    private static void restorePortalApertureDepth(Minecraft minecraft, Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                                                   PortalGunPortalEntity portal, float partialTick) {
        PortalGunWorldPortalShape shape = portal.getWorldPortalShape().scaleAperture(portal.getPortalVisualScale(partialTick) * PORTAL_APERTURE_SCALE);
        Matrix4f combined = new Matrix4f(projectionMatrix).mul(viewMatrix);
        List<Vector4f> polygon = projectPortalPolygon(combined, cameraPos, shape);
        if (polygon.size() < 3) {
            return;
        }
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        for (int index = 1; index + 1 < polygon.size(); index++) {
            addDepthVertex(buffer, polygon.get(0));
            addDepthVertex(buffer, polygon.get(index));
            addDepthVertex(buffer, polygon.get(index + 1));
        }
        MeshData mesh = buffer.build();
        int previousDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        try {
            drawClipSpaceMesh(minecraft, mesh);
        } finally {
            GL11.glDepthFunc(previousDepthFunction);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    private static void addDepthVertex(BufferBuilder buffer, Vector4f clipPosition) {
        float inverseW = 1.0F / clipPosition.w();
        buffer.addVertex(clipPosition.x() * inverseW, clipPosition.y() * inverseW, clipPosition.z() * inverseW);
    }

    private static void drawClipSpaceMesh(Minecraft minecraft, MeshData mesh) {
        if (mesh == null) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
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
            if (cullEnabled) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (blendEnabled) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
        }
    }

    private static List<Vector4f> projectPortalPolygon(Matrix4f combined, Vec3 cameraPos, PortalGunWorldPortalShape shape) {
        Vec3[] corners = shape.getCorners(SURFACE_OFFSET);
        List<Vector4f> polygon = new ArrayList<>(corners.length);
        for (Vec3 corner : corners) {
            Vec3 relative = corner.subtract(cameraPos);
            polygon.add(new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1.0F).mul(combined));
        }
        polygon = clipPolygon(polygon, false);
        return clipPolygon(polygon, true);
    }

    private static void addPortalViewVertex(BufferBuilder buffer, Vector4f clipPosition) {
        float inverseW = 1.0F / clipPosition.w();
        float ndcX = clipPosition.x() * inverseW;
        float ndcY = clipPosition.y() * inverseW;
        float ndcZ = clipPosition.z() * inverseW;
        buffer.addVertex(ndcX, ndcY, ndcZ).setUv(ndcX * 0.5F + 0.5F, ndcY * 0.5F + 0.5F);
    }

    private static void renderPortalTransitionEntities(Minecraft minecraft, Vec3 cameraPos, float partialTick, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal) {
        if (minecraft.level == null || minecraft.getEntityRenderDispatcher() == null) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = PortalGunPortalRendererPool.bufferSource();
        PoseStack poseStack = new PoseStack();
        AABB transitionBounds = sourcePortal.getWorldPortalShape().getBoundsForCulling();
        List<Entity> renderEntities = minecraft.level.getEntities(
                (Entity) null,
                transitionBounds,
                entity -> shouldRenderTransitionEntity(entity, sourcePortal, destinationPortal)
        );
        Set<Entity> renderedEntities = Collections.newSetFromMap(new IdentityHashMap<>());
        try {
            for (Entity entity : renderEntities) {
                renderTransitionEntityTree(minecraft, cameraPos, partialTick, sourcePortal, destinationPortal, entity, poseStack, bufferSource, renderedEntities);
            }
        } finally {
            bufferSource.endBatch();
        }
    }

    private static void renderTransitionEntityTree(Minecraft minecraft, Vec3 cameraPos, float partialTick, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, Entity entity, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Set<Entity> renderedEntities) {
        if (!shouldRenderTransitionEntity(entity, sourcePortal, destinationPortal) || !renderedEntities.add(entity)) {
            return;
        }
        renderTransitionEntity(minecraft, cameraPos, partialTick, sourcePortal, destinationPortal, entity, poseStack, bufferSource);
        for (Entity passenger : entity.getPassengers()) {
            renderTransitionEntityTree(minecraft, cameraPos, partialTick, sourcePortal, destinationPortal, passenger, poseStack, bufferSource, renderedEntities);
        }
    }

    private static boolean shouldRenderTransitionEntity(Entity entity, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal) {
        return entity != null
                && entity.isAlive()
                && !(entity instanceof PortalGunPortalEntity)
                && entity != sourcePortal
                && entity != destinationPortal
                && sourcePortal.intersectsEntityBounds(entity);
    }

    private static void renderTransitionEntity(Minecraft minecraft, Vec3 cameraPos, float partialTick, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, Entity entity, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        PortalGunEntityTransformationStack transformationStack = new PortalGunEntityTransformationStack(entity);
        transformationStack.push();
        try {
            Vec3 transformedPosition = transformationStack.moveEntity(sourcePortal, destinationPortal, partialTick);
            float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            int light = minecraft.getEntityRenderDispatcher().getPackedLightCoords(entity, partialTick);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            PortalGunPortalRenderState.pushDestinationShape(destinationPortal.getWorldPortalShape());
            try {
                minecraft.getEntityRenderDispatcher().render(
                        entity,
                        transformedPosition.x - cameraPos.x,
                        transformedPosition.y - cameraPos.y,
                        transformedPosition.z - cameraPos.z,
                        yaw,
                        partialTick,
                        poseStack,
                        bufferSource,
                        light
                );
            } finally {
                PortalGunPortalRenderState.popDestinationShape();
            }
        } finally {
            transformationStack.pop();
        }
    }

    private static void drawPortalOverlay(Minecraft minecraft, Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal) {
        if (!portal.shouldRenderFront(cameraPos)) {
            return;
        }
        RenderSystem.disableScissor();
        float time = (minecraft.level.getGameTime() + minecraft.getTimer().getGameTimeDeltaPartialTick(false)) * 0.075F;
        float pulse = 0.72F + 0.18F * (float) Math.sin(time);
        float edge = 0.38F + 0.08F * (float) Math.cos(time * 1.7F);
        float channelRed = portal.getChannelRed() / 255.0F;
        float channelGreen = portal.getChannelGreen() / 255.0F;
        float channelBlue = portal.getChannelBlue() / 255.0F;
        if (portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE) {
            drawTexturedPortal(cameraPos, poseMatrix, portal, BLUE_OVERLAY, OVERLAY_OFFSET, 0.78F, 0.94F, 1.0F, pulse, false);
            drawTexturedPortal(cameraPos, poseMatrix, portal, BLUE_OVERLAY, OVERLAY_OFFSET + 0.0008D, channelRed, channelGreen, channelBlue, edge, true);
            drawAnimatedPortalOverlay(minecraft, cameraPos, poseMatrix, portal);
            return;
        }
        drawTexturedPortal(cameraPos, poseMatrix, portal, ORANGE_OVERLAY, OVERLAY_OFFSET, 1.0F, 0.84F, 0.46F, pulse, false);
        drawTexturedPortal(cameraPos, poseMatrix, portal, ORANGE_OVERLAY, OVERLAY_OFFSET + 0.0008D, channelRed, channelGreen, channelBlue, edge, true);
        drawAnimatedPortalOverlay(minecraft, cameraPos, poseMatrix, portal);
    }

    private static void drawAnimatedPortalOverlay(Minecraft minecraft, Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal) {
        if (!AntarchySettings.portalGunFancyPortals()
                || minecraft.options.graphicsMode().get() != GraphicsStatus.FANCY) {
            return;
        }
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "textures/entity/portal_gun/" + (portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE
                        ? "portal_blue_vortex.png"
                        : "portal_orange_vortex.png"));
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        float portalScale = portal.getPortalVisualScale(partialTick);
        Vec3 widthVec = portal.getWidthVec().normalize().scale(portal.getPortalWidth() / 2.0D * portalScale);
        Vec3 upVec = portal.getUpVec().normalize().scale(portal.getPortalHeight() / 2.0D * portalScale);
        Vec3 normalVec = portal.getNormalVec().normalize().scale(OVERLAY_OFFSET + 0.0016D);
        Vec3 center = portal.position().add(normalVec);
        Vec3 bottomLeft = center.subtract(widthVec).subtract(upVec).subtract(cameraPos);
        Vec3 topLeft = center.subtract(widthVec).add(upVec).subtract(cameraPos);
        Vec3 topRight = center.add(widthVec).add(upVec).subtract(cameraPos);
        Vec3 bottomRight = center.add(widthVec).subtract(upVec).subtract(cameraPos);
        float red = portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE ? 0.78F : 1.0F;
        float green = portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE ? 0.94F : 0.84F;
        float blue = portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE ? 1.0F : 0.46F;
        float frameV = Math.floorMod((int) ((minecraft.level.getGameTime() + minecraft.getTimer().getGameTimeDeltaPartialTick(false)) / 5.0F), 4) * 0.25F;
        float frameBottomV = frameV + 0.25F;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(poseMatrix, (float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).setUv(0.0F, frameBottomV);
        buffer.addVertex(poseMatrix, (float) topLeft.x, (float) topLeft.y, (float) topLeft.z).setUv(0.0F, frameV);
        buffer.addVertex(poseMatrix, (float) topRight.x, (float) topRight.y, (float) topRight.z).setUv(1.0F, frameV);
        buffer.addVertex(poseMatrix, (float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).setUv(1.0F, frameBottomV);
        MeshData mesh = buffer.build();
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(red, green, blue, 0.92F);
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        } finally {
            restorePortalDrawState();
        }
    }

    private static void drawTexturedPortal(Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal, ResourceLocation texture, double offset, float red, float green, float blue, float alpha, boolean additive) {
        float portalScale = portal.getPortalVisualScale(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        Vec3 widthVec = portal.getWidthVec().normalize().scale(portal.getPortalWidth() / 2.0D * portalScale);
        Vec3 upVec = portal.getUpVec().normalize().scale(portal.getPortalHeight() / 2.0D * portalScale);
        Vec3 normalVec = portal.getNormalVec().normalize().scale(offset);
        Vec3 center = portal.position().add(normalVec);
        Vec3 bottomLeft = center.subtract(widthVec).subtract(upVec).subtract(cameraPos);
        Vec3 topLeft = center.subtract(widthVec).add(upVec).subtract(cameraPos);
        Vec3 topRight = center.add(widthVec).add(upVec).subtract(cameraPos);
        Vec3 bottomRight = center.add(widthVec).subtract(upVec).subtract(cameraPos);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(poseMatrix, (float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).setUv(0.0F, 1.0F);
        buffer.addVertex(poseMatrix, (float) topLeft.x, (float) topLeft.y, (float) topLeft.z).setUv(0.0F, 0.0F);
        buffer.addVertex(poseMatrix, (float) topRight.x, (float) topRight.y, (float) topRight.z).setUv(1.0F, 0.0F);
        buffer.addVertex(poseMatrix, (float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).setUv(1.0F, 1.0F);
        MeshData mesh = buffer.build();
        try {
            RenderSystem.enableBlend();
            if (additive) {
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            } else {
                RenderSystem.defaultBlendFunc();
            }
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(red, green, blue, alpha);
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        } finally {
            restorePortalDrawState();
        }
    }

    private static void drawTexturedPortal(Vec3 cameraPos, Matrix4f poseMatrix, PortalGunPortalEntity portal, int textureId, double offset, float red, float green, float blue, float alpha, boolean additive, float bottomLeftU, float bottomLeftV, float topLeftU, float topLeftV, float topRightU, float topRightV, float bottomRightU, float bottomRightV) {
        float portalScale = portal.getPortalVisualScale(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        Vec3 widthVec = portal.getWidthVec().normalize().scale(portal.getPortalWidth() / 2.0D * portalScale);
        Vec3 upVec = portal.getUpVec().normalize().scale(portal.getPortalHeight() / 2.0D * portalScale);
        Vec3 normalVec = portal.getNormalVec().normalize().scale(offset);
        Vec3 center = portal.position().add(normalVec);
        Vec3 bottomLeft = center.subtract(widthVec).subtract(upVec).subtract(cameraPos);
        Vec3 topLeft = center.subtract(widthVec).add(upVec).subtract(cameraPos);
        Vec3 topRight = center.add(widthVec).add(upVec).subtract(cameraPos);
        Vec3 bottomRight = center.add(widthVec).subtract(upVec).subtract(cameraPos);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(poseMatrix, (float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).setUv(bottomLeftU, bottomLeftV);
        buffer.addVertex(poseMatrix, (float) topLeft.x, (float) topLeft.y, (float) topLeft.z).setUv(topLeftU, topLeftV);
        buffer.addVertex(poseMatrix, (float) topRight.x, (float) topRight.y, (float) topRight.z).setUv(topRightU, topRightV);
        buffer.addVertex(poseMatrix, (float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).setUv(bottomRightU, bottomRightV);
        MeshData mesh = buffer.build();
        try {
            RenderSystem.enableBlend();
            if (additive) {
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            } else {
                RenderSystem.defaultBlendFunc();
            }
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, textureId);
            RenderSystem.setShaderColor(red, green, blue, alpha);
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        } finally {
            restorePortalDrawState();
        }
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
            super.setup(level, cameraEntity, false, false, partialTick);
            Vec3 normalizedLook = look.normalize();
            Vec3 normalizedUp = up.subtract(normalizedLook.scale(up.dot(normalizedLook))).normalize();
            float yaw = PortalGunTransformUtil.yawFromLook(normalizedLook);
            float pitch = PortalGunTransformUtil.pitchFromLook(normalizedLook);
            this.setPosition(position);
            this.setRotation(yaw, pitch);
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

    private static final class PortalViewBudget {
        private int remaining;

        private PortalViewBudget(int maximumViews) {
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
    }

    private record TargetAllocationFailure(int width, int height) {
    }
}
