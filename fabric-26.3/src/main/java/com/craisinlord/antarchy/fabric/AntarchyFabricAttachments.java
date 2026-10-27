package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.util.SyncedEntityFlags;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class AntarchyFabricAttachments {
    public static final AttachmentType<Boolean> JUDGMENT_MARKED = flag("judgment_marked");
    public static final AttachmentType<Boolean> ON_ANTIMETAL_RAIL = flag("on_antimetal_rail");

    private AntarchyFabricAttachments() {
    }

    private static AttachmentType<Boolean> flag(String name) {
        return AttachmentRegistry.<Boolean>create(
                Identifier.fromNamespaceAndPath(Antarchy.MODID, name),
                builder -> builder.initializer(() -> Boolean.FALSE).syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
        );
    }

    private static AttachmentType<Boolean> type(SyncedEntityFlags.Flag flag) {
        return switch (flag) {
            case JUDGMENT_MARKED -> JUDGMENT_MARKED;
            case ON_ANTIMETAL_RAIL -> ON_ANTIMETAL_RAIL;
        };
    }

    public static void register() {
        SyncedEntityFlags.bind(new SyncedEntityFlags.Backend() {
            @Override
            public boolean get(Entity entity, SyncedEntityFlags.Flag flag) {
                return entity.getAttachedOrElse(type(flag), Boolean.FALSE);
            }

            @Override
            public void set(Entity entity, SyncedEntityFlags.Flag flag, boolean value) {
                entity.setAttached(type(flag), value);
            }
        });
    }
}
