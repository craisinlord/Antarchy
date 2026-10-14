package com.craisinlord.antarchy.fabric.network;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.SpringyBootsHelper;
import com.craisinlord.antarchy.fabric.util.SpringyBootsFabricHelper;
import com.craisinlord.antarchy.content.item.SpringyBootsItem;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.TemporalTunerItem;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import com.craisinlord.antarchy.content.network.SpringyBootsLaunchPayload;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import com.craisinlord.anthology.network.AnthologyNetwork;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync.setSender(AntarchyFabricNetworking::send);
    }

    private static final Map<ResourceLocation, Consumer<CustomPacketPayload>> CLIENT_HANDLERS = new ConcurrentHashMap<>();

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        AnthologyNetwork.registerClientbound(type, codec, (payload, player) -> {
            Consumer<CustomPacketPayload> handler = CLIENT_HANDLERS.get(type.id());
            if (handler != null) {
                handler.accept(payload);
            }
        });
    }

    private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        SERVER_CODECS.put(type.id(), codec);
    }

    private static final Map<ResourceLocation, StreamCodec<? super FriendlyByteBuf, ?>> SERVER_CODECS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void registerGlobalReceiver(CustomPacketPayload.Type<T> type, BiConsumer<T, ServerPlayer> handler) {
        StreamCodec<? super FriendlyByteBuf, T> codec = (StreamCodec<? super FriendlyByteBuf, T>) SERVER_CODECS.get(type.id());
        AnthologyNetwork.registerServerbound(type, codec, handler);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void setClientHandler(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
        CLIENT_HANDLERS.put(type.id(), payload -> handler.accept((T) payload));
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        AnthologyNetwork.sendToPlayer(player, payload);
    }

    private static void registerPayloadTypes() {
        registerS2C(PortalGunIndicatorPayload.TYPE, PortalGunIndicatorPayload.STREAM_CODEC);
        registerS2C(BloodglassStatePayload.TYPE, BloodglassStatePayload.STREAM_CODEC);
        registerS2C(TigerEyeCamouflageStatePayload.TYPE, TigerEyeCamouflageStatePayload.STREAM_CODEC);
        registerS2C(BloodCrystalKatanaTrailPayload.TYPE, BloodCrystalKatanaTrailPayload.STREAM_CODEC);
        registerS2C(ScorpionWhipTetherPayload.TYPE, ScorpionWhipTetherPayload.STREAM_CODEC);
        registerS2C(WormHookTetherPayload.TYPE, WormHookTetherPayload.STREAM_CODEC);
        registerS2C(BrutalflyElytraAnimationPayload.TYPE, BrutalflyElytraAnimationPayload.STREAM_CODEC);
        registerS2C(HerculesBeetleImpactShakePayload.TYPE, HerculesBeetleImpactShakePayload.STREAM_CODEC);
        registerS2C(KingJudgmentFlashPayload.TYPE, KingJudgmentFlashPayload.STREAM_CODEC);
        registerS2C(HordeIntensityPayload.TYPE, HordeIntensityPayload.STREAM_CODEC);

        registerC2S(GravityGunPrimaryPayload.TYPE, GravityGunPrimaryPayload.STREAM_CODEC);
        registerC2S(PortalGunPrimaryPayload.TYPE, PortalGunPrimaryPayload.STREAM_CODEC);
        registerC2S(PortalGunIndicatorRequestPayload.TYPE, PortalGunIndicatorRequestPayload.STREAM_CODEC);
        registerC2S(PortalGunResetPayload.TYPE, PortalGunResetPayload.STREAM_CODEC);
        registerC2S(com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload.TYPE, com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload.STREAM_CODEC);
        registerC2S(EyeOfStormPrimaryPayload.TYPE, EyeOfStormPrimaryPayload.STREAM_CODEC);
        registerC2S(OpenGiantFryingPanPayload.TYPE, OpenGiantFryingPanPayload.STREAM_CODEC);
        registerC2S(RoyalMountActionPayload.TYPE, RoyalMountActionPayload.STREAM_CODEC);
        registerC2S(RoyalMountVerticalPayload.TYPE, RoyalMountVerticalPayload.STREAM_CODEC);
        registerC2S(GravityGunScrollPayload.TYPE, GravityGunScrollPayload.STREAM_CODEC);
        registerC2S(TemporalTunerScrollPayload.TYPE, TemporalTunerScrollPayload.STREAM_CODEC);
        registerC2S(BigBerthaModeCyclePayload.TYPE, BigBerthaModeCyclePayload.STREAM_CODEC);
        registerC2S(com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload.TYPE,
                com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload.STREAM_CODEC);
        registerC2S(ToggleTigerEyeCamouflagePayload.TYPE, ToggleTigerEyeCamouflagePayload.STREAM_CODEC);
        registerC2S(ToggleRoyalInversionPayload.TYPE, ToggleRoyalInversionPayload.STREAM_CODEC);
        registerC2S(com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload.TYPE, com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload.STREAM_CODEC);
        registerC2S(DiamondMinecartInputPayload.TYPE, DiamondMinecartInputPayload.STREAM_CODEC);
        registerC2S(BrutalflyElytraFlapPayload.TYPE, BrutalflyElytraFlapPayload.STREAM_CODEC);
        registerC2S(SpringyBootsLaunchPayload.TYPE, SpringyBootsLaunchPayload.STREAM_CODEC);
        registerC2S(DorrieJumpInputPayload.TYPE, DorrieJumpInputPayload.STREAM_CODEC);
        registerC2S(com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload.TYPE, com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload.STREAM_CODEC);
        registerC2S(HerculesBeetleJumpInputPayload.TYPE, HerculesBeetleJumpInputPayload.STREAM_CODEC);
        registerC2S(HerculesBeetleFlightTogglePayload.TYPE, HerculesBeetleFlightTogglePayload.STREAM_CODEC);
        registerC2S(HerculesBeetleMountedAttackPayload.TYPE, HerculesBeetleMountedAttackPayload.STREAM_CODEC);
        registerC2S(HerculesBeetleMountedChargePayload.TYPE, HerculesBeetleMountedChargePayload.STREAM_CODEC);
    }

    private static void registerServerReceivers() {
        registerGlobalReceiver(GravityGunPrimaryPayload.TYPE, (payload, player) -> handleGravityGunPrimary(player, payload));
        registerGlobalReceiver(PortalGunPrimaryPayload.TYPE, (payload, player) -> handlePortalGunPrimary(player, payload));
        registerGlobalReceiver(PortalGunIndicatorRequestPayload.TYPE, (payload, player) -> com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync.handleRequest(player, payload));
        registerGlobalReceiver(com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload.TYPE, (payload, player) -> com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.handle(player, payload));
        registerGlobalReceiver(PortalGunResetPayload.TYPE, (payload, player) -> com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.handleInput(player, payload.action()));
        registerGlobalReceiver(EyeOfStormPrimaryPayload.TYPE, (payload, player) -> handleEyeOfStormPrimary(player, payload));
        registerGlobalReceiver(OpenGiantFryingPanPayload.TYPE, (payload, player) -> com.craisinlord.antarchy.content.item.GiantFryingPanItem.openHeldMenu(player));
        registerGlobalReceiver(RoyalMountActionPayload.TYPE, (payload, player) -> handleRoyalMountAction(player, payload));
        registerGlobalReceiver(RoyalMountVerticalPayload.TYPE, (payload, player) -> handleRoyalMountVertical(player, payload));
        registerGlobalReceiver(GravityGunScrollPayload.TYPE, (payload, player) -> handleGravityGunScroll(player, payload));
        registerGlobalReceiver(TemporalTunerScrollPayload.TYPE, (payload, player) -> handleTemporalTunerScroll(player, payload));
        registerGlobalReceiver(BigBerthaModeCyclePayload.TYPE, (payload, player) -> handleBigBerthaModeCycle(player, payload));
        registerGlobalReceiver(com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload.TYPE, (payload, player) -> handleRoyalGuardianSwordModeCycle(player));
        registerGlobalReceiver(ToggleTigerEyeCamouflagePayload.TYPE, (payload, player) -> handleTigerEyeCamouflageToggle(player));
        registerGlobalReceiver(ToggleRoyalInversionPayload.TYPE, (payload, player) -> com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem.toggleInversion(player));
        registerGlobalReceiver(com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload.TYPE, (payload, player) -> com.craisinlord.antarchy.content.effect.RoyalBoundaryManager.activate(player));
        registerGlobalReceiver(DiamondMinecartInputPayload.TYPE, (payload, player) -> handleDiamondMinecartInput(player, payload));
        registerGlobalReceiver(BrutalflyElytraFlapPayload.TYPE, (payload, player) -> handleBrutalflyFlap(player, payload));
        registerGlobalReceiver(SpringyBootsLaunchPayload.TYPE, (payload, player) -> handleSpringyBootsLaunch(player, payload));
        registerGlobalReceiver(DorrieJumpInputPayload.TYPE, (payload, player) -> handleDorrieJumpInput(player, payload));
        registerGlobalReceiver(com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload.TYPE, (payload, player) -> handleDorrieChargeJumpInput(player, payload));
        registerGlobalReceiver(HerculesBeetleJumpInputPayload.TYPE, (payload, player) -> handleHerculesBeetleJumpInput(player, payload));
        registerGlobalReceiver(HerculesBeetleFlightTogglePayload.TYPE, (payload, player) -> handleHerculesBeetleFlightToggle(player, payload));
        registerGlobalReceiver(HerculesBeetleMountedAttackPayload.TYPE, (payload, player) -> handleHerculesBeetleMountedAttack(player, payload));
        registerGlobalReceiver(HerculesBeetleMountedChargePayload.TYPE, (payload, player) -> handleHerculesBeetleMountedCharge(player, payload));
    }

    public static void syncBloodglass(ServerPlayer player, int shieldsActive, int shieldsMax) {
        send(player, new BloodglassStatePayload(shieldsActive, shieldsMax));
    }

    public static void syncTigerEyeCamouflage(ServerPlayer player) {
        TigerEyeCamouflageStatePayload payload = TigerEyeCamouflageSync.payload(player);
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            send(tracking, payload);
        }
        send(player, payload);
    }

    public static void syncKatanaTrail(Entity entity, int durationTicks) {
        BloodCrystalKatanaTrailPayload payload = new BloodCrystalKatanaTrailPayload(entity.getId(), durationTicks);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            send(player, payload);
        }
        if (entity instanceof ServerPlayer serverPlayer) {
            send(serverPlayer, payload);
        }
    }

    public static void syncScorpionWhipTether(ServerPlayer player, int targetId) {
        ScorpionWhipTetherPayload payload = new ScorpionWhipTetherPayload(player.getId(), targetId);
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            send(tracking, payload);
        }
        send(player, payload);
    }

    public static void syncWormHookTether(ServerPlayer player, int hookId) {
        WormHookTetherPayload payload = new WormHookTetherPayload(player.getId(), hookId);
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            send(tracking, payload);
        }
        send(player, payload);
    }

    private static void handleGravityGunPrimary(ServerPlayer player, GravityGunPrimaryPayload payload) {
        if (!(player.getMainHandItem().getItem() instanceof GravityGunItem gravityGunItem)) {
            return;
        }
        gravityGunItem.firePrimary(player.serverLevel(), player, player.getMainHandItem());
    }

    private static void handlePortalGunPrimary(ServerPlayer player, PortalGunPrimaryPayload payload) {
        ItemStack stack = payload.offhand() ? player.getOffhandItem() : player.getMainHandItem();
        if (!(stack.getItem() instanceof PortalGunItem portalGunItem)) {
            return;
        }
        portalGunItem.firePrimary(player.serverLevel(), player, stack, payload.offhand());
    }

    private static void handleRoyalMountAction(ServerPlayer player, RoyalMountActionPayload payload) {
        if (payload.action() == RoyalMountActionPayload.MOUNT) {
            com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity.tryAssistedMount(player);
            return;
        }
        if (!(player.getVehicle() instanceof com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity mount)) {
            return;
        }
        switch (payload.action()) {
            case RoyalMountActionPayload.FLIGHT_TOGGLE -> mount.toggleMountedFlight(player);
            case RoyalMountActionPayload.BITE -> mount.handleMountedBite(player);
            case RoyalMountActionPayload.SPIT -> mount.handleMountedSpit(player);
            default -> {
            }
        }
    }

    private static void handleRoyalMountVertical(ServerPlayer player, RoyalMountVerticalPayload payload) {
        if (player.getVehicle() instanceof com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity mount) {
            mount.setRiderAscend(payload.ascend());
            mount.setRiderDescend(payload.descend());
        }
    }

    private static void handleEyeOfStormPrimary(ServerPlayer player, EyeOfStormPrimaryPayload payload) {
        if (!(player.getMainHandItem().getItem() instanceof EyeOfTheStormItem eyeOfTheStormItem)) {
            return;
        }
        eyeOfTheStormItem.firePrimary(player.serverLevel(), player, player.getMainHandItem());
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
        bigBerthaItem.tryCycleModeFromInput(player.serverLevel(), player, player.getMainHandItem());
    }

    private static void handleTemporalTunerScroll(ServerPlayer player, TemporalTunerScrollPayload payload) {
        if (TemporalTunerItem.isAvailable(player)) {
            TemporalTunerItem.adjust(player, payload.delta());
        }
    }

    private static void handleRoyalGuardianSwordModeCycle(ServerPlayer player) {
        if (!(player.getMainHandItem().getItem() instanceof com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem sword)) {
            return;
        }
        sword.tryCycleModeFromInput(player.serverLevel(), player, player.getMainHandItem());
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
        chestStack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(EquipmentSlot.CHEST));
        player.getCooldowns().addCooldown(chestStack.getItem(), BrutalflyElytraFlightHelper.FLAP_COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.9F,
                tier == BrutalflyElytraFlightHelper.FlapTier.PERFECT ? 0.8F : tier == BrutalflyElytraFlightHelper.FlapTier.GOOD ? 0.95F : 1.1F);

        BrutalflyElytraAnimationPayload animationPayload = new BrutalflyElytraAnimationPayload(
                player.getId(),
                BrutalflyElytraFlightHelper.animationTicksFor(tier),
                lift
        );
        for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
            send(tracking, animationPayload);
        }
        send(player, animationPayload);
    }

    private static void handleSpringyBootsLaunch(ServerPlayer player, SpringyBootsLaunchPayload payload) {
        if (!SpringyBootsItem.isWearingSpringyBoots(player)) return;
        if (player.isSpectator() || player.isPassenger()) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (player.getCooldowns().isOnCooldown(boots.getItem())) return;

        int clampedCharge = Math.min(payload.chargeTicks(), SpringyBootsHelper.CHARGE_TICKS_MAX);
        if (clampedCharge <= 0) return;

        float verticalBoost = SpringyBootsHelper.verticalBoostFor(clampedCharge);
        Vec3 current = player.getDeltaMovement();
        double newX = current.x;
        double newZ = current.z;

        if (payload.sprinting()) {
            Vec3 look = player.getLookAngle();
            newX += look.x * SpringyBootsHelper.SPRINT_FORWARD_BOOST;
            newZ += look.z * SpringyBootsHelper.SPRINT_FORWARD_BOOST;
        }

        player.setDeltaMovement(newX, verticalBoost, newZ);
        player.setPos(player.getX(), player.getY() + 0.001, player.getZ());
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.resetFallDistance();
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        player.level().playSound(null, player.blockPosition(), SoundEvents.SLIME_JUMP, SoundSource.PLAYERS,
                1.0F, 0.6F + (clampedCharge / (float) SpringyBootsHelper.CHARGE_TICKS_MAX) * 0.6F);

        SpringyBootsFabricHelper.setProtectionUntil(player,
                player.level().getGameTime() + SpringyBootsHelper.FALL_PROTECTION_TICKS);

        player.getCooldowns().addCooldown(boots.getItem(), SpringyBootsHelper.COOLDOWN_TICKS);
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
}
