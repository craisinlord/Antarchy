package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.minecraft.core.Holder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ParalyzedClientHandler {
    private static final Holder<net.minecraft.world.effect.MobEffect> PARALYZED_EFFECT =
            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(AntarchyFabricContent.PARALYZED.get());

    
    private static final int PARTICLE_INTERVAL = 3;
    
    private static final int PARTICLES_PER_ENTITY = 4;
    
    private static final double PARTICLE_RADIUS = 24.0;

    private ParalyzedClientHandler() {
    }

    
    public static void clampPlayerInput(LocalPlayer player) {
        if (!player.hasEffect(PARALYZED_EFFECT)) {
            return;
        }

        var input = player.input;
        input.left          = false;
        input.right         = false;
        input.up            = false;
        input.down          = false;
        input.leftImpulse   = 0.0F;
        input.forwardImpulse = 0.0F;
        input.jumping       = false;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        LocalPlayer player = mc.player;
        if (player.hasEffect(PARALYZED_EFFECT)) {
            while (mc.options.keyAttack.consumeClick()) {  }
            while (mc.options.keyUse.consumeClick())    {  }
        }
        if (player.tickCount % PARTICLE_INTERVAL != 0) {
            return;
        }

        ClientLevel level = mc.level;
        level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(PARTICLE_RADIUS),
                entity -> entity != player
                        && entity.isAlive()
                        && entity.hasEffect(PARALYZED_EFFECT)
        ).forEach(entity -> spawnStoneParticles(level, entity));
    }

    private static void spawnStoneParticles(ClientLevel level, LivingEntity entity) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, stone);

        float w = entity.getBbWidth();
        float h = entity.getBbHeight();

        for (int i = 0; i < PARTICLES_PER_ENTITY; i++) {
            double px = entity.getX() + (Math.random() - 0.5) * w;
            double py = entity.getY() + Math.random() * h;
            double pz = entity.getZ() + (Math.random() - 0.5) * w;

            double vx = (Math.random() - 0.5) * 0.08;
            double vy = Math.random() * 0.06;
            double vz = (Math.random() - 0.5) * 0.08;

            level.addParticle(particle, px, py, pz, vx, vy, vz);
        }
    }
}
