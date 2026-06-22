package com.craisinlord.antarchy.content.entity.multipart;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class MultipartFramework {
    public interface PartFactory {
        Entity create(MultipartEntityOwner owner, int partIndex, MultipartPartDefinition spec);
    }

    public interface NetworkBridge {
        void sendAttack(UUID parentId, int partIndex, float damage);

        void sendInteract(UUID parentId, int partIndex, int handId);
    }

    private static final NetworkBridge NOOP_BRIDGE = new NetworkBridge() {
        @Override
        public void sendAttack(UUID parentId, int partIndex, float damage) {
        }

        @Override
        public void sendInteract(UUID parentId, int partIndex, int handId) {
        }
    };

    @Nullable
    private static PartFactory partFactory;
    private static NetworkBridge networkBridge = NOOP_BRIDGE;

    private MultipartFramework() {
    }

    public static void bootstrap(
            PartFactory partFactory,
            NetworkBridge networkBridge
    ) {
        MultipartFramework.partFactory = Objects.requireNonNull(partFactory, "partFactory");
        MultipartFramework.networkBridge = Objects.requireNonNull(networkBridge, "networkBridge");
    }

    public static Entity[] createMultipartParts(MultipartEntityOwner owner) {
        MultipartLayout layout = owner.antarchy$getMultipartLayout();
        MultipartPartDefinition[] parts = layout.parts();
        Entity[] created = new Entity[parts.length];
        for (int i = 0; i < parts.length; i++) {
            created[i] = createMultipartPart(owner, i, parts[i]);
        }
        return created;
    }

    public static Entity createMultipartPart(MultipartEntityOwner owner, int index, MultipartPartDefinition spec) {
        PartFactory factory = partFactory;
        if (factory == null) {
            throw new IllegalStateException("Multipart part factory has not been bootstrapped");
        }

        return factory.create(owner, index, spec);
    }

    public static void spawnMultipartParts(MultipartEntityOwner owner) {
        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            parts = owner.antarchy$createMultipartParts();
            owner.antarchy$setMultipartParts(parts);
        }
        syncMultipartParts(owner);
    }

    public static void discardMultipartParts(MultipartEntityOwner owner) {
        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            return;
        }

        for (Entity part : parts) {
            if (part != null) {
                part.discard();
            }
        }

        owner.antarchy$setMultipartParts(null);
    }

    public static void syncMultipartParts(MultipartEntityOwner owner) {
        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            return;
        }

        for (Entity part : parts) {
            if (part instanceof MultipartPartAccess access) {
                access.antarchy$syncFromParent();
            }
        }
    }

    public static void sendMultipartAttack(UUID parentId, int partIndex, float damage) {
        networkBridge.sendAttack(parentId, partIndex, damage);
    }

    public static void sendMultipartInteract(UUID parentId, int partIndex, int handId) {
        networkBridge.sendInteract(parentId, partIndex, handId);
    }
}
