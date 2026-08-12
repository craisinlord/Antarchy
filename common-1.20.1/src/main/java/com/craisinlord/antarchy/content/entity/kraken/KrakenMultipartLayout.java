package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;

public final class KrakenMultipartLayout implements MultipartLayout {
    public static final KrakenMultipartLayout INSTANCE = new KrakenMultipartLayout();

    public static final MultipartPartDefinition TENTACLE_MASS = new MultipartPartDefinition(
            "tentacle_mass",
            7.5F,
            14.0F,
            0.90F,
            -0.35D,
            14.00D,
            0.00D,
            true
    );

    public static final MultipartPartDefinition HEAD = new MultipartPartDefinition(
            "head",
            9.0F,
            4.0F,
            1.00F,
            1.50D,
            28.00D,
            0.00D,
            true
    );

    public static final MultipartPartDefinition BACK_HEAD = new MultipartPartDefinition(
            "back_head",
            5.5F,
            4.5F,
            1.10F,
            0.70D,
            31.00D,
            0.00D,
            true
    );

    public static final MultipartPartDefinition MANTLE = new MultipartPartDefinition(
            "mantle",
            8.0F,
            7.0F,
            1.00F,
            0.60D,
            34.50D,
            0.00D,
            true
    );

    public static final MultipartPartDefinition CREST = new MultipartPartDefinition(
            "crest",
            4.5F,
            4.0F,
            1.10F,
            0.55D,
            41.50D,
            0.00D,
            true
    );

    private static final MultipartPartDefinition[] PARTS = {
            TENTACLE_MASS,
            HEAD,
            BACK_HEAD,
            MANTLE,
            CREST
    };

    private KrakenMultipartLayout() {
    }

    @Override
    public MultipartPartDefinition[] parts() {
        return PARTS;
    }
}
