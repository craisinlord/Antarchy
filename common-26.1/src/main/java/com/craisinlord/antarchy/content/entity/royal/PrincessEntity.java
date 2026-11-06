package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.anthology.time.TimeDilationApi;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PrincessEntity extends RoyalMountEntity {
    public PrincessEntity(EntityType<? extends PrincessEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RoyalMountEntity.createBaseAttributes(
                AntarchySettings.princessHealth(), AntarchySettings.princessAttackDamage(),
                AntarchySettings.princessMovementSpeed(), AntarchySettings.princessFlyingSpeed(),
                AntarchySettings.princessArmor(), AntarchySettings.princessKnockbackResistance(),
                AntarchySettings.princessFollowRange());
    }

    @Override
    protected RoyalBeamElement boltElement(int lateralIndex) {
        return RoyalBeamElement.GENERIC;
    }

    @Override
    protected Vec3 boltOrigin(int lateralIndex) {
        float scale = this.getGrowthScale();
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        return this.position()
                .add(0.0D, (lateralIndex == 0 ? 0.4D : 0.33D) * scale, 0.0D)
                .add(forward.scale((lateralIndex == 0 ? 0.32D : 0.23D) * scale + this.getBbWidth() * 0.15D))
                .add(side.scale(lateralIndex * 0.24D * scale));
    }

    @Override
    protected int nextBeamHead(int previous) {
        return 1;
    }

    @Override
    protected ParticleOptions beamWindupParticle() {
        return ParticleTypes.REVERSE_PORTAL;
    }

    @Override
    protected float beamBaseDamage() {
        return 10.0F;
    }

    @Override
    public boolean prefersAerialCombat() {
        return this.canFly();
    }

    @Override
    public double preferredCombatRange() {
        return 8.0D + 1.5D * this.getGrowthScale();
    }

    @Override
    protected float biteWeight() {
        return 1.0F;
    }

    @Override
    protected float spitWeight() {
        return 2.5F;
    }

    @Override
    protected float beamWeight() {
        return 1.6F;
    }

    @Override
    protected float signatureWeight() {
        return 2.5F;
    }

    @Override
    protected int signatureCooldownTicks() {
        return 300;
    }

    private double fieldRadius() {
        return 5.0D + 0.8D * this.getGrowthScale();
    }

    private boolean ownerInside(Vec3 center) {
        LivingEntity owner = this.getOwner();
        if (owner == null || owner.level() != this.level() || this.hasPassenger(owner)) {
            return false;
        }
        double clearance = this.fieldRadius() + 1.5D;
        return owner.position().distanceToSqr(center) < clearance * clearance;
    }

    @Override
    protected boolean isSignatureUseful(LivingEntity target) {
        return this.combatGap(target) < 10.0D + 1.5D * this.getGrowthScale() && !this.ownerInside(target.position());
    }

    private Vec3 fieldCenter(@Nullable LivingEntity target, @Nullable Vec3 aimPoint) {
        if (target != null && target.isAlive()) {
            return target.position();
        }
        return aimPoint != null ? aimPoint : this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(this.getBbWidth() + 6.0D));
    }

    @Override
    protected void signatureTelegraph(ServerLevel level, int tick, @Nullable LivingEntity target, @Nullable Vec3 aimPoint) {
        if (tick % 2 != 0) {
            return;
        }
        Vec3 center = this.fieldCenter(target, aimPoint);
        double radius = this.fieldRadius() * (1.0D - tick / 24.0D) + 0.5D;
        for (int i = 0; i < 12; i++) {
            double angle = i * Mth.TWO_PI / 12.0D - tick * 0.15D;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x + Math.cos(angle) * radius, center.y + 0.2D,
                    center.z + Math.sin(angle) * radius, 1, 0.0D, 0.05D, 0.0D, 0.0D);
        }
        Vec3 orb = this.boltOrigin(0);
        level.sendParticles(ParticleTypes.END_ROD, orb.x, orb.y, orb.z, 2, 0.1D, 0.1D, 0.1D, 0.01D);
    }

    @Override
    protected boolean performSignature(ServerLevel level, @Nullable LivingEntity target, @Nullable Vec3 aimPoint, boolean ridden) {
        Vec3 center = this.fieldCenter(target, aimPoint);
        if (this.ownerInside(center)) {
            return false;
        }
        TimeDilationApi.createField(level, center, this.fieldRadius(), 0.35D, 100 + 10 * Mth.floor(this.getGrowthScale()), this.getUUID());
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.5D, center.z, 36, this.fieldRadius() * 0.3D, 0.6D, this.fieldRadius() * 0.3D, 0.03D);
        return true;
    }

    @Override
    protected String geoName() {
        return "princess";
    }

    @Override
    protected EntityType<? extends RoyalBoltEntity> boltType() {
        return AntarchyObjects.ROYAL_BOLT.get();
    }

    @Override
    protected SoundEvent idleSound() {
        return AntarchySoundEvents.PRINCESS_IDLE.get();
    }

    @Override
    protected SoundEvent biteSound() {
        return AntarchySoundEvents.PRINCESS_BITE.get();
    }

    @Override
    protected SoundEvent shootSound() {
        return AntarchySoundEvents.PRINCESS_SHOOT.get();
    }

    @Override
    protected SoundEvent flySound() {
        return AntarchySoundEvents.PRINCESS_FLY.get();
    }

    @Override
    protected SoundEvent stepSound() {
        return AntarchySoundEvents.PRINCESS_STEP.get();
    }
}
