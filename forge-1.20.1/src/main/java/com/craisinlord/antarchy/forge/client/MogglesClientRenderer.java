package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.MogglesCaveDetector;
import com.craisinlord.antarchy.content.item.MogglesLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MogglesClientRenderer {
    private MogglesClientRenderer() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
