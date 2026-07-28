package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import software.bernie.geckolib.animatable.GeoEntity;

public class RedAntEntity extends BaseAntEntity implements GeoEntity {
    public RedAntEntity(EntityType<? extends BaseAntEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected ResourceKey<Level> destinationDimension() {
        return AntarchySettings.redAntDestinationDimension();
    }

    @Override
    protected TagKey<Item> activationItemsTag() {
        return AntarchyTags.Items.RED_ANT_ACTIVATION_ITEMS;
    }

    @Override
    protected TagKey<Item> breedingFoodsTag() {
        return AntarchyTags.Items.RED_ANT_BREEDING_FOODS;
    }

    @Override
    protected boolean requiresActivationReagent() {
        return AntarchySettings.redAntRequiresReagent();
    }

    @Override
    protected String activationMessageKey() {
        return "message.antarchy.red_ant_activated";
    }

    @Override
    protected String needsReagentMessageKey() {
        return "message.antarchy.red_ant_needs_reagent";
    }

    @Override
    protected double configuredMaxHealth() {
        return AntarchySettings.redAntHealth();
    }

    @Override
    protected double configuredAttackDamage() {
        return AntarchySettings.redAntAttackDamage();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isLeashed() && !this.hasCustomName();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(Fluids.LAVA) || super.canStandOnFluid(fluidState);
    }

    @Override
    protected boolean canTraverseFluidFloor(FluidState fluidState) {
        return fluidState.is(Fluids.LAVA);
    }
}
