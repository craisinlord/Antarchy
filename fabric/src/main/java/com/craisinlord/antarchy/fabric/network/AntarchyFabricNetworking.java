package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartAttackPayload;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartInteractPayload;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.JumpyBootsHelper;
import com.craisinlord.antarchy.fabric.util.JumpyBootsFabricHelper;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.content.network.JumpyBootsLaunchPayload;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherSnapshot;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class AntarchyFabricNetworking {
    private AntarchyFabricNetworking() {
    }

    public static void register() {
        registerPayloadTypes();
        registerServerReceivers();
    }

    private static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(GravityStatePayload.TYPE, GravityStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BloodglassStatePayload.TYPE, BloodglassStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BloodCrystalKatanaTrailPayload.TYPE, BloodCrystalKatanaTrailPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ScorpionWhipTetherPayload.TYPE, ScorpionWhipTetherPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BrutalflyElytraAnimationPayload.TYPE, BrutalflyElytraAnimationPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ThoraxisWeatherPayload.TYPE, ThoraxisWeatherPayload.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(GravityGunPrimaryPayload.TYPE, GravityGunPrimaryPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(GravityGunScrollPayload.TYPE, GravityGunScrollPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DiamondMinecartInputPayload.TYPE, DiamondMinecartInputPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BrutalflyElytraFlapPayload.TYPE, BrutalflyElytraFlapPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(JumpyBootsLaunchPayload.TYPE, JumpyBootsLaunchPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(MultipartAttackPayload.TYPE, MultipartAttackPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(MultipartInteractPayload.TYPE, MultipartInteractPayload.STREAM_CODEC);
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(GravityGunPrimaryPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleGravityGunPrimary(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(GravityGunScrollPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleGravityGunScroll(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(DiamondMinecartInputPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleDiamondMinecartInput(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(BrutalflyElytraFlapPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleBrutalflyFlap(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(JumpyBootsLaunchPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleJumpyBootsLaunch(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(MultipartAttackPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleMultipartAttack(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(MultipartInteractPayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleMultipartInteract(context.player(), payload)));
    }

    public static void syncGravityToPlayer(ServerPlayer target, Entity entity) {
        ServerPlayNetworking.send(target, new GravityStatePayload(
                entity.getId(),
                AntarchyGravityApi.getGravityDirection(entity),
                AntarchyGravityApi.getPrevGravityDirection(entity),
                AntarchyGravityApi.isGravityForced(entity),
                AntarchyGravityApi.getTransitionDuration(entity),
                AntarchyGravityApi.getTransitionRemaining(entity)
        ));
    }

    public static void syncGravityEntity(Entity entity) {
        GravityStatePayload payload = new GravityStatePayload(
                entity.getId(),
                AntarchyGravityApi.getGravityDirection(entity),
                AntarchyGravityApi.getPrevGravityDirection(entity),
                AntarchyGravityApi.isGravityForced(entity),
                AntarchyGravityApi.getTransitionDuration(entity),
                AntarchyGravityApi.getTransitionRemaining(entity)
        );

        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
        if (entity instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncBloodglass(ServerPlayer player, int shieldsActive, int shieldsMax) {
        ServerPlayNetworking.send(player, new BloodglassStatePayload(shieldsActive, shieldsMax));
    }

    public static void syncKatanaTrail(Entity entity, int durationTicks) {
        BloodCrystalKatanaTrailPayload payload = new BloodCrystalKatanaTrailPayload(entity.getId(), durationTicks);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
        if (entity instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }

    public static void syncScorpionWhipTether(ServerPlayer player, int targetId) {
        ScorpionWhipTetherPayload payload = new ScorpionWhipTetherPayload(player.getId(), targetId);
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(tracking, payload);
        }
        ServerPlayNetworking.send(player, payload);
    }

    public static void syncThoraxisWeather(ServerLevel level, ThoraxisWeatherSnapshot snapshot) {
        ThoraxisWeatherPayload payload = new ThoraxisWeatherPayload(
                snapshot.dimension(),
                snapshot.kind(),
                snapshot.expiresAt(),
                snapshot.anchor().getX(),
                snapshot.anchor().getY(),
                snapshot.anchor().getZ()
        );
        for (ServerPlayer player : level.players()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void handleGravityState(Entity contextPlayer, GravityStatePayload payload) {
        Entity entity = contextPlayer.level().getEntity(payload.entityId());
        if (entity == null && contextPlayer.getId() == payload.entityId()) {
            entity = contextPlayer;
        }
        if (entity == null) {
            return;
        }
        AntarchyGravityApi.applySyncedState(
                entity,
                payload.direction(),
                payload.previousDirection(),
                payload.forced(),
                payload.transitionDuration(),
                payload.transitionRemaining()
        );
    }

    private static void handleGravityGunPrimary(ServerPlayer player, GravityGunPrimaryPayload payload) {
        if (!(player.getMainHandItem().getItem() instanceof GravityGunItem gravityGunItem)) {
            return;
        }
        gravityGunItem.firePrimary(player.serverLevel(), player, player.getMainHandItem());
    }

    private static void handleGravityGunScroll(ServerPlayer player, GravityGunScrollPayload payload) {
        if (!(player.getMainHandItem().getItem() instanceof GravityGunItem)) {
            return;
        }
        GravityGunItem.adjustHeldDistance(player.getMainHandItem(), payload.distanceDelta());
    }

    private static void handleDiamondMinecartInput(ServerPlayer player, DiamondMinecartInputPayload payload) {
        if (player.getVehicle() instanceof DiamondMinecartEntity cart) {
            cart.onInputReceived(payload.inputFlags());
        }
    }

    private static void handleBrutalflyFlap(ServerPlayer player, BrutalflyElytraFlapPayload payload) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestStack.getItem() instanceof BrutalflyElytraItem)) {
            return;
        }
        if (player.isSpectator() || !player.isFallFlying() || player.isPassenger()) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(chestStack.getItem())) {
            return;
        }

        BrutalflyElytraFlightHelper.FlapTier tier = BrutalflyElytraFlightHelper.resolveFlapTier(payload.chargeTicks());
        if (tier == BrutalflyElytraFlightHelper.FlapTier.FAIL) {
            return;
        }

        float lift = BrutalflyElytraFlightHelper.liftFor(tier);
        float forwardBoost = BrutalflyElytraFlightHelper.forwardBoostFor(tier);
        Vec3 look = player.getLookAngle();
        Vec3 boost = new Vec3(look.x * forwardBoost, lift, look.z * forwardBoost);
        player.setDeltaMovement(player.getDeltaMovement().add(boost));
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.resetFallDistance();
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        chestStack.hurtAndBreak(1, player, EquipmentSlot.CHEST);
        player.getCooldowns().addCooldown(chestStack.getItem(), BrutalflyElytraFlightHelper.FLAP_COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.9F,
                tier == BrutalflyElytraFlightHelper.FlapTier.PERFECT ? 0.8F : tier == BrutalflyElytraFlightHelper.FlapTier.GOOD ? 0.95F : 1.1F);

        BrutalflyElytraAnimationPayload animationPayload = new BrutalflyElytraAnimationPayload(
                player.getId(),
                BrutalflyElytraFlightHelper.animationTicksFor(tier),
                lift
        );
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(tracking, animationPayload);
        }
        ServerPlayNetworking.send(player, animationPayload);
    }

    private static void handleMultipartAttack(ServerPlayer serverPlayer, MultipartAttackPayload payload) {
        Entity entity = serverPlayer.serverLevel().getEntity(payload.parentId());
        if (!(entity instanceof MultipartEntityOwner owner)) {
            return;
        }

        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            parts = owner.antarchy$createMultipartParts();
            owner.antarchy$setMultipartParts(parts);
            owner.antarchy$syncMultipartParts();
        }
        if (payload.partIndex() < 0 || payload.partIndex() >= parts.length) {
            return;
        }

        Entity part = parts[payload.partIndex()];
        if (part == null || part.isRemoved()) {
            return;
        }
        if (serverPlayer.distanceToSqr(part) > 10000.0D) {
            return;
        }

        DamageSource source = serverPlayer.level().damageSources().playerAttack(serverPlayer);
        owner.antarchy$hurtMultipartPart(part, source, payload.damage());
    }

    private static void handleMultipartInteract(ServerPlayer serverPlayer, MultipartInteractPayload payload) {
        Entity entity = serverPlayer.serverLevel().getEntity(payload.parentId());
        if (!(entity instanceof MultipartEntityOwner owner)) {
            return;
        }

        Entity[] parts = owner.antarchy$getMultipartParts();
        if (parts == null) {
            parts = owner.antarchy$createMultipartParts();
            owner.antarchy$setMultipartParts(parts);
            owner.antarchy$syncMultipartParts();
        }
        if (payload.partIndex() < 0 || payload.partIndex() >= parts.length) {
            return;
        }

        Entity part = parts[payload.partIndex()];
        if (part == null || part.isRemoved()) {
            return;
        }

        InteractionHand hand = payload.handId() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        owner.antarchy$interactMultipartPart(part, serverPlayer, part.position(), hand);
    }

    private static void handleJumpyBootsLaunch(ServerPlayer player, JumpyBootsLaunchPayload payload) {
        if (!JumpyBootsItem.isWearingJumpyBoots(player)) return;
        if (player.isSpectator() || player.isPassenger()) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (player.getCooldowns().isOnCooldown(boots.getItem())) return;

        int clampedCharge = Math.min(payload.chargeTicks(), JumpyBootsHelper.CHARGE_TICKS_MAX);
        if (clampedCharge <= 0) return;

        float verticalBoost = JumpyBootsHelper.verticalBoostFor(clampedCharge);
        Vec3 current = player.getDeltaMovement();
        double newX = current.x;
        double newZ = current.z;

        if (payload.sprinting()) {
            Vec3 look = player.getLookAngle();
            newX += look.x * JumpyBootsHelper.SPRINT_FORWARD_BOOST;
            newZ += look.z * JumpyBootsHelper.SPRINT_FORWARD_BOOST;
        }

        player.setDeltaMovement(newX, verticalBoost, newZ);
        player.setPos(player.getX(), player.getY() + 0.001, player.getZ());
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.resetFallDistance();
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        player.level().playSound(null, player.blockPosition(), SoundEvents.SLIME_JUMP, SoundSource.PLAYERS,
                1.0F, 0.6F + (clampedCharge / (float) JumpyBootsHelper.CHARGE_TICKS_MAX) * 0.6F);

        JumpyBootsFabricHelper.setProtectionUntil(player,
                player.level().getGameTime() + JumpyBootsHelper.FALL_PROTECTION_TICKS);

        player.getCooldowns().addCooldown(boots.getItem(), JumpyBootsHelper.COOLDOWN_TICKS);
    }
}
