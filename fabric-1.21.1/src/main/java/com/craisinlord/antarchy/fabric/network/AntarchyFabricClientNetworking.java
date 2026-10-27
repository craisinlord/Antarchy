package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.client.BloodglassClientState;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState;
import com.craisinlord.antarchy.content.client.WormHookTetherClientState;
import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.fabric.client.BloodCrystalKatanaTrailClientState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class AntarchyFabricClientNetworking {
    private AntarchyFabricClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(AntarchyGameResultPayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.AntarchyGameClientState.update(payload)));
        com.craisinlord.antarchy.content.network.AntarchyGameNetworking.setSender(ClientPlayNetworking::send);
        com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.setRequestSender(ClientPlayNetworking::send);
        com.craisinlord.antarchy.content.client.MoonSkyTracker.setSender(ClientPlayNetworking::send);
        com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.setClientReporter(com.craisinlord.antarchy.content.client.MoonSkyTracker::report);
        ClientPlayNetworking.registerGlobalReceiver(PortalGunIndicatorPayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.update(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BloodglassStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax())));
        ClientPlayNetworking.registerGlobalReceiver(TigerEyeCamouflageStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> TigerEyeCamouflageClientState.update(payload.entityId(), payload.active(), payload.blockStateId())));
        ClientPlayNetworking.registerGlobalReceiver(BloodCrystalKatanaTrailPayload.TYPE, (payload, context) ->
                context.client().execute(() -> BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(ScorpionWhipTetherPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId())));
        ClientPlayNetworking.registerGlobalReceiver(WormHookTetherPayload.TYPE, (payload, context) ->
                context.client().execute(() -> WormHookTetherClientState.update(payload.playerId(), payload.hookId())));
        ClientPlayNetworking.registerGlobalReceiver(BrutalflyElytraAnimationPayload.TYPE, (payload, context) ->
                context.client().execute(() -> BrutalflyElytraClientState.trigger(payload.entityId(), payload.durationTicks(), payload.strength())));
        ClientPlayNetworking.registerGlobalReceiver(HerculesBeetleImpactShakePayload.TYPE, (payload, context) ->
                context.client().execute(() -> HerculesBeetleImpactShakeClientState.trigger(payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(KingJudgmentFlashPayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.KingJudgmentFlashClientState.trigger(payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(HordeIntensityPayload.TYPE, (payload, context) ->
                context.client().execute(() -> HordeClientState.update(payload.intensity())));
        ClientPlayNetworking.registerGlobalReceiver(com.craisinlord.antarchy.content.network.ThrowbackZonePayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.ThrowbackClientState.update(payload.strength())));
        ClientPlayNetworking.registerGlobalReceiver(com.craisinlord.antarchy.content.network.AntDescentPayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.AntDescentClientState.handle(payload)));
        ClientPlayNetworking.registerGlobalReceiver(com.craisinlord.antarchy.content.network.ElythiaEchoTestPayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.ElythiaEchoes.handleTest(payload.immediate())));
    }
}
