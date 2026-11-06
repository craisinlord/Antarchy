package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class WindLineParticle extends SingleQuadParticle {
    private static final int SEGMENTS = 4;

    private final float dirX;
    private final float dirZ;
    private final float length;
    private final float halfWidth;
    private final float peakAlpha;
    private final float wavePhase;
    private final float waveHeight;

    private WindLineParticle(ClientLevel level, double x, double y, double z, double velocityX, double gust, double velocityZ, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, sprites.first());
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
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        Vec3 eye = camera.position();
        float centerX = (float) (Mth.lerp(partialTick, this.xo, this.x) - eye.x);
        float centerY = (float) (Mth.lerp(partialTick, this.yo, this.y) - eye.y);
        float centerZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - eye.z);
        float progress = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        float fade = Math.min(Mth.clamp(progress / 0.25F, 0.0F, 1.0F), Mth.clamp((1.0F - progress) / 0.4F, 0.0F, 1.0F));
        int color = ARGB.colorFromFloat(this.peakAlpha * fade * fade * (3.0F - 2.0F * fade), this.rCol, this.gCol, this.bCol);
        int light = this.getLightCoords(partialTick);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        float sideX = -this.dirZ;
        float sideZ = this.dirX;
        float wave = this.wavePhase + progress * 3.0F;
        Vector3f tangent = new Vector3f();
        Vector3f width = new Vector3f();
        Vector3f normal = new Vector3f();
        Matrix3f basis = new Matrix3f();
        Quaternionf rotation = new Quaternionf();
        float lastX = 0.0F;
        float lastY = 0.0F;
        float lastZ = 0.0F;
        float lastU = u0;
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            float t = segment / (float) SEGMENTS;
            float along = (t - 0.5F) * this.length;
            float ripple = Mth.sin(t * Mth.TWO_PI * 0.75F + wave);
            float px = centerX + this.dirX * along + sideX * ripple * this.waveHeight * 0.6F;
            float py = centerY + Mth.cos(t * Mth.TWO_PI * 0.6F + wave) * this.waveHeight;
            float pz = centerZ + this.dirZ * along + sideZ * ripple * this.waveHeight * 0.6F;
            float u = Mth.lerp(t, u0, u1);
            if (segment > 0) {
                this.extractSegment(state, lastX, lastY, lastZ, px, py, pz, lastU, u, v0, v1, color, light, tangent, width, normal, basis, rotation);
            }
            lastX = px;
            lastY = py;
            lastZ = pz;
            lastU = u;
        }
    }

    private void extractSegment(QuadParticleRenderState state, float x0, float y0, float z0, float x1, float y1, float z1,
                                float u0, float u1, float v0, float v1, int color, int light,
                                Vector3f tangent, Vector3f width, Vector3f normal, Matrix3f basis, Quaternionf rotation) {
        tangent.set(x1 - x0, y1 - y0, z1 - z0);
        float segmentLength = tangent.length();
        if (segmentLength < 1.0E-4F) {
            return;
        }
        tangent.div(segmentLength);
        float midX = (x0 + x1) * 0.5F;
        float midY = (y0 + y1) * 0.5F;
        float midZ = (z0 + z1) * 0.5F;
        normal.set(-midX, -midY, -midZ);
        if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(0.0F, 0.0F, 1.0F);
        }
        normal.cross(tangent, width);
        if (width.lengthSquared() < 1.0E-6F) {
            width.set(0.0F, 1.0F, 0.0F);
        }
        width.normalize();
        tangent.cross(width, normal).normalize();
        basis.setColumn(0, tangent).setColumn(1, width).setColumn(2, normal);
        basis.getNormalizedRotation(rotation);
        int steps = Math.max(1, Mth.ceil(segmentLength / (this.halfWidth * 2.0F)));
        for (int i = 0; i < steps; i++) {
            float a = i / (float) steps;
            float b = (i + 1) / (float) steps;
            float mid = (a + b) * 0.5F;
            state.add(this.getLayer(), Mth.lerp(mid, x0, x1), Mth.lerp(mid, y0, y1), Mth.lerp(mid, z0, z1),
                    rotation.x, rotation.y, rotation.z, rotation.w, this.halfWidth,
                    Mth.lerp(a, u0, u1), Mth.lerp(b, u0, u1), v0, v1, color, light);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            return new WindLineParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
