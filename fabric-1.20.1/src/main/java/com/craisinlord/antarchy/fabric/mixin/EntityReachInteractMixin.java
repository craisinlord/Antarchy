package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.compat.entity.EntityReach;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class EntityReachInteractMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyExpressionValue(
            method = "handleInteract",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D"
            )
    )
    private double antarchy$entityInteractionDistance(double original) {
        AttributeInstance reach = EntityReach.instance(this.player);
        if (reach == null || reach.getModifiers().isEmpty()) {
            return original;
        }
        double range = EntityReach.entityInteractionRange(this.player) + 1.0D;
        return range * range;
    }
}
