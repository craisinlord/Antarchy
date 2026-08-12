package com.craisinlord.antarchy.content.entity.multipart;

public record MultipartPartDefinition(
        String name,
        float width,
        float height,
        float damageMultiplier,
        double forwardOffset,
        double yOffset,
        double lateralOffset,
        boolean collisionEnabled
) {
}
