package com.craisinlord.antarchy.content.movement;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.PortalGunRollClientState;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.craisinlord.anthology.effect.AnthologyEffects;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class AntarchyGravityHooks {
    private AntarchyGravityHooks() {
    }

    public static void register() {
        GravityHooks.addInvertedRegion(ThoraxisUndersideManager::isInUnderside);
        GravityHooks.addInvertedFluid(AntarchyFluidChecks::isAntiwater);
        GravityHooks.addLoadListener(AntarchyGravityHooks::clearStaleUndersideInversion);
    }

    public static void registerClient() {
        GravityHooks.addCameraRollSource(PortalGunRollClientState::getRollDegrees);
    }

    private static void clearStaleUndersideInversion(Entity entity) {
        if (!(entity instanceof Mob mob)
                || !GravityApi.isGravityInverted(mob)
                || !GravityApi.isGravityForced(mob)) {
            return;
        }
        MobEffectInstance invertedEffect = mob.getEffect(AnthologyEffects.inverted());
        boolean aboveUndersideExit = ThoraxisUndersideManager.isAboveUndersideExit(mob);
        int nearbyDimensionalTears = countNearbyDimensionalTears(mob);
        if (invertedEffect != null
                && invertedEffect.isAmbient()
                && !invertedEffect.isVisible()
                && aboveUndersideExit) {
            clearInversion(mob);
            Antarchy.LOGGER.warn(
                    "[antarchy-gravity] cleared stale underside inversion from loaded mob type={} uuid={} pos=({}, {}, {}) dim={} effectDuration={} nearbyDimensionalTears={}",
                    mob.getType(), mob.getUUID(), mob.getX(), mob.getY(), mob.getZ(),
                    mob.level() != null ? mob.level().dimension().location() : "?",
                    invertedEffect.getDuration(), nearbyDimensionalTears
            );
            return;
        }
        if (invertedEffect != null
                && mob instanceof BedBugEntity
                && aboveUndersideExit
                && ThoraxisUndersideManager.isThoraxis(mob.level())) {
            clearInversion(mob);
            Antarchy.LOGGER.warn(
                    "[antarchy-gravity] cleared loaded inverted bed bug above underside exit type={} uuid={} pos=({}, {}, {}) dim={} effectDuration={} effectAmbient={} effectVisible={} nearbyDimensionalTears={}",
                    mob.getType(), mob.getUUID(), mob.getX(), mob.getY(), mob.getZ(),
                    mob.level() != null ? mob.level().dimension().location() : "?",
                    invertedEffect.getDuration(), invertedEffect.isAmbient(), invertedEffect.isVisible(), nearbyDimensionalTears
            );
            return;
        }
        Antarchy.LOGGER.info(
                "[antarchy-gravity] loaded inverted mob type={} uuid={} pos=({}, {}, {}) dim={} hasInvertedEffect={} effectDuration={} effectAmbient={} effectVisible={} aboveUndersideExit={} nearbyDimensionalTears={}",
                mob.getType(), mob.getUUID(), mob.getX(), mob.getY(), mob.getZ(),
                mob.level() != null ? mob.level().dimension().location() : "?",
                invertedEffect != null,
                invertedEffect != null ? invertedEffect.getDuration() : -1,
                invertedEffect != null && invertedEffect.isAmbient(),
                invertedEffect != null && invertedEffect.isVisible(),
                aboveUndersideExit,
                nearbyDimensionalTears
        );
    }

    private static void clearInversion(Mob mob) {
        mob.removeEffect(AnthologyEffects.inverted());
        GravityApi.resetGravityState(mob);
    }

    private static int countNearbyDimensionalTears(Mob mob) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return -1;
        }
        return serverLevel.getEntitiesOfClass(
                DimensionalTearEntity.class,
                mob.getBoundingBox().inflate(48.0D),
                Entity::isAlive
        ).size();
    }
}
