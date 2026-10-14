package com.craisinlord.antarchy.content.block.entity.undervault;

import net.minecraft.util.Mth;

public class UnderVaultClientData {
    public static final float ROTATION_SPEED = 10.0F;
    private float currentSpin;
    private float previousSpin;

    public float currentSpin() {
        return this.currentSpin;
    }

    public float previousSpin() {
        return this.previousSpin;
    }

    void updateDisplayItemSpin() {
        this.previousSpin = this.currentSpin;
        this.currentSpin = Mth.wrapDegrees(this.currentSpin + ROTATION_SPEED);
    }
}
