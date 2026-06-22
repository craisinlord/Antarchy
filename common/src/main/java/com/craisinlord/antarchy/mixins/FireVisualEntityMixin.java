package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.fire.AntarchyFireVisualAccess;
import com.craisinlord.antarchy.content.fire.AntarchyFireVisualType;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class FireVisualEntityMixin implements AntarchyFireVisualAccess {
    @Unique
    private AntarchyFireVisualType antarchy$fireVisualType = AntarchyFireVisualType.NORMAL;

    @Override
    public AntarchyFireVisualType antarchy$getFireVisualType() {
        return this.antarchy$fireVisualType;
    }

    @Override
    public void antarchy$setFireVisualType(AntarchyFireVisualType type) {
        this.antarchy$fireVisualType = type == null ? AntarchyFireVisualType.NORMAL : type;
    }

    @Inject(method = "setRemainingFireTicks", at = @At("TAIL"))
    private void antarchy$resetFireVisualWhenExtinguished(int ticks, CallbackInfo ci) {
        if (ticks <= 0) {
            this.antarchy$fireVisualType = AntarchyFireVisualType.NORMAL;
        }
    }
}
