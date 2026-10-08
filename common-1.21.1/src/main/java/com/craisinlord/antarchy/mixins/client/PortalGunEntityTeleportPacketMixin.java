package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunWorldPortalShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class PortalGunEntityTeleportPacketMixin {
    @Inject(method = "handleTeleportEntity", at = @At("TAIL"))
    private void antarchy$snapPortalTransitEntity(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.getId());
        Vec3 destination = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        if (entity == null) {
            return;
        }
        antarchy$snapToPortalExit(entity, destination);
    }

    private static void antarchy$snapToPortalExit(Entity entity, Vec3 destination) {
        if (!isNearPortalExit(entity, destination)) {
            return;
        }
        entity.lerpTo(destination.x, destination.y, destination.z, entity.getYRot(), entity.getXRot(), 0);
        entity.setPortalCooldown(PortalGunPortalEntity.TELEPORT_COOLDOWN_TICKS);
        entity.setPos(destination.x, destination.y, destination.z);
        entity.xo = destination.x;
        entity.yo = destination.y;
        entity.zo = destination.z;
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
        if (entity instanceof LivingEntity livingEntity) {
            LivingEntityPortalLerpAccessor accessor = (LivingEntityPortalLerpAccessor) livingEntity;
            accessor.antarchy$setLerpHeadSteps(0);
        }
    }

    private static boolean isNearPortalExit(Entity entity, Vec3 destination) {
        Vec3 offset = destination.subtract(entity.position());
        double entityExtent = Math.max(entity.getBbWidth(), entity.getBbHeight());
        double edgeAllowance = entityExtent * 0.5D;
        AABB search = entity.getBoundingBox().move(offset).inflate(entityExtent + PortalGunWorldPortalShape.DEFAULT_SCAN_DISTANCE);
        Vec3 probe = destination.add(0.0D, entity.getEyeHeight(), 0.0D);
        for (PortalGunPortalEntity portal : com.craisinlord.antarchy.content.portalgun.PortalGunPortalRegistry.near(entity.level(), search)) {
            PortalGunWorldPortalShape.PortalLocalCoords coords = portal.getWorldPortalShape().localCoords(probe);
            if (coords.depth() >= -entityExtent - 0.5D
                    && coords.depth() <= PortalGunWorldPortalShape.DEFAULT_SCAN_DISTANCE + 0.5D
                    && Math.abs(coords.horizontal()) <= portal.getWorldPortalShape().halfWidth() + edgeAllowance
                    && Math.abs(coords.vertical()) <= portal.getWorldPortalShape().halfHeight() + edgeAllowance) {
                return true;
            }
        }
        return false;
    }
}
