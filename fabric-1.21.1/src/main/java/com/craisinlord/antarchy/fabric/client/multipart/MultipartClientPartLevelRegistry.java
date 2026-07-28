package com.craisinlord.antarchy.fabric.client.multipart;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class MultipartClientPartLevelRegistry {
    private MultipartClientPartLevelRegistry() {
    }

    public static void register(MultipartEntityOwner owner) {
        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            return;
        }

        ClientLevel clientLevel = (ClientLevel) ((Entity) owner).level();
        for (Entity part : parts) {
            if (part != null && clientLevel.getEntity(part.getId()) != part) {
                clientLevel.addEntity(part);
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
