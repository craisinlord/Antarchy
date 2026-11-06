package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class MoleTruffles {
    static final String PACIFIED_KEY = "TrufflePacifiedTicks";
    private static final ResourceLocation ROASTED_TRUFFLE = new ResourceLocation(Antarchy.MODID, "roasted_truffle");
    private static final int RAW_PACIFY_TICKS = 1200;
    private static final int ROASTED_PACIFY_TICKS = 3600;
    private static final int CALM_PARTICLE_INTERVAL = 30;

    private MoleTruffles() {
    }

    static boolean isTruffle(ItemStack stack) {
        return stack.is(AntarchyTags.Items.MOLE_TRUFFLES);
    }

    static boolean lures(LivingEntity entity) {
        return entity instanceof Player player && isTruffle(player.getMainHandItem());
    }

    static int feed(Mob mob, Player player, InteractionHand hand, int currentTicks) {
        ItemStack stack = player.getItemInHand(hand);
        int ticks = BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ROASTED_TRUFFLE) ? ROASTED_PACIFY_TICKS : RAW_PACIFY_TICKS;
        stack.shrink(1);
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.playSound(SoundEvents.GENERIC_EAT, 1.0F, 0.9F + mob.getRandom().nextFloat() * 0.2F);
        burst(mob, ParticleTypes.HEART, 6);
        return Math.max(currentTicks, ticks);
    }

    static void tickCalm(Mob mob) {
        if (mob.tickCount % CALM_PARTICLE_INTERVAL == 0) {
            burst(mob, ParticleTypes.HAPPY_VILLAGER, 2);
        }
    }

    static void burst(Mob mob, ParticleOptions particle, int count) {
        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    particle,
                    mob.getX(),
                    mob.getY() + mob.getBbHeight() + 0.2D,
                    mob.getZ(),
                    count,
                    mob.getBbWidth() * 0.4D,
                    0.15D,
                    mob.getBbWidth() * 0.4D,
                    0.0D
            );
        }
    }
}
