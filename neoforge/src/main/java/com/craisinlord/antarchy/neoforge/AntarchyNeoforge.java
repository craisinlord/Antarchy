package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.neoforge.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;


@Mod(Antarchy.MODID)
public class AntarchyNeoforge {
    public static final boolean CREATE_LOADED = isModLoaded("create");

    public static IEventBus modEventBusTempHolder = null;

    public AntarchyNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBusTempHolder = modEventBus;
        AntarchyConfigModuleNeoforge.init(modContainer);
        AntarchyNeoForgeEvents.register(modEventBus);
        AntarchyNeoforgeSounds.register(modEventBus);
        AntarchyNeoforgeEntites.register(modEventBus);
        AntarchyNeoforgeBlocks.register(modEventBus);
        AntarchyNeoforgeItems.register(modEventBus);
        AntarchyNeoforgeMisc.register(modEventBus);
        AntarchyNeoforgeSpawnPlacements.register(modEventBus);
        AntarchyNeoforgeCreativeModeTabs.register(modEventBus);
        AntarchyNeoforgeEntityAttributes.register(modEventBus);
        AntarchyNeoforgePayloadHandlers.register(modEventBus);
        Antarchy.init();
    }
    private static boolean isModLoaded(String modId) {
        try {
            return ModList.get().isLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
