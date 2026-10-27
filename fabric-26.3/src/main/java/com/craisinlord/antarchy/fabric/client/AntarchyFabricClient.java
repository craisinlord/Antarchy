package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.QueenMusicHandler;
import com.craisinlord.antarchy.content.client.game.AntarchyComputerGames;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class AntarchyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.craisinlord.antarchy.Antarchy.physicalClient = true;
        com.craisinlord.antarchy.content.AntarchyAnthologyHooks.registerClient();
        ClientTickEvents.END_CLIENT_TICK.register(client -> QueenMusicHandler.tick(client, AntarchyFabricSounds.THE_QUEEN));
        AntarchyFabricClientNetworking.register();
        AntarchyFabricClientBootstrap.register();
        AntarchyComputerGames.register();
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, "tiger_eye_camouflage_client_cache"),
                new PreparableReloadListener() {
                    @Override
                    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor,
                                                          PreparableReloadListener.PreparationBarrier barrier, Executor applyExecutor) {
                        return barrier.wait(null).thenRunAsync(TigerEyeCamouflageClientHandler::clearClientCaches, applyExecutor);
                    }
                }
        );
    }
}
