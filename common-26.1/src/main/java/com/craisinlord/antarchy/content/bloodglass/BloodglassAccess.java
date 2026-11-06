package com.craisinlord.antarchy.content.bloodglass;

public interface BloodglassAccess {
    int antarchy$getArmorShieldsActive();
    void antarchy$setArmorShieldsActive(int count);
    int antarchy$getArmorShieldLostCount();
    void antarchy$setArmorShieldLostCount(int count);
    int antarchy$getArmorRechargeTimer();
    void antarchy$setArmorRechargeTimer(int ticks);

    int antarchy$getAppleShieldsActive();
    void antarchy$setAppleShieldsActive(int count);
    int antarchy$getAppleShieldLostCount();
    void antarchy$setAppleShieldLostCount(int count);
    int antarchy$getAppleRechargeTimer();
    void antarchy$setAppleRechargeTimer(int ticks);

    default int antarchy$getTotalShieldsActive() {
        return antarchy$getArmorShieldsActive() + antarchy$getAppleShieldsActive();
    }

    default int antarchy$getTotalShieldsMax() {
        return antarchy$getArmorShieldsActive() + antarchy$getArmorShieldLostCount()
                + antarchy$getAppleShieldsActive() + antarchy$getAppleShieldLostCount();
    }
}
