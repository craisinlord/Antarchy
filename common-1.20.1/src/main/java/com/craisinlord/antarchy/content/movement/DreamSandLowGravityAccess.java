package com.craisinlord.antarchy.content.movement;

public interface DreamSandLowGravityAccess {
    boolean antarchy$isDreamSandLowGravityActive();

    void antarchy$setDreamSandLowGravityActive(boolean active);

    int antarchy$getDreamSandLowGravityTicksRemaining();

    void antarchy$setDreamSandLowGravityTicksRemaining(int ticks);

    int antarchy$getDreamSandLandingGraceTicks();

    void antarchy$setDreamSandLandingGraceTicks(int ticks);

    default void antarchy$clearDreamSandLowGravity() {
        antarchy$setDreamSandLowGravityActive(false);
        antarchy$setDreamSandLowGravityTicksRemaining(0);
        antarchy$setDreamSandLandingGraceTicks(0);
    }
}
