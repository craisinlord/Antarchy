package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.util.AntarchyLivingRenderStateAccess;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements AntarchyLivingRenderStateAccess {
    @Unique
    private boolean antarchy$onHoverboard;
    @Unique
    private boolean antarchy$glimmering;
    @Unique
    private boolean antarchy$tigerEyeCamouflaged;

    @Override
    public boolean antarchy$isOnHoverboard() {
        return this.antarchy$onHoverboard;
    }

    @Override
    public void antarchy$setOnHoverboard(boolean onHoverboard) {
        this.antarchy$onHoverboard = onHoverboard;
    }

    @Override
    public boolean antarchy$isGlimmering() {
        return this.antarchy$glimmering;
    }

    @Override
    public void antarchy$setGlimmering(boolean glimmering) {
        this.antarchy$glimmering = glimmering;
    }

    @Override
    public boolean antarchy$isTigerEyeCamouflaged() {
        return this.antarchy$tigerEyeCamouflaged;
    }

    @Override
    public void antarchy$setTigerEyeCamouflaged(boolean camouflaged) {
        this.antarchy$tigerEyeCamouflaged = camouflaged;
    }
}
