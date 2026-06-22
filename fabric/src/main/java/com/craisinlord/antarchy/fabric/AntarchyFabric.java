package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.BloodCrystalShardItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import net.fabricmc.api.ModInitializer;

public final class AntarchyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AntarchyFabricNetworking.register();
        AntarchyConfigModuleFabric.init();
        AntarchyFabricContent.register();
        AntarchyFabricEvents.register();
        BloodglassManager.register();
        Antarchy.init();
        BloodCrystalShardItem.SYNC_BLOODGLASS = BloodglassManager::syncBloodglass;
    }
}
