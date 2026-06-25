package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.JumpyBootsHelper;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.content.network.JumpyBootsLaunchPayload;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class JumpyBootsNetworking {
    private JumpyBootsNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                JumpyBootsLaunchPayload.TYPE,
                JumpyBootsLaunchPayload.STREAM_CODEC,
                JumpyBootsNetworking::handleLaunch
        );
    }

    private static void handleLaunch(JumpyBootsLaunchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!JumpyBootsItem.isWearingJumpyBoots(player)) return;
            if (player.isSpectator() || player.isPassenger()) return;

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (player.getCooldowns().isOnCooldown(boots.getItem())) return;

            int clampedCharge = Math.min(payload.chargeTicks(), JumpyBootsHelper.CHARGE_TICKS_MAX);
            if (clampedCharge <= 0) return;

            float verticalBoost = JumpyBootsHelper.verticalBoostFor(clampedCharge);
            Vec3 current = player.getDeltaMovement();

            double newY = verticalBoost;
            double newX = current.x;
            double newZ = current.z;

            if (payload.sprinting()) {
                Vec3 look = player.getLookAngle();
                newX += look.x * JumpyBootsHelper.SPRINT_FORWARD_BOOST;
                newZ += look.z * JumpyBootsHelper.SPRINT_FORWARD_BOOST;
            }

            player.setDeltaMovement(newX, newY, newZ);
            player.hasImpulse = true;
            player.hurtMarked = true;
            player.resetFallDistance();
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            player.level().playSound(null, player.blockPosition(), SoundEvents.SLIME_JUMP, SoundSource.PLAYERS, 1.0F, 0.6F + (clampedCharge / (float) JumpyBootsHelper.CHARGE_TICKS_MAX) * 0.6F);

            player.getPersistentData().putLong(JumpyBootsHelper.FALL_PROTECTION_NBT_KEY, player.level().getGameTime() + JumpyBootsHelper.FALL_PROTECTION_TICKS);

            player.getCooldowns().addCooldown(boots.getItem(), JumpyBootsHelper.COOLDOWN_TICKS);
        });
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!JumpyBootsItem.isWearingJumpyBoots(player)) return;
        long protectionUntil = player.getPersistentData().getLong(JumpyBootsHelper.FALL_PROTECTION_NBT_KEY);
        if (player.level().getGameTime() < protectionUntil) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.25F);
        }
    }
}
