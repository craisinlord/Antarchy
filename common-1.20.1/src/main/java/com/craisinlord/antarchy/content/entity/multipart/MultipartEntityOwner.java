package com.craisinlord.antarchy.content.entity.multipart;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface MultipartEntityOwner {
    MultipartLayout antarchy$getMultipartLayout();

    Entity[] antarchy$getMultipartParts();

    void antarchy$setMultipartParts(Entity[] parts);

    default Entity[] antarchy$createMultipartParts() {
        return MultipartFramework.createMultipartParts(this);
    }

    default void antarchy$spawnMultipartParts() {
        MultipartFramework.spawnMultipartParts(this);
    }

    default void antarchy$discardMultipartParts() {
        MultipartFramework.discardMultipartParts(this);
    }

    default void antarchy$syncMultipartParts() {
        MultipartFramework.syncMultipartParts(this);
    }

    default boolean antarchy$hurtMultipartPart(Entity part, DamageSource source, float amount) {
        return ((Entity) this).hurt(source, amount);
    }

    default InteractionResult antarchy$interactMultipartPart(Entity part, Player player, Vec3 hitPosition, InteractionHand hand) {
        return ((Entity) this).interactAt(player, hitPosition, hand);
    }
}
