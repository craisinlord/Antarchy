package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.ARGB;

public class PrinceEntity extends RoyalMountEntity {
    private static final DustParticleOptions COMMAND_DUST = new DustParticleOptions(ARGB.colorFromFloat(1.0F, 1.0F, 0.75F, 0.15F), 1.6F);

    public PrinceEntity(EntityType<? extends PrinceEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RoyalMountEntity.createBaseAttributes(
                AntarchySettings.princeHealth(), AntarchySettings.princeAttackDamage(),
                AntarchySettings.princeMovementSpeed(), AntarchySettings.princeFlyingSpeed(),
                AntarchySettings.princeArmor(), AntarchySettings.princeKnockbackResistance(),
                AntarchySettings.princeFollowRange());
    }

    @Override
    protected RoyalBeamElement boltElement(int lateralIndex) {
        return switch (lateralIndex) {
            case -1 -> RoyalBeamElement.FIRE;
            case 1 -> RoyalBeamElement.ICE;
            default -> RoyalBeamElement.LIGHTNING;
        };
    }

    @Override
    public RoyalBeamElement getBeamElement() {
        return switch (this.getBeamHead()) {
            case 0 -> RoyalBeamElement.FIRE;
            case 2 -> RoyalBeamElement.ICE;
            default -> RoyalBeamElement.LIGHTNING;
        };
    }

    @Override
    protected ParticleOptions beamWindupParticle() {
        return switch (this.getBeamElement()) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            default -> ParticleTypes.ELECTRIC_SPARK;
        };
    }

    @Override
    protected float beamBaseDamage() {
        return 12.0F;
    }

    @Override
    public boolean prefersAerialCombat() {
        return false;
    }

    @Override
    public double preferredCombatRange() {
        return 0.0D;
    }

    @Override
    protected float biteWeight() {
        return 3.0F;
    }

    @Override
    protected float spitWeight() {
        return 1.0F;
    }

    @Override
    protected float beamWeight() {
        return 1.2F;
    }

    @Override
    protected float signatureWeight() {
        return 4.0F;
    }

    @Override
    protected int signatureCooldownTicks() {
        return 360;
    }

    private double commandRadius() {
        return 14.0D + 2.0D * this.getGrowthScale();
    }

    private int commandLimit() {
        return 3 + Mth.floor(this.getGrowthScale());
    }

    private List<Mob> commandCandidates(Vec3 center) {
        double radius = this.commandRadius();
        return this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(radius), mob ->
                mob != this && mob.isAlive() && mob instanceof Enemy && !mob.isAlliedTo(this)
                        && !(mob instanceof RoyalBossEntity) && RoyalEffectEligibility.canApplyCommanded(mob)
                        && RoyalEffectHooks.commandedHolder() != null
                        && !mob.hasEffect(RoyalEffectHooks.commandedHolder())
                        && mob.distanceToSqr(center) <= radius * radius);
    }

    @Override
    protected boolean isSignatureUseful(LivingEntity target) {
        return RoyalEffectHooks.commandedHolder() != null && this.commandCandidates(this.position()).size() >= 2;
    }

    @Override
    protected void signatureTelegraph(ServerLevel level, int tick, @Nullable LivingEntity target, @Nullable Vec3 aimPoint) {
        if (tick % 3 != 0) {
            return;
        }
        double radius = (this.getBbWidth() * 0.5D + 1.0D) + tick * 0.25D * this.getGrowthScale();
        for (int i = 0; i < 16; i++) {
            double angle = i * Mth.TWO_PI / 16.0D + tick * 0.1D;
            level.sendParticles(COMMAND_DUST, this.getX() + Math.cos(angle) * radius, this.getY() + 0.3D,
                    this.getZ() + Math.sin(angle) * radius, 1, 0.0D, 0.05D, 0.0D, 0.0D);
        }
        if (tick == 3) {
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + this.getBbHeight(), this.getZ(),
                    20, this.getBbWidth() * 0.3D, 0.3D, this.getBbWidth() * 0.3D, 0.1D);
        }
    }

    @Override
    protected boolean performSignature(ServerLevel level, @Nullable LivingEntity target, @Nullable Vec3 aimPoint, boolean ridden) {
        if (RoyalEffectHooks.commandedHolder() == null) {
            return false;
        }
        Vec3 center = this.position();
        List<Mob> candidates = this.commandCandidates(center);
        candidates.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(center)));
        int applied = 0;
        for (Mob mob : candidates) {
            if (applied >= this.commandLimit()) {
                break;
            }
            if (mob.addEffect(new MobEffectInstance(RoyalEffectHooks.commandedHolder(), 300 + 20 * Mth.floor(this.getGrowthScale())), this)) {
                applied++;
                level.sendParticles(COMMAND_DUST, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3D, mob.getZ(), 8, 0.25D, 0.2D, 0.25D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        return applied > 0;
    }

    @Override
    protected String geoName() {
        return "prince";
    }

    @Override
    protected EntityType<? extends RoyalBoltEntity> boltType() {
        return AntarchyObjects.ROYAL_BOLT.get();
    }

    @Override
    protected SoundEvent idleSound() {
        return AntarchySoundEvents.PRINCE_IDLE.get();
    }

    @Override
    protected SoundEvent biteSound() {
        return AntarchySoundEvents.PRINCE_BITE.get();
    }

    @Override
    protected SoundEvent shootSound() {
        return AntarchySoundEvents.PRINCE_SHOOT.get();
    }

    @Override
    protected SoundEvent flySound() {
        return AntarchySoundEvents.PRINCE_FLY.get();
    }

    @Override
    protected SoundEvent stepSound() {
        return AntarchySoundEvents.PRINCE_STEP.get();
    }
}
