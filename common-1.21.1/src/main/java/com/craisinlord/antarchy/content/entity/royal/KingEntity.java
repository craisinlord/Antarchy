package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.decree.AdvanceDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.CloseQuartersDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.KeepYourDistanceDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.NoRetreatDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.RoyalDecree;
import java.util.List;

public class KingEntity extends RoyalBossEntity {
    @Nullable
    private Vec3 patrolCenter;
    private int patrolCooldownTicks;
    private int decreeCooldownTicks;
    private int fireAttackCooldownTicks;
    private int lightningAttackCooldownTicks;
    private RoyalDecree activeDecree = new AdvanceDecree();
    private static final List<RoyalDecree> DECREES = List.of(new CloseQuartersDecree(), new KeepYourDistanceDecree(), new NoRetreatDecree(), new AdvanceDecree());
    public KingEntity(EntityType<? extends KingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.kingHealth(), AntarchySettings.kingAttackDamage());
    }

    @Override
    protected String geoName() {
        return "king";
    }

    @Override
    protected boolean isFlyingBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.KING_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.KING_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.KING_DEATH.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.KING_BITE.get();
    }

    @Override
    protected SoundEvent royalBeamShootSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamLoopSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamEndSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected RoyalBeamSettings royalBeamSettings() {
        return new RoyalBeamSettings(AntarchySettings.kingBeamRange(), AntarchySettings.kingBeamTracking(), 7.5D,
                AntarchySettings.kingBeamDurationTicks(), AntarchySettings.kingBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.kingBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.kingBeamTerrainRadius(), 4.0F, AntarchySettings.kingBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.BUILD_ICE;
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode(@Nullable RoyalHead head) {
        return head != null && head.slot() == RoyalHead.Slot.RIGHT ? RoyalBeamTerrainMode.BUILD_ICE : RoyalBeamTerrainMode.NONE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.getTarget() != null && this.level() instanceof ServerLevel level) {
            this.tickElementalAttacks(level, this.getTarget());
        }
        if (this.level().isClientSide || this.getTarget() != null || this.isDeadOrDying()
                || !(this.level() instanceof ServerLevel)) {
            if (!this.level().isClientSide && this.getTarget() != null && this.level() instanceof ServerLevel level) {
                this.tickDecree(level, this.getTarget());
            }
            return;
        }
        if (this.patrolCenter == null) {
            this.patrolCenter = this.position();
        }
        if (this.patrolCooldownTicks-- <= 0) {
            this.patrolCooldownTicks = 100 + this.random.nextInt(100);
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 12.0D + this.random.nextDouble() * 18.0D;
            this.getMoveControl().setWantedPosition(this.patrolCenter.x + Math.cos(angle) * radius,
                    this.patrolCenter.y + 6.0D + this.random.nextDouble() * 12.0D,
                    this.patrolCenter.z + Math.sin(angle) * radius, 1.0D);
        }
    }

    private void tickDecree(ServerLevel level, net.minecraft.world.entity.LivingEntity target) {
        if (this.decreeCooldownTicks-- <= 0) {
            this.decreeCooldownTicks = 220;
            this.activeDecree = DECREES.get(this.random.nextInt(DECREES.size()));
            this.playSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 3.0F, 0.9F + this.random.nextFloat() * 0.15F);
        }
        this.activeDecree.apply(level, this, target);
    }

    private void tickElementalAttacks(ServerLevel level, net.minecraft.world.entity.LivingEntity target) {
        if (this.fireAttackCooldownTicks-- <= 0) {
            this.fireAttackCooldownTicks = 110;
            Vec3 start = this.position().add(0.0D, 5.0D, 0.0D);
            RoyalBoltEntity bolt = new RoyalBoltEntity(AntarchyObjects.ROYAL_BOLT.get(), level);
            bolt.setOwner(this);
            bolt.setPos(start);
            Vec3 delta = target.getEyePosition().subtract(start);
            bolt.shoot(delta.x, delta.y, delta.z, 1.15F, 0.0F);
            level.addFreshEntity(bolt);
            this.playSound(AntarchySoundEvents.KING_FIREBALL_SHOOT.get(), 3.0F, 1.0F);
        }
        if (this.lightningAttackCooldownTicks-- <= 0) {
            this.lightningAttackCooldownTicks = 150;
            for (net.minecraft.world.entity.LivingEntity living : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    target.getBoundingBox().inflate(5.0D), entity -> entity.isAlive() && entity != this)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt == null) continue;
                bolt.moveTo(living.getX(), living.getY(), living.getZ());
                level.addFreshEntity(bolt);
            }
        }
    }
}
