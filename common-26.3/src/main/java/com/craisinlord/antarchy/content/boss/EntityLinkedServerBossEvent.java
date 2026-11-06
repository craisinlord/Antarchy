package com.craisinlord.antarchy.content.boss;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;

public class EntityLinkedServerBossEvent extends ServerBossEvent {
    private final Supplier<UUID> entityId;

    public EntityLinkedServerBossEvent(Entity entity, Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        super(entity.getUUID(), name, color, overlay);
        this.entityId = entity::getUUID;
    }

    public EntityLinkedServerBossEvent(UUID entityId, Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        super(entityId, name, color, overlay);
        this.entityId = () -> entityId;
    }

    @Override
    public UUID getId() {
        return this.entityId.get();
    }
}
