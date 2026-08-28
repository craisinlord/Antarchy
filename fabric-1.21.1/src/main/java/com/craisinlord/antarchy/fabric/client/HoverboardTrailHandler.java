package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.client.HoverboardTrailSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class HoverboardTrailHandler {
    private HoverboardTrailHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> HoverboardTrailSystem.tick());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> HoverboardTrailSystem.render(context.camera(), context.matrixStack().last().pose()));
    }
}
