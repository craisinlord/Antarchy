package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;
import com.craisinlord.antarchy.forge.network.AntarchyGravityNetworking;
import com.craisinlord.antarchy.forge.network.BrutalflyElytraNetworking;
import com.craisinlord.antarchy.forge.network.DorrieJumpNetworking;
import com.craisinlord.antarchy.forge.network.HerculesBeetleNetworking;
import com.craisinlord.antarchy.forge.network.JumpyBootsNetworking;
import com.craisinlord.antarchy.forge.network.MultipartNetworking;
import com.craisinlord.antarchy.forge.network.TigerEyeNetworking;

public class AntarchyForgePayloadHandlers {
    private AntarchyForgePayloadHandlers() {}

    public static void register() {
        AntarchyGravityNetworking.register();
        BrutalflyElytraNetworking.register();
        JumpyBootsNetworking.register();
        DorrieJumpNetworking.register();
        HerculesBeetleNetworking.register();
        TigerEyeNetworking.register();
        AntarchyForgeNetworkCore.registerS2C(
                com.craisinlord.antarchy.content.network.BloodglassStatePayload.class,
                com.craisinlord.antarchy.content.network.BloodglassStatePayload.STREAM_CODEC,
                payload -> com.craisinlord.antarchy.content.client.BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax())
        );
        AntarchyForgeNetworkCore.registerS2C(
                BloodCrystalKatanaTrailPayload.class,
                BloodCrystalKatanaTrailPayload.STREAM_CODEC,
                payload -> com.craisinlord.antarchy.content.client.BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks())
        );
        AntarchyForgeNetworkCore.registerS2C(
                com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload.class,
                com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload.STREAM_CODEC,
                payload -> com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId())
        );
        AntarchyForgeNetworkCore.registerS2C(
                com.craisinlord.antarchy.content.network.HordeIntensityPayload.class,
                com.craisinlord.antarchy.content.network.HordeIntensityPayload.STREAM_CODEC,
                payload -> com.craisinlord.antarchy.content.client.HordeClientState.update(payload.intensity())
        );
        MultipartNetworking.register();
    }
}
