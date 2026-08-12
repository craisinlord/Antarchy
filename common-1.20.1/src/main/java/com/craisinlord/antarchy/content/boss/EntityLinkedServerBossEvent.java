package com.craisinlord.antarchy.content.boss;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

public class EntityLinkedServerBossEvent extends ServerBossEvent {
    private final UUID entityId;

    public EntityLinkedServerBossEvent(UUID entityId, Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        super(name, color, overlay);
        this.entityId = entityId;
    }

    @Override
    public UUID getId() {
        return this.entityId;
    }
}
