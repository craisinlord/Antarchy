package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class MinersDreamExcavationManager {
    private static final int MAX_ACTIVE_TASKS = 8;
    private static final int SAFE_MIN_RANGE = 8;
    private static final int SAFE_MAX_RANGE = 256;
    private static final Map<UUID, MinersDreamExcavation> ACTIVE = new ConcurrentHashMap<>();

    private MinersDreamExcavationManager() {
    }

    public static boolean hasActiveExcavation(UUID owner) {
        return ACTIVE.containsKey(owner);
    }

    public static MinersDreamExcavation tryStart(ServerLevel level, UUID owner, BlockPos startPos, Direction direction) {
        if (ACTIVE.containsKey(owner) || ACTIVE.size() >= MAX_ACTIVE_TASKS) {
            return null;
        }

        int minRange = Mth.clamp(AntarchySettings.minersDreamMinimumRange(), SAFE_MIN_RANGE, SAFE_MAX_RANGE);
        int maxRange = Mth.clamp(AntarchySettings.minersDreamMaximumRange(), SAFE_MIN_RANGE, SAFE_MAX_RANGE);
        if (minRange > maxRange) {
            int swap = minRange;
            minRange = maxRange;
            maxRange = swap;
        }

        RandomSource random = level.getRandom();
        int length = minRange == maxRange ? minRange : minRange + random.nextInt(maxRange - minRange + 1);

        MinersDreamCaveGenerator.CaveGenerationResult result = MinersDreamCaveGenerator.generate(
                startPos,
                direction,
                length,
                AntarchySettings.minersDreamTorchSpacing(),
                level.getMinBuildHeight(),
                level.getMaxBuildHeight(),
                random
        );

        if (result.nodes().isEmpty()) {
            return null;
        }

        MinersDreamExcavation excavation = new MinersDreamExcavation(owner, level.dimension(), result.nodes(), result.torchCheckpoints());
        ACTIVE.put(owner, excavation);
        return excavation;
    }

    public static void tick(ServerLevel level) {
        if (ACTIVE.isEmpty()) {
            return;
        }

        int budget = Math.max(1, AntarchySettings.minersDreamBlocksPerTick());
        Iterator<Map.Entry<UUID, MinersDreamExcavation>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            MinersDreamExcavation excavation = iterator.next().getValue();
            if (!excavation.dimension().equals(level.dimension())) {
                continue;
            }

            excavation.tick(level, budget);
            if (excavation.isFinished()) {
                iterator.remove();
            }
        }
    }
}
