package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class ParalyzedClientHandler {
    private static Field moveVectorField;
    private static boolean moveVectorLookupFailed;

    private static final int PARTICLE_INTERVAL = 3;

    private static final int PARTICLES_PER_ENTITY = 4;

    private static final double PARTICLE_RADIUS = 24.0;

    private ParalyzedClientHandler() {
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!event.getEntity().hasEffect(AntarchyNeoforgeMisc.PARALYZED)) {
            return;
        }

        ClientInput input = event.getInput();
        input.keyPresses = new Input(false, false, false, false, false, input.keyPresses.shift(), input.keyPresses.sprint());
        clearMoveVector(input);
    }

    private static void clearMoveVector(ClientInput input) {
        if (moveVectorField == null && !moveVectorLookupFailed) {
            try {
                Field field = ClientInput.class.getDeclaredField("moveVector");
                field.setAccessible(true);
                moveVectorField = field;
            } catch (ReflectiveOperationException | RuntimeException e) {
                moveVectorLookupFailed = true;
                Antarchy.LOGGER.error("Paralysis cannot clear ClientInput.moveVector; paralyzed players can still walk", e);
            }
        }
        if (moveVectorField == null) {
            return;
        }
        try {
            moveVectorField.set(input, Vec2.ZERO);
        } catch (IllegalAccessException e) {
            moveVectorField = null;
            moveVectorLookupFailed = true;
            Antarchy.LOGGER.error("Paralysis cannot clear ClientInput.moveVector; paralyzed players can still walk", e);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        Player player = mc.player;
        if (player.hasEffect(AntarchyNeoforgeMisc.PARALYZED)) {
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
                        && entity.hasEffect(AntarchyNeoforgeMisc.PARALYZED)
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
