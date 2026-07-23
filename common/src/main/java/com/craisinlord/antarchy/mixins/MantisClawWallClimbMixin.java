package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.MantisClawItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MantisClawWallClimbMixin extends LivingEntity {
    @Unique
    private static final int antarchy$MANTIS_CLAW_CLIMB_DURABILITY_INTERVAL = 20;
    @Unique
    private boolean antarchy$mantisClinging;
    @Unique
    private int antarchy$mantisWallJumpCooldown;
    @Unique
    private int antarchy$mantisClingDurabilityTicks;

    protected MantisClawWallClimbMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickMantisWallCling(CallbackInfo ci) {
        if (this.antarchy$mantisWallJumpCooldown > 0) {
            this.antarchy$mantisWallJumpCooldown--;
        }

        if (!this.antarchy$canMantisCling()) {
            this.antarchy$mantisClinging = false;
            this.antarchy$mantisClingDurabilityTicks = 0;
            return;
        }

        if (!this.antarchy$mantisClinging && !this.level().isClientSide()) {
            this.playSound(
                    SoundEvents.STONE_BREAK,
                    0.45F,
                    0.9F + this.getRandom().nextFloat() * 0.1F
            );
        }

        Vec3 motion = this.getDeltaMovement();
        double clampedY = motion.y < MantisClawItem.WALL_CLING_FALL_SPEED ? MantisClawItem.WALL_CLING_FALL_SPEED : motion.y;
        if (this.jumping && this.antarchy$mantisWallJumpCooldown <= 0) {
            clampedY = Math.max(clampedY, MantisClawItem.WALL_CLIMB_JUMP_VELOCITY);
            this.antarchy$mantisWallJumpCooldown = 6;
        }

        this.setDeltaMovement(motion.x, clampedY, motion.z);
        this.hasImpulse = true;
        this.fallDistance = 0.0F;
        this.antarchy$mantisClinging = true;
        this.antarchy$damageMantisClawsWhileClimbing();
    }

    @Unique
    private boolean antarchy$canMantisCling() {
        Player player = (Player) (Object) this;
        return MantisClawItem.isDualWielding(player)
                && this.horizontalCollision
                && !this.onGround()
                && !this.isInWaterOrBubble()
                && !this.isPassenger()
                && !this.isFallFlying()
                && !player.getAbilities().flying
                && !player.isSpectator();
    }

    @Unique
    private void antarchy$damageMantisClawsWhileClimbing() {
        if (this.level().isClientSide()) {
            return;
        }

        this.antarchy$mantisClingDurabilityTicks++;
        if (this.antarchy$mantisClingDurabilityTicks < antarchy$MANTIS_CLAW_CLIMB_DURABILITY_INTERVAL) {
            return;
        }
        this.antarchy$mantisClingDurabilityTicks = 0;

        Player player = (Player) (Object) this;
        this.antarchy$damageClaw(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        this.antarchy$damageClaw(player.getOffhandItem(), player, InteractionHand.OFF_HAND);
    }

    @Unique
    private void antarchy$damageClaw(ItemStack stack, Player player, InteractionHand hand) {
        if (!(stack.getItem() instanceof MantisClawItem)) {
            return;
        }

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        if (!stack.isDamageableItem()) {
            return;
        }

        ItemStack updatedStack = stack.copy();
        int nextDamage = updatedStack.getDamageValue() + 1;
        if (nextDamage < updatedStack.getMaxDamage()) {
            updatedStack.setDamageValue(nextDamage);
            player.setItemSlot(slot, updatedStack);
        } else {
            player.setItemSlot(slot, ItemStack.EMPTY);
            player.onEquippedItemBroken(updatedStack.getItem(), slot);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
        }
    }
}
