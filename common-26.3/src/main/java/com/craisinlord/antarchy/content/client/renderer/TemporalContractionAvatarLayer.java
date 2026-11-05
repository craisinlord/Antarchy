package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.anthology.time.client.ContractionAfterimages;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class TemporalContractionAvatarLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final float BASE_ALPHA = 0.42F;
    private static final int SAMPLE_STRIDE = 2;
    private final LivingEntityRenderer<?, AvatarRenderState, PlayerModel> livingRenderer;

    public TemporalContractionAvatarLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
        this.livingRenderer = (LivingEntityRenderer<?, AvatarRenderState, PlayerModel>) renderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible || Minecraft.getInstance().level == null) {
            return;
        }
        if (!(Minecraft.getInstance().level.getEntity(state.id) instanceof Player player) || !ContractionAfterimages.isActive(player)) {
            return;
        }
        Deque<ContractionAfterimages.Sample> samples = ContractionAfterimages.samples(player);
        if (samples == null || samples.isEmpty()) {
            return;
        }

        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        double curX = Mth.lerp(partialTick, player.xo, player.getX());
        double curY = Mth.lerp(partialTick, player.yo, player.getY());
        double curZ = Mth.lerp(partialTick, player.zo, player.getZ());
        int now = (int) (player.level().getGameTime() & 0x7fffffffL);
        Matrix4f inverse = new Matrix4f(poseStack.last().pose()).invert();
        Identifier texture = this.livingRenderer.getTextureLocation(state);
        RenderType type = RenderTypes.entityTranslucent(texture);
        PlayerModel model = this.getParentModel();
        int index = 0;
        for (ContractionAfterimages.Sample sample : samples) {
            if (index++ % SAMPLE_STRIDE != 0) {
                continue;
            }
            double dx = sample.x - curX;
            double dy = sample.y - curY;
            double dz = sample.z - curZ;
            if (dx * dx + dy * dy + dz * dz < 0.0025D) {
                continue;
            }
            float fade = ContractionAfterimages.fade(sample, now);
            if (fade <= 0.02F) {
                continue;
            }
            Vector3f local = inverse.transformDirection(new Vector3f((float) dx, (float) dy, (float) dz));
            int color = ((int) (BASE_ALPHA * fade * 255.0F) & 0xFF) << 24 | ContractionAfterimages.colorFor(player);
            poseStack.pushPose();
            poseStack.translate(local.x, local.y, local.z);
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
                float oldWalkPos = state.walkAnimationPos;
                float oldWalkSpeed = state.walkAnimationSpeed;
                float oldAge = state.ageInTicks;
                float oldBodyRot = state.bodyRot;
                float oldYRot = state.yRot;
                float oldXRot = state.xRot;
                state.walkAnimationPos = sample.limbPos;
                state.walkAnimationSpeed = sample.limbSpeed;
                state.ageInTicks = sample.ageInTicks;
                state.bodyRot = sample.bodyYaw;
                state.yRot = sample.headYaw;
                state.xRot = sample.xRot;
                model.setupAnim(state);
                PoseStack renderPose = new PoseStack();
                renderPose.last().set(pose);
                model.renderToBuffer(renderPose, buffer, LightCoordsUtil.FULL_BRIGHT, 0, color);
                state.walkAnimationPos = oldWalkPos;
                state.walkAnimationSpeed = oldWalkSpeed;
                state.ageInTicks = oldAge;
                state.bodyRot = oldBodyRot;
                state.yRot = oldYRot;
                state.xRot = oldXRot;
                model.setupAnim(state);
            });
            poseStack.popPose();
        }
    }
}
