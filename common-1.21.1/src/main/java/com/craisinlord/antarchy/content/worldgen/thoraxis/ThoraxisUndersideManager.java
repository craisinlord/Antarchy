package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ThoraxisUndersideManager {
    public static final int GRAVITY_FLIP_Y = 0;
    private static final ResourceLocation THORAXIS_DIMENSION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");
    private static final AntarchyGravityTransition TRANSITION = new AntarchyGravityTransition(12);
    private static final Set<UUID> UNDERSIDE_FORCED_GRAVITY = new HashSet<>();

    private ThoraxisUndersideManager() {
    }

    public static void tick(ServerLevel level) {
        if (!isThoraxis(level)) {
            return;
        }

        Set<UUID> activeThisTick = new HashSet<>();
        for (Entity entity : level.getAllEntities()) {
            if (!entity.isAlive() || entity.isSpectator()) {
                continue;
            }

            UUID uuid = entity.getUUID();
            if (entity.getY() < GRAVITY_FLIP_Y) {
                activeThisTick.add(uuid);
                UNDERSIDE_FORCED_GRAVITY.add(uuid);
                if (AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP
                        || !AntarchyGravityApi.isGravityForced(entity)) {
                    AntarchyGravityApi.setForcedGravityDirection(entity, AntarchyGravityDirection.UP, TRANSITION);
                }
                continue;
            }

            if (!UNDERSIDE_FORCED_GRAVITY.remove(uuid)) {
                continue;
            }

            if (shouldReleaseGravity(entity)) {
                AntarchyGravityApi.setGravityDirection(entity, AntarchyGravityDirection.DOWN, false, TRANSITION);
            }
        }

        UNDERSIDE_FORCED_GRAVITY.retainAll(activeThisTick);
    }

    private static boolean shouldReleaseGravity(Entity entity) {
        if (AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP) {
            return false;
        }

        if (entity instanceof LivingEntity living && living.hasEffect(AntarchyObjects.INVERTED_EFFECT.get())) {
            return false;
        }

        return AntarchyGravityApi.isGravityForced(entity);
    }

    public static boolean isThoraxis(ServerLevel level) {
        return level.dimension().location().equals(THORAXIS_DIMENSION);
    }
}
