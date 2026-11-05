package com.craisinlord.antarchy.content.client;

public final class PrimordialGrassTrampleField {
    public static final int MAX_POINTS = 32;
    public static final float GRASS_HEIGHT = 1.125F;
    public static final float LEAN = 0.7F;
    public static final float DROP = 0.24F;
    public static final float DRAG = 0.35F;
    public static final float WIND_DAMPING = 0.6F;
    public static final float FLUTTER = 0.1F;
    public static final double RANGE = 48.0D;

    private static final float PRESS_STIFFNESS = 0.12F;
    private static final float PRESS_DAMPING = 0.42F;
    private static final float RELEASE_STIFFNESS = 0.016F;
    private static final float RELEASE_DAMPING = 0.13F;
    private static final float REST_VALUE = 0.004F;
    private static final float REST_VELOCITY = 0.002F;
    private static final float HEADING_BLEND = 0.25F;
    private static final double REFRESH_HORIZONTAL_SQR = 0.9D * 0.9D;
    private static final double REFRESH_VERTICAL = 1.0D;

    private final int[] entityIds = new int[MAX_POINTS];
    private final double[] xs = new double[MAX_POINTS];
    private final double[] ys = new double[MAX_POINTS];
    private final double[] zs = new double[MAX_POINTS];
    private final float[] radii = new float[MAX_POINTS];
    private final float[] headingXs = new float[MAX_POINTS];
    private final float[] headingZs = new float[MAX_POINTS];
    private final float[] values = new float[MAX_POINTS];
    private final float[] previous = new float[MAX_POINTS];
    private final float[] velocities = new float[MAX_POINTS];
    private final boolean[] held = new boolean[MAX_POINTS];
    private int count;

    public void clear() {
        count = 0;
    }

    public void stamp(int entityId, double x, double y, double z, float radius, float headingX, float headingZ) {
        for (int i = 0; i < count; i++) {
            if (entityIds[i] != entityId || Math.abs(ys[i] - y) > REFRESH_VERTICAL) {
                continue;
            }
            double dx = xs[i] - x;
            double dz = zs[i] - z;
            if (dx * dx + dz * dz <= REFRESH_HORIZONTAL_SQR) {
                held[i] = true;
                headingXs[i] += (headingX - headingXs[i]) * HEADING_BLEND;
                headingZs[i] += (headingZ - headingZs[i]) * HEADING_BLEND;
                return;
            }
        }
        int slot = count < MAX_POINTS ? count++ : weakestPoint();
        entityIds[slot] = entityId;
        xs[slot] = x;
        ys[slot] = y;
        zs[slot] = z;
        radii[slot] = radius;
        headingXs[slot] = headingX;
        headingZs[slot] = headingZ;
        values[slot] = 0.0F;
        previous[slot] = 0.0F;
        velocities[slot] = 0.0F;
        held[slot] = true;
    }

    public void advance() {
        for (int i = count - 1; i >= 0; i--) {
            boolean pressed = held[i];
            held[i] = false;
            float target = pressed ? 1.0F : 0.0F;
            float stiffness = pressed ? PRESS_STIFFNESS : RELEASE_STIFFNESS;
            float damping = pressed ? PRESS_DAMPING : RELEASE_DAMPING;
            previous[i] = values[i];
            velocities[i] += stiffness * (target - values[i]) - damping * velocities[i];
            values[i] += velocities[i];
            if (!pressed && Math.abs(values[i]) < REST_VALUE && Math.abs(velocities[i]) < REST_VELOCITY) {
                removeAt(i);
            }
        }
    }

    public int write(double camX, double camY, double camZ, float partialTick, float[] positions, float[] motions) {
        for (int i = 0; i < count; i++) {
            int offset = i * 4;
            positions[offset] = (float) (xs[i] - camX);
            positions[offset + 1] = (float) (ys[i] - camY);
            positions[offset + 2] = (float) (zs[i] - camZ);
            positions[offset + 3] = radii[i];
            int motion = i * 3;
            motions[motion] = headingXs[i];
            motions[motion + 1] = headingZs[i];
            motions[motion + 2] = previous[i] + (values[i] - previous[i]) * partialTick;
        }
        return count;
    }

    public static void evaluate(float[] positions, float[] motions, int count, float rootX, float rootY, float rootZ, float[] out) {
        float pushX = 0.0F;
        float pushZ = 0.0F;
        for (int i = 0; i < count; i++) {
            int offset = i * 4;
            float vertical = 1.0F - smoothstep(1.0F, 1.6F, Math.abs(rootY - positions[offset + 1]));
            if (vertical <= 0.0F) {
                continue;
            }
            float dx = rootX - positions[offset];
            float dz = rootZ - positions[offset + 2];
            float dist = (float) Math.sqrt(dx * dx + dz * dz);
            float r = positions[offset + 3];
            int motion = i * 3;
            float w = motions[motion + 2] * vertical * (1.0F - smoothstep(r * 0.35F, r, dist));
            float span = Math.max(dist, 0.3F);
            pushX += w * (dx / span + motions[motion] * DRAG);
            pushZ += w * (dz / span + motions[motion + 1] * DRAG);
        }
        float lean = (float) Math.sqrt(pushX * pushX + pushZ * pushZ);
        if (lean > 1.0F) {
            pushX /= lean;
            pushZ /= lean;
            lean = 1.0F;
        }
        out[0] = pushX;
        out[1] = pushZ;
        out[2] = lean;
    }

    public static float bend(float height) {
        return height * (0.35F + 0.65F * height);
    }

    public static float variation(float hash) {
        return 0.8F + 0.35F * hash;
    }

    public static float tuftHash(int x, int z) {
        int hash = x * 73856093 ^ z * 19349663;
        hash ^= hash >>> 13;
        hash *= 0x5BD1E995;
        hash ^= hash >>> 15;
        return (hash & 255) / 255.0F;
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Math.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private int weakestPoint() {
        int weakest = 0;
        float lowest = Float.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            float score = Math.abs(values[i]) + (held[i] ? 10.0F : 0.0F);
            if (score < lowest) {
                lowest = score;
                weakest = i;
            }
        }
        return weakest;
    }

    private void removeAt(int index) {
        int last = --count;
        entityIds[index] = entityIds[last];
        xs[index] = xs[last];
        ys[index] = ys[last];
        zs[index] = zs[last];
        radii[index] = radii[last];
        headingXs[index] = headingXs[last];
        headingZs[index] = headingZs[last];
        values[index] = values[last];
        previous[index] = previous[last];
        velocities[index] = velocities[last];
        held[index] = held[last];
    }
}
