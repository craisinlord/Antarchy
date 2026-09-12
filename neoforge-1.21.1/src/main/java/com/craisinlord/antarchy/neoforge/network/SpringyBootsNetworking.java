package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.SpringyBootsHelper;
import com.craisinlord.antarchy.content.item.SpringyBootsItem;
import com.craisinlord.antarchy.content.network.SpringyBootsLaunchPayload;
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
public final class SpringyBootsNetworking {
    private SpringyBootsNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                SpringyBootsLaunchPayload.TYPE,
                SpringyBootsLaunchPayload.STREAM_CODEC,
                SpringyBootsNetworking::handleLaunch
        );
    }

    private static void handleLaunch(SpringyBootsLaunchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!SpringyBootsItem.isWearingSpringyBoots(player)) return;
            if (player.isSpectator() || player.isPassenger()) return;

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (player.getCooldowns().isOnCooldown(boots.getItem())) return;

            int clampedCharge = Math.min(payload.chargeTicks(), SpringyBootsHelper.CHARGE_TICKS_MAX);
            if (clampedCharge <= 0) return;

            float verticalBoost = SpringyBootsHelper.verticalBoostFor(clampedCharge);
            Vec3 current = player.getDeltaMovement();

            double newY = verticalBoost;
            double newX = current.x;
            double newZ = current.z;

            if (payload.sprinting()) {
                Vec3 look = player.getLookAngle();
                newX += look.x * SpringyBootsHelper.SPRINT_FORWARD_BOOST;
                newZ += look.z * SpringyBootsHelper.SPRINT_FORWARD_BOOST;
            }

            player.setDeltaMovement(newX, newY, newZ);
            player.setPos(player.getX(), player.getY() + 0.001, player.getZ());
            player.hasImpulse = true;
            player.hurtMarked = true;
            player.resetFallDistance();
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            player.level().playSound(null, player.blockPosition(), SoundEvents.SLIME_JUMP, SoundSource.PLAYERS, 1.0F, 0.6F + (clampedCharge / (float) SpringyBootsHelper.CHARGE_TICKS_MAX) * 0.6F);

            player.getPersistentData().putLong(SpringyBootsHelper.FALL_PROTECTION_NBT_KEY, player.level().getGameTime() + SpringyBootsHelper.FALL_PROTECTION_TICKS);

            player.getCooldowns().addCooldown(boots.getItem(), SpringyBootsHelper.COOLDOWN_TICKS);
        });
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!SpringyBootsItem.isWearingSpringyBoots(player)) return;
        long protectionUntil = player.getPersistentData().getLong(SpringyBootsHelper.FALL_PROTECTION_NBT_KEY);
        if (player.level().getGameTime() < protectionUntil) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.25F);
        }
    }
}
