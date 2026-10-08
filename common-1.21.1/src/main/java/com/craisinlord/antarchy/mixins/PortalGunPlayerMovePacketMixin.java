package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class PortalGunPlayerMovePacketMixin {
    @Unique
    private static final double MAX_CLAIMED_MOVEMENT_SQR = 100.0D;
    @Unique
    private AABB antarchy$portalGunMoveStartBox;
    @Unique
    private Vec3 antarchy$portalGunMoveStartPosition;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void antarchy$capturePortalGunMoveStart(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        ServerPlayer player = ((ServerGamePacketListenerImpl) (Object) this).player;
        if (player.getServer() == null || !player.getServer().isSameThread()) {
            return;
        }
        this.antarchy$portalGunMoveStartBox = null;
        this.antarchy$portalGunMoveStartPosition = null;
        if (!packet.hasPosition() || ((ServerGamePacketListenerTeleportAccessor) this).antarchy$getAwaitingPositionFromClient() != null) {
            return;
        }
        this.antarchy$portalGunMoveStartBox = player.getBoundingBox();
        this.antarchy$portalGunMoveStartPosition = player.position();
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void antarchy$teleportPlayerOnPortalCrossing(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        AABB startBox = this.antarchy$portalGunMoveStartBox;
        Vec3 startPosition = this.antarchy$portalGunMoveStartPosition;
        this.antarchy$portalGunMoveStartBox = null;
        this.antarchy$portalGunMoveStartPosition = null;
        if (startBox == null || startPosition == null) {
            return;
        }
        ServerPlayer player = ((ServerGamePacketListenerImpl) (Object) this).player;
        Vec3 claimedMovement = new Vec3(packet.getX(startPosition.x), packet.getY(startPosition.y), packet.getZ(startPosition.z))
                .subtract(startPosition);
        double lengthSqr = claimedMovement.lengthSqr();
        if (lengthSqr > 1.0E-10D && lengthSqr < MAX_CLAIMED_MOVEMENT_SQR) {
            PortalGunPortalEntity.teleportPlayerAfterMovementPacket(player, startBox, claimedMovement);
        }
    }
}
