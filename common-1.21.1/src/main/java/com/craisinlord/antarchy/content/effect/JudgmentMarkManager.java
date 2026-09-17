package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.RoyalGuardianArmorItem;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Server-owned, player-specific Judgment marks. The visible bit is synchronized by vanilla entity data. */
public final class JudgmentMarkManager {
    private static final Map<ServerLevel, MarkBook> MARKS = new WeakHashMap<>();

    private JudgmentMarkManager() {
    }

    /** Returns true when this owner had already marked this target before the current hit. */
    public static boolean recordSuccessfulHit(Player owner, LivingEntity target) {
        if (!(owner instanceof ServerPlayer serverPlayer) || !(target.level() instanceof ServerLevel level)
                || owner.level() != target.level() || !hasGuardianArmor(owner) || !target.isAlive()) {
            return false;
        }

        MarkBook book = MARKS.computeIfAbsent(level, ignored -> new MarkBook());
        UUID ownerId = owner.getUUID();
        Mark current = book.byOwner.get(ownerId);
        boolean wasMarked = current != null && current.targetId.equals(target.getUUID())
                && current.expiresAt > level.getGameTime();
        if (current != null && !current.targetId.equals(target.getUUID())) {
            removeOwnerFromTarget(book, ownerId, current.targetId);
            refreshVisibleState(level, current.targetId);
        }
        book.byOwner.put(ownerId, new Mark(target.getUUID(), level.getGameTime()
                + Math.max(1, AntarchySettings.royalGuardianJudgmentDurationTicks())));
        book.ownersByTarget.computeIfAbsent(target.getUUID(), ignored -> new HashSet<>()).add(ownerId);
        refreshVisibleState(target);
        return wasMarked;
    }

    public static boolean isMarkedBy(Player owner, LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) {
            return false;
        }
        MarkBook book = MARKS.get(level);
        if (book == null) {
            return false;
        }
        Mark mark = book.byOwner.get(owner.getUUID());
        return mark != null && mark.targetId.equals(target.getUUID()) && mark.expiresAt > level.getGameTime()
                && hasGuardianArmor(owner);
    }

    public static LivingEntity markedTarget(Player owner) {
        if (!(owner instanceof ServerPlayer serverPlayer) || !(owner.level() instanceof ServerLevel level)) return null;
        MarkBook book = MARKS.get(level);
        Mark mark = book == null ? null : book.byOwner.get(owner.getUUID());
        if (mark == null || mark.expiresAt <= level.getGameTime() || !hasGuardianArmor(owner)) return null;
        net.minecraft.world.entity.Entity entity = level.getEntity(mark.targetId);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    /** Prunes marks from this target and updates the vanilla-synchronized render bit. */
    public static void tickTarget(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }
        MarkBook book = MARKS.get(level);
        if (book == null || book.byOwner.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        UUID targetId = target.getUUID();
        Set<UUID> ownerIds = book.ownersByTarget.get(targetId);
        if (ownerIds == null || ownerIds.isEmpty()) {
            if (target instanceof JudgmentMarkAccess access) access.antarchy$setJudgmentMarked(false);
            return;
        }
        for (UUID ownerId : Set.copyOf(ownerIds)) {
            Mark mark = book.byOwner.get(ownerId);
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            if (mark == null || mark.expiresAt <= now || owner == null || owner.level() != level || !hasGuardianArmor(owner)) {
                book.byOwner.remove(ownerId);
                ownerIds.remove(ownerId);
            }
        }
        if (ownerIds.isEmpty()) book.ownersByTarget.remove(targetId);
        boolean active = !ownerIds.isEmpty();
        if (target instanceof JudgmentMarkAccess access) {
            access.antarchy$setJudgmentMarked(active);
        }
        if (book.byOwner.isEmpty()) {
            MARKS.remove(level);
        }
    }

    private static void refreshVisibleState(LivingEntity target) {
        if (target instanceof JudgmentMarkAccess access) {
            access.antarchy$setJudgmentMarked(true);
        }
    }

    private static void refreshVisibleState(ServerLevel level, UUID targetId) {
        net.minecraft.world.entity.Entity entity = level.getEntity(targetId);
        if (entity instanceof LivingEntity target && target instanceof JudgmentMarkAccess access) {
            MarkBook book = MARKS.get(level);
            boolean stillMarked = book != null && !book.ownersByTarget.getOrDefault(targetId, Set.of()).isEmpty();
            access.antarchy$setJudgmentMarked(stillMarked);
        }
    }

    private static void removeOwnerFromTarget(MarkBook book, UUID ownerId, UUID targetId) {
        Set<UUID> owners = book.ownersByTarget.get(targetId);
        if (owners != null) {
            owners.remove(ownerId);
            if (owners.isEmpty()) book.ownersByTarget.remove(targetId);
        }
    }

    private static boolean hasGuardianArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof RoyalGuardianArmorItem
                || player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RoyalGuardianArmorItem
                || player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof RoyalGuardianArmorItem
                || player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof RoyalGuardianArmorItem;
    }

    private record Mark(UUID targetId, long expiresAt) {
    }

    private static final class MarkBook {
        private final Map<UUID, Mark> byOwner = new HashMap<>();
        private final Map<UUID, Set<UUID>> ownersByTarget = new HashMap<>();
    }
}
