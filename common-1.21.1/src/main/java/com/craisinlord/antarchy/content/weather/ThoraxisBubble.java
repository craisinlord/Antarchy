package com.craisinlord.antarchy.content.weather;

import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record ThoraxisBubble(UUID id, double x, double y, double z, double vx, double vy, double vz,
                             long segment, long born, int life, float radius, float rate, boolean impact) {
    public static final StreamCodec<ByteBuf, ThoraxisBubble> STREAM_CODEC = StreamCodec.of(
            (buf, bubble) -> {
                buf.writeLong(bubble.id.getMostSignificantBits());
                buf.writeLong(bubble.id.getLeastSignificantBits());
                buf.writeDouble(bubble.x);
                buf.writeDouble(bubble.y);
                buf.writeDouble(bubble.z);
                buf.writeDouble(bubble.vx);
                buf.writeDouble(bubble.vy);
                buf.writeDouble(bubble.vz);
                buf.writeLong(bubble.segment);
                buf.writeLong(bubble.born);
                buf.writeInt(bubble.life);
                buf.writeFloat(bubble.radius);
                buf.writeFloat(bubble.rate);
                buf.writeBoolean(bubble.impact);
            },
            buf -> new ThoraxisBubble(new UUID(buf.readLong(), buf.readLong()), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readLong(), buf.readLong(),
                    buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readBoolean())
    );

    public static final double GRAVITY = 0.006D;
    public static final int FADE_TICKS = 40;
    private static final double SWAY = 2.0D;

    public static ThoraxisBubble spawn(UUID id, Vec3 position, double vx, double vz, long time, int life, float radius, float rate) {
        Vec3 sway = sway(id, 0.0D);
        return new ThoraxisBubble(id, position.x - sway.x, position.y, position.z - sway.z, vx, 0.0D, vz,
                time, time, life, radius, rate, false);
    }

    public static double down(double y) {
        return y >= ThoraxisUndersideManager.GRAVITY_FLIP_Y ? -1.0D : 1.0D;
    }

    private static Vec3 sway(UUID id, double age) {
        float phase = (id.hashCode() & 0xFFFF) / 4096.0F;
        return new Vec3(Mth.sin((float) (age * 0.013D) + phase) * SWAY, 0.0D,
                Mth.cos((float) (age * 0.011D) + phase * 0.6F) * SWAY);
    }

    public double down() {
        return down(this.y);
    }

    public Vec3 base(double time) {
        double age = time - this.segment;
        return new Vec3(this.x + this.vx * age,
                this.y + this.vy * age + 0.5D * GRAVITY * this.down() * age * age,
                this.z + this.vz * age);
    }

    public Vec3 position(double time) {
        return this.base(time).add(sway(this.id, time - this.born));
    }

    public double verticalVelocity(double time) {
        return this.vy + GRAVITY * this.down() * (time - this.segment);
    }

    public ThoraxisBubble rebase(long time, Vec3 offset, double vx, double vy, double vz, boolean impact) {
        Vec3 base = this.base(time).add(offset);
        return new ThoraxisBubble(this.id, base.x, base.y, base.z, vx, vy, vz, time, this.born, this.life,
                this.radius, this.rate, impact);
    }

    public ThoraxisBubble withLife(int life) {
        return new ThoraxisBubble(this.id, this.x, this.y, this.z, this.vx, this.vy, this.vz, this.segment, this.born,
                life, this.radius, this.rate, this.impact);
    }

    public boolean expired(long time) {
        return time >= this.born + this.life;
    }

    public boolean fading(long time) {
        return this.born + this.life - time <= FADE_TICKS;
    }

    public float presence(double time) {
        double age = time - this.born;
        float fadeIn = (float) Mth.clamp(age / FADE_TICKS, 0.0D, 1.0D);
        float fadeOut = (float) Mth.clamp((this.born + this.life - time) / FADE_TICKS, 0.0D, 1.0D);
        return Math.min(fadeIn, fadeOut);
    }

    public boolean slow() {
        return this.rate < 1.0F;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", this.id);
        tag.putDouble("X", this.x);
        tag.putDouble("Y", this.y);
        tag.putDouble("Z", this.z);
        tag.putDouble("VX", this.vx);
        tag.putDouble("VY", this.vy);
        tag.putDouble("VZ", this.vz);
        tag.putLong("Segment", this.segment);
        tag.putLong("Born", this.born);
        tag.putInt("Life", this.life);
        tag.putFloat("Radius", this.radius);
        tag.putFloat("Rate", this.rate);
        tag.putBoolean("Impact", this.impact);
        return tag;
    }

    public static ThoraxisBubble load(CompoundTag tag) {
        long born = tag.getLong("Born");
        return new ThoraxisBubble(tag.getUUID("Id"), tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"),
                tag.getDouble("VX"), tag.getDouble("VY"), tag.getDouble("VZ"),
                tag.contains("Segment") ? tag.getLong("Segment") : born, born, tag.getInt("Life"),
                tag.getFloat("Radius"), tag.getFloat("Rate"), tag.getBoolean("Impact"));
    }
}
