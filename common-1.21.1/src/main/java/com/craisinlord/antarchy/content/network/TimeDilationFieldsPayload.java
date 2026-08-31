package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimeDilationFieldsPayload(List<TimeDilationFieldSnapshot> fields) implements CustomPacketPayload {
    public static final Type<TimeDilationFieldsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "time_dilation_fields"));

    private static final StreamCodec<ByteBuf, TimeDilationFieldSnapshot> FIELD_CODEC = StreamCodec.of(
            (buf, field) -> {
                buf.writeDouble(field.x());
                buf.writeDouble(field.y());
                buf.writeDouble(field.z());
                buf.writeDouble(field.radius());
                buf.writeDouble(field.rate());
                buf.writeInt(field.age());
                buf.writeInt(field.durationTicks());
            },
            buf -> new TimeDilationFieldSnapshot(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readInt(), buf.readInt()
            )
    );

    public static final StreamCodec<ByteBuf, TimeDilationFieldsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(java.util.ArrayList::new, FIELD_CODEC, 128),
            TimeDilationFieldsPayload::fields,
            TimeDilationFieldsPayload::new
    );

    public TimeDilationFieldsPayload {
        fields = List.copyOf(fields);
    }

    @Override
    public Type<TimeDilationFieldsPayload> type() {
        return TYPE;
    }
}
