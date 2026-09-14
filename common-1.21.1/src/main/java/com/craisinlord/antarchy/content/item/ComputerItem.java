package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.block.ComputerBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public final class ComputerItem extends BlockItem implements GeoItem {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/computer.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/block/computer/computer.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/computer.animation.json");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ComputerItem(ComputerBlock block, Properties properties) {
        super(block, properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "state", state -> state.setAndContinue(RawAnimation.begin().thenLoop("off_state"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<ComputerItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL, TEXTURE, ANIMATION));
                }
                return renderer;
            }
        });
    }
}
