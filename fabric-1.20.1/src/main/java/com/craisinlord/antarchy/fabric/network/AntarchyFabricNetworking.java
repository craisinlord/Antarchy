package com.craisinlord.antarchy.fabric.network;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.content.entity.multipart.MultipartFramework;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartAttackPayload;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartInteractPayload;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.JumpyBootsHelper;
import com.craisinlord.antarchy.fabric.util.JumpyBootsFabricHelper;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import com.craisinlord.antarchy.content.network.JumpyBootsLaunchPayload;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherSnapshot;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import com.craisinlord.antarchy.fabric.entity.multipart.MultipartPartEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class AntarchyFabricNetworking {
    private AntarchyFabricNetworking() {
    }

    public static void register() {
        registerServerReceivers();
    }

    public static <T extends com.craisinlord.antarchy.compat.network.CustomPacketPayload> void sendToPlayer(
            ServerPlayer player,
            T payload,
            com.craisinlord.antarchy.compat.network.StreamCodec<io.netty.buffer.ByteBuf, T> streamCodec,
            com.craisinlord.antarchy.compat.network.CustomPacketPayload.Type<T> type
    ) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        streamCodec.encode(buf, payload);
        ServerPlayNetworking.send(player, type.id(), buf);
    }

    private static <T extends com.craisinlord.antarchy.compat.network.CustomPacketPayload> void registerC2SReceiver(
            com.craisinlord.antarchy.compat.network.CustomPacketPayload.Type<T> type,
            com.craisinlord.antarchy.compat.network.StreamCodec<io.netty.buffer.ByteBuf, T> streamCodec,
            java.util.function.BiConsumer<ServerPlayer, T> handler
    ) {
        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, handlerConn, buf, sender) -> {
            T payload = streamCodec.decode(buf);
            server.execute(() -> handler.accept(player, payload));
        });
    }

    public static void bootstrapMultipartCommon() {
        MultipartFramework.bootstrap(
                AntarchyFabricNetworking::createMultipartPart,
                new MultipartFramework.NetworkBridge() {
                    @Override
                    public void sendAttack(java.util.UUID parentId, int partIndex, float damage) {
                    }

                    @Override
                    public void sendInteract(java.util.UUID parentId, int partIndex, int handId) {
                    }
                }
        );
    }

    public static MultipartPartEntity createMultipartPart(MultipartEntityOwner owner, int partIndex, com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition spec) {
        return new MultipartPartEntity(AntarchyFabricEntities.KRAKEN_PART.get(), ((Entity) owner).level())
                .antarchy$configure(owner, partIndex, spec);
    }

    private static void registerServerReceivers() {
        registerC2SReceiver(GravityGunPrimaryPayload.TYPE, GravityGunPrimaryPayload.STREAM_CODEC, AntarchyFabricNetworking::handleGravityGunPrimary);
        registerC2SReceiver(GravityGunScrollPayload.TYPE, GravityGunScrollPayload.STREAM_CODEC, AntarchyFabricNetworking::handleGravityGunScroll);
        registerC2SReceiver(BigBerthaModeCyclePayload.TYPE, BigBerthaModeCyclePayload.STREAM_CODEC, AntarchyFabricNetworking::handleBigBerthaModeCycle);
        registerC2SReceiver(DiamondMinecartInputPayload.TYPE, DiamondMinecartInputPayload.STREAM_CODEC, AntarchyFabricNetworking::handleDiamondMinecartInput);
        registerC2SReceiver(BrutalflyElytraFlapPayload.TYPE, BrutalflyElytraFlapPayload.STREAM_CODEC, AntarchyFabricNetworking::handleBrutalflyFlap);
        registerC2SReceiver(JumpyBootsLaunchPayload.TYPE, JumpyBootsLaunchPayload.STREAM_CODEC, AntarchyFabricNetworking::handleJumpyBootsLaunch);
        registerC2SReceiver(DorrieJumpInputPayload.TYPE, DorrieJumpInputPayload.STREAM_CODEC, AntarchyFabricNetworking::handleDorrieJumpInput);
        registerC2SReceiver(com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload.TYPE, com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload.STREAM_CODEC, AntarchyFabricNetworking::handleDorrieChargeJumpInput);
        registerC2SReceiver(HerculesBeetleJumpInputPayload.TYPE, HerculesBeetleJumpInputPayload.STREAM_CODEC, AntarchyFabricNetworking::handleHerculesBeetleJumpInput);
        registerC2SReceiver(HerculesBeetleFlightTogglePayload.TYPE, HerculesBeetleFlightTogglePayload.STREAM_CODEC, AntarchyFabricNetworking::handleHerculesBeetleFlightToggle);
        registerC2SReceiver(HerculesBeetleMountedAttackPayload.TYPE, HerculesBeetleMountedAttackPayload.STREAM_CODEC, AntarchyFabricNetworking::handleHerculesBeetleMountedAttack);
        registerC2SReceiver(HerculesBeetleMountedChargePayload.TYPE, HerculesBeetleMountedChargePayload.STREAM_CODEC, AntarchyFabricNetworking::handleHerculesBeetleMountedCharge);
        registerC2SReceiver(MultipartAttackPayload.TYPE, MultipartAttackPayload.STREAM_CODEC, AntarchyFabricNetworking::handleMultipartAttack);
        registerC2SReceiver(MultipartInteractPayload.TYPE, MultipartInteractPayload.STREAM_CODEC, AntarchyFabricNetworking::handleMultipartInteract);
        registerC2SReceiver(ToggleTigerEyeCamouflagePayload.TYPE, ToggleTigerEyeCamouflagePayload.STREAM_CODEC, (player, payload) -> handleTigerEyeCamouflageToggle(player));
    }

    public static void syncTigerEyeCamouflage(ServerPlayer player) {
        TigerEyeCamouflageStatePayload payload = TigerEyeCamouflageSync.payload(player);
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            sendToPlayer(tracking, payload, TigerEyeCamouflageStatePayload.STREAM_CODEC, TigerEyeCamouflageStatePayload.TYPE);
        }
        sendToPlayer(player, payload, TigerEyeCamouflageStatePayload.STREAM_CODEC, TigerEyeCamouflageStatePayload.TYPE);
    }

    private static void handleTigerEyeCamouflageToggle(ServerPlayer player) {
        TigerEyeCamouflageController.ToggleResult result = TigerEyeCamouflageController.toggle(player);
        if (result != TigerEyeCamouflageController.ToggleResult.NO_CHANGE) {
            syncTigerEyeCamouflage(player);
        }
        String messageKey = switch (result) {
            case ACTIVATED -> "message.antarchy.tiger_eye_camouflage.activated";
            case DEACTIVATED -> "message.antarchy.tiger_eye_camouflage.disabled";
            case FULL_SET_REQUIRED -> TigerEyeArmorUtil.countEquippedPieces(player) > 0
                    ? "message.antarchy.tiger_eye_camouflage.full_set_required"
                    : null;
            case INVALID_BLOCK -> "message.antarchy.tiger_eye_camouflage.invalid_block";
            case NO_CHANGE -> null;
        };
        if (messageKey != null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(messageKey), true);
        }
    }

    public static void syncGravityToPlayer(ServerPlayer target, Entity entity) {
        GravityStatePayload payload = new GravityStatePayload(
                entity.getId(),
                AntarchyGravityApi.getGravityDirection(entity),
                AntarchyGravityApi.getPrevGravityDirection(entity),
                AntarchyGravityApi.isGravityForced(entity),
                AntarchyGravityApi.getTransitionDuration(entity),
                AntarchyGravityApi.getTransitionRemaining(entity)
        );
        sendToPlayer(target, payload, GravityStatePayload.STREAM_CODEC, GravityStatePayload.TYPE);
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
            sendToPlayer(player, payload, GravityStatePayload.STREAM_CODEC, GravityStatePayload.TYPE);
        }
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload, GravityStatePayload.STREAM_CODEC, GravityStatePayload.TYPE);
        }
    }

    public static void syncBloodglass(ServerPlayer player, int shieldsActive, int shieldsMax) {
        sendToPlayer(player, new BloodglassStatePayload(shieldsActive, shieldsMax), BloodglassStatePayload.STREAM_CODEC, BloodglassStatePayload.TYPE);
    }

    public static void syncKatanaTrail(Entity entity, int durationTicks) {
        BloodCrystalKatanaTrailPayload payload = new BloodCrystalKatanaTrailPayload(entity.getId(), durationTicks);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload, BloodCrystalKatanaTrailPayload.STREAM_CODEC, BloodCrystalKatanaTrailPayload.TYPE);
        }
        if (entity instanceof ServerPlayer serverPlayer) {
            sendToPlayer(serverPlayer, payload, BloodCrystalKatanaTrailPayload.STREAM_CODEC, BloodCrystalKatanaTrailPayload.TYPE);
        }
    }

    public static void syncScorpionWhipTether(ServerPlayer player, int targetId) {
        ScorpionWhipTetherPayload payload = new ScorpionWhipTetherPayload(player.getId(), targetId);
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            sendToPlayer(tracking, payload, ScorpionWhipTetherPayload.STREAM_CODEC, ScorpionWhipTetherPayload.TYPE);
        }
        sendToPlayer(player, payload, ScorpionWhipTetherPayload.STREAM_CODEC, ScorpionWhipTetherPayload.TYPE);
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
            sendToPlayer(player, payload, ThoraxisWeatherPayload.STREAM_CODEC, ThoraxisWeatherPayload.TYPE);
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

    private static void handleBigBerthaModeCycle(ServerPlayer player, BigBerthaModeCyclePayload payload) {
        if (!(player.getMainHandItem().getItem() instanceof BigBerthaItem bigBerthaItem)) {
            return;
        }
        bigBerthaItem.tryCycleModeWhileCoolingDown(player.serverLevel(), player, player.getMainHandItem());
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
        chestStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.CHEST));
        player.getCooldowns().addCooldown(chestStack.getItem(), BrutalflyElytraFlightHelper.FLAP_COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.9F,
                tier == BrutalflyElytraFlightHelper.FlapTier.PERFECT ? 0.8F : tier == BrutalflyElytraFlightHelper.FlapTier.GOOD ? 0.95F : 1.1F);

        BrutalflyElytraAnimationPayload animationPayload = new BrutalflyElytraAnimationPayload(
                player.getId(),
                BrutalflyElytraFlightHelper.animationTicksFor(tier),
                lift
        );
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            sendToPlayer(tracking, animationPayload, BrutalflyElytraAnimationPayload.STREAM_CODEC, BrutalflyElytraAnimationPayload.TYPE);
        }
        sendToPlayer(player, animationPayload, BrutalflyElytraAnimationPayload.STREAM_CODEC, BrutalflyElytraAnimationPayload.TYPE);
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

        serverPlayer.attack(part);
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

    private static void handleDorrieJumpInput(ServerPlayer player, DorrieJumpInputPayload payload) {
        if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
        dorrie.setPressingJump(payload.pressing());
    }

    private static void handleDorrieChargeJumpInput(ServerPlayer player, com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload payload) {
        if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
        if (payload.pressing()) {
            dorrie.startJumpCharge();
        } else {
            dorrie.releaseJump();
        }
    }

    private static void handleHerculesBeetleJumpInput(ServerPlayer player, HerculesBeetleJumpInputPayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            beetle.setRiderJumpPressed(payload.pressing());
        }
    }

    private static void handleHerculesBeetleMountedAttack(ServerPlayer player, HerculesBeetleMountedAttackPayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            beetle.handleMountedRegularAttack(player);
        }
    }

    private static void handleHerculesBeetleFlightToggle(ServerPlayer player, HerculesBeetleFlightTogglePayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            beetle.toggleMountedFlight(player);
        }
    }

    private static void handleHerculesBeetleMountedCharge(ServerPlayer player, HerculesBeetleMountedChargePayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            if (payload.pressing()) {
                beetle.startMountedCharge(player);
            } else {
                beetle.releaseMountedCharge(player);
            }
        }
    }

    public static void triggerHerculesBeetleImpactShake(int durationTicks) {
        HerculesBeetleImpactShakeClientState.trigger(durationTicks);
    }

    public static void triggerImpactShake(ImpactShakePayload payload) {
        CameraShakeClientState.triggerImpact(
                new Vec3(payload.x(), payload.y(), payload.z()),
                payload.intensity(),
                payload.durationTicks(),
                payload.radius()
        );
    }
}
