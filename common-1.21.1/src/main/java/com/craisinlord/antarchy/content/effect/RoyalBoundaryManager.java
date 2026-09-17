package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.RoyalGuardianArmorItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Server-authoritative Royal Guardian chestplate field and temporary projectile arrest state. */
public final class RoyalBoundaryManager {
    private static final int ALLY_REFRESH_INTERVAL = 10;
    private static final DustParticleOptions ROYAL_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.12F), 1.15F);
    private static final Map<ServerLevel, Map<UUID, Boundary>> BOUNDARIES = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<UUID, ArrestedProjectile>> ARRESTED = new WeakHashMap<>();

    private RoyalBoundaryManager() {
    }

    public static boolean activate(ServerPlayer owner) {
        if (!(owner.level() instanceof ServerLevel level) || !wearsGuardianChestplate(owner)) return false;
        long now = level.getGameTime();
        Map<UUID, Boundary> boundaries = BOUNDARIES.computeIfAbsent(level, ignored -> new HashMap<>());
        net.minecraft.world.item.Item chestplate = owner.getItemBySlot(EquipmentSlot.CHEST).getItem();
        if (owner.getCooldowns().isOnCooldown(chestplate) || boundaries.containsKey(owner.getUUID())) {
            owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.antarchy.royal_boundary.recharging"), true);
            return false;
        }
        double radius = Math.max(0.5D, AntarchySettings.royalGuardianBoundaryRadius());
        boundaries.put(owner.getUUID(), new Boundary(now + Math.max(1, AntarchySettings.royalGuardianBoundaryDurationTicks()), radius));
        owner.getCooldowns().addCooldown(chestplate, Math.max(0, AntarchySettings.royalGuardianBoundaryCooldownTicks()));
        owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.antarchy.royal_boundary.activated"), true);
        level.playSound(null, owner.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.25F);
        burst(level, center(owner), radius, 36);
        return true;
    }

    /** Called from ServerPlayer.tick; ends fields immediately when their owner can no longer sustain them. */
    public static void tickOwner(ServerPlayer owner) {
        if (!(owner.level() instanceof ServerLevel level)) return;
        UUID ownerId = owner.getUUID();
        for (ServerLevel otherLevel : BOUNDARIES.keySet().toArray(ServerLevel[]::new)) {
            if (otherLevel != level) removeBoundary(otherLevel, ownerId);
        }
        Map<UUID, Boundary> boundaries = BOUNDARIES.get(level);
        if (boundaries == null) return;
        Boundary boundary = boundaries.get(ownerId);
        if (boundary == null) return;
        long now = level.getGameTime();
        if (!owner.isAlive() || owner.isRemoved() || !wearsGuardianChestplate(owner) || now >= boundary.expiresAt) {
            boundaries.remove(ownerId);
            if (boundaries.isEmpty()) BOUNDARIES.remove(level);
            return;
        }
        if (owner.tickCount % ALLY_REFRESH_INTERVAL == 0) {
            AABB area = owner.getBoundingBox().inflate(boundary.radius);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                    living -> living.isAlive() && living.distanceToSqr(owner) <= boundary.radius * boundary.radius && isAlly(owner, living))) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ALLY_REFRESH_INTERVAL + 5,
                        Math.max(0, AntarchySettings.royalGuardianBoundaryStrengthAmplifier()), true, false, true), owner);
            }
        }
        if (owner.tickCount % 5 == 0) renderBoundary(level, center(owner), boundary.radius);
    }

    public static void clearOwner(ServerPlayer owner) {
        UUID ownerId = owner.getUUID();
        for (ServerLevel level : BOUNDARIES.keySet().toArray(ServerLevel[]::new)) removeBoundary(level, ownerId);
    }

    private static void removeBoundary(ServerLevel level, UUID ownerId) {
        Map<UUID, Boundary> boundaries = BOUNDARIES.get(level);
        if (boundaries != null) {
            boundaries.remove(ownerId);
            if (boundaries.isEmpty()) BOUNDARIES.remove(level);
        }
    }

    /** Predicts this tick's movement so a fast projectile is arrested before its own collision pass. */
    public static void prepareProjectile(Projectile projectile) {
        if (!(projectile.level() instanceof ServerLevel level) || projectile.isRemoved()) return;
        Map<UUID, ArrestedProjectile> arrested = ARRESTED.get(level);
        if (arrested != null && arrested.containsKey(projectile.getUUID())) {
            ArrestedProjectile state = arrested.get(projectile.getUUID());
            if (!state.dropping) {
                projectile.setDeltaMovement(Vec3.ZERO);
                projectile.setNoGravity(true);
            }
            return;
        }
        Vec3 start = projectile.position();
        Vec3 movement = projectile.getDeltaMovement();
        if (movement.lengthSqr() > 1.0E-8D) {
            tryCapture(level, projectile, start, start.add(movement));
        }
    }

    /** Swept fallback for unusual projectiles and arrest-state progression. */
    public static void tickProjectile(Projectile projectile) {
        if (!(projectile.level() instanceof ServerLevel level)) return;
        Map<UUID, ArrestedProjectile> arrested = ARRESTED.get(level);
        ArrestedProjectile state = arrested == null ? null : arrested.get(projectile.getUUID());
        if (state != null) {
            tickArrested(level, projectile, state);
            return;
        }
        tryCapture(level, projectile, new Vec3(projectile.xo, projectile.yo, projectile.zo), projectile.position());
    }

    private static void tryCapture(ServerLevel level, Projectile projectile, Vec3 start, Vec3 end) {
        Map<UUID, Boundary> boundaries = BOUNDARIES.get(level);
        if (boundaries == null || boundaries.isEmpty() || projectile.isRemoved()) return;
        var boundaryIterator = boundaries.entrySet().iterator();
        while (boundaryIterator.hasNext()) {
            Map.Entry<UUID, Boundary> entry = boundaryIterator.next();
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (owner == null || owner.level() != level || !owner.isAlive() || !wearsGuardianChestplate(owner)
                    || level.getGameTime() >= entry.getValue().expiresAt) {
                boundaryIterator.remove();
                continue;
            }
            Map<UUID, ArrestedProjectile> states = ARRESTED.computeIfAbsent(level, ignored -> new HashMap<>());
            long ownedArrests = states.values().stream().filter(arrest -> entry.getKey().equals(arrest.ownerId)).count();
            if (ownedArrests >= Math.max(0, AntarchySettings.royalGuardianBoundaryProjectileCap())) continue;
            if (isProjectileAlly(owner, projectile)) continue;
            Vec3 crossing = inwardIntersection(start, end, center(owner), entry.getValue().radius);
            if (crossing == null) continue;
            projectile.setPos(crossing.x, crossing.y, crossing.z);
            projectile.setDeltaMovement(Vec3.ZERO);
            boolean noGravity = projectile.isNoGravity();
            projectile.setNoGravity(true);
            states.put(projectile.getUUID(), new ArrestedProjectile(entry.getKey(),
                    level.getGameTime() + Math.max(0, AntarchySettings.royalGuardianBoundaryArrestTicks()), false, 0L, noGravity));
            level.playSound(null, crossing.x, crossing.y, crossing.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.65F, 1.7F);
            level.sendParticles(ROYAL_GOLD, crossing.x, crossing.y, crossing.z, 18, 0.3D, 0.3D, 0.3D, 0.3D);
            return;
        }
        if (boundaries.isEmpty()) BOUNDARIES.remove(level);
    }

    private static void tickArrested(ServerLevel level, Projectile projectile, ArrestedProjectile state) {
        if (projectile.isRemoved()) {
            removeArrest(level, projectile.getUUID());
            return;
        }
        long now = level.getGameTime();
        if (!state.dropping && now < state.releaseAt) {
            projectile.setDeltaMovement(Vec3.ZERO);
            projectile.setNoGravity(true);
            if (now % 2 == 0) {
                level.sendParticles(ROYAL_GOLD, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.12D, 0.12D, 0.12D, 0.02D);
            }
            if (state.releaseAt - now <= 6) projectile.setXRot((float) (90.0D * (1.0D - (state.releaseAt - now) / 6.0D)));
            return;
        }
        if (!state.dropping) {
            state.dropping = true;
            state.dropUntil = now + Math.max(0, AntarchySettings.royalGuardianBoundaryDropTicks());
            projectile.setNoGravity(false);
            projectile.setXRot(90.0F);
        }
        if (now < state.dropUntil) {
            projectile.setDeltaMovement(0.0D, -0.45D, 0.0D);
            projectile.setNoGravity(false);
        } else {
            projectile.setNoGravity(state.wasNoGravity);
            removeArrest(level, projectile.getUUID());
        }
    }

    private static void removeArrest(ServerLevel level, UUID projectileId) {
        Map<UUID, ArrestedProjectile> states = ARRESTED.get(level);
        if (states != null) {
            states.remove(projectileId);
            if (states.isEmpty()) ARRESTED.remove(level);
        }
    }

    private static Vec3 inwardIntersection(Vec3 start, Vec3 end, Vec3 center, double radius) {
        Vec3 offset = start.subtract(center);
        if (offset.lengthSqr() <= radius * radius) return null;
        Vec3 segment = end.subtract(start);
        double a = segment.lengthSqr();
        if (a < 1.0E-8D) return null;
        double b = 2.0D * offset.dot(segment);
        double c = offset.lengthSqr() - radius * radius;
        double discriminant = b * b - 4.0D * a * c;
        if (discriminant < 0.0D) return null;
        double t = (-b - Math.sqrt(discriminant)) / (2.0D * a);
        return t >= 0.0D && t <= 1.0D ? start.add(segment.scale(t)) : null;
    }

    private static boolean isProjectileAlly(ServerPlayer owner, Projectile projectile) {
        Entity shooter = projectile.getOwner();
        return shooter == owner || (shooter != null && (shooter.isAlliedTo(owner) || owner.isAlliedTo(shooter)
                || (shooter instanceof CommandedEntityAccess access && owner.getUUID().equals(access.antarchy$getCommanderUuid()))));
    }

    private static boolean isAlly(ServerPlayer owner, LivingEntity entity) {
        if (entity == owner || entity.isAlliedTo(owner) || owner.isAlliedTo(entity)) return true;
        if (entity instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID())) return true;
        return entity instanceof CommandedEntityAccess access && owner.getUUID().equals(access.antarchy$getCommanderUuid());
    }

    private static boolean wearsGuardianChestplate(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RoyalGuardianArmorItem armor
                && armor.getArmorType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE;
    }

    private static Vec3 center(ServerPlayer owner) {
        return owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);
    }

    private static void renderBoundary(ServerLevel level, Vec3 center, double radius) {
        for (int i = 0; i < 24; i++) {
            double angle = (Math.PI * 2.0D * i) / 24.0D;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(ROYAL_GOLD, x, center.y - 1.0D, z, 1, 0.04D, 0.1D, 0.04D, 0.02D);
            if (i % 4 == 0) level.sendParticles(ParticleTypes.END_ROD, x, center.y + 1.2D, z, 1, 0.02D, 0.3D, 0.02D, 0.0D);
        }
    }

    private static void burst(ServerLevel level, Vec3 center, double radius, int count) {
        level.sendParticles(ROYAL_GOLD, center.x, center.y, center.z, count, radius * 0.35D, 1.2D, radius * 0.35D, 0.18D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, count / 2, radius * 0.25D, 0.8D, radius * 0.25D, 0.08D);
    }

    private static final class Boundary {
        private final long expiresAt;
        private final double radius;
        private Boundary(long expiresAt, double radius) {
            this.expiresAt = expiresAt;
            this.radius = radius;
        }
    }

    private static final class ArrestedProjectile {
        private final UUID ownerId;
        private final long releaseAt;
        private boolean dropping;
        private long dropUntil;
        private final boolean wasNoGravity;

        private ArrestedProjectile(UUID ownerId, long releaseAt, boolean dropping, long dropUntil, boolean wasNoGravity) {
            this.ownerId = ownerId;
            this.releaseAt = releaseAt;
            this.dropping = dropping;
            this.dropUntil = dropUntil;
            this.wasNoGravity = wasNoGravity;
        }
    }
}
