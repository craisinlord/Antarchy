package com.craisinlord.antarchy.content.client.renderer;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public interface PortalTerrain {
    static boolean canView(Minecraft minecraft, Vec3 exitCenter) {
        return SodiumCompat.isLoaded()
                ? com.craisinlord.antarchy.content.client.renderer.sodium.SodiumPortalTerrain.canView(minecraft, exitCenter)
                : PortalGunPortalSectionViews.canView(minecraft, exitCenter);
    }

    static PortalTerrain prepare(Minecraft minecraft, java.util.UUID exitId, Vec3 exitCenter, Vec3 exitNormal, Frustum frustum) {
        return SodiumCompat.isLoaded()
                ? com.craisinlord.antarchy.content.client.renderer.sodium.SodiumPortalTerrain.prepare(minecraft, frustum)
                : VanillaPortalTerrain.prepare(minecraft, exitId, exitCenter, exitNormal, frustum);
    }

    int sectionCount();

    void begin(Minecraft minecraft);

    void end(Minecraft minecraft);

    void drawLayer(Minecraft minecraft, RenderType renderType, Vec3 cameraPosition, Matrix4f viewMatrix, Matrix4f projection);

    void forEachBlockEntity(Minecraft minecraft, Frustum frustum, Consumer<BlockEntity> consumer);
}
