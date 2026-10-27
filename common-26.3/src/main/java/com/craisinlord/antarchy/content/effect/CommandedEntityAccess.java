package com.craisinlord.antarchy.content.effect;

import java.util.UUID;

public interface CommandedEntityAccess {
    UUID antarchy$getCommanderUuid();
    void antarchy$setCommanderUuid(UUID uuid);
    boolean antarchy$isCommandedTargetOwned();
    void antarchy$setCommandedTargetOwned(boolean owned);
    boolean antarchy$isRoyalInvested();
    void antarchy$setRoyalInvested(boolean invested);
}
