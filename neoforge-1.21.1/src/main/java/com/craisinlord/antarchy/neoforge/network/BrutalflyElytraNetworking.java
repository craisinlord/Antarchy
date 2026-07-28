package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.network.BrutalflyElytraAnimationPayload;
import com.craisinlord.antarchy.content.network.BrutalflyElytraFlapPayload;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BrutalflyElytraNetworking {
    private BrutalflyElytraNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                BrutalflyElytraFlapPayload.TYPE,
                BrutalflyElytraFlapPayload.STREAM_CODEC,
                BrutalflyElytraNetworking::handleFlap
        ).playToClient(
                BrutalflyElytraAnimationPayload.TYPE,
                BrutalflyElytraAnimationPayload.STREAM_CODEC,
                BrutalflyElytraNetworking::handleAnimation
        );
    }

    private static void handleFlap(com.craisinlord.antarchy.content.network.BrutalflyElytraFlapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ItemStack chestStack = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);
            if (!(chestStack.getItem() instanceof BrutalflyElytraItem)) {
                return;
            }
            if (serverPlayer.isSpectator() || !serverPlayer.isFallFlying() || serverPlayer.isPassenger()) {
                return;
            }
            if (serverPlayer.getCooldowns().isOnCooldown(chestStack.getItem())) {
                return;
            }

            BrutalflyElytraFlightHelper.FlapTier tier = BrutalflyElytraFlightHelper.resolveFlapTier(payload.chargeTicks());
            if (tier == BrutalflyElytraFlightHelper.FlapTier.FAIL) {
                return;
            }

            float lift = BrutalflyElytraFlightHelper.liftFor(tier);
            float forwardBoost = BrutalflyElytraFlightHelper.forwardBoostFor(tier);
            Vec3 look = serverPlayer.getLookAngle();
            Vec3 boost = new Vec3(look.x * forwardBoost, lift, look.z * forwardBoost);
            serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().add(boost));
            serverPlayer.hasImpulse = true;
            serverPlayer.hurtMarked = true;
            serverPlayer.resetFallDistance();
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            chestStack.hurtAndBreak(1, serverPlayer, EquipmentSlot.CHEST);
            serverPlayer.getCooldowns().addCooldown(chestStack.getItem(), BrutalflyElytraFlightHelper.FLAP_COOLDOWN_TICKS);
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.blockPosition(),
                    SoundEvents.PHANTOM_FLAP,
                    SoundSource.PLAYERS,
                    0.9F,
                    tier == BrutalflyElytraFlightHelper.FlapTier.PERFECT ? 0.8F : tier == BrutalflyElytraFlightHelper.FlapTier.GOOD ? 0.95F : 1.1F
            );
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    serverPlayer,
                    new BrutalflyElytraAnimationPayload(
                            serverPlayer.getId(),
                            BrutalflyElytraFlightHelper.animationTicksFor(tier),
                            lift
                    )
            );
        });
    }

    private static void handleAnimation(BrutalflyElytraAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> BrutalflyElytraClientState.trigger(
                payload.entityId(),
                payload.durationTicks(),
                payload.strength()
        ));
    }
}
