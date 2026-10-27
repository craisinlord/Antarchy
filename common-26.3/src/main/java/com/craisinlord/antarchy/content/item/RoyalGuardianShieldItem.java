package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import com.craisinlord.antarchy.content.effect.CommandedBehavior;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class RoyalGuardianShieldItem extends ShieldItem {
    public RoyalGuardianShieldItem(Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(
                properties
                        .equippableUnswappable(EquipmentSlot.OFFHAND)
                        .delayedComponent(
                                DataComponents.BLOCKS_ATTACKS,
                                context -> new BlocksAttacks(
                                        0.25F,
                                        1.0F,
                                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                        Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                        Optional.of(SoundEvents.SHIELD_BLOCK),
                                        Optional.of(SoundEvents.SHIELD_BREAK)
                                )
                        )
                        .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                        .repairable(AntarchyRepairTags.ROYAL_GUARDIAN_SHIELD),
                AntarchySettings::royalWeaponEnchantability
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_guardian_shield.command")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Holder<MobEffect> commanded = RoyalEffectHooks.commandedHolder();
        if (commanded == null) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        int rosterSize = Math.max(0, AntarchySettings.royalGuardianMusterMobCap());
        int duration = Math.max(1, AntarchySettings.royalGuardianMusterDurationTicks());
        if (rosterSize == 0) return InteractionResult.FAIL;

        Set<UUID> rosterIds = new HashSet<>();
        List<Mob> existingRoster = StreamSupport.stream(serverLevel.getAllEntities().spliterator(), false)
                .filter(Mob.class::isInstance).map(Mob.class::cast)
                .filter(mob -> mob.hasEffect(commanded) && mob instanceof CommandedEntityAccess access
                        && access.antarchy$isRoyalInvested() && player.getUUID().equals(access.antarchy$getCommanderUuid()))
                .sorted(Comparator.comparingDouble((Mob mob) -> player.distanceToSqr(mob)).thenComparingInt(Mob::getId))
                .limit(rosterSize).toList();
        for (Mob mob : existingRoster) {
            rosterIds.add(mob.getUUID());
            mob.addEffect(new MobEffectInstance(commanded, duration), player);
        }

        int slotsRemaining = rosterSize - existingRoster.size();
        int recruited = 0;
        List<Mob> candidates = level.getEntitiesOfClass(Mob.class,
                        new AABB(player.blockPosition()).inflate(Math.max(0.0D, AntarchySettings.royalGuardianMusterRange())),
                        mob -> !rosterIds.contains(mob.getUUID()) && RoyalEffectEligibility.canApplyCommanded(mob))
                .stream()
                .sorted(Comparator.comparing((Mob mob) -> !isInCommandLookCone(player, mob))
                        .thenComparingDouble(player::distanceToSqr).thenComparingInt(Mob::getId))
                .toList();
        for (Mob target : candidates) {
            if (recruited >= slotsRemaining) break;
            if (!target.addEffect(new MobEffectInstance(commanded, duration), player)) continue;
            CommandedEntityAccess access = (CommandedEntityAccess) target;
            access.antarchy$setRoyalInvested(true);
            CommandedBehavior.syncInvestiture(target, true);
            rosterIds.add(target.getUUID());
            recruited++;
        }
        if (existingRoster.isEmpty() && recruited == 0) {
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(stack, Math.max(0, AntarchySettings.royalGuardianMusterCooldownTicks()));
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.75F);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 32, 1.0D, 0.7D, 1.0D, 0.2D);
        return InteractionResult.SUCCESS;
    }

    private static boolean isInCommandLookCone(Player player, Mob mob) {
        var direction = mob.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();
        return player.getViewVector(1.0F).dot(direction) >= 0.9063078D;
    }
}
