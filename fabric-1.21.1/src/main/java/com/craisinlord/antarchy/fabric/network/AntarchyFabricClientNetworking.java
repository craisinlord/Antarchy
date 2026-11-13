package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.craisinlord.antarchy.content.client.AntarchyGameClientState;
import com.craisinlord.antarchy.content.client.BloodglassClientState;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.client.ElythiaEchoes;
import com.craisinlord.antarchy.content.client.ElythiaEventClient;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.client.KingJudgmentFlashClientState;
import com.craisinlord.antarchy.content.client.MoonSkyTracker;
import com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState;
import com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState;
import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.client.WormHookTetherClientState;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync;
import com.craisinlord.antarchy.fabric.client.BloodCrystalKatanaTrailClientState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class AntarchyFabricClientNetworking {
    private AntarchyFabricClientNetworking() {
    }

    public static void register() {
        AntarchyNetwork.bindClient(ClientPlayNetworking::send);
        ClientPlayNetworking.registerGlobalReceiver(AntarchyGameResultPayload.TYPE, (payload, context) ->
                context.client().execute(() -> AntarchyGameClientState.update(payload)));
        PortalGunMoonAimSync.setClientReporter(MoonSkyTracker::report);
        ClientPlayNetworking.registerGlobalReceiver(PortalGunIndicatorPayload.TYPE, (payload, context) ->
                context.client().execute(() -> PortalGunIndicatorClientState.update(payload)));
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
                context.client().execute(() -> KingJudgmentFlashClientState.trigger(payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(HordeIntensityPayload.TYPE, (payload, context) ->
                context.client().execute(() -> HordeClientState.update(payload.intensity())));
        ClientPlayNetworking.registerGlobalReceiver(ThrowbackZonePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ThrowbackClientState.update(payload.strength())));
        ClientPlayNetworking.registerGlobalReceiver(AntTransitPayload.TYPE, (payload, context) ->
                context.client().execute(() -> AntTransitClientState.handle(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ElythiaEchoTestPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ElythiaEchoes.handleTest(payload.immediate())));
        ClientPlayNetworking.registerGlobalReceiver(ElythiaEventPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ElythiaEventClient.handle(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ThoraxisEventPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ThoraxisEventClient.handle(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ThoraxisBubbleUpdatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ThoraxisEventClient.handleUpdate(payload)));
    }
}
