package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem;
import com.craisinlord.antarchy.content.item.RoyalGuardianArmorItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class RoyalEffectEligibility {
    private RoyalEffectEligibility() {
    }

    public static boolean isCommanded(MobEffectInstance instance) {
        return RoyalEffectHooks.commandedHolder() != null && instance.is(RoyalEffectHooks.commandedHolder());
    }

    public static boolean isDilated(MobEffectInstance instance) {
        return RoyalEffectHooks.dilatedHolder() != null && instance.is(RoyalEffectHooks.dilatedHolder());
    }

    public static boolean canApplyCommanded(LivingEntity entity) {
        return entity instanceof Mob mob
                && !(entity instanceof Player)
                && !entity.getType().is(AntarchyTags.Entities.COMMANDED_BLACKLIST)
                && !(mob.isNoAi())
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof RoyalGuardianArmorItem)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof RoyalGuardianArmorItem)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof RoyalGuardianArmorItem)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof RoyalGuardianArmorItem);
    }

    public static boolean canApplyDilated(LivingEntity entity) {
        return !entity.getType().is(AntarchyTags.Entities.DILATED_BLACKLIST)
                && !entity.getType().is(AntarchyTags.Entities.TIME_DILATION_IMMUNE)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof RoyalAssailantArmorItem)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof RoyalAssailantArmorItem)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof RoyalAssailantArmorItem)
                && !(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof RoyalAssailantArmorItem);
    }

    public static boolean canApply(LivingEntity entity, MobEffectInstance instance) {
        if (isCommanded(instance)) {
            return canApplyCommanded(entity);
        }
        if (isDilated(instance)) {
            return canApplyDilated(entity);
        }
        return true;
    }

    public static boolean canUseCommandSource(Entity source) {
        return source instanceof Player;
    }
}
