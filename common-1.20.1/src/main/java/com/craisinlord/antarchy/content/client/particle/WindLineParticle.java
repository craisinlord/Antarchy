package com.craisinlord.antarchy.content.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class WindLineParticle extends TextureSheetParticle {
    private static final int SEGMENTS = 4;

    private final float dirX;
    private final float dirZ;
    private final float length;
    private final float halfWidth;
    private final float peakAlpha;
    private final float wavePhase;
    private final float waveHeight;

    private WindLineParticle(ClientLevel level, double x, double y, double z, double velocityX, double gust, double velocityZ, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.setSprite(sprites.get(this.random));
        float strength = Mth.clamp((float) gust, 0.0F, 1.0F);
        float speed = Mth.sqrt((float) (velocityX * velocityX + velocityZ * velocityZ));
        this.dirX = speed > 0.0F ? (float) velocityX / speed : 1.0F;
        this.dirZ = speed > 0.0F ? (float) velocityZ / speed : 0.0F;
        this.xd = velocityX;
        this.yd = 0.0D;
        this.zd = velocityZ;
        this.lifetime = 10 + this.random.nextInt(9);
        this.length = 1.5F + strength * 2.5F + this.random.nextFloat() * 0.5F;
        this.halfWidth = 0.018F + strength * 0.018F;
        this.peakAlpha = 0.2F + strength * 0.3F;
        this.wavePhase = this.random.nextFloat() * Mth.TWO_PI;
        this.waveHeight = 0.05F + this.random.nextFloat() * 0.09F;
        this.setAlpha(0.0F);
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.setSize(this.length, 0.4F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.x += this.xd;
        this.z += this.zd;
        this.setPos(this.x, this.y, this.z);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 eye = camera.getPosition();
        float centerX = (float) (Mth.lerp(partialTick, this.xo, this.x) - eye.x);
        float centerY = (float) (Mth.lerp(partialTick, this.yo, this.y) - eye.y);
        float centerZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - eye.z);
        float progress = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        float fade = Math.min(Mth.clamp(progress / 0.25F, 0.0F, 1.0F), Mth.clamp((1.0F - progress) / 0.4F, 0.0F, 1.0F));
        float alpha = this.peakAlpha * fade * fade * (3.0F - 2.0F * fade);
        int light = this.getLightColor(partialTick);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        float sideX = -this.dirZ;
        float sideZ = this.dirX;
        float wave = this.wavePhase + progress * 3.0F;
        float lastX = 0.0F;
        float lastY = 0.0F;
        float lastZ = 0.0F;
        float lastWidthX = 0.0F;
        float lastWidthY = 0.0F;
        float lastWidthZ = 0.0F;
        float lastU = u0;
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            float t = segment / (float) SEGMENTS;
            float along = (t - 0.5F) * this.length;
            float ripple = Mth.sin(t * Mth.TWO_PI * 0.75F + wave);
            float px = centerX + this.dirX * along + sideX * ripple * this.waveHeight * 0.6F;
            float py = centerY + Mth.cos(t * Mth.TWO_PI * 0.6F + wave) * this.waveHeight;
            float pz = centerZ + this.dirZ * along + sideZ * ripple * this.waveHeight * 0.6F;
            float widthX = this.dirZ * py;
            float widthY = this.dirX * pz - this.dirZ * px;
            float widthZ = -this.dirX * py;
            float widthLength = Mth.sqrt(widthX * widthX + widthY * widthY + widthZ * widthZ);
            if (widthLength < 1.0E-4F) {
                widthX = 0.0F;
                widthY = this.halfWidth;
                widthZ = 0.0F;
            } else {
                float scale = this.halfWidth / widthLength;
                widthX *= scale;
                widthY *= scale;
                widthZ *= scale;
            }
            float u = Mth.lerp(t, u0, u1);
            if (segment > 0) {
                vertex(consumer, lastX - lastWidthX, lastY - lastWidthY, lastZ - lastWidthZ, lastU, v1, alpha, light);
                vertex(consumer, lastX + lastWidthX, lastY + lastWidthY, lastZ + lastWidthZ, lastU, v0, alpha, light);
                vertex(consumer, px + widthX, py + widthY, pz + widthZ, u, v0, alpha, light);
                vertex(consumer, px - widthX, py - widthY, pz - widthZ, u, v1, alpha, light);
            }
            lastX = px;
            lastY = py;
            lastZ = pz;
            lastWidthX = widthX;
            lastWidthY = widthY;
            lastWidthZ = widthZ;
            lastU = u;
        }
    }

    private void vertex(VertexConsumer consumer, float x, float y, float z, float u, float v, float alpha, int light) {
        consumer.vertex(x, y, z).uv(u, v).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GroundMistRenderType.INSTANCE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new WindLineParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
