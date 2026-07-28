package com.craisinlord.antarchy.fabric.entity.multipart;

import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;

public final class FabricKrakenMultipartLayout {
    private static final MultipartPartDefinition[] PARTS = {
            new MultipartPartDefinition(
                    "tentacle_mass",
                    14.0F,
                    18.0F,
                    0.90F,
                    12.0D,
                    13.5D,
                    0.00D,
                    true
            ),
            new MultipartPartDefinition(
                    "head",
                    15.0F,
                    11.5F,
                    1.00F,
                    26.0D,
                    19.0D,
                    0.00D,
                    true
            ),
            new MultipartPartDefinition(
                    "back_head",
                    13.0F,
                    11.0F,
                    1.10F,
                    38.0D,
                    20.5D,
                    0.00D,
                    true
            ),
            new MultipartPartDefinition(
                    "mantle",
                    16.0F,
                    14.0F,
                    1.00F,
                    51.0D,
                    23.0D,
                    0.00D,
                    true
            ),
            new MultipartPartDefinition(
                    "crest",
                    12.0F,
                    10.0F,
                    1.10F,
                    63.0D,
                    28.0D,
                    0.00D,
                    true
            )
    };

    private FabricKrakenMultipartLayout() {
    }

    public static MultipartPartDefinition part(int partIndex) {
        return PARTS[partIndex];
    }
}
