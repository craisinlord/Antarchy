package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.portal.PortalShape;
import com.craisinlord.anthology.portal.PortalTransforms;
import com.craisinlord.anthology.portal.client.PortalViewDraw;
import com.craisinlord.anthology.portal.client.PortalViewPipelines;
import com.craisinlord.anthology.portal.client.PortalViewUniforms;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PortalGunMoonViewRenderer {
    private static final Identifier EARTH = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/moon_view/earth.png");
    private static final double SKY_DISTANCE = 100.0D;
    private static final double EARTH_HALF_SIZE = 32.0D;
    private static final double EARTH_PLANET_FRACTION = 0.5D;
    private static final double EARTH_ELEVATION = Math.toRadians(10.0D);
    private static final int STAR_COUNT = 1800;
    private static final Vec3 EXIT_NORMAL = new Vec3(0.0D, 0.0D, 1.0D);
    private static final Vec3 EXIT_UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static float[] stars;

    private PortalGunMoonViewRenderer() {
    }

    public static void render(Minecraft minecraft, RenderTarget target, PortalShape source, Vec3 look, Vec3 up,
                              Matrix4f rootProjection) {
        PortalShape exit = new PortalShape(Vec3.ZERO, EXIT_NORMAL, EXIT_UP, EXIT_NORMAL.cross(EXIT_UP),
                source.halfWidth(), source.halfHeight(), source.halfDepth());
        Vec3 moonLook = PortalTransforms.transformVector(source, exit, look).normalize();
        Vec3 moonUp = PortalTransforms.transformVector(source, exit, up).normalize();
        Matrix4f view = new Matrix4f().rotation(PortalTransforms.orientationQuaternion(moonLook, moonUp).conjugate(new Quaternionf()));
        Vec3 earthDirection = new Vec3(0.0D, Math.sin(EARTH_ELEVATION), Math.cos(EARTH_ELEVATION));
        GpuBufferSlice projection = PortalViewUniforms.projection(rootProjection);
        drawStars(target, view, projection);
        drawEarth(minecraft, target, view, projection, earthDirection);
    }

    private static float[] stars() {
        if (stars == null) {
            RandomSource random = RandomSource.create(7342L);
            float[] data = new float[STAR_COUNT * 5];
            int count = 0;
            while (count < STAR_COUNT) {
                Vector3f direction = new Vector3f(random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F);
                float lengthSquared = direction.lengthSquared();
                if (lengthSquared < 1.0E-4F || lengthSquared > 1.0F) {
                    continue;
                }
                direction.normalize();
                data[count * 5] = direction.x;
                data[count * 5 + 1] = direction.y;
                data[count * 5 + 2] = direction.z;
                data[count * 5 + 3] = 0.12F + random.nextFloat() * 0.22F;
                data[count * 5 + 4] = 0.35F + random.nextFloat() * 0.65F;
                count++;
            }
            stars = data;
        }
        return stars;
    }

    private static void drawStars(RenderTarget target, Matrix4f view, GpuBufferSlice projection) {
        float[] data = stars();
        BufferBuilder buffer = PortalViewDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < STAR_COUNT; i++) {
            Vec3 direction = new Vec3(data[i * 5], data[i * 5 + 1], data[i * 5 + 2]);
            double size = data[i * 5 + 3];
            int brightness = (int) (255.0F * data[i * 5 + 4]);
            Vec3[] basis = basis(direction);
            Vec3 center = direction.scale(SKY_DISTANCE);
            Vec3 right = basis[0].scale(size);
            Vec3 upward = basis[1].scale(size);
            addColored(buffer, center.subtract(right).subtract(upward), brightness);
            addColored(buffer, center.subtract(right).add(upward), brightness);
            addColored(buffer, center.add(right).add(upward), brightness);
            addColored(buffer, center.add(right).subtract(upward), brightness);
        }
        PortalViewDraw.draw(target, PortalViewPipelines.MOON_COLOR, buffer.build(), view, projection, null, null);
    }

    private static void drawEarth(Minecraft minecraft, RenderTarget target, Matrix4f view, GpuBufferSlice projection, Vec3 earthDirection) {
        Vec3[] basis = basis(earthDirection);
        Vec3 center = earthDirection.scale(SKY_DISTANCE);
        Vec3 right = basis[0];
        Vec3 upward = basis[1];

        double planet = EARTH_HALF_SIZE * EARTH_PLANET_FRACTION;
        BufferBuilder backing = PortalViewDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addColored(backing, center.subtract(right.scale(planet)).subtract(upward.scale(planet)), 0);
        addColored(backing, center.subtract(right.scale(planet)).add(upward.scale(planet)), 0);
        addColored(backing, center.add(right.scale(planet)).add(upward.scale(planet)), 0);
        addColored(backing, center.add(right.scale(planet)).subtract(upward.scale(planet)), 0);
        PortalViewDraw.draw(target, PortalViewPipelines.MOON_COLOR, backing.build(), view, projection, null, null);

        BufferBuilder buffer = PortalViewDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
        addTextured(buffer, center.subtract(right.scale(EARTH_HALF_SIZE)).subtract(upward.scale(EARTH_HALF_SIZE)), 0.0F, 1.0F);
        addTextured(buffer, center.subtract(right.scale(EARTH_HALF_SIZE)).add(upward.scale(EARTH_HALF_SIZE)), 0.0F, 0.0F);
        addTextured(buffer, center.add(right.scale(EARTH_HALF_SIZE)).add(upward.scale(EARTH_HALF_SIZE)), 1.0F, 0.0F);
        addTextured(buffer, center.add(right.scale(EARTH_HALF_SIZE)).subtract(upward.scale(EARTH_HALF_SIZE)), 1.0F, 1.0F);
        AbstractTexture earth = minecraft.getTextureManager().getTexture(EARTH);
        PortalViewDraw.draw(target, PortalViewPipelines.MOON_TEXTURE, buffer.build(), view, projection, earth.getTextureView(), earth.getSampler());
    }

    private static Vec3[] basis(Vec3 direction) {
        Vec3 reference = Math.abs(direction.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : EXIT_UP;
        Vec3 right = direction.cross(reference).normalize();
        Vec3 upward = right.cross(direction).normalize();
        return new Vec3[] {right, upward};
    }

    private static void addColored(BufferBuilder buffer, Vec3 position, int brightness) {
        buffer.addVertex((float) position.x, (float) position.y, (float) position.z).setColor(brightness, brightness, brightness, 255);
    }

    private static void addTextured(BufferBuilder buffer, Vec3 position, float u, float v) {
        buffer.addVertex((float) position.x, (float) position.y, (float) position.z).setUv(u, v);
    }
}
