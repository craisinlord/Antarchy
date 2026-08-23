package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.WormHookProjectileEntity;
import com.craisinlord.antarchy.content.util.WormHookRope;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative swing physics for the worm hook: pulls the holding player taut against
 * a rope anchored to a stuck {@link WormHookProjectileEntity}, wrapping the rope around block
 * corners via {@link WormHookRope} so the swing radius shortens realistically when the player
 * swings around an obstacle.
 *
 * A tether entry is created the moment the hook is thrown (so the rope can be rendered following
 * the flying projectile) and only gains real swing physics once the hook sticks into a block.
 */
public final class WormHookTetherManager {
    private static final double MIN_LEN = 2.0D;
    private static final double REEL_SPEED = 0.12D;
    private static final double REEL_PULL_STRENGTH = 0.16D;
    private static final double BREAK_RANGE_MULTIPLIER = 1.5D;
    private static final double DEFAULT_BREAK_RANGE = 48.0D;
    private static final double SNAP_BUFFER = 4.0D;
    private static final double PULL_CORRECTION = 0.35D;

    private static final Map<UUID, TetherState> TETHERS = new ConcurrentHashMap<>();

    private WormHookTetherManager() {
    }

    public static boolean hasTether(ServerPlayer player) {
        return TETHERS.containsKey(player.getUUID());
    }

    public static WormHookProjectileEntity getHook(ServerPlayer player) {
        TetherState state = TETHERS.get(player.getUUID());
        if (state == null) {
            return null;
        }
        Entity hook = player.serverLevel().getEntity(state.hookEntityId);
        return hook instanceof WormHookProjectileEntity wormHook ? wormHook : null;
    }

    /** Registers the hook the moment it's thrown, purely so the rope can be rendered following it. */
    public static void launch(ServerPlayer player, WormHookProjectileEntity hook) {
        clear(player);
        TETHERS.put(player.getUUID(), new TetherState(hook.getId()));
        WormHookTetherSync.send(player, hook.getId());
    }

    /** Called once the hook sticks into a block; turns the cosmetic tether into real swing physics. */
    public static boolean attach(ServerPlayer player, WormHookProjectileEntity hook) {
        TetherState state = TETHERS.get(player.getUUID());
        if (state == null || state.hookEntityId != hook.getId()) {
            return false;
        }

        double dist = player.getEyePosition().distanceTo(hook.position());
        if (dist < MIN_LEN) {
            clear(player);
            return false;
        }

        state.maxLen = Math.max(MIN_LEN, dist);
        state.rope = new WormHookRope(hook.position(), player.getEyePosition());
        return true;
    }

    public static void detach(ServerPlayer player) {
        WormHookProjectileEntity hook = getHook(player);
        clear(player);
        if (hook != null) {
            hook.removeHook();
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 0.7F, 1.2F);
    }

    private static void clear(ServerPlayer player) {
        TetherState removed = TETHERS.remove(player.getUUID());
        if (removed != null) {
            WormHookTetherSync.send(player, -1);
        }
    }

    public static void tick(ServerPlayer player) {
        TetherState state = TETHERS.get(player.getUUID());
        if (state == null) {
            return;
        }

        WormHookProjectileEntity hook = getHook(player);
        if (!isValid(player, hook, state)) {
            clear(player);
            if (hook != null) {
                hook.removeHook();
            }
            return;
        }

        if (state.rope == null) {
            // Hook is still flying; nothing to simulate yet, just tracking it for the render sync.
            return;
        }

        boolean reeling = player.isShiftKeyDown();
        if (reeling) {
            state.maxLen = Math.max(MIN_LEN, state.maxLen - REEL_SPEED);
        }

        Vec3 anchor = hook.position();
        Vec3 playerEye = player.getEyePosition();

        state.rope.update(hook, anchor, playerEye, state.maxLen);

        Vec3 pivot = state.rope.getPivot();
        double consumed = state.rope.getConsumedLength();
        double remaining = Math.max(0.5D, state.maxLen - consumed);

        Vec3 toPlayer = playerEye.subtract(pivot);
        double dist = toPlayer.length();
        if (dist < 0.001D) {
            return;
        }

        Vec3 towardPivot = toPlayer.scale(1.0D / dist);

        // Sneaking actively winds the player in toward the anchor, not just shortening
        // the max length (which alone has no effect until the rope goes taut).
        if (reeling && dist > MIN_LEN) {
            Vec3 pull = towardPivot.scale(-REEL_PULL_STRENGTH);
            Vec3 reelMotion = player.getDeltaMovement().add(pull);
            reelMotion = new Vec3(reelMotion.x, Mth.clamp(reelMotion.y, -0.6D, 0.6D), reelMotion.z);
            player.setDeltaMovement(reelMotion);
            player.hurtMarked = true;
            player.fallDistance = 0;
        }

        if (dist <= remaining) {
            return;
        }

        double overshoot = dist - remaining;
        if (overshoot > SNAP_BUFFER) {
            clear(player);
            hook.removeHook();
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.9F);
            return;
        }

        Vec3 motion = player.getDeltaMovement();
        double outward = motion.dot(towardPivot);
        if (outward > 0) {
            motion = motion.subtract(towardPivot.scale(outward));
        }

        Vec3 correction = towardPivot.scale(-overshoot * PULL_CORRECTION);
        Vec3 newMotion = motion.add(correction);
        newMotion = new Vec3(newMotion.x, Mth.clamp(newMotion.y, -1.0D, 1.0D), newMotion.z);
        player.setDeltaMovement(newMotion);
        player.hurtMarked = true;
        player.fallDistance = 0;
    }

    private static boolean isValid(ServerPlayer player, WormHookProjectileEntity hook, TetherState state) {
        double breakRange = (state.rope == null ? DEFAULT_BREAK_RANGE : state.maxLen) * BREAK_RANGE_MULTIPLIER;
        return player.isAlive()
                && hook != null
                && hook.isAlive()
                && !hook.isRemoved()
                && player.level() == hook.level()
                && isHoldingHook(player)
                && player.distanceToSqr(hook) <= breakRange * breakRange;
    }

    private static boolean isHoldingHook(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof WormHookItem
                || player.getOffhandItem().getItem() instanceof WormHookItem;
    }

    private static final class TetherState {
        private final int hookEntityId;
        private double maxLen;
        private WormHookRope rope;

        private TetherState(int hookEntityId) {
            this.hookEntityId = hookEntityId;
        }
    }
}
