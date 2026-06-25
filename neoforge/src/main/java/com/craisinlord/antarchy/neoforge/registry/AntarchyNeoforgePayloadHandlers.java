package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.neoforge.network.AntarchyGravityNetworking;
import com.craisinlord.antarchy.neoforge.network.BrutalflyElytraNetworking;
import com.craisinlord.antarchy.neoforge.network.JumpyBootsNetworking;
import com.craisinlord.antarchy.neoforge.network.MultipartNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class AntarchyNeoforgePayloadHandlers {
    private AntarchyNeoforgePayloadHandlers() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoforgePayloadHandlers::registerPayloadHandlers);
    }

    static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        AntarchyGravityNetworking.register(registrar);
        BrutalflyElytraNetworking.register(registrar);
        JumpyBootsNetworking.register(registrar);
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.BloodglassStatePayload.TYPE,
                com.craisinlord.antarchy.content.network.BloodglassStatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax())
                )
        );
        registrar.playToClient(
                BloodCrystalKatanaTrailPayload.TYPE,
                BloodCrystalKatanaTrailPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks())
                )
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload.TYPE,
                com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId())
                )
        );
        MultipartNetworking.register(registrar);
        // ThoraxisWeatherNetworking.register(registrar);
    }
}
