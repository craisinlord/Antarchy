package com.craisinlord.antarchy.content.client.renderer;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class TemporalContractionAvatarLayer extends TemporalContractionAfterimageLayer<AvatarRenderState, PlayerModel> {
    public TemporalContractionAvatarLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }
}
