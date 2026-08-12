package com.craisinlord.antarchy.content.gravity;

public interface AntarchyGravityAccess {
    AntarchyGravityDirection antarchy$getGravityDirection();

    AntarchyGravityDirection antarchy$getPrevGravityDirection();

    boolean antarchy$isGravityForced();

    int antarchy$getGravityTransitionDuration();

    int antarchy$getGravityTransitionRemaining();

    void antarchy$setGravityState(AntarchyGravityDirection direction, boolean forced, AntarchyGravityTransition transition);

    void antarchy$applySyncedGravityState(
            AntarchyGravityDirection direction,
            AntarchyGravityDirection previousDirection,
            boolean forced,
            int transitionDuration,
            int transitionRemaining
    );
}
