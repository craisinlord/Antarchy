package com.craisinlord.antarchy.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;
import java.util.UUID;
import com.craisinlord.anthology.portal.client.PortalClientHooks;
import com.craisinlord.anthology.portal.PortalSettings;
import com.craisinlord.anthology.portal.PortalHooks;
import com.craisinlord.anthology.portal.PortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.client.renderer.PortalGunMoonViewRenderer;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.anthology.shake.client.CameraShake;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.craisinlord.antarchy.content.item.TemporalTunerAccess;
import com.craisinlord.antarchy.content.item.TemporalTunerItem;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.craisinlord.anthology.effect.AnthologyEffects;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityHooks;
import com.craisinlord.anthology.time.TimeDilationApi;
import com.craisinlord.anthology.time.TimeDilationFieldEntity;
import com.craisinlord.anthology.time.TimeDilationHooks;
import com.craisinlord.anthology.time.TimeDilationMath;
import com.craisinlord.anthology.time.client.ContractionAfterimages;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class AntarchyAnthologyHooks {
    private AntarchyAnthologyHooks() {
    }

    public static void register() {
        GravityHooks.addInvertedRegion(ThoraxisUndersideManager::isInUnderside);
        GravityHooks.addInvertedFluid(AntarchyFluidChecks::isAntiwater);
        GravityHooks.addLoadListener(AntarchyAnthologyHooks::clearStaleUndersideInversion);
        TimeDilationHooks.addTrackedPredicate(entity -> entity instanceof Player player && TemporalTunerItem.isAvailable(player));
        TimeDilationHooks.addRateSource(AntarchyAnthologyHooks::temporalTunerRate);
        TimeDilationHooks.addFieldExclusion(AntarchyAnthologyHooks::excludedFromField);
        TimeDilationHooks.addChronosphereLevelSource(AntarchyEnchantments::chronosphereLevel);
        PortalHooks.setSettings(new AntarchyPortalSettings());
        PortalHooks.addTransitExclusion(entity -> entity instanceof PortalGunProjectileEntity);
    }

    public static void registerClient() {
        PortalClientHooks.registerCustomView(portal -> portal instanceof PortalGunPortalEntity gunPortal && gunPortal.isMoonPortal(), PortalGunMoonViewRenderer::render);
        PortalClientHooks.setOutlinePolicy(AntarchyAnthologyHooks::outlinesPortal);
        CameraShake.addStrengthSource(cameraPos -> HerculesBeetleImpactShakeClientState.getTicks() / 25.0F * 2.0F);
        CameraShake.addStrengthSource(cameraPos -> BigBerthaItem.clientShakeTicks / 25.0F * 2.0F);
        ContractionAfterimages.addActivator(living -> living instanceof Player player
                && TemporalTunerItem.isAvailable(player)
                && TimeDilationApi.getRate(living) > 1.0D);
    }

    private static boolean outlinesPortal(PortalEntity portal, LocalPlayer viewer) {
        if (!(portal instanceof PortalGunPortalEntity gunPortal) || gunPortal.getOwnerId() == null) {
            return false;
        }
        UUID owner = gunPortal.getOwnerId();
        return owner.equals(viewer.getUUID())
                || owner.equals(heldGunOwner(viewer.getMainHandItem(), viewer.getUUID()))
                || owner.equals(heldGunOwner(viewer.getOffhandItem(), viewer.getUUID()));
    }

    private static UUID heldGunOwner(ItemStack stack, UUID fallback) {
        return stack.getItem() instanceof PortalGunItem ? PortalGunItem.getPortalOwnerId(stack, fallback) : null;
    }

    private static final class AntarchyPortalSettings implements PortalSettings {
        @Override
        public boolean seeThroughEnabled() {
            return AntarchySettings.portalGunSeeThroughPortals();
        }

        @Override
        public int maxLiveViews() {
            return AntarchySettings.portalGunMaxLiveViews();
        }

        @Override
        public double minFloorExitSpeed() {
            return AntarchySettings.portalGunMinFloorExitSpeed();
        }

        @Override
        public double maxExitSpeed() {
            return AntarchySettings.portalGunMaxExitSpeed();
        }

        @Override
        public boolean fancySurfaces() {
            return AntarchySettings.portalGunFancyPortals();
        }
    }

    private static double temporalTunerRate(Entity entity) {
        if (!(entity instanceof Player player) || !(player instanceof TemporalTunerAccess access)) {
            return TimeDilationMath.NORMAL_RATE;
        }
        if (TemporalTunerItem.isAvailable(player)) {
            return TemporalTunerItem.getRate(player);
        }
        access.antarchy$setTemporalTunerRate(TimeDilationMath.NORMAL_RATE);
        TemporalTunerItem.syncTooltipRate(player, TimeDilationMath.NORMAL_RATE);
        return TimeDilationMath.NORMAL_RATE;
    }

    private static boolean excludedFromField(TimeDilationFieldEntity field, Entity entity) {
        if (!field.isOwnerAnchored() && entity instanceof RoyalBlackHoleEntity) {
            return true;
        }
        if (!(field.owner() instanceof QueenEntity)) {
            return false;
        }
        if (entity instanceof LivingEntity living && !RoyalEffectEligibility.canApplyDilated(living)) {
            return true;
        }
        return !field.isOwnerAnchored() && entity.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK);
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
