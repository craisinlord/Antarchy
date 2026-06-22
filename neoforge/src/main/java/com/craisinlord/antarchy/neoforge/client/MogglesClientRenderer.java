package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.MogglesCaveDetector;
import com.craisinlord.antarchy.content.item.MogglesLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Antarchy.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class MogglesClientRenderer {
    private MogglesClientRenderer() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;

        if (!MogglesLogic.isWearingMoggles(player) || level.canSeeSky(player.blockPosition())) {
            MogglesCaveDetector.clear();
            return;
        }

        // Run cave BFS (internally rate-limited to every 40 ticks).
        MogglesCaveDetector.tick(player, level);

        BlockPos target = MogglesCaveDetector.getCaveTarget();
        if (target == null) return;

        // Spawn particle trail toward the cave every 2 ticks.
        if (level.getGameTime() % 2 != 0) return;

        Vec3 eye = player.getEyePosition();
        Vec3 end = Vec3.atCenterOf(target);
        double totalDist = eye.distanceTo(end);
        if (totalDist < 1.5) return;

        Vec3 dir = end.subtract(eye).normalize();

        // Place up to 5 particles spaced evenly between 1.5 m ahead and the target.
        int steps = Math.min(5, (int) (totalDist / 1.5));
        double spacing = (totalDist - 1.5) / Math.max(steps, 1);

        for (int i = 0; i < steps; i++) {
            double t = 1.5 + i * spacing;
            Vec3 p = eye.add(dir.scale(t));
            Vec3 vel = dir.scale(0.04);
            level.addParticle(ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    vel.x, vel.y, vel.z);
        }
    }
}
