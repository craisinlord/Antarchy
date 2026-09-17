package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import com.craisinlord.antarchy.content.effect.CommandedBehavior;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.StreamSupport;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class RoyalGuardianShieldItem extends ShieldItem {
    private final Supplier<Ingredient> repairIngredient;

    public RoyalGuardianShieldItem(Item.Properties properties, Supplier<Ingredient> repairIngredient) {
        super(properties);
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return this.repairIngredient.get().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalWeaponEnchantability();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_guardian_shield.command")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        Holder<MobEffect> commanded = RoyalEffectHooks.commandedHolder();
        if (commanded == null) {
            return InteractionResultHolder.fail(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }
        int rosterSize = Math.max(0, AntarchySettings.royalGuardianMusterMobCap());
        int duration = Math.max(1, AntarchySettings.royalGuardianMusterDurationTicks());
        if (rosterSize == 0) return InteractionResultHolder.fail(stack);

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
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, Math.max(0, AntarchySettings.royalGuardianMusterCooldownTicks()));
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.75F);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 32, 1.0D, 0.7D, 1.0D, 0.2D);
        return InteractionResultHolder.success(stack);
    }

    private static boolean isInCommandLookCone(Player player, Mob mob) {
        var direction = mob.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();
        return player.getViewVector(1.0F).dot(direction) >= 0.9063078D; // 25-degree half-angle
    }
}
