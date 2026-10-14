package com.craisinlord.antarchy.content.entity.royal.attack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.util.RandomSource;

public final class RoyalAttackScheduler {
    public interface Action {
        default void onStart() {}
        default void onWindup(int elapsedTicks, int remainingTicks) {}
        default void onActive(int elapsedTicks) {}
        default void onComplete() {}
    }

    private static final class RunningAction {
        private final Action action;
        private final int windupTicks;
        private final int activeTicks;
        private final int recoveryTicks;
        private int elapsedTicks;

        private RunningAction(Action action, int windupTicks, int activeTicks, int recoveryTicks) {
            this.action = action;
            this.windupTicks = Math.max(0, windupTicks);
            this.activeTicks = Math.max(1, activeTicks);
            this.recoveryTicks = Math.max(0, recoveryTicks);
        }

        private int totalTicks() {
            return this.windupTicks + this.activeTicks + this.recoveryTicks;
        }
    }
    private final Map<String, Integer> cooldowns = new HashMap<>();
    private final Map<RoyalAttackLane, RunningAction> active = new EnumMap<>(RoyalAttackLane.class);
    private final Deque<String> recent = new ArrayDeque<>();

    public void tick() {
        cooldowns.replaceAll((key, value) -> Math.max(0, value - 1));
        for (var iterator = active.entrySet().iterator(); iterator.hasNext();) {
            RunningAction running = iterator.next().getValue();
            running.elapsedTicks++;
            int activeStart = running.windupTicks;
            int activeEnd = activeStart + running.activeTicks;
            if (running.elapsedTicks < activeStart) {
                running.action.onWindup(running.elapsedTicks, activeStart - running.elapsedTicks);
            }
            if (running.elapsedTicks >= activeStart && running.elapsedTicks < activeEnd) {
                running.action.onActive(running.elapsedTicks - activeStart);
            }
            if (running.elapsedTicks >= running.totalTicks()) {
                iterator.remove();
                running.action.onComplete();
            }
        }
    }

    public boolean ready(String id, RoyalAttackLane lane) {
        return cooldowns.getOrDefault(id, 0) <= 0 && !active.containsKey(lane);
    }

    public boolean laneBusy(RoyalAttackLane lane) {
        return active.containsKey(lane);
    }

    public boolean start(String id, RoyalAttackLane lane, int cooldownTicks, int activeTicks) {
        return start(id, lane, cooldownTicks, 0, activeTicks, 0, new Action() {});
    }

    public boolean start(String id, RoyalAttackLane lane, int cooldownTicks, int windupTicks,
                         int activeTicks, int recoveryTicks, Action action) {
        if (!ready(id, lane)) {
            return false;
        }
        cooldowns.put(id, Math.max(1, cooldownTicks));
        active.put(lane, new RunningAction(action, windupTicks, activeTicks, recoveryTicks));
        action.onStart();
        recent.remove(id);
        recent.addFirst(id);
        while (recent.size() > 8) {
            recent.removeLast();
        }
        return true;
    }

    public int repetitionPenalty(String id) {
        int penalty = 0;
        for (String previous : recent) {
            if (previous.equals(id)) {
                penalty += 3;
            }
        }
        return penalty;
    }

    public String chooseWeighted(String[] ids, ToIntFunction<String> weightFunction, RandomSource random) {
        int total = 0;
        int[] weights = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            weights[i] = Math.max(0, weightFunction.applyAsInt(ids[i]) - repetitionPenalty(ids[i]));
            total += weights[i];
        }
        if (total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < ids.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return ids[i];
            }
        }
        return null;
    }

    public int cooldown(String id) {
        return cooldowns.getOrDefault(id, 0);
    }

    public void resetCooldown(String id) {
        cooldowns.remove(id);
    }

    public void cancel(RoyalAttackLane lane) {
        active.remove(lane);
    }
}
