package com.craisinlord.antarchy.content.time;

public interface TimeDilationEntityAccess {
    double antarchy$getTimeDilationRate();

    void antarchy$setTimeDilationRate(double rate);

    boolean antarchy$consumeTimeDilationTick(String timerKey, double rate);

    boolean antarchy$isInTimeDilationMove();

    void antarchy$setInTimeDilationMove(boolean inMove);

    boolean antarchy$isApplyingExternalImpulse();

    void antarchy$setApplyingExternalImpulse(boolean applying);
}
