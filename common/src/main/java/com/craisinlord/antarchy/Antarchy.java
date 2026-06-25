package com.craisinlord.antarchy;

import com.craisinlord.antarchy.content.AttributeRangeInit;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Antarchy {
    public static final String MODID = "antarchy";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        AttributeRangeInit.apply();
        ServerGoingToStartEvent.EVENT.addListener(Antarchy::serverAboutToStart);
    }
    private static void serverAboutToStart(final ServerGoingToStartEvent event) {
    }
}