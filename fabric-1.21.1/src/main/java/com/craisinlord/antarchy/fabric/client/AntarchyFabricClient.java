package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;
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
        AntarchyFabricClientNetworking.register();
        AntarchyFabricClientNetworking.bootstrapMultipartClient();
        AntarchyFabricClientBootstrap.register();
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

