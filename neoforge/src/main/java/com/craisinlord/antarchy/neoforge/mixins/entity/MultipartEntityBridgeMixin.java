package com.craisinlord.antarchy.neoforge.mixins.entity;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
/*
 * Exposes multipart child lookup from base entities.
 */
public abstract class MultipartEntityBridgeMixin {
    @Unique
    private Entity[] antarchy$multipartSourceParts;

    @Unique
    private PartEntity<?>[] antarchy$multipartBridgeParts;

    public boolean isMultipartEntity() {
        return (Object) this instanceof MultipartEntityOwner;
    }

    @Nullable
    public PartEntity<?>[] getParts() {
        if (!((Object) this instanceof MultipartEntityOwner owner)) {
            return null;
        }

        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            parts = owner.antarchy$createMultipartParts();
            owner.antarchy$setMultipartParts(parts);
            owner.antarchy$syncMultipartParts();
        }

        if (parts.length == 0) {
            return new PartEntity<?>[0];
        }

        if (this.antarchy$multipartSourceParts != parts || this.antarchy$multipartBridgeParts == null || this.antarchy$multipartBridgeParts.length != parts.length) {
            PartEntity<?>[] bridgedParts = new PartEntity<?>[parts.length];
            for (int i = 0; i < parts.length; i++) {
                if (!(parts[i] instanceof PartEntity<?> part)) {
                    throw new IllegalStateException("Multipart owner returned a non-PartEntity child: " + parts[i]);
                }
                bridgedParts[i] = part;
            }
            this.antarchy$multipartSourceParts = parts;
            this.antarchy$multipartBridgeParts = bridgedParts;
        }

        return this.antarchy$multipartBridgeParts;
    }
}
