package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

public final class PortalGunPortalRim {
    public static final float EXTRA_WIDTH = 0.0F;
    public static final float EXTRA_HEIGHT = 0.0F;
    public static final int ENTITY_ALPHA = 150;
    private static final double OVERLAY_OFFSET = 0.05D;
    private static final int HIDDEN_RIM_ALPHA = 170;
    private static final ResourceLocation BLUE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_blue.png");
    private static final ResourceLocation ORANGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_orange.png");

    private PortalGunPortalRim() {
    }

    public static ResourceLocation texture(PortalGunPortalEntity portal) {
        return portal.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE ? BLUE_TEXTURE : ORANGE_TEXTURE;
    }

    static void drawOverView(PortalGunPortalEntity portal, Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix, float partialTick) {
        withCameraMatrices(viewMatrix, projectionMatrix, () -> {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            drawRims(List.of(portal), cameraPos, partialTick, ENTITY_ALPHA);
        });
    }

    static void drawHiddenOutlines(Minecraft minecraft, Vec3 cameraPos, Matrix4f viewMatrix, Matrix4f projectionMatrix, float partialTick) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        List<PortalGunPortalEntity> ownedPortals = collectOwnedPortals(minecraft);
        if (ownedPortals.isEmpty()) {
            return;
        }
        withCameraMatrices(viewMatrix, projectionMatrix, () -> {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.depthFunc(GL11.GL_GREATER);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            drawRims(ownedPortals, cameraPos, partialTick, HIDDEN_RIM_ALPHA);
        });
    }

    private static void withCameraMatrices(Matrix4f viewMatrix, Matrix4f projectionMatrix, Runnable draw) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting previousSorting = RenderSystem.getVertexSorting();
        modelViewStack.pushMatrix();
        try {
            modelViewStack.set(viewMatrix);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f(projectionMatrix), VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            draw.run();
        } finally {
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.setProjectionMatrix(previousProjection, previousSorting);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static List<PortalGunPortalEntity> collectOwnedPortals(Minecraft minecraft) {
        Set<UUID> owners = new HashSet<>();
        Player player = minecraft.player;
        owners.add(player.getUUID());
        addGunOwner(owners, player.getMainHandItem(), player.getUUID());
        addGunOwner(owners, player.getOffhandItem(), player.getUUID());
        List<PortalGunPortalEntity> portals = new ArrayList<>();
        for (PortalGunPortalEntity portal : com.craisinlord.antarchy.content.portalgun.PortalGunPortalRegistry.all(minecraft.level)) {
            if (portal.isAlive() && owners.contains(portal.getOwnerId())) {
                portals.add(portal);
            }
        }
        return portals;
    }

    private static void addGunOwner(Set<UUID> owners, ItemStack stack, UUID fallback) {
        if (stack.getItem() instanceof PortalGunItem) {
            UUID owner = PortalGunItem.getPortalOwnerId(stack, fallback);
            if (owner != null) {
                owners.add(owner);
            }
        }
    }

    private static void drawRims(List<PortalGunPortalEntity> portals, Vec3 cameraPos, float partialTick, int alpha) {
        drawRimsWithTexture(portals, cameraPos, partialTick, alpha, BLUE_TEXTURE, PortalGunPortalEntity.PortalSide.BLUE);
        drawRimsWithTexture(portals, cameraPos, partialTick, alpha, ORANGE_TEXTURE, PortalGunPortalEntity.PortalSide.ORANGE);
    }

    private static void drawRimsWithTexture(List<PortalGunPortalEntity> portals, Vec3 cameraPos, float partialTick, int alpha,
                                            ResourceLocation texture, PortalGunPortalEntity.PortalSide side) {
        BufferBuilder buffer = null;
        for (PortalGunPortalEntity portal : portals) {
            if (portal.getPortalSide() != side) {
                continue;
            }
            if (buffer == null) {
                buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            }
            addRim(buffer, portal, cameraPos, partialTick, alpha);
        }
        if (buffer == null) {
            return;
        }
        MeshData mesh = buffer.build();
        if (mesh == null) {
            return;
        }
        RenderSystem.setShaderTexture(0, texture);
        BufferUploader.drawWithShader(mesh);
    }

    private static void addRim(BufferBuilder buffer, PortalGunPortalEntity portal, Vec3 cameraPos, float partialTick, int alpha) {
        float scale = portal.getPortalVisualScale(partialTick);
        if (scale <= 0.0F) {
            return;
        }
        Vec3 right = portal.getWidthVec().normalize();
        Vec3 up = portal.getUpVec().normalize();
        Vec3 center = portal.position().add(portal.getNormalVec().normalize().scale(OVERLAY_OFFSET)).subtract(cameraPos);
        double halfWidth = (portal.getPortalWidth() * 0.5D + EXTRA_WIDTH) * scale;
        double halfHeight = (portal.getPortalHeight() * 0.5D + EXTRA_HEIGHT) * scale;
        int color = portal.getPortalColor();
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        addVertex(buffer, center, right, up, -halfWidth, -halfHeight, 0.0F, 1.0F, red, green, blue, alpha);
        addVertex(buffer, center, right, up, -halfWidth, halfHeight, 0.0F, 0.0F, red, green, blue, alpha);
        addVertex(buffer, center, right, up, halfWidth, halfHeight, 1.0F, 0.0F, red, green, blue, alpha);
        addVertex(buffer, center, right, up, halfWidth, -halfHeight, 1.0F, 1.0F, red, green, blue, alpha);
    }

    private static void addVertex(BufferBuilder buffer, Vec3 center, Vec3 right, Vec3 up, double horizontal, double vertical,
                                  float u, float v, int red, int green, int blue, int alpha) {
        Vec3 position = center.add(right.scale(horizontal)).add(up.scale(vertical));
        buffer.addVertex((float) position.x, (float) position.y, (float) position.z)
                .setUv(u, v)
                .setColor(red, green, blue, alpha);
    }
}
