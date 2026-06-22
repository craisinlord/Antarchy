package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class InvertedGeyserPlumeParticle extends TextureSheetParticle {
    private final double startY;
    private final double limitY;
    private final float horizontalSprayX;
    private final float horizontalSprayZ;
    private final float initialPropulsion;
    private final float minSize;
    private final float maxSize;
    private final SpriteSet sprites;
    private final Direction direction;

    public InvertedGeyserPlumeParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            InvertedGeyserParticleOptions options,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xd, yd, zd);
        int forceHeight = 5 * Math.max(1, options.fluidBlocks());
        this.hasPhysics = true;
        this.speedUpWhenYMotionIsBlocked = true;
        this.lifetime = forceHeight * 5;
        this.yd = 0.0D;
        this.startY = y;
        this.direction = options.direction();
        this.limitY = this.startY + this.direction.getStepY() * (forceHeight - 1);
        if (this.direction == Direction.DOWN) {
            this.roll = Mth.PI;
            this.oRoll = Mth.PI;
        }
        this.horizontalSprayX = (level.getRandom().nextFloat() - 0.5F) * 0.2F;
        this.horizontalSprayZ = (level.getRandom().nextFloat() - 0.5F) * 0.2F;
        this.friction = 1.0F;
        this.initialPropulsion = (options.fluidBlocks() == 1 ? 1.5F : 1.0F) * forceHeight * 1.45F;
        this.gravity = -this.direction.getStepY() * this.initialPropulsion;
        float baseSize = this.quadSize * 0.75F;
        this.minSize = baseSize * (2.0F + forceHeight / 8.0F);
        this.maxSize = baseSize * (3.0F + forceHeight / 8.0F);
        this.quadSize = this.minSize;
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed && hasReachedTerminalState()) {
            this.lifetime = Math.min(this.lifetime, this.age + 5);
            this.friction = 0.0F;
            this.remove();
        }

        double progress = Mth.clamp((this.y - this.startY) / (this.limitY - this.startY), 0.0D, 1.0D);
        double progressCurve = Math.pow(progress, 3.0D);
        this.gravity = this.direction.getStepY() * this.initialPropulsion * (float) progressCurve * 0.12F;
        this.xd = progress * this.horizontalSprayX;
        this.zd = progress * this.horizontalSprayZ;
        this.setSpriteFromAge(this.sprites);
        this.quadSize = this.minSize + (float) (progress * (this.maxSize - this.minSize));
    }

    private boolean hasReachedTerminalState() {
        if (this.direction == Direction.UP) {
            return this.yd < 0.0D || this.y > this.limitY || this.y == this.yo;
        }
        return this.yd > 0.0D || this.y < this.limitY || this.y == this.yo;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider implements ParticleProvider<InvertedGeyserParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

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
            double shiftedX = x + (level.getRandom().nextFloat() - 0.5F) * 0.2F;
            double shiftedY = y + level.getRandom().nextFloat() * options.direction().getStepY();
            double shiftedZ = z + (level.getRandom().nextFloat() - 0.5F) * 0.2F;
            return new InvertedGeyserPlumeParticle(level, shiftedX, shiftedY, shiftedZ, xd, yd, zd, options, this.sprites);
        }
    }
}
