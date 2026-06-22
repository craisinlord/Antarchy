package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;

public final class KrakenMultipartLayout implements MultipartLayout {
    public static final KrakenMultipartLayout INSTANCE = new KrakenMultipartLayout();

    public static final MultipartPartDefinition HEAD = new MultipartPartDefinition(
            "head",
            3.0F,
            2.8F,
            1.00F,
            2.15D,
            5.35D,
            0.00D,
            true
    );

    public static final MultipartPartDefinition BACK_HEAD = new MultipartPartDefinition(
            "back_head",
            4.8F,
            4.4F,
            1.10F,
            0.70D,
            6.55D,
            0.00D,
            true
    );

    public static final MultipartPartDefinition TENTACLE_MASS = new MultipartPartDefinition(
            "tentacle_mass",
            8.5F,
            3.4F,
            0.90F,
            -0.35D,
            1.70D,
            0.00D,
            true
    );

    private static final MultipartPartDefinition[] PARTS = {
            HEAD,
            BACK_HEAD,
            TENTACLE_MASS
    };

    private KrakenMultipartLayout() {
    }

    @Override
    public MultipartPartDefinition[] parts() {
        return PARTS;
    }
}
