package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.entity.trades.DrTrayaurusTradeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DrTrayaurusEntity extends AbstractVillager {
    private static final Component DISPLAY_NAME = Component.literal("Dr. Trayaurus");

    public DrTrayaurusEntity(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.45D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public Component getName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected Component getTypeName() {
        return DISPLAY_NAME;
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        offers.clear();
        offers.addAll(DrTrayaurusTradeManager.createOffers());
        this.overrideXp(1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.interactLivingEntity(player, this, hand).consumesAction()) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.level().isClientSide && this.isAlive() && !this.isTrading()) {
            this.setTradingPlayer(player);
            MerchantOffers offers = this.getOffers();
            if (!offers.isEmpty()) {
                this.openTradingScreen(player, this.getName(), 0);
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public void rewardTradeXp(net.minecraft.world.item.trading.MerchantOffer offer) {
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
