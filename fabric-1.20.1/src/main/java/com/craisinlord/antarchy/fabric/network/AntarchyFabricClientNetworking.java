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
import com.craisinlord.anthology.network.AnthologyNetwork;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class AntarchyFabricClientNetworking {
    private AntarchyFabricClientNetworking() {
    }

    public static void send(CustomPacketPayload payload) {
        AnthologyNetwork.sendToServer(payload);
    }

    public static void register() {
        com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.setRequestSender(AntarchyFabricClientNetworking::send);
        com.craisinlord.antarchy.content.client.MoonSkyTracker.setSender(AntarchyFabricClientNetworking::send);
        com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.setClientReporter(com.craisinlord.antarchy.content.client.MoonSkyTracker::report);
        AntarchyFabricNetworking.setClientHandler(PortalGunIndicatorPayload.TYPE, payload -> com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.update(payload));
        AntarchyFabricNetworking.setClientHandler(BloodglassStatePayload.TYPE, payload -> BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax()));
        AntarchyFabricNetworking.setClientHandler(TigerEyeCamouflageStatePayload.TYPE, payload -> TigerEyeCamouflageClientState.update(payload.entityId(), payload.active(), payload.blockStateId()));
        AntarchyFabricNetworking.setClientHandler(BloodCrystalKatanaTrailPayload.TYPE, payload -> BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks()));
        AntarchyFabricNetworking.setClientHandler(ScorpionWhipTetherPayload.TYPE, payload -> ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId()));
        AntarchyFabricNetworking.setClientHandler(WormHookTetherPayload.TYPE, payload -> WormHookTetherClientState.update(payload.playerId(), payload.hookId()));
        AntarchyFabricNetworking.setClientHandler(BrutalflyElytraAnimationPayload.TYPE, payload -> BrutalflyElytraClientState.trigger(payload.entityId(), payload.durationTicks(), payload.strength()));
        AntarchyFabricNetworking.setClientHandler(HerculesBeetleImpactShakePayload.TYPE, payload -> HerculesBeetleImpactShakeClientState.trigger(payload.durationTicks()));
        AntarchyFabricNetworking.setClientHandler(KingJudgmentFlashPayload.TYPE, payload -> com.craisinlord.antarchy.content.client.KingJudgmentFlashClientState.trigger(payload.durationTicks()));
        AntarchyFabricNetworking.setClientHandler(HordeIntensityPayload.TYPE, payload -> HordeClientState.update(payload.intensity()));
    }
}
