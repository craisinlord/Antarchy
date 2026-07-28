package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.content.client.particle.InvertedGeyserBaseParticleOptions;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;

public final class InvertedGeyserEruptionParticle extends NoRenderParticle {
    private final int fluidBlocks;
    private final double motionX;
    private final double motionY;
    private final double motionZ;
    private final InvertedGeyserParticleOptions plumeParticle;
    private final InvertedGeyserBaseParticleOptions baseParticle;
    private final InvertedGeyserBaseParticleOptions poofParticle;

    protected InvertedGeyserEruptionParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            InvertedGeyserParticleOptions options
    ) {
        super(level, x, y, z);
        this.motionX = xd;
        this.motionY = yd;
        this.motionZ = zd;
        this.fluidBlocks = options.fluidBlocks();
        this.lifetime = 20;
        this.plumeParticle = new InvertedGeyserParticleOptions(AntarchyNeoforgeMisc.INVERTED_GEYSER_PLUME.get(), this.fluidBlocks, options.direction());
        this.baseParticle = new InvertedGeyserBaseParticleOptions(AntarchyNeoforgeMisc.INVERTED_GEYSER_BASE.get(), this.fluidBlocks, 1.5F, options.direction());
        this.poofParticle = new InvertedGeyserBaseParticleOptions(AntarchyNeoforgeMisc.INVERTED_GEYSER_POOF.get(), this.fluidBlocks, 2.0F, options.direction());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age % 2 == 0) {
            for (int i = 0; i < 2; i++) {
                this.level.addParticle(this.baseParticle, this.x, this.y, this.z, this.motionX, this.motionY, this.motionZ);
            }
        }

        for (int i = 0; i < this.fluidBlocks + 2; i++) {
            this.level.addParticle(this.plumeParticle, this.x, this.y, this.z, this.motionX, this.motionY, this.motionZ);
        }

        if (this.age % 10 == 0) {
            for (int i = 0; i < 20; i++) {
                this.level.addParticle(this.poofParticle, this.x, this.y, this.z, this.motionX, this.motionY, this.motionZ);
            }
        }
    }

    public static final class Provider implements ParticleProvider<InvertedGeyserParticleOptions> {
        @Override
        public Particle createParticle(
                InvertedGeyserParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            return new InvertedGeyserEruptionParticle(level, x, y, z, xd, yd, zd, options);
        }
    }
}
