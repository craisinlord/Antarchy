package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.function.BiConsumer;
import com.craisinlord.antarchy.content.network.PortalGunGrabStatePayload;

public final class PortalGunGrabManager {
    private static final double REACH = 5.0D;
    private static final double BASE_HOLD_DISTANCE = 3.5D;
    private static final double MAX_HOLD_DISTANCE = 6.0D;
    private static final Map<UUID, GrabState> GRABS = new HashMap<>();
    private static BiConsumer<ServerPlayer, PortalGunGrabStatePayload> stateSender = (player, payload) -> {};

    private PortalGunGrabManager() {
    }

    public static void setStateSender(BiConsumer<ServerPlayer, PortalGunGrabStatePayload> sender) {
        stateSender = sender;
    }

    public static void toggle(ServerPlayer player) {
        GrabState active = GRABS.remove(player.getUUID());
        if (active != null) {
            release(active);
            stateSender.accept(player, new PortalGunGrabStatePayload(false));
            return;
        }
        ItemStack gunStack = getHeldPortalGun(player);
        if (gunStack.isEmpty()) {
            return;
        }
        int grabStrength = PortalGunItem.getGrabStrength(gunStack);
        Entity target = findTarget(player, grabStrength);
        if (target == null) {
            target = findBlockTarget(player, grabStrength);
            if (target == null) {
                return;
            }
        }
        for (Map.Entry<UUID, GrabState> entry : new HashMap<>(GRABS).entrySet()) {
            if (entry.getValue().target() == target) {
                GRABS.remove(entry.getKey());
                release(entry.getValue());
                ServerPlayer previousHolder = player.serverLevel().getServer().getPlayerList().getPlayer(entry.getKey());
                if (previousHolder != null) {
                    stateSender.accept(previousHolder, new PortalGunGrabStatePayload(false));
                }
            }
        }
        double holdDistance = BASE_HOLD_DISTANCE;
        if (target.getBbWidth() > 2.0F) {
            holdDistance = Math.min(MAX_HOLD_DISTANCE, holdDistance + target.getBbWidth() - 2.0D);
        }
        GrabState state = new GrabState(player.getUUID(), player.serverLevel(), target, holdDistance, target.isNoGravity());
        GRABS.put(player.getUUID(), state);
        target.setNoGravity(true);
        stateSender.accept(player, new PortalGunGrabStatePayload(true));
    }

    public static void tick(MinecraftServer server) {
        for (Map.Entry<UUID, GrabState> entry : new HashMap<>(GRABS).entrySet()) {
            GrabState state = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(state.playerId());
            Entity target = state.target();
            if (player == null || !player.isAlive() || player.serverLevel() != state.level() || !isHoldingPortalGun(player)
                    || !target.isAlive() || target.isRemoved() || target.level() != state.level()
                    || target.distanceTo(player) > state.holdDistance() + 6.25D) {
                GRABS.remove(entry.getKey());
                release(state);
                if (player != null) {
                    stateSender.accept(player, new PortalGunGrabStatePayload(false));
                }
                continue;
            }
            Vec3 holdPosition = player.getEyePosition(1.0F).add(player.getLookAngle().scale(state.holdDistance()));
            if (target.position().distanceTo(holdPosition) > 5.0D) {
                target.teleportTo(player.getX(), player.getY(), player.getZ());
            }
            Vec3 velocity = holdPosition.subtract(target.getX(), target.getBoundingBox().getCenter().y, target.getZ());
            target.setDeltaMovement(velocity);
            target.move(MoverType.SELF, velocity);
            velocity = holdPosition.subtract(target.getX(), target.getBoundingBox().getCenter().y, target.getZ());
            target.setDeltaMovement(velocity);
            target.setNoGravity(true);
            if (target instanceof FallingBlockEntity fallingBlock) {
                fallingBlock.time = 2;
            }
            if (target instanceof AbstractHurtingProjectile projectile) {
                projectile.accelerationPower = 0.0D;
            }
            target.fallDistance = (float) -(velocity.y * velocity.y);
            target.setOnGround(false);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    public static void clear(MinecraftServer server) {
        for (GrabState state : GRABS.values()) {
            release(state);
            ServerPlayer player = server.getPlayerList().getPlayer(state.playerId());
            if (player != null) {
                stateSender.accept(player, new PortalGunGrabStatePayload(false));
            }
        }
        GRABS.clear();
    }

    private static Entity findTarget(ServerPlayer player, int grabStrength) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(REACH));
        BlockHitResult blockHit = player.serverLevel().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistance = blockHit.getType() == HitResult.Type.BLOCK ? start.distanceTo(blockHit.getLocation()) : REACH;
        AABB searchBounds = player.getBoundingBox().expandTowards(look.scale(maxDistance)).inflate(1.0D);
        EntityHitResult closest = null;
        double closestDistance = maxDistance * maxDistance;
        for (Entity candidate : player.serverLevel().getEntities(player, searchBounds, entity -> canGrab(entity, grabStrength))) {
            var hit = candidate.getBoundingBox().inflate(candidate.getPickRadius()).clip(start, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(hit.get());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = new EntityHitResult(candidate, hit.get());
            }
        }
        return closest == null ? null : closest.getEntity();
    }

    private static Entity findBlockTarget(ServerPlayer player, int grabStrength) {
        if (grabStrength < 2) {
            return null;
        }
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(REACH));
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(AntarchyTags.Blocks.GRAVITY_GUN_BLACKLIST)) {
            return null;
        }
        BlockPos basePos = pos;
        BlockPos upperPos = null;
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            basePos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockState baseState = level.getBlockState(basePos);
            BlockPos candidateUpper = basePos.above();
            BlockState upperState = level.getBlockState(candidateUpper);
            if (!baseState.is(state.getBlock()) || !baseState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    || baseState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER
                    || !upperState.is(state.getBlock()) || !upperState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    || upperState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) {
                return null;
            }
            state = baseState;
            upperPos = candidateUpper;
        }
        if (state.getDestroySpeed(level, basePos) < 0.0F) {
            return null;
        }
        if (AntarchySettings.portalGunEntityGrabWeightBase() == 0
                && !Antarchy.MODID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace())) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(basePos);
        if (blockEntity != null && grabStrength < 3) {
            return null;
        }
        if (!state.isSolidRender(level, basePos)
                && state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED
                && !state.is(Blocks.SNOW)
                && !state.is(Blocks.GLOWSTONE)
                && !state.is(Blocks.CAKE)
                && !state.is(Blocks.TNT)
                && !state.is(Blocks.ICE)
                && !state.is(BlockTags.SLABS)
                && !state.is(Blocks.ANVIL)
                && !state.is(BlockTags.STAIRS)) {
            return null;
        }
        CompoundTag blockEntityData = blockEntity == null ? null : blockEntity.saveWithId(level.registryAccess());
        if (blockEntity != null) {
            level.removeBlockEntity(basePos);
        }
        if (upperPos != null) {
            level.removeBlock(upperPos, false);
        }
        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, basePos, state);
        if (fallingBlock == null) {
            return null;
        }
        if (blockEntityData != null) {
            fallingBlock.blockData = blockEntityData;
        }
        fallingBlock.setDeltaMovement(Vec3.ZERO);
        fallingBlock.setNoGravity(true);
        fallingBlock.hurtMarked = true;
        return fallingBlock;
    }

    private static boolean canGrab(Entity entity, int grabStrength) {
        if (!entity.isAlive() || entity instanceof Player || entity instanceof PortalGunPortalEntity || entity.isPassenger()) {
            return false;
        }
        double volume = entity.getBbWidth() * entity.getBbHeight() * entity.getBbWidth();
        double strengthScale = switch (grabStrength) {
            case 1 -> 1.0D;
            case 2 -> 1.5D;
            case 3 -> 2.25D;
            default -> 3.0D;
        };
        int grabWeightBase = AntarchySettings.portalGunEntityGrabWeightBase();
        boolean modEntityExempt = grabWeightBase == 0
                && Antarchy.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
        return (modEntityExempt || Math.cbrt(volume) < grabWeightBase * strengthScale / 100.0D) && entity.isPickable();
    }

    private static ItemStack getHeldPortalGun(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof PortalGunItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof PortalGunItem) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static boolean isHoldingPortalGun(ServerPlayer player) {
        return !getHeldPortalGun(player).isEmpty();
    }

    private static void release(GrabState state) {
        Entity target = state.target();
        if (target.isAlive() && !target.isRemoved()) {
            target.setNoGravity(state.hadNoGravity());
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    private record GrabState(UUID playerId, ServerLevel level, Entity target, double holdDistance, boolean hadNoGravity) {
    }
}
