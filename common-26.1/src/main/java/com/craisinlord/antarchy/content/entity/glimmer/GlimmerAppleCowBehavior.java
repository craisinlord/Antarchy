package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.animal.cow.CowSoundVariant;
import net.minecraft.world.entity.animal.cow.CowSoundVariants;
import net.minecraft.world.level.storage.TagValueOutput;

public class GlimmerAppleCowBehavior implements GlimmerVariantBehavior {
    private static final CowSoundVariant COW_SOUNDS = SoundEvents.COW_SOUNDS.get(CowSoundVariants.SoundSet.CLASSIC);
    private static final int SHEAR_REGROW_TICKS = 20 * 60 * 2;
    private static final float EXHAUSTION_REDUCTION = 0.25F;
    private static final int LOW_FOOD_THRESHOLD = 16;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 120;

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.0D));
        entity.addGoal(6, new LookAtPlayerGoal(entity, Player.class, 8.0F));
        entity.addGoal(7, new RandomLookAroundGoal(entity));
    }

    @Override
    public Identifier modelGeo() {
        return rl("apple_cow");
    }

    @Override
    public Identifier animationFile() {
        return rl("apple_cow");
    }

    @Override
    public Identifier texture(GlimmerEntity entity) {
        return entity.isShearCooldownActive()
                ? rl("textures/entity/glimmer/apple_cow_glimmer_sheared.png")
                : rl("textures/entity/glimmer/apple_cow_glimmer.png");
    }

    @Override
    public Identifier emissiveTexture(GlimmerEntity entity) {
        return entity.isShearCooldownActive()
                ? rl("textures/entity/glimmer/apple_cow_glimmer_sheared_emissive.png")
                : rl("textures/entity/glimmer/apple_cow_glimmer_emissive.png");
    }

    @Override
    public SoundEvent ambientSound() {
        return COW_SOUNDS.ambientSound().value();
    }

    @Override
    public SoundEvent hurtSound() {
        return COW_SOUNDS.hurtSound().value();
    }

    @Override
    public SoundEvent deathSound() {
        return COW_SOUNDS.deathSound().value();
    }

    @Override
    public EntityDimensions adultDimensions() {
        return EntityDimensions.scalable(0.9F, 1.4F);
    }

    @Override
    @Nullable
    public InteractionResult onInteract(GlimmerEntity entity, Player player, ItemStack stack, InteractionHand hand) {
        if ((entity.isTame() && !entity.isOwnedBy(player)) || entity.isShearCooldownActive() || !stack.is(Items.SHEARS)) {
            return null;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        entity.startShearCooldown(SHEAR_REGROW_TICKS);
        entity.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        entity.spawnAtLocation(serverLevel, new net.minecraft.world.item.ItemStack(AntarchyObjects.SPIRIT_APPLE.get()), entity.getBbHeight() * 0.6F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
        FoodData food = owner.getFoodData();
        float current = exhaustionLevel(food);
        float last = entity.getLastOwnerExhaustion();
        if (last >= 0.0F && current > last) {
            owner.causeFoodExhaustion(-(current - last) * EXHAUSTION_REDUCTION);
        }
        entity.setLastOwnerExhaustion(exhaustionLevel(food));
    }

    private static float exhaustionLevel(FoodData food) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        food.addAdditionalSaveData(output);
        return output.buildResult().getFloatOr("foodExhaustionLevel", 0.0F);
    }

    @Override
    public void clearPassive(GlimmerEntity entity, Player owner) {
        entity.setLastOwnerExhaustion(-1.0F);
    }

    @Override
    public int abilityCooldownTicks() {
        return ABILITY_COOLDOWN_TICKS;
    }

    @Override
    public void tickAbilityCheck(GlimmerEntity entity, Player owner) {
        if (owner.getFoodData().getFoodLevel() > LOW_FOOD_THRESHOLD) {
            return;
        }

        owner.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 1, false, false, true));
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1, false, true, true));
        entity.playSound(COW_SOUNDS.ambientSound().value(), 1.2F, 0.8F);
        this.sendAbilityMessage(owner, Component.translatable("entity.antarchy.glimmer.ability.apple_cow"));
        entity.startAbilityCooldown();
    }
}
