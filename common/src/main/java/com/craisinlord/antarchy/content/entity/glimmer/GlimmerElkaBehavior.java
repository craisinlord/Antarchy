package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GlimmerElkaBehavior implements GlimmerVariantBehavior {
    private static final int PASSIVE_REFRESH_DURATION = 20 * 4;
    private static final int WARD_AMPLIFIER = 1;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 60;
    private static final int CRY_ANIM_TICKS = 40;
    private static final double SHOCKWAVE_RADIUS = 6.0D;
    private static final double SHOCKWAVE_KNOCKBACK = 1.3D;
    private static final float SHOCKWAVE_DAMAGE = 8.0F;

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.0D));
        entity.addGoal(6, new LookAtPlayerGoal(entity, Player.class, 8.0F));
        entity.addGoal(7, new RandomLookAroundGoal(entity));
    }

    @Override
    public ResourceLocation modelGeo() {
        return rl("geo/elka.geo.json");
    }

    @Override
    public ResourceLocation animationFile() {
        return rl("animations/elka.animation.json");
    }

    @Override
    public ResourceLocation texture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/elka_glimmer.png");
    }

    @Override
    public ResourceLocation emissiveTexture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/elka_glimmer_emissive.png");
    }

    @Override
    public SoundEvent ambientSound() {
        return SoundEvents.PANDA_AMBIENT;
    }

    @Override
    public SoundEvent hurtSound() {
        return SoundEvents.PANDA_BITE;
    }

    @Override
    public SoundEvent deathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Override
    public EntityDimensions adultDimensions() {
        return EntityDimensions.scalable(2.2F, 3.5F);
    }

    @Override
    public EntityDimensions babyDimensions() {
        return EntityDimensions.scalable(1.0F, 1.5F);
    }

    @Override
    public void tickPassive(GlimmerEntity entity, Player owner) {
        owner.addEffect(new MobEffectInstance(AntarchyObjects.BLOODGLASS_WARD.get(), PASSIVE_REFRESH_DURATION, WARD_AMPLIFIER, false, false, true));
    }

    @Override
    public void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
        if (!(owner instanceof BloodglassAccess access)) {
            return;
        }

        long lostNow = access.antarchy$getArmorShieldLostCount() + access.antarchy$getAppleShieldLostCount();
        long lastLost = entity.getLastOwnerShieldLostCount();
        if (lastLost >= 0L && lostNow > lastLost && !entity.isAbilityOnCooldown()) {
            this.shatterWard(entity, owner);
        }
        entity.setLastOwnerShieldLostCount(lostNow);
    }

    @Override
    public void clearPassive(GlimmerEntity entity, Player owner) {
        entity.setLastOwnerShieldLostCount(-1L);
    }

    @Override
    public int abilityCooldownTicks() {
        return ABILITY_COOLDOWN_TICKS;
    }

    @Override
    @Nullable
    public software.bernie.geckolib.animation.RawAnimation abilityAnimation() {
        return software.bernie.geckolib.animation.RawAnimation.begin().thenPlay("cry");
    }

    private void shatterWard(GlimmerEntity entity, Player owner) {
        entity.playAbilityAnimation(CRY_ANIM_TICKS);
        entity.playSound(SoundEvents.PANDA_CANT_BREED, 1.2F, 0.7F);
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, entity.getSoundSource(), 1.0F, 0.6F);

        AABB area = owner.getBoundingBox().inflate(SHOCKWAVE_RADIUS);
        for (Entity nearby : owner.level().getEntities(owner, area)) {
            if (nearby instanceof Projectile projectile) {
                Vec3 away = projectile.position().subtract(owner.position()).normalize();
                projectile.setDeltaMovement(away.scale(1.5D));
                continue;
            }

            if (nearby instanceof LivingEntity living && (nearby instanceof Enemy || living.getLastHurtByMob() == owner)) {
                if (nearby instanceof Enemy) {
                    living.hurt(owner.damageSources().mobAttack(entity), SHOCKWAVE_DAMAGE);
                }
                Vec3 away = living.position().subtract(owner.position());
                double horizontalDistance = Math.max(0.1D, away.horizontalDistance());
                living.knockback(SHOCKWAVE_KNOCKBACK, -away.x / horizontalDistance, -away.z / horizontalDistance);
            }
        }

        this.sendAbilityMessage(owner, Component.translatable("entity.antarchy.glimmer.ability.elka"));
        entity.startAbilityCooldown();
    }
}
