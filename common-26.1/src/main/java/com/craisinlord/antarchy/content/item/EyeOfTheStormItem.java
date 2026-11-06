package com.craisinlord.antarchy.content.item;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.InteractionResult;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.entity.vortex.WindVortexEntity;
import com.craisinlord.anthology.gravity.GravityApi;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class EyeOfTheStormItem extends Item implements GeoItem {
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "eye_of_the_storm");
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "textures/item/eye_of_the_storm/eye_of_the_storm.png");
    private static final Identifier ANIMATION_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "eye_of_the_storm");
    private static final String IDLE_CONTROLLER = "idle_controller";
    private static final String IDLE_ANIMATION = "spinning_idle";
    private static final float SURGE_PROJECTILE_SPEED = 0.65F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public EyeOfTheStormItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!AntarchySettings.eyeOfTheStormEnabled()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        WindVortexEntity vortex = WindVortexEntity.create(
                serverLevel,
                AntarchyObjects.WIND_VORTEX.get(),
                player.position(),
                Vec3.ZERO,
                player,
                false
        );
        vortex.setMode(WindVortexEntity.VortexMode.UPWARD);
        vortex.setAxis(GravityApi.isGravityInverted(player) ? Direction.DOWN : Direction.UP);
        vortex.setVortexSize((float) AntarchySettings.eyeOfTheStormUpdraftHeight(), (float) AntarchySettings.eyeOfTheStormUpdraftRadius());
        vortex.setVortexDurationTicks(AntarchySettings.eyeOfTheStormUpdraftDurationTicks());
        serverLevel.addFreshEntity(vortex);

        Vec3 gravityUp = Vec3.atLowerCornerOf(
                (GravityApi.isGravityInverted(player) ? Direction.DOWN : Direction.UP).getUnitVec3i());
        GravityApi.setWorldVelocity(player,
                GravityApi.getWorldVelocity(player).add(gravityUp.scale(AntarchySettings.eyeOfTheStormUpdraftLaunchStrength())));
        player.needsSync = true;
        player.resetFallDistance();
        player.getCooldowns().addCooldown(stack, AntarchySettings.eyeOfTheStormUpdraftCooldownTicks());
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.9F, 1.1F);

        player.awardStat(Stats.ITEM_USED.get(this));
        stack.hurtAndBreak(1, player, usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return InteractionResult.CONSUME;
    }

    public void firePrimary(ServerLevel level, Player player, ItemStack stack) {
        if (!AntarchySettings.eyeOfTheStormEnabled() || player.getMainHandItem() != stack) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return;
        }

        Vec3 look = player.getViewVector(1.0F);
        launchStormVortex(level, player.getEyePosition().add(look), look, player);
        player.getCooldowns().addCooldown(stack, AntarchySettings.eyeOfTheStormSurgeCooldownTicks());

        player.awardStat(Stats.ITEM_USED.get(this));
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    public static void launchStormVortex(ServerLevel level, Vec3 origin, Vec3 direction, @Nullable LivingEntity owner) {
        if (!AntarchySettings.eyeOfTheStormEnabled()) {
            return;
        }
        Vec3 dir = direction.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();

        WindVortexEntity vortex = WindVortexEntity.create(
                level,
                AntarchyObjects.WIND_VORTEX.get(),
                origin,
                Vec3.ZERO,
                owner,
                true
        );
        vortex.setMode(WindVortexEntity.VortexMode.UPWARD);
        vortex.setAxis(owner != null && GravityApi.isGravityInverted(owner) ? Direction.DOWN : Direction.UP);
        vortex.setVortexSize((float) AntarchySettings.eyeOfTheStormSurgeHeight(), (float) AntarchySettings.eyeOfTheStormSurgeRadius());
        vortex.setVortexDurationTicks(AntarchySettings.eyeOfTheStormSurgeDurationTicks());
        vortex.setVortexStrengths(AntarchySettings.eyeOfTheStormSurgePullStrength(), AntarchySettings.eyeOfTheStormSurgeReturnStrength());
        vortex.setDamageOverride(AntarchySettings.eyeOfTheStormSurgeDamage());
        vortex.setTravel(dir.scale(SURGE_PROJECTILE_SPEED));
        vortex.setHoming(true);
        vortex.setBounceOnImpact(true);
        level.addFreshEntity(vortex);

        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.eye_of_the_storm.updraft").withStyle(ChatFormatting.DARK_AQUA));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.eye_of_the_storm.surge").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<EyeOfTheStormItem>(IDLE_CONTROLLER, state ->
                state.setAndContinue(RawAnimation.begin().thenLoop(IDLE_ANIMATION))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<EyeOfTheStormItem> renderer;

            @Override
            public com.geckolib.renderer.GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }
}
