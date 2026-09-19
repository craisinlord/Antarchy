package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class RoyalAssailantBlackHoleEntity extends RoyalBlackHoleEntity {
    public RoyalAssailantBlackHoleEntity(EntityType<? extends RoyalAssailantBlackHoleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static RoyalAssailantBlackHoleEntity create(ServerLevel level, Vec3 center, @Nullable UUID ownerId) {
        RoyalAssailantBlackHoleEntity hole = new RoyalAssailantBlackHoleEntity(
                AntarchyObjects.ROYAL_ASSAILANT_BLACK_HOLE.get(), level);
        hole.configure(center, AntarchySettings.queenBlackHoleRadius(),
                AntarchySettings.queenBlackHoleActiveTicks(), ownerId);
        return hole;
    }

    @Override
    protected boolean risesWhileActive() {
        return true;
    }

    @Override
    protected double timeDilationRadius() {
        return Math.max(2.0D, this.radius() * 0.55D);
    }

    @Override
    protected double effectGrowthStart() {
        return 0.45D;
    }

    @Override
    protected double pullRadius() {
        return this.effectRadius();
    }

    @Override
    protected boolean hasVisibleTimeDilationField() {
        return false;
    }

    @Override
    protected int collapseDurationTicks() {
        return 12;
    }

    @Override
    protected boolean pullsTerrainBlocks() {
        return false;
    }
}
