package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class AntarchyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AntarchyFabricClientNetworking.register();
        AntarchyFabricClientBootstrap.register();
    }
}

