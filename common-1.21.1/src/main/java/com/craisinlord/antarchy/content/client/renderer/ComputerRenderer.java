package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import com.craisinlord.antarchy.content.client.model.ComputerModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class ComputerRenderer extends GeoBlockRenderer<ComputerBlockEntity> {
    public ComputerRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context) {
        super(new ComputerModel());
    }
}
