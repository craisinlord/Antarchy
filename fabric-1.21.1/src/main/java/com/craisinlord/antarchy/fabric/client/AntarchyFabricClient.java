package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.QueenMusicHandler;
import com.craisinlord.antarchy.content.client.game.AntarchyComputerGames;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking;
import com.craisinlord.antarchy.fabric.client.SeparateLargeItemModels;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class AntarchyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.craisinlord.antarchy.Antarchy.physicalClient = true;
        ModelLoadingPlugin.register(plugin -> plugin.addModels(SeparateLargeItemModels.all()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> QueenMusicHandler.tick(client, AntarchyFabricSounds.THE_QUEEN.get()));
        AntarchyFabricClientNetworking.register();
        AntarchyFabricClientNetworking.bootstrapMultipartClient();
        AntarchyFabricClientBootstrap.register();
        AntarchyComputerGames.register();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "tiger_eye_camouflage_client_cache");
            }

            @Override
            public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                                                  ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                                  Executor prepareExecutor, Executor applyExecutor) {
                return barrier.wait(null).thenRunAsync(TigerEyeCamouflageClientHandler::clearClientCaches, applyExecutor);
            }
        });
    }
}
