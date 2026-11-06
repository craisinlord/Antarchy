package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.util.SyncedEntityFlags;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class AntarchyNeoForgeAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Antarchy.MODID);
    public static final Supplier<AttachmentType<Boolean>> JUDGMENT_MARKED = flag("judgment_marked");
    public static final Supplier<AttachmentType<Boolean>> ON_ANTIMETAL_RAIL = flag("on_antimetal_rail");

    private AntarchyNeoForgeAttachments() {
    }

    private static Supplier<AttachmentType<Boolean>> flag(String name) {
        return ATTACHMENT_TYPES.register(name, () -> AttachmentType.builder(() -> Boolean.FALSE).sync(ByteBufCodecs.BOOL).build());
    }

    private static AttachmentType<Boolean> type(SyncedEntityFlags.Flag flag) {
        return switch (flag) {
            case JUDGMENT_MARKED -> JUDGMENT_MARKED.get();
            case ON_ANTIMETAL_RAIL -> ON_ANTIMETAL_RAIL.get();
        };
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        SyncedEntityFlags.bind(new SyncedEntityFlags.Backend() {
            @Override
            public boolean get(Entity entity, SyncedEntityFlags.Flag flag) {
                return entity.getData(type(flag));
            }

            @Override
            public void set(Entity entity, SyncedEntityFlags.Flag flag, boolean value) {
                entity.setData(type(flag), value);
            }
        });
    }
}
