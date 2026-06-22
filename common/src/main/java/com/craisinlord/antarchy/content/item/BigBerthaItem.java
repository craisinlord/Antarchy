package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BigBerthaItem extends SwordItem implements GeoItem {
    private static final ResourceLocation MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "geo/big_bertha.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/item/big_bertha/big_bertha.png");
    private static final ResourceLocation ANIMATION_LOCATION = ResourceLocation.fromNamespaceAndPath("antarchy", "animations/static_item.animation.json");
    private static final String MODE_TAG = "antarchy.big_bertha_mode";
    private static final String SPIN_TICKS_TAG = "antarchy.big_bertha_spin_ticks";
    private static final String SPIN_DIRECTION_X_TAG = "antarchy.big_bertha_spin_direction_x";
    private static final String SPIN_DIRECTION_Z_TAG = "antarchy.big_bertha_spin_direction_z";
    private static final ResourceLocation ATTACK_RANGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "big_bertha_attack_range");
    private final Tier tier;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BigBerthaItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
        this.tier = tier;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(
                this.tier,
                (int) Math.round(AntarchySettings.bigBerthaAttackDamage()),
                (float) AntarchySettings.bigBerthaAttackSpeed()
        ).withModifierAdded(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ATTACK_RANGE_MODIFIER_ID, AntarchySettings.bigBerthaReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        BossMode mode = getMode(stack);
        tooltipComponents.add(Component.translatable("tooltip.antarchy.big_bertha.mode", Component.translatable(mode.translationKey)).withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable(mode.tooltipKey).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.big_bertha.toggle_mode").withStyle(ChatFormatting.DARK_GRAY));
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleMode(level, player, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        BossMode mode = getMode(stack);

        if (mode == BossMode.BASILISK) {
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }
            applyBasiliskRightClick(level, player, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (mode != BossMode.MOLEVORE) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        startMolevoreSpin(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean didHurt = super.hurtEnemy(stack, target, attacker);
        if (!didHurt || attacker.level().isClientSide || !(attacker instanceof Player player) || player.getMainHandItem() != stack) {
            return didHurt;
        }

        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return didHurt;
        }

        switch (getMode(stack)) {
            case BASILISK -> applyBasiliskHit(target, serverLevel);
            case LUCID -> applyLucidHit(target, player, serverLevel);
            case NIGHTMARE -> applyNightmareHit(target, serverLevel);
            case KRAKEN -> applyKrakenHit(player, target, serverLevel);
            case MOLEVORE -> {
            }
        }

        return didHurt;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }

        int spinTicks = getSpinTicks(stack);
        if (spinTicks <= 0) {
            return;
        }

        if (player.getMainHandItem() != stack) {
            if (!level.isClientSide) {
                clearSpinState(stack);
            }
            return;
        }

        alignPlayerToSpinDirection(player, getStoredSpinDirection(stack));
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            tickMolevoreSpin((ServerLevel) level, serverPlayer, stack, spinTicks);
        }
    }

    private void applyBasiliskRightClick(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide) {
            double range = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 1.0;
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookDir = player.getLookAngle();
            Vec3 endPos = eyePos.add(lookDir.scale(range));
            AABB searchBox = player.getBoundingBox().expandTowards(lookDir.scale(range)).inflate(1.0);
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                    player, eyePos, endPos, searchBox,
                    e -> e != player && !e.isSpectator() && e instanceof LivingEntity,
                    range * range);
            if (hit != null && hit.getEntity() instanceof LivingEntity target) {
                if (!target.getType().is(AntarchyTags.Entities.PARALYSIS_IMMUNE)) {
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 1.2F, 0.6F);
                    target.addEffect(new MobEffectInstance(
                            AntarchyObjects.PARALYZED_EFFECT.get(),
                            AntarchySettings.bigBerthaBasiliskParalyzeDurationTicks(),
                            0,
                            false,
                            true,
                            true));
                }
            }
        }
        int cooldownTicks = Math.round((float) (AntarchySettings.bigBerthaBasiliskCooldownSeconds() * 20));
        player.getCooldowns().addCooldown(this, cooldownTicks);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void applyBasiliskHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(
                MobEffects.POISON,
                AntarchySettings.basiliskDaggerPoisonDurationTicks(),
                AntarchySettings.basiliskDaggerPoisonAmplifier()
        ));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        level.sendParticles(ParticleTypes.SNEEZE, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.25D, 0.2D, 0.25D, 0.02D);
    }

    private void applyLucidHit(LivingEntity target, Player player, ServerLevel level) {
        int effectDurationTicks = (int) Math.round(AntarchySettings.bigBerthaLucidInvertedDurationSeconds() * 20.0D);
        if (effectDurationTicks > 0
                && LucidBoltEntity.invertedEffectSupplier != null
                && !target.getType().is(AntarchyTags.Entities.INVERTED_IMMUNE)) {
            target.addEffect(new MobEffectInstance(
                    LucidBoltEntity.invertedEffectSupplier.get(),
                    effectDurationTicks,
                    0,
                    false,
                    true,
                    true));
        }

        level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY(0.8D), target.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.08D);
        if (!AntarchyGravityApi.isGravityInverted(player)) {
            return;
        }

        double bonusPercent = AntarchySettings.bigBerthaLucidInvertedDamageBonusPercent();
        if (bonusPercent <= 0.0D) {
            return;
        }

        float bonusDamage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (bonusPercent / 100.0D));
        if (bonusDamage <= 0.0F) {
            return;
        }

        target.hurt(player.damageSources().playerAttack(player), bonusDamage);
        target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.08D, 0.0D));
        target.hurtMarked = true;
    }

    private void applyNightmareHit(LivingEntity target, ServerLevel level) {
        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(AntarchyObjects.DREAD.get(), AntarchySettings.nightmareDreadTicks(), 0));
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, AntarchySettings.nightmareWeaknessTicks(), 0));
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.8D), target.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.01D);
        level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
    }

    private void applyKrakenHit(Player attacker, LivingEntity target, ServerLevel level) {
        Vec3 push = target.position().subtract(attacker.position()).multiply(1.0D, 0.0D, 1.0D);
        if (push.lengthSqr() < 1.0E-4D) {
            push = attacker.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        if (push.lengthSqr() >= 1.0E-4D) {
            Vec3 normalized = push.normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(normalized.x * 1.5D, 0.35D, normalized.z * 1.5D));
            target.hurtMarked = true;
        }

        LightningBolt lightningBolt = new LightningBolt(net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, level);
        lightningBolt.moveTo(target.getX(), target.getY(), target.getZ());
        lightningBolt.setVisualOnly(true);
        level.addFreshEntity(lightningBolt);

        target.hurt(attacker.damageSources().lightningBolt(), (float) AntarchySettings.krakenLightningDamagePhaseOne());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, AntarchySettings.bigBerthaKrakenSlowTicks(), 1));
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.7D), target.getZ(), 16, 0.35D, 0.3D, 0.35D, 0.08D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    private void cycleMode(Level level, Player player, ItemStack stack) {
        BossMode nextMode = getMode(stack).next();
        setMode(stack, nextMode);
        clearSpinState(stack);
        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.translatable("message.antarchy.big_bertha.mode", Component.translatable(nextMode.translationKey)),
                    true
            );
        }
    }

    private void startMolevoreSpin(Level level, Player player, ItemStack stack) {
        Vec3 direction = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = Vec3.directionFromRotation(0.0F, player.getYRot()).multiply(1.0D, 0.0D, 1.0D);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        Vec3 normalized = direction.normalize();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(SPIN_TICKS_TAG, AntarchySettings.molevoreSpinTicks());
            tag.putDouble(SPIN_DIRECTION_X_TAG, normalized.x);
            tag.putDouble(SPIN_DIRECTION_Z_TAG, normalized.z);
        });

        player.getCooldowns().addCooldown(this, AntarchySettings.molevoreCooldownTicks());
        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null, player.blockPosition(), AntarchySoundEvents.MOLEVORE_ATTACK.get(), SoundSource.PLAYERS, 1.0F, 0.9F);
        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    private void tickMolevoreSpin(ServerLevel level, ServerPlayer player, ItemStack stack, int spinTicks) {
        Vec3 direction = getStoredSpinDirection(stack);
        double verticalBoost = player.onGround() ? 0.08D : Math.max(-0.08D, player.getDeltaMovement().y);
        Vec3 motion = direction.scale(AntarchySettings.molevoreChargeSpeed()).add(0.0D, verticalBoost, 0.0D);
        player.setDeltaMovement(motion);
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        alignPlayerToSpinDirection(player, direction);

        if ((spinTicks & 1) == 0) {
            dealSpinDamage(level, player, direction);
        }
        boolean brokeBlocks = breakDrillBlocks(level, player, direction);
        if (player.horizontalCollision && !brokeBlocks) {
            clearSpinState(stack);
            return;
        }
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(0.35D), player.getZ(), 6, 0.35D, 0.1D, 0.35D, 0.01D);
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY(0.7D), player.getZ(), 4, 0.25D, 0.2D, 0.25D, 0.02D);

        if (spinTicks <= 1) {
            clearSpinState(stack);
            return;
        }

        setSpinTicks(stack, spinTicks - 1);
    }

    private void dealSpinDamage(ServerLevel level, ServerPlayer player, Vec3 direction) {
        AABB hitBox = player.getBoundingBox().inflate(0.7D).expandTowards(direction.scale(0.9D));
        float spinDamage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.35D);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                hitBox,
                entity -> entity.isAlive() && entity != player && !entity.isSpectator()
        )) {
            if (!target.hurt(player.damageSources().playerAttack(player), spinDamage)) {
                continue;
            }

            target.push(direction.x * 1.1D, 0.3D, direction.z * 1.1D);
            target.hurtMarked = true;
        }
    }

    private boolean breakDrillBlocks(ServerLevel level, ServerPlayer player, Vec3 direction) {
        Vec3 forward = direction.normalize();
        Vec3 center = player.position()
                .add(forward.scale(AntarchySettings.molevoreBreakRange() * 0.5D))
                .add(0.0D, player.getBbHeight() * 0.4D, 0.0D);
        BlockPos min = BlockPos.containing(
                center.x - AntarchySettings.molevoreBreakHalfWidth(),
                center.y - AntarchySettings.molevoreBreakVerticalRange() * 0.5D,
                center.z - AntarchySettings.molevoreBreakHalfWidth()
        );
        BlockPos max = BlockPos.containing(
                center.x + AntarchySettings.molevoreBreakHalfWidth(),
                center.y + AntarchySettings.molevoreBreakVerticalRange() * 0.5D,
                center.z + AntarchySettings.molevoreBreakHalfWidth()
        );
        ItemStack stack = player.getMainHandItem();
        Vec3 horizontalForward = forward.multiply(1.0D, 0.0D, 1.0D);
        boolean broke = false;

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!canDrillBlock(level, player, stack, pos, state)) {
                continue;
            }

            if (horizontalForward.lengthSqr() > 1.0E-4D) {
                Vec3 toBlock = Vec3.atCenterOf(pos).subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
                if (toBlock.lengthSqr() > 1.0E-4D && horizontalForward.normalize().dot(toBlock.normalize()) < -0.15D) {
                    continue;
                }
            }

            if (player.gameMode.destroyBlock(pos)) {
                broke = true;
            }
        }

        return broke;
    }

    private boolean canDrillBlock(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos pos, BlockState state) {
        if (!level.isLoaded(pos)
                || state.isAir()
                || state.hasBlockEntity()
                || state.getDestroySpeed(level, pos) < 0.0F
                || !state.is(AntarchyTags.Blocks.MOLEVORE_BREAKABLE_BLOCKS)
                || !player.mayUseItemAt(pos, Direction.UP, stack)
                || player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
            return false;
        }

        return true;
    }

    private void alignPlayerToSpinDirection(Player player, Vec3 direction) {
        float yaw = (float)(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0D);
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
    }

    private static BossMode getMode(ItemStack stack) {
        return BossMode.fromId(readInt(stack, MODE_TAG));
    }

    private static void setMode(ItemStack stack, BossMode mode) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (mode == BossMode.BASILISK) {
                tag.remove(MODE_TAG);
            } else {
                tag.putInt(MODE_TAG, mode.id);
            }
        });
    }

    private static int getSpinTicks(ItemStack stack) {
        return Math.max(0, readInt(stack, SPIN_TICKS_TAG));
    }

    private static void setSpinTicks(ItemStack stack, int ticks) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (ticks <= 0) {
                tag.remove(SPIN_TICKS_TAG);
            } else {
                tag.putInt(SPIN_TICKS_TAG, ticks);
            }
        });
    }

    private static Vec3 getStoredSpinDirection(ItemStack stack) {
        double x = readDouble(stack, SPIN_DIRECTION_X_TAG);
        double z = readDouble(stack, SPIN_DIRECTION_Z_TAG);
        Vec3 direction = new Vec3(x, 0.0D, z);
        return direction.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

    private static void clearSpinState(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(SPIN_TICKS_TAG);
            tag.remove(SPIN_DIRECTION_X_TAG);
            tag.remove(SPIN_DIRECTION_Z_TAG);
        });
    }

    private static int readInt(ItemStack stack, String tagName) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(tagName);
    }

    private static double readDouble(ItemStack stack, String tagName) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble(tagName);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AnimatedHeldItemRenderer<BigBerthaItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }

    private enum BossMode {
        BASILISK(0, "tooltip.antarchy.big_bertha.mode.basilisk", "message.antarchy.big_bertha.mode.basilisk"),
        NIGHTMARE(1, "tooltip.antarchy.big_bertha.mode.nightmare", "message.antarchy.big_bertha.mode.nightmare"),
        KRAKEN(2, "tooltip.antarchy.big_bertha.mode.kraken", "message.antarchy.big_bertha.mode.kraken"),
        MOLEVORE(3, "tooltip.antarchy.big_bertha.mode.molevore", "message.antarchy.big_bertha.mode.molevore"),
        LUCID(4, "tooltip.antarchy.big_bertha.mode.lucid", "message.antarchy.big_bertha.mode.lucid");

        private final int id;
        private final String tooltipKey;
        private final String translationKey;

        BossMode(int id, String tooltipKey, String translationKey) {
            this.id = id;
            this.tooltipKey = tooltipKey;
            this.translationKey = translationKey;
        }

        private BossMode next() {
            return switch (this) {
                case BASILISK -> LUCID;
                case LUCID -> NIGHTMARE;
                case NIGHTMARE -> KRAKEN;
                case KRAKEN -> MOLEVORE;
                case MOLEVORE -> BASILISK;
            };
        }

        private static BossMode fromId(int id) {
            for (BossMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return BASILISK;
        }
    }
}
