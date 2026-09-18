package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BugSprayItem extends Item {
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> ANARCHY_ARTHROPODS = TagKey.create(
            net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("antarchy", "arthropods"));

    public BugSprayItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 origin = player.getEyePosition().add(look.scale(0.6D));
            var sprayParticle = AntarchyObjects.BUG_SPRAY_PARTICLE.get();
            serverLevel.sendParticles(sprayParticle, origin.x, origin.y, origin.z, 18,
                    look.x * 0.45D, look.y * 0.45D, look.z * 0.45D, 0.06D);
            AABB bounds = new AABB(origin, origin.add(look.scale(7.0D))).inflate(2.2D);
            for (Entity entity : level.getEntities(player, bounds, candidate -> candidate instanceof Mob mob
                    && (mob.getType().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)
                    || mob.getType().is(ANARCHY_ARTHROPODS) || mob instanceof BaseAntEntity))) {
                if (!(entity instanceof Mob mob)) continue;
                Vec3 toward = mob.position().subtract(origin);
                double distance = toward.length();
                if (distance > 9.0D || distance < 0.001D || toward.normalize().dot(look) < 0.55D) continue;
                Vec3 flee = mob.position().subtract(player.position()).normalize();
                mob.getNavigation().moveTo(mob.getX() + flee.x * 6.0D, mob.getY(), mob.getZ() + flee.z * 6.0D, 1.25D);
                mob.setTarget(null);
                mob.setLastHurtByMob(null);
                mob.setDeltaMovement(mob.getDeltaMovement().add(flee.scale(0.55D)).add(0.0D, 0.15D, 0.0D));
                mob.hurtMarked = true;
                serverLevel.sendParticles(sprayParticle, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(), 5, 0.12D, 0.12D, 0.12D, 0.02D);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7F, 1.2F);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
