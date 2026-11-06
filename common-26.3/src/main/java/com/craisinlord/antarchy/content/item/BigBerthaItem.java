package com.craisinlord.antarchy.content.item;

import net.minecraft.world.entity.EntityTypes;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.client.model.ResourceBackedGeoItemModel;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.craisinlord.antarchy.content.client.renderer.AnimatedHeldItemRenderer;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;

public class BigBerthaItem extends Item implements GeoItem {
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "big_bertha");
    private static final Identifier DEFAULT_TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "textures/item/big_bertha/big_bertha.png");
    private static final Identifier ANIMATION_LOCATION = Identifier.fromNamespaceAndPath("antarchy", "static_item");
    private static final String MODE_TAG = "antarchy.big_bertha_mode";
    public static final int MODE_TREMORSAURUS = 8;
    public static final int MODE_FORSAKEN = 9;
    private static final String SPIN_TICKS_TAG = "antarchy.big_bertha_spin_ticks";
    private static final String SPIN_DIRECTION_X_TAG = "antarchy.big_bertha_spin_direction_x";
    private static final String SPIN_DIRECTION_Z_TAG = "antarchy.big_bertha_spin_direction_z";
    private static final String TORETERROR_JUMPING_TAG = "antarchy.toreterror_jumping";
    private static final String TORETERROR_LAUNCHED_TAG = "antarchy.toreterror_launched";
    private static final int TORETERROR_LANDING_RADIUS = 6;
    private static final int TORETERROR_COOLDOWN_TICKS = 200;
    private static final int TORETERROR_SHAKE_TICKS = 25;

    public static int clientShakeTicks = 0;
    private static final Identifier ATTACK_RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("antarchy", "big_bertha_attack_range");
    private final Identifier textureLocation;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BigBerthaItem(ToolMaterial material, Item.Properties properties) {
        this(material, properties, DEFAULT_TEXTURE_LOCATION);
    }

    public BigBerthaItem(ToolMaterial material, Item.Properties properties, Identifier textureLocation) {
        super(AntarchyItemProperties.configAttributes(
                properties.sword(material,
                        (int) Math.round(AntarchySettings.bigBerthaAttackDamage()),
                        (float) AntarchySettings.bigBerthaAttackSpeed()),
                () -> createBerthaAttributes(material)));
        this.textureLocation = textureLocation;
        GeoItem.registerSyncedAnimatable(this);
    }

    public static ItemAttributeModifiers createBerthaAttributes(ToolMaterial material) {
        return AntarchyItemProperties.toolAttributes(
                material,
                (int) Math.round(AntarchySettings.bigBerthaAttackDamage()),
                (float) AntarchySettings.bigBerthaAttackSpeed()
        ).withModifierAdded(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ATTACK_RANGE_MODIFIER_ID, AntarchySettings.bigBerthaReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        BossMode mode = getMode(stack);
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.big_bertha.mode", Component.translatable(mode.translationKey)).withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.translatable(mode.tooltipKey).withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.big_bertha.toggle_mode").withStyle(ChatFormatting.DARK_GRAY));
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BossMode mode = getMode(stack);

        if (mode == BossMode.BASILISK) {
            if (player.getCooldowns().isOnCooldown(stack)) {
                return InteractionResult.FAIL;
            }
            applyBasiliskRightClick(level, player, stack);
            return InteractionResult.SUCCESS;
        }

        if (mode == BossMode.TORETERROR) {
            if (player.getCooldowns().isOnCooldown(stack)) {
                return InteractionResult.FAIL;
            }
            startToreterrorJump(level, player, stack);
            return InteractionResult.SUCCESS;
        }

        if (mode != BossMode.MOLEVORE) {
            return InteractionResult.PASS;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        startMolevoreSpin(level, player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide() || !(attacker instanceof Player player) || player.getMainHandItem() != stack) {
            return;
        }

        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (getMode(stack)) {
            case NONE -> applyNoneHit(player, target);
            case BASILISK -> applyBasiliskHit(target, serverLevel);
            case LUCID -> applyLucidHit(target, player, serverLevel);
            case NIGHTMARE -> applyNightmareHit(player, target, serverLevel);
            case KRAKEN -> applyKrakenHit(player, target, serverLevel);
            case HERCULES -> applyHerculesHit(player, target, serverLevel);
            case MOLEVORE, TORETERROR, TREMORSAURUS, FORSAKEN -> {
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player)) {
            return;
        }
        this.tickHeld(stack, level, player);
    }

    public static void clientTick(Player player) {
        Level level = player.level();
        if (!level.isClientSide()) {
            return;
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof BigBerthaItem bertha) {
                bertha.tickHeld(stack, level, player);
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof BigBerthaItem bertha) {
            bertha.tickHeld(offhand, level, player);
        }
    }

    private void tickHeld(ItemStack stack, Level level, Player player) {
        if (isToreterrorJumping(stack) && player.getMainHandItem() == stack) {
            player.fallDistance = 0.0F;
            boolean launched = readInt(stack, TORETERROR_LAUNCHED_TAG) == 1;
            if (!launched && !player.onGround()) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TORETERROR_LAUNCHED_TAG, 1));
            }
            if (launched && player.onGround()) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                    tag.remove(TORETERROR_JUMPING_TAG);
                    tag.remove(TORETERROR_LAUNCHED_TAG);
                });
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    applyToreterrorLanding((ServerLevel) level, serverPlayer);
                }
                if (level.isClientSide()) {
                    clientShakeTicks = TORETERROR_SHAKE_TICKS;
                }
            }
        }

        if (level.isClientSide() && clientShakeTicks > 0) {
            clientShakeTicks--;
        }

        int spinTicks = getSpinTicks(stack);
        if (spinTicks <= 0) {
            return;
        }

        if (player.getMainHandItem() != stack) {
            if (!level.isClientSide()) {
                clearSpinState(stack);
            }
            return;
        }

        alignPlayerToSpinDirection(player, getStoredSpinDirection(stack));
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            tickMolevoreSpin((ServerLevel) level, serverPlayer, stack, spinTicks);
        }
    }

    private void applyBasiliskRightClick(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide()) {
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
                if (!target.is(AntarchyTags.Entities.PARALYSIS_IMMUNE)) {
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
        player.getCooldowns().addCooldown(stack, cooldownTicks);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void applyNoneHit(Player attacker, LivingEntity target) {
        double bonusPercent = AntarchySettings.bigBerthaNoneModeDamageBonusPercent();
        if (bonusPercent <= 0.0D) {
            return;
        }

        float bonusDamage = (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (bonusPercent / 100.0D));
        if (bonusDamage <= 0.0F) {
            return;
        }

        target.setInvulnerableTime(0);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            target.hurtServer(serverLevel, attacker.damageSources().playerAttack(attacker), bonusDamage);
        }
        target.syncVelocity = true;
    }

    private void applyBasiliskHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(
                MobEffects.POISON,
                AntarchySettings.basiliskDaggerPoisonDurationTicks(),
                AntarchySettings.basiliskDaggerPoisonAmplifier()
        ));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
        level.sendParticles(ParticleTypes.SNEEZE, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.25D, 0.2D, 0.25D, 0.02D);
    }

    private void applyLucidHit(LivingEntity target, Player player, ServerLevel level) {
        int effectDurationTicks = (int) Math.round(AntarchySettings.bigBerthaLucidInvertedDurationSeconds() * 20.0D);
        if (effectDurationTicks > 0
                && LucidBoltEntity.invertedEffectSupplier != null
                && !target.is(AntarchyTags.Entities.INVERTED_IMMUNE)) {
            target.addEffect(new MobEffectInstance(
                    LucidBoltEntity.invertedEffectSupplier.get(),
                    effectDurationTicks,
                    0,
                    false,
                    true,
                    true));
        }

        Vec3 impactPos = new Vec3(target.getX(), target.getY(0.8D), target.getZ());
        LucidBoltEntity.spawnImpactParticles(level, impactPos);
        level.sendParticles(ParticleTypes.PORTAL, impactPos.x, impactPos.y, impactPos.z, 12, 0.25D, 0.25D, 0.25D, 0.08D);
        if (!GravityApi.isGravityInverted(player)) {
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

        target.hurtServer(level, player.damageSources().playerAttack(player), bonusDamage);
        target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.08D, 0.0D));
        target.syncVelocity = true;
    }

    private void applyNightmareHit(Player attacker, LivingEntity target, ServerLevel level) {
        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(AntarchyObjects.DREAD.get(), AntarchySettings.nightmareDreadTicks(), 0));
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, AntarchySettings.nightmareWeaknessTicks(), 0));

        double bonusPercent = AntarchySettings.bigBerthaNightmareDamageBonusPercent();
        if (bonusPercent > 0.0D) {
            float bonusDamage = (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (bonusPercent / 100.0D));
            if (bonusDamage > 0.0F) {
                target.setInvulnerableTime(0);
                target.hurtServer(level, attacker.damageSources().playerAttack(attacker), bonusDamage);
                target.syncVelocity = true;
            }
        }

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
            target.syncVelocity = true;
        }

        LightningBolt lightningBolt = new LightningBolt(net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT, level);
        lightningBolt.snapTo(target.getX(), target.getY(), target.getZ());
        lightningBolt.setVisualOnly(true);
        level.addFreshEntity(lightningBolt);

        target.hurtServer(level, attacker.damageSources().lightningBolt(), (float) AntarchySettings.krakenLightningDamagePhaseOne());
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, AntarchySettings.bigBerthaKrakenSlowTicks(), 1));
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.7D), target.getZ(), 16, 0.35D, 0.3D, 0.35D, 0.08D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.02D);
        com.craisinlord.antarchy.content.entity.kraken.TentacleEntity.spawnAt(level, target.position(), attacker);
    }

    private void applyHerculesHit(Player attacker, LivingEntity target, ServerLevel level) {
        float bonusDamage = (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6D);
        Vec3 push = target.position().subtract(attacker.position()).multiply(1.0D, 0.0D, 1.0D);
        if (push.lengthSqr() < 1.0E-4D) {
            push = attacker.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }

        if (bonusDamage > 0.0F) {
            target.setInvulnerableTime(0);
            target.hurtServer(level, attacker.damageSources().playerAttack(attacker), bonusDamage);
        }

        if (push.lengthSqr() >= 1.0E-4D) {
            Vec3 normalized = push.normalize();
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x + normalized.x * 0.55D, Math.max(motion.y, 1.35D), motion.z + normalized.z * 0.55D);
        } else {
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x, Math.max(motion.y, 1.35D), motion.z);
        }
        target.syncVelocity = true;
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.7D), target.getZ(), 12, 0.3D, 0.35D, 0.3D, 0.04D);
        level.playSound(null, target.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private void cycleMode(Level level, Player player, ItemStack stack) {
        BossMode nextMode = getMode(stack).next();
        while (!nextMode.isAvailable()) {
            nextMode = nextMode.next();
        }
        setMode(stack, nextMode);
        clearSpinState(stack);
        if (!level.isClientSide()) {
            player.sendOverlayMessage(
                    Component.translatable("message.antarchy.big_bertha.mode", Component.translatable(nextMode.translationKey))
            );
        }
    }

    public static int getModeId(ItemStack stack) {
        return getMode(stack).id;
    }

    public boolean tryCycleModeFromInput(Level level, Player player, ItemStack stack) {
        if (player.getMainHandItem() != stack) {
            return false;
        }

        cycleMode(level, player, stack);
        return true;
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

        player.getCooldowns().addCooldown(stack, AntarchySettings.molevoreCooldownTicks());
        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null, player.blockPosition(), AntarchySoundEvents.MOLEVORE_ATTACK.get(), SoundSource.PLAYERS, 1.0F, 0.9F);
        if (!level.isClientSide()) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    private void startToreterrorJump(Level level, Player player, ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(TORETERROR_JUMPING_TAG, 1);
            tag.remove(TORETERROR_LAUNCHED_TAG);
        });
        player.setDeltaMovement(player.getDeltaMovement().x, 1.5D, player.getDeltaMovement().z);
        player.needsSync = true;
        player.fallDistance = 0.0F;
        player.getCooldowns().addCooldown(stack, TORETERROR_COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null, player.blockPosition(), AntarchySoundEvents.TORETERROR_JUMP_UP.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
    }

    private void applyToreterrorLanding(ServerLevel level, ServerPlayer player) {
        ItemStack heldStack = player.getMainHandItem();
        float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0D);
        AABB aoe = player.getBoundingBox().inflate(TORETERROR_LANDING_RADIUS, 3.0D, TORETERROR_LANDING_RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, aoe,
                e -> e.isAlive() && e != player && !e.isSpectator())) {
            victim.hurtServer(level, player.damageSources().playerAttack(player), damage);
            Vec3 knockDir = victim.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (knockDir.lengthSqr() < 1.0E-4D) knockDir = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            Vec3 normalized = knockDir.normalize();
            victim.push(normalized.x * 2.5D, 0.6D, normalized.z * 2.5D);
            victim.syncVelocity = true;
        }
        level.playSound(null, player.blockPosition(), AntarchySoundEvents.TORETERROR_JUMP_LAND.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5D, player.getZ(), 4, 1.5D, 0.3D, 1.5D, 0.0D);
        level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 0.5D, player.getZ(), 60, 3.0D, 0.5D, 3.0D, 0.2D);
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.5D, player.getZ(), 20, 2.0D, 0.5D, 2.0D, 0.1D);
        heldStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    private static boolean isToreterrorJumping(ItemStack stack) {
        return readInt(stack, TORETERROR_JUMPING_TAG) == 1;
    }

    private void tickMolevoreSpin(ServerLevel level, ServerPlayer player, ItemStack stack, int spinTicks) {
        Vec3 direction = getStoredSpinDirection(stack);
        double verticalBoost = player.onGround() ? 0.08D : Math.max(-0.08D, player.getDeltaMovement().y);
        Vec3 motion = direction.scale(AntarchySettings.molevoreChargeSpeed()).add(0.0D, verticalBoost, 0.0D);
        player.setDeltaMovement(motion);
        player.needsSync = true;
        player.syncVelocity = true;
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
            if (!target.hurtServer(level, player.damageSources().playerAttack(player), spinDamage)) {
                continue;
            }

            target.push(direction.x * 1.1D, 0.3D, direction.z * 1.1D);
            target.syncVelocity = true;
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
            if (mode == BossMode.NONE) {
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
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(tagName, 0);
    }

    private static double readDouble(ItemStack stack, String tagName) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDoubleOr(tagName, 0.0D);
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
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AnimatedHeldItemRenderer<>(new ResourceBackedGeoItemModel<>(MODEL_LOCATION, BigBerthaItem.this.textureLocation, ANIMATION_LOCATION));
                }

                return this.renderer;
            }
        });
    }

    private enum BossMode {
        NONE(0, "tooltip.antarchy.big_bertha.mode.none", "message.antarchy.big_bertha.mode.none"),
        BASILISK(7, "tooltip.antarchy.big_bertha.mode.basilisk", "message.antarchy.big_bertha.mode.basilisk"),
        NIGHTMARE(1, "tooltip.antarchy.big_bertha.mode.nightmare", "message.antarchy.big_bertha.mode.nightmare"),
        KRAKEN(2, "tooltip.antarchy.big_bertha.mode.kraken", "message.antarchy.big_bertha.mode.kraken"),
        MOLEVORE(3, "tooltip.antarchy.big_bertha.mode.molevore", "message.antarchy.big_bertha.mode.molevore"),
        LUCID(4, "tooltip.antarchy.big_bertha.mode.lucid", "message.antarchy.big_bertha.mode.lucid"),
        TORETERROR(5, "tooltip.antarchy.big_bertha.mode.toreterror", "message.antarchy.big_bertha.mode.toreterror"),
        TREMORSAURUS(MODE_TREMORSAURUS, "tooltip.antarchy.big_bertha.mode.tremorsaurus", "message.antarchy.big_bertha.mode.tremorsaurus"),
        FORSAKEN(MODE_FORSAKEN, "tooltip.antarchy.big_bertha.mode.forsaken", "message.antarchy.big_bertha.mode.forsaken"),
        HERCULES(6, "tooltip.antarchy.big_bertha.mode.hercules", "message.antarchy.big_bertha.mode.hercules");

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
                case NONE -> BASILISK;
                case BASILISK -> LUCID;
                case LUCID -> NIGHTMARE;
                case NIGHTMARE -> KRAKEN;
                case KRAKEN -> HERCULES;
                case HERCULES -> MOLEVORE;
                case MOLEVORE -> TORETERROR;
                case TORETERROR -> TREMORSAURUS;
                case TREMORSAURUS -> FORSAKEN;
                case FORSAKEN -> NONE;
            };
        }

        private boolean isAvailable() {
            return switch (this) {
                case TREMORSAURUS -> alexsCavesEntityPresent("tremorsaurus");
                case FORSAKEN -> alexsCavesEntityPresent("forsaken");
                default -> true;
            };
        }

        private static boolean alexsCavesEntityPresent(String path) {
            return BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("alexscaves", path));
        }

        private static BossMode fromId(int id) {
            for (BossMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return NONE;
        }
    }
}
