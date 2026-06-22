package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
/*
 * Fixes move packet checks for inverted players.
 */
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    private double lastGoodX;
    @Shadow
    private double lastGoodY;
    @Shadow
    private double lastGoodZ;

    @ModifyVariable(method = "handleMovePlayer",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0)
    private boolean antarchy$fixHandleMovePlayerFlag(boolean originalFlag,
                                                      ServerboundMovePlayerPacket packet) {
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this.player);
        if (!gravity.isInverted()) {
            return originalFlag;
        }
        double dx = packet.getX(this.player.getX()) - this.lastGoodX;
        double dy = packet.getY(this.player.getY()) - this.lastGoodY;
        double dz = packet.getZ(this.player.getZ()) - this.lastGoodZ;
        Vec3 localVec = AntarchyGravityRotationUtil.vecWorldToPlayer(new Vec3(dx, dy, dz), gravity);
        return localVec.y > 0.0;
    }

    @ModifyArg(method = "handleMovePlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private Vec3 antarchy$fixHandleMovePlayerVector(Vec3 worldDelta) {
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this.player);
        if (!gravity.isInverted()) {
            return worldDelta;
        }
        return AntarchyGravityRotationUtil.vecWorldToPlayer(worldDelta, gravity);
    }

    @ModifyArg(method = "handleMovePlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setOnGroundWithMovement(ZLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private boolean antarchy$fixHandleMovePlayerOnGround(boolean originalOnGround) {
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this.player);
        if (!gravity.isInverted()) {
            return originalOnGround;
        }
        return originalOnGround || this.player.verticalCollisionBelow || this.player.verticalCollision;
    }

    @ModifyArg(method = "handleMovePlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;doCheckFallDamage(DDDZ)V"),
            index = 3)
    private boolean antarchy$fixHandleMovePlayerFallDamageOnGround(boolean originalOnGround) {
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this.player);
        if (!gravity.isInverted()) {
            return originalOnGround;
        }
        return originalOnGround || this.player.verticalCollisionBelow || this.player.verticalCollision;
    }
}
