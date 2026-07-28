package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;

public final class NightmareMultipartLayout implements MultipartLayout {
    public static final NightmareMultipartLayout INSTANCE = new NightmareMultipartLayout();

    public static final MultipartPartDefinition LEFT_WING = new MultipartPartDefinition(
            "left_wing",
            2.0F,
            1.0F,
            1.00F,
            0.10D,
            3.05D,
            -1.90D,
            false
    );

    public static final MultipartPartDefinition LEFT_WING_OUTER = new MultipartPartDefinition(
            "left_wing_outer",
            1.55F,
            0.95F,
            0.90F,
            -0.25D,
            3.20D,
            -3.70D,
            false
    );

    public static final MultipartPartDefinition RIGHT_WING = new MultipartPartDefinition(
            "right_wing",
            2.0F,
            1.0F,
            1.00F,
            0.10D,
            3.05D,
            1.90D,
            false
    );

    public static final MultipartPartDefinition RIGHT_WING_OUTER = new MultipartPartDefinition(
            "right_wing_outer",
            1.55F,
            0.95F,
            0.90F,
            -0.25D,
            3.20D,
            3.70D,
            false
    );

    private static final MultipartPartDefinition[] PARTS = {
            LEFT_WING,
            LEFT_WING_OUTER,
            RIGHT_WING,
            RIGHT_WING_OUTER
    };

    private NightmareMultipartLayout() {
    }

    @Override
    public MultipartPartDefinition[] parts() {
        return PARTS;
    }
}
