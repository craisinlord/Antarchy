package com.craisinlord.antarchy;

import com.craisinlord.antarchy.content.computer.AntarchyArchiveLocators;
import com.craisinlord.antarchy.content.util.AttributeRangeInit;
import com.craisinlord.antarchy.content.worldgen.VillagePoolAdditions;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Antarchy {
    public static final String MODID = "antarchy";
    public static final String MOD_VERSION = "2.0.0";
    public static final Logger LOGGER = LogManager.getLogger();
    public static volatile boolean physicalClient;

    public static void init() {
        AttributeRangeInit.apply();
        AntarchyArchiveLocators.register();
        ServerGoingToStartEvent.EVENT.addListener(Antarchy::serverAboutToStart);
    }
    private static void serverAboutToStart(final ServerGoingToStartEvent event) {
        VillagePoolAdditions.apply(event.getServer());
    }
}
