package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.craisinlord.antarchy.content.entity.vortex.WindVortexEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EyeOfTheStormItem extends Item implements GeoItem {
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/eye_of_the_storm.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/item/eye_of_the_storm/eye_of_the_storm.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/eye_of_the_storm.animation.json");
    private static final String IDLE_CONTROLLER = "idle_controller";
    private static final String IDLE_ANIMATION = "spinning_idle";
    private static final Map<UUID, Long> LAST_UPDRAFT_TICK = new ConcurrentHashMap<>();
    private static final float SURGE_PROJECTILE_SPEED = 0.65F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public EyeOfTheStormItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!AntarchySettings.eyeOfTheStormEnabled()) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }

        long now = level.getGameTime();
        Long last = LAST_UPDRAFT_TICK.get(player.getUUID());
        if (last != null && now - last < AntarchySettings.eyeOfTheStormUpdraftCooldownTicks()) {
            return InteractionResultHolder.fail(stack);
        }
        LAST_UPDRAFT_TICK.put(player.getUUID(), now);

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
        vortex.setAxis(Direction.UP);
        vortex.setVortexSize((float) AntarchySettings.eyeOfTheStormUpdraftHeight(), (float) AntarchySettings.eyeOfTheStormUpdraftRadius());
        vortex.setVortexDurationTicks(AntarchySettings.eyeOfTheStormUpdraftDurationTicks());
        serverLevel.addFreshEntity(vortex);

        player.setDeltaMovement(player.getDeltaMovement().add(0.0D, AntarchySettings.eyeOfTheStormUpdraftLaunchStrength(), 0.0D));
        player.hasImpulse = true;
        player.resetFallDistance();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.9F, 1.1F);

        player.awardStat(Stats.ITEM_USED.get(this));
        stack.hurtAndBreak(1, player, usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return InteractionResultHolder.consume(stack);
    }

    public void firePrimary(ServerLevel level, Player player, ItemStack stack) {
        if (!AntarchySettings.eyeOfTheStormEnabled() || player.getMainHandItem() != stack) {
            return;
        }

        Vec3 look = player.getViewVector(1.0F);
        launchStormVortex(level, player.getEyePosition().add(look), look, player);

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
        vortex.setAxis(Direction.UP);
        vortex.setVortexSize((float) AntarchySettings.eyeOfTheStormSurgeHeight(), (float) AntarchySettings.eyeOfTheStormSurgeRadius());
        vortex.setVortexDurationTicks(AntarchySettings.eyeOfTheStormSurgeDurationTicks());
        vortex.setVortexStrengths(AntarchySettings.eyeOfTheStormSurgePullStrength(), AntarchySettings.eyeOfTheStormSurgeReturnStrength());
        vortex.setDamageOverride(AntarchySettings.eyeOfTheStormSurgeDamage());
        vortex.setTravel(dir.scale(SURGE_PROJECTILE_SPEED));
        vortex.setHoming(true);
        level.addFreshEntity(vortex);

        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.eye_of_the_storm.updraft").withStyle(ChatFormatting.DARK_AQUA));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.eye_of_the_storm.surge").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, IDLE_CONTROLLER, state ->
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
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }
                return this.renderer;
            }
        });
    }
}
