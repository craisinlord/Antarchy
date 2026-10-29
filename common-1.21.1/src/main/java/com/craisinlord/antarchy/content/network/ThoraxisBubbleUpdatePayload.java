package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.weather.ThoraxisBubble;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThoraxisBubbleUpdatePayload(long seed, List<ThoraxisBubble> bubbles) implements CustomPacketPayload {
    public static final Type<ThoraxisBubbleUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis_bubble_update"));
    public static final StreamCodec<ByteBuf, ThoraxisBubbleUpdatePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeLong(payload.seed);
                buf.writeInt(payload.bubbles.size());
                for (ThoraxisBubble bubble : payload.bubbles) {
                    ThoraxisBubble.STREAM_CODEC.encode(buf, bubble);
                }
            },
            buf -> {
                long seed = buf.readLong();
                int count = buf.readInt();
                List<ThoraxisBubble> bubbles = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    bubbles.add(ThoraxisBubble.STREAM_CODEC.decode(buf));
                }
                return new ThoraxisBubbleUpdatePayload(seed, bubbles);
            }
    );

    @Override
    public Type<ThoraxisBubbleUpdatePayload> type() {
        return TYPE;
    }
}
