package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class PeachLeavesParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;
    private static final float WIND_BIG = 2.0F;
    private static final boolean SWIRL = false;
    private static final boolean FLOW_AWAY = true;
    private static final float GRAVITY_SCALE = 1.0F;
    private static final float INITIAL_Y_SPEED = 0.0F;

    private float rotSpeed;
    private final float spinAcceleration;
    private final double xaFlowScale;
    private final double zaFlowScale;
    private final double swirlPeriod;
    private final boolean swirl;
    private final boolean flowAway;
    private final SpriteSet sprites;

    private PeachLeavesParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.setSprite(sprites.get(level.random));
        this.rotSpeed = (float) Math.toRadians(this.random.nextBoolean() ? -30.0D : 30.0D);
        this.spinAcceleration = (float) Math.toRadians(this.random.nextBoolean() ? -5.0D : 5.0D);
        this.swirl = SWIRL;
        this.flowAway = FLOW_AWAY;
        this.lifetime = INITIAL_LIFETIME;
        this.gravity = GRAVITY_SCALE * 1.2F * ACCELERATION_SCALE;

        float size = this.random.nextBoolean() ? 0.05F : 0.075F;
        this.quadSize = size;
        this.setSize(size, size);
        this.friction = 1.0F;
        this.yd = -INITIAL_Y_SPEED;

        float randomAngle = this.random.nextFloat();
        double flowAngle = Math.toRadians(randomAngle * 60.0F);
        this.xaFlowScale = Math.cos(flowAngle) * WIND_BIG;
        this.zaFlowScale = Math.sin(flowAngle) * WIND_BIG;
        this.swirlPeriod = Math.toRadians(1000.0F + randomAngle * 3000.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }

        float ageProgress = Math.min((INITIAL_LIFETIME - this.lifetime) / (float) INITIAL_LIFETIME, 1.0F);
        double windX = 0.0D;
        double windZ = 0.0D;

        if (this.flowAway) {
            double flowScale = Math.pow(ageProgress, 1.25D);
            windX += this.xaFlowScale * flowScale;
            windZ += this.zaFlowScale * flowScale;
        }

        if (this.swirl) {
            double swirlScale = ageProgress * ageProgress;
            windX += swirlScale * Math.cos(swirlScale * this.swirlPeriod) * WIND_BIG;
            windZ += swirlScale * Math.sin(swirlScale * this.swirlPeriod) * WIND_BIG;
        }

        this.xd += windX * ACCELERATION_SCALE;
        this.zd += windZ * ACCELERATION_SCALE;
        this.yd -= this.gravity;

        this.rotSpeed += this.spinAcceleration / 20.0F;
        this.oRoll = this.roll;
        this.roll += this.rotSpeed / 20.0F;

        this.move(this.xd, this.yd, this.zd);
        if (this.onGround || this.lifetime < INITIAL_LIFETIME - 1 && this.xd == 0.0D && this.zd == 0.0D) {
            this.remove();
            return;
        }

        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        this.setSprite(this.sprites.get(INITIAL_LIFETIME - this.lifetime, INITIAL_LIFETIME));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new PeachLeavesParticle(level, x, y, z, this.sprites);
        }
    }
}
