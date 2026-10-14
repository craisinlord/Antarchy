package com.craisinlord.antarchy.forge.network;

import net.minecraft.world.entity.player.Player;

public record AntarchyPayloadContext(Player player) {
    public void enqueueWork(Runnable work) {
        work.run();
    }
}
