package com.craisinlord.antarchy.content.time;

import java.util.Map;

public interface TimeDilationEntityAccess {
    double antarchy$getTimeDilationCeiling();

    void antarchy$setTimeDilationCeiling(double ceiling);

    Map<String, Double> antarchy$getPersonalTimeDilationRates();

    double antarchy$getTimeDilationRate();

    void antarchy$setTimeDilationRate(double rate);

    double antarchy$getInheritedTimeDilationRate();

    void antarchy$setInheritedTimeDilationRate(double rate);

    boolean antarchy$consumeTimeDilationTick(String timerKey, double rate);

    int antarchy$consumeTimeDilationTicks(String timerKey, double rate);

    boolean antarchy$isInTimeDilationMove();

    void antarchy$setInTimeDilationMove(boolean inMove);

    boolean antarchy$isApplyingExternalImpulse();

    void antarchy$setApplyingExternalImpulse(boolean applying);
}
