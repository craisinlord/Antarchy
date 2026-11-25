package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.fabric.compat.AlexsCavesPack;
import net.fabricmc.api.DedicatedServerModInitializer;

public final class AntarchyFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        AlexsCavesPack.register();
    }
}
