package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import java.util.UUID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRoyalEffectsMixin {
    @Unique
    private UUID antarchy$previousCommander;

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void antarchy$checkRoyalEffectBlacklist(MobEffectInstance instance, CallbackInfoReturnable<Boolean> cir) {
        if (!RoyalEffectEligibility.canApply((LivingEntity) (Object) this, instance)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void antarchy$prepareCommandedSource(MobEffectInstance instance, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (!RoyalEffectEligibility.isCommanded(instance)) {
            return;
        }
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!RoyalEffectEligibility.canApplyCommanded(entity) || !(entity instanceof Mob mob)) {
            cir.setReturnValue(false);
            return;
        }
        Entity commander = antarchy$resolveCommander(source);
        if (commander == null) {
            cir.setReturnValue(false);
            return;
        }
        CommandedEntityAccess access = (CommandedEntityAccess) mob;
        UUID current = access.antarchy$getCommanderUuid();
        if (current != null && !current.equals(commander.getUUID())) {
            cir.setReturnValue(false);
            return;
        }
        antarchy$previousCommander = current;
        access.antarchy$setCommanderUuid(commander.getUUID());
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
    private void antarchy$finishCommandedSource(MobEffectInstance instance, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (!RoyalEffectEligibility.isCommanded(instance) || cir.getReturnValue()) {
            return;
        }
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof CommandedEntityAccess access) {
            access.antarchy$setCommanderUuid(antarchy$previousCommander);
        }
    }

    @Inject(method = "onEffectRemoved", at = @At("HEAD"))
    private void antarchy$clearCommandedState(MobEffectInstance instance, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (RoyalEffectEligibility.isCommanded(instance) && (Object) this instanceof Mob mob) {
            com.craisinlord.antarchy.content.effect.CommandedBehavior.clear(mob);
        }
    }

    @Unique
    private Entity antarchy$resolveCommander(Entity source) {
        if (source instanceof net.minecraft.world.entity.player.Player player) {
            return player;
        }
        if (source instanceof Projectile projectile && projectile.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
            return player;
        }
        if (source instanceof AreaEffectCloud cloud && cloud.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
            return player;
        }
        return source instanceof RoyalBossEntity ? source : null;
    }
}
