package com.craisinlord.antarchy.content.boss;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;

public class EntityLinkedServerBossEvent extends ServerBossEvent {
    private final Entity entity;

    public EntityLinkedServerBossEvent(Entity entity, Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        super(name, color, overlay);
        this.entity = entity;
    }

    @Override
    public UUID getId() {
        return this.entity.getUUID();
    }
}
