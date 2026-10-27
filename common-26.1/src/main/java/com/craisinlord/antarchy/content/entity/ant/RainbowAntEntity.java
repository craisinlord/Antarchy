package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityWarpResult;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.portal.RainbowPortalIgniter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class RainbowAntEntity extends BaseAntEntity implements GeoEntity {
    private static final String DIMENSION_ID_TAG = "InfinityDimensionId";
    private static final String ACTIVATED_TAG = "InfinityActivated";
    private static final String INHERITED_TAG = "InfinityInherited";
    private static final String REROLL_ON_NEXT_REAGENT_TAG = "InfinityRerollOnNextReagent";
    private static final String INFINITY_NAMESPACE = "infinity";
    private String infinityDimensionId = "";
    private boolean infinityActivated;
    private boolean infinityInherited;
    private boolean infinityRerollOnNextReagent;

    public RainbowAntEntity(EntityType<? extends BaseAntEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static InteractionResult teleportPlayerFromStoredData(ServerPlayer player, @Nullable CompoundTag entityData) {
        if (!AntarchySettings.rainbowAntsLeadToInfinityDimensions() || !InfinityCompat.get().isAvailable()) {
            return AntTeleportHelper.teleportPlayerToDimension(player, AntarchySettings.rainbowAntNonInfinityFallbackDimension());
        }

        if (INFINITY_NAMESPACE.equals(player.level().dimension().identifier().getNamespace())) {
            return AntTeleportHelper.teleportPlayerToReturnDestination(player);
        }

        Identifier dimensionId = parseStoredDimensionId(entityData);
        if (dimensionId == null) {
            return AntTeleportHelper.teleportPlayerToDimension(player, AntarchySettings.rainbowAntNonInfinityFallbackDimension());
        }

        InfinityWarpResult result = InfinityCompat.get().requestWarp(player, dimensionId);
        if (result == InfinityWarpResult.REJECTED) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_failed_dimension_create"));
            return InteractionResult.CONSUME;
        }

        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.hasActiveInfinityDestination() || super.requiresCustomPersistence();
    }

    public boolean tryIgniteInfinityPortalOnDeath() {
        if (!this.infinityActivated) {
            return false;
        }
        if (!AntarchySettings.permanentPortalsEnabled() || !AntarchySettings.rainbowAntsLeadToInfinityDimensions()
                || !InfinityCompat.get().isAvailable()) {
            return false;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        Identifier dimensionId = this.resolveDimensionId();
        if (dimensionId == null) {
            return false;
        }

        return RainbowPortalIgniter.tryIgnite(serverLevel, this.getBoundingBox(), dimensionId);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!AntTeleportHelper.isRightClickDimensionEnabled(this)) {
            ItemStack heldItem = player.getItemInHand(hand);
            return this.isFood(heldItem) ? super.mobInteract(player, hand) : InteractionResult.PASS;
        }

        if (!AntarchySettings.rainbowAntsLeadToInfinityDimensions() || !InfinityCompat.get().isAvailable()) {
            return super.mobInteract(player, hand);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (this.isInInfinityDimension(serverPlayer.level())) {
            ServerLevel returnLevel = this.resolveReturnDestinationLevel(serverPlayer);
            AntTeleportHelper.prepareArrival(serverPlayer, returnLevel, returnPos -> {
                if (returnPos == null) {
                    serverPlayer.sendOverlayMessage(Component.translatable("message.antarchy.teleport_arrival_failed"));
                    return;
                }
                serverPlayer.teleport(new TeleportTransition(returnLevel, returnPos, Vec3.ZERO, serverPlayer.getYRot(), serverPlayer.getXRot(), TeleportTransition.DO_NOTHING));
                serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            });
            return InteractionResult.CONSUME;
        }

        if (!this.infinityActivated) {
            if (!AntarchySettings.rainbowAntRequiresReagent()) {
                if (this.isFood(heldItem)) {
                    return super.mobInteract(player, hand);
                }

                if (!heldItem.isEmpty() && !heldItem.is(AntarchyTags.Items.RAINBOW_ANT_ACTIVATION_ITEMS)) {
                    return InteractionResult.PASS;
                }

                return this.activateInfinityDestination(serverPlayer, heldItem);
            }

            if (heldItem.is(AntarchyTags.Items.RAINBOW_ANT_ACTIVATION_ITEMS)) {
                return this.activateInfinityDestination(serverPlayer, heldItem);
            }

            if (!heldItem.isEmpty()) {
                return InteractionResult.PASS;
            }

            serverPlayer.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_needs_reagent"));
            return InteractionResult.CONSUME;
        }

        if (this.isFood(heldItem)) {
            return super.mobInteract(player, hand);
        }

        if (!heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        Identifier dimensionId = this.resolveDimensionId();
        if (dimensionId == null) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_infinity_missing"));
            return InteractionResult.CONSUME;
        }

        if (!this.teleportToInfinity(serverPlayer, dimensionId)) {
            return InteractionResult.CONSUME;
        }

        serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(DIMENSION_ID_TAG, this.infinityDimensionId);
        output.putBoolean(ACTIVATED_TAG, this.infinityActivated);
        output.putBoolean(INHERITED_TAG, this.infinityInherited);
        output.putBoolean(REROLL_ON_NEXT_REAGENT_TAG, this.infinityRerollOnNextReagent);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.infinityDimensionId = input.getStringOr(DIMENSION_ID_TAG, "");
        this.infinityActivated = input.getBooleanOr(ACTIVATED_TAG, false);
        this.infinityInherited = input.getBooleanOr(INHERITED_TAG, false);
        this.infinityRerollOnNextReagent = input.getBooleanOr(REROLL_ON_NEXT_REAGENT_TAG, false);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        RainbowAntEntity child = this.getType().create(level, EntitySpawnReason.BREEDING) instanceof RainbowAntEntity rainbowAnt ? rainbowAnt : null;
        if (child == null) {
            return null;
        }

        child.setNestPos(this.blockPosition());
        if (!(otherParent instanceof RainbowAntEntity otherRainbowAnt)) {
            child.assignDimensionId(this.getStoredDimensionId(), false, true);
            return child;
        }

        double roll = level.getRandom().nextDouble();
        if (roll < 0.45D) {
            child.assignDimensionId(this.getStoredDimensionId(), false, true);
        } else if (roll < 0.90D) {
            child.assignDimensionId(otherRainbowAnt.getStoredDimensionId(), false, true);
        } else {
            child.assignDimensionId(child.createRandomDimensionId(), false, false);
        }
        return child;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.infinityActivated && super.isFood(stack);
    }

    @Override
    protected ResourceKey<Level> destinationDimension() {
        return AntarchySettings.rainbowAntNonInfinityFallbackDimension();
    }

    @Override
    protected TagKey<Item> activationItemsTag() {
        return AntarchyTags.Items.RAINBOW_ANT_ACTIVATION_ITEMS;
    }

    @Override
    protected TagKey<Item> breedingFoodsTag() {
        return AntarchyTags.Items.RAINBOW_ANT_BREEDING_FOODS;
    }

    @Override
    protected String activationMessageKey() {
        return "message.antarchy.rainbow_ant_activated";
    }

    @Override
    protected String needsReagentMessageKey() {
        return "message.antarchy.rainbow_ant_needs_reagent";
    }

    @Override
    protected double configuredMaxHealth() {
        return AntarchySettings.rainbowAntHealth();
    }

    private InteractionResult activateInfinityDestination(ServerPlayer player, ItemStack stack) {
        if (this.infinityRerollOnNextReagent) {
            this.rerollDimensionIdForNextActivation();
        }

        Identifier dimensionId = this.resolveDimensionId();
        if (dimensionId == null) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_infinity_missing"));
            return InteractionResult.CONSUME;
        }

        boolean existedBefore = this.infinityDimensionExists(player.level().getServer(), dimensionId);
        this.infinityActivated = true;
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (this.infinityInherited && existedBefore) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_activated_inherited_existing"));
        } else if (this.infinityInherited) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_activated_inherited"));
        } else {
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_activated"));
        }
        this.playSound(SoundEvents.END_PORTAL_SPAWN, 0.8F, 1.1F);
        return InteractionResult.CONSUME;
    }

    private void ensureInfinityIdentity() {
        if (this.infinityDimensionId.isEmpty()) {
            this.infinityDimensionId = this.createRandomDimensionId();
            this.infinityInherited = false;
            this.infinityRerollOnNextReagent = false;
        }
    }

    private boolean hasActiveInfinityDestination() {
        return this.infinityActivated && !this.infinityDimensionId.isEmpty();
    }

    private Identifier resolveDimensionId() {
        this.ensureInfinityIdentity();
        return this.parseDimensionId(this.infinityDimensionId);
    }

    private String getStoredDimensionId() {
        this.ensureInfinityIdentity();
        return this.infinityDimensionId;
    }

    private void assignDimensionId(String dimensionId, boolean activated, boolean inherited) {
        this.infinityDimensionId = dimensionId;
        this.infinityActivated = activated;
        this.infinityInherited = inherited;
        this.infinityRerollOnNextReagent = false;
    }

    private void markForRerollOnNextReagent() {
        this.infinityActivated = false;
        this.infinityRerollOnNextReagent = true;
    }

    private void rerollDimensionIdForNextActivation() {
        this.assignDimensionId(this.createRandomDimensionId(), false, false);
    }

    private boolean infinityDimensionExists(MinecraftServer server, Identifier dimensionId) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        return server.getLevel(key) != null || server.levelKeys().contains(key);
    }

    private String createRandomDimensionId() {
        Identifier dimensionId = InfinityCompat.get().getRandomDimensionId(this.random, AntarchySettings.rainbowAntInfinityEasterEggChance());
        if (dimensionId == null) {
            return "";
        }
        return dimensionId.toString();
    }

    private boolean isInInfinityDimension(ServerLevel level) {
        return INFINITY_NAMESPACE.equals(level.dimension().identifier().getNamespace());
    }

    private Identifier parseDimensionId(String storedDimensionId) {
        try {
            return Identifier.parse(storedDimensionId);
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Nullable
    private static Identifier parseStoredDimensionId(@Nullable CompoundTag entityData) {
        if (entityData == null || !entityData.contains(DIMENSION_ID_TAG)) {
            return null;
        }

        try {
            String storedDimensionId = entityData.getStringOr(DIMENSION_ID_TAG, "");
            return storedDimensionId.isEmpty() ? null : Identifier.parse(storedDimensionId);
        } catch (Throwable throwable) {
            return null;
        }
    }

    private boolean teleportToInfinity(ServerPlayer player, Identifier dimensionId) {
        InfinityWarpResult result = InfinityCompat.get().requestWarp(player, dimensionId);
        if (result == InfinityWarpResult.FAILED_GENERATION) {
            this.deleteGeneratedArtifacts(player.level().getServer(), dimensionId);
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_failed_dimension_create"));
            return false;
        }
        if (result == InfinityWarpResult.REJECTED) {
            player.sendOverlayMessage(Component.translatable("message.antarchy.rainbow_ant_failed_dimension_create"));
            return false;
        }
        return true;
    }

    private void deleteGeneratedArtifacts(MinecraftServer server, Identifier dimensionId) {
        if (!INFINITY_NAMESPACE.equals(dimensionId.getNamespace())) {
            return;
        }

        Path datapackPath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(dimensionId.getPath());
        Path dimensionPath = server.getWorldPath(LevelResource.ROOT)
                .resolve("dimensions")
                .resolve(dimensionId.getNamespace())
                .resolve(dimensionId.getPath());

        this.deletePathRecursively(datapackPath);
        this.deletePathRecursively(dimensionPath);
    }

    private void deletePathRecursively(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(currentPath -> {
                try {
                    Files.deleteIfExists(currentPath);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (IOException | RuntimeException ignored) {
        }
    }
}
