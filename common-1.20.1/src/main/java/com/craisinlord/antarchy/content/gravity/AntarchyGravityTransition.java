package com.craisinlord.antarchy.content.gravity;

public record AntarchyGravityTransition(int durationTicks) {
    public static final AntarchyGravityTransition INSTANT = new AntarchyGravityTransition(0);

    public AntarchyGravityTransition {
        durationTicks = Math.max(0, durationTicks);
    }
}
