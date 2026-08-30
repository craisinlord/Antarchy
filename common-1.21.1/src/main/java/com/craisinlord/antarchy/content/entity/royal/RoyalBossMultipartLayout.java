package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;

public final class RoyalBossMultipartLayout implements MultipartLayout {
    public static final RoyalBossMultipartLayout INSTANCE = new RoyalBossMultipartLayout();

    public static final MultipartPartDefinition BODY = new MultipartPartDefinition(
            "body", 20.0F, 24.0F, 1.00F, 0.0D, 3.0D, 0.0D, true);
    public static final MultipartPartDefinition LEFT_HEAD = new MultipartPartDefinition(
            "left_head", 4.0F, 4.0F, 1.30F, 12.0D, 20.0D, -7.0D, true);
    public static final MultipartPartDefinition CENTER_HEAD = new MultipartPartDefinition(
            "center_head", 4.5F, 4.5F, 1.20F, 13.0D, 22.0D, 0.0D, true);
    public static final MultipartPartDefinition RIGHT_HEAD = new MultipartPartDefinition(
            "right_head", 4.0F, 4.0F, 1.30F, 12.0D, 20.0D, 7.0D, true);
    public static final MultipartPartDefinition LEFT_WING = new MultipartPartDefinition(
            "left_wing", 14.0F, 16.0F, 0.85F, -2.0D, 14.0D, -13.0D, true);
    public static final MultipartPartDefinition RIGHT_WING = new MultipartPartDefinition(
            "right_wing", 14.0F, 16.0F, 0.85F, -2.0D, 14.0D, 13.0D, true);

    private static final MultipartPartDefinition[] PARTS = {
            BODY,
            LEFT_HEAD,
            CENTER_HEAD,
            RIGHT_HEAD,
            LEFT_WING,
            RIGHT_WING
    };

    private RoyalBossMultipartLayout() {
    }

    @Override
    public MultipartPartDefinition[] parts() {
        return PARTS;
    }
}
