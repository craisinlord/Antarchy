package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class InvertedGeyserBaseParticle extends BaseAshSmokeParticle {
    protected InvertedGeyserBaseParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            float scale,
            float lifetimeScale,
            Direction direction,
            SpriteSet sprites
    ) {
        super(level, x, y, z, scale, scale, scale, xd, yd, zd, lifetimeScale, sprites, 0.0F, 0, 0.0F, true);
        this.friction = 0.725F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        if (direction == Direction.DOWN) {
            this.roll = Mth.PI;
            this.oRoll = Mth.PI;
        }
        this.yd = Math.abs(this.yd) * direction.getStepY();
        float lifetimeFactor = 0.8F + 0.2F * level.getRandom().nextFloat();
        this.lifetime = (int) (25.0F * lifetimeFactor);
    }

    public static final class Provider implements ParticleProvider<InvertedGeyserBaseParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                InvertedGeyserBaseParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd
        ) {
            double shiftedX = x + (level.getRandom().nextFloat() - 0.5F) * 0.5F;
            double shiftedY = y + (level.getRandom().nextFloat() - 0.5F) * 0.5F + 0.2D * options.direction().getStepY();
            double shiftedZ = z + (level.getRandom().nextFloat() - 0.5F) * 0.5F;
            float scale = options.burstImpulseBase() + 0.25F + options.fluidBlocks();
            float lifetimeScale = 3.0F + 0.125F * options.fluidBlocks();
            return new InvertedGeyserBaseParticle(
                    level,
                    shiftedX,
                    shiftedY,
                    shiftedZ,
                    xd,
                    yd,
                    zd,
                    scale,
                    lifetimeScale,
                    options.direction(),
                    this.sprites
            );
        }
    }
}
