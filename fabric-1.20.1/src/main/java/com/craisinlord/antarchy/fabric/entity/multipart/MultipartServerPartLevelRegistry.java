package com.craisinlord.antarchy.fabric.entity.multipart;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class MultipartServerPartLevelRegistry {
    private MultipartServerPartLevelRegistry() {
    }

    public static void register(MultipartEntityOwner owner) {
        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) ((Entity) owner).level();
        for (Entity part : parts) {
            if (part != null && serverLevel.getEntity(part.getId()) != part) {
                serverLevel.addFreshEntity(part);
            }
        }
    }

    public static void unregister(MultipartEntityOwner owner) {
        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            return;
        }

        for (Entity part : parts) {
            if (part != null && !part.isRemoved()) {
                part.discard();
            }
        }
    }
}
