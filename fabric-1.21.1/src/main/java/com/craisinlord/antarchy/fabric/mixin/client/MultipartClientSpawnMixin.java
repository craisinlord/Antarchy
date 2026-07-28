package com.craisinlord.antarchy.fabric.mixin.client;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MultipartClientSpawnMixin {
    @Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true)
    private void antarchy$skipTrackedMultipartPlaceholders(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (packet.getType() == AntarchyFabricEntities.KRAKEN_PART.get()) {
            ci.cancel();
        }
    }
}
