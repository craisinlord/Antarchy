package com.craisinlord.antarchy.compat.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public final class ByteBufCodecs {
    private ByteBufCodecs() {
    }

    public static final StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<>() {
        @Override
        public Integer decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readVarInt();
        }

        @Override
        public void encode(ByteBuf buf, Integer value) {
            ((FriendlyByteBuf) buf).writeVarInt(value);
        }
    };

    public static final StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<>() {
        @Override
        public Long decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readVarLong();
        }

        @Override
        public void encode(ByteBuf buf, Long value) {
            ((FriendlyByteBuf) buf).writeVarLong(value);
        }
    };

    public static final StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<>() {
        @Override
        public Boolean decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readBoolean();
        }

        @Override
        public void encode(ByteBuf buf, Boolean value) {
            ((FriendlyByteBuf) buf).writeBoolean(value);
        }
    };

    public static final StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<>() {
        @Override
        public Byte decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readByte();
        }

        @Override
        public void encode(ByteBuf buf, Byte value) {
            ((FriendlyByteBuf) buf).writeByte(value);
        }
    };

    public static final StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<>() {
        @Override
        public Float decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readFloat();
        }

        @Override
        public void encode(ByteBuf buf, Float value) {
            ((FriendlyByteBuf) buf).writeFloat(value);
        }
    };

    public static final StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<>() {
        @Override
        public Double decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readDouble();
        }

        @Override
        public void encode(ByteBuf buf, Double value) {
            ((FriendlyByteBuf) buf).writeDouble(value);
        }
    };

    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = new StreamCodec<>() {
        @Override
        public String decode(ByteBuf buf) {
            return ((FriendlyByteBuf) buf).readUtf();
        }

        @Override
        public void encode(ByteBuf buf, String value) {
            ((FriendlyByteBuf) buf).writeUtf(value);
        }
    };
}
