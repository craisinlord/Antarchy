package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record TimeDilationFieldsPayload(List<TimeDilationFieldSnapshot> fields) implements CustomPacketPayload {
    public static final Type<TimeDilationFieldsPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "time_dilation_fields"));
    public static final StreamCodec<ByteBuf, TimeDilationFieldsPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public TimeDilationFieldsPayload decode(ByteBuf buf) {
            int count = Math.min(buf.readInt(), 128);
            List<TimeDilationFieldSnapshot> fields = new ArrayList<>(count);
            for (int i = 0; i < count; i++) fields.add(new TimeDilationFieldSnapshot(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readInt()));
            return new TimeDilationFieldsPayload(fields);
        }
        @Override public void encode(ByteBuf buf, TimeDilationFieldsPayload value) {
            int count = Math.min(value.fields().size(), 128);
            buf.writeInt(count);
            for (int i = 0; i < count; i++) { TimeDilationFieldSnapshot f = value.fields().get(i); buf.writeDouble(f.x()); buf.writeDouble(f.y()); buf.writeDouble(f.z()); buf.writeDouble(f.radius()); buf.writeDouble(f.rate()); buf.writeInt(f.age()); buf.writeInt(f.durationTicks()); }
        }
    };
    public TimeDilationFieldsPayload { fields = List.copyOf(fields); }
    @Override public Type<TimeDilationFieldsPayload> type() { return TYPE; }
}
