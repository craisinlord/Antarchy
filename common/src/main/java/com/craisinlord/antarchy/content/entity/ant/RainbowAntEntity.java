package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;

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

    @Override
    public boolean requiresCustomPersistence() {
        return this.hasActiveInfinityDestination() || super.requiresCustomPersistence();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!AntarchySettings.rainbowAntsLeadToInfinityDimensions() || !InfinityCompat.get().isAvailable()) {
            return super.mobInteract(player, hand);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (this.isInInfinityDimension(serverPlayer.serverLevel())) {
            ServerLevel returnLevel = this.resolveReturnDestinationLevel(serverPlayer);
            Vec3 returnPos = this.getDestinationPosition(returnLevel, serverPlayer);
            serverPlayer.teleportTo(returnLevel, returnPos.x, returnPos.y, returnPos.z, serverPlayer.getYRot(), serverPlayer.getXRot());
            serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
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

            serverPlayer.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_needs_reagent"), true);
            return InteractionResult.CONSUME;
        }

        if (this.isFood(heldItem)) {
            return super.mobInteract(player, hand);
        }

        if (!heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        ResourceLocation dimensionId = this.resolveDimensionId();
        if (dimensionId == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_infinity_missing"), true);
            return InteractionResult.CONSUME;
        }

        if (!this.teleportToInfinity(serverPlayer, dimensionId)) {
            return InteractionResult.CONSUME;
        }

        serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(DIMENSION_ID_TAG, this.infinityDimensionId);
        tag.putBoolean(ACTIVATED_TAG, this.infinityActivated);
        tag.putBoolean(INHERITED_TAG, this.infinityInherited);
        tag.putBoolean(REROLL_ON_NEXT_REAGENT_TAG, this.infinityRerollOnNextReagent);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.infinityDimensionId = tag.getString(DIMENSION_ID_TAG);
        this.infinityActivated = tag.getBoolean(ACTIVATED_TAG);
        this.infinityInherited = tag.getBoolean(INHERITED_TAG);
        this.infinityRerollOnNextReagent = tag.getBoolean(REROLL_ON_NEXT_REAGENT_TAG);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        RainbowAntEntity child = this.getType().create(level) instanceof RainbowAntEntity rainbowAnt ? rainbowAnt : null;
        if (child == null) {
            return null;
        }

        child.setNestPos(this.blockPosition());
        if (!(otherParent instanceof RainbowAntEntity otherRainbowAnt)) {
            child.assignDimensionId(this.getStoredDimensionId(), false, true);
            return child;
        }

        double roll = level.random.nextDouble();
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

        ResourceLocation dimensionId = this.resolveDimensionId();
        if (dimensionId == null) {
            player.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_infinity_missing"), true);
            return InteractionResult.CONSUME;
        }

        boolean existedBefore = this.infinityDimensionExists(player.server, dimensionId);
        this.infinityActivated = true;
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (this.infinityInherited && existedBefore) {
            player.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_activated_inherited_existing"), true);
        } else if (this.infinityInherited) {
            player.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_activated_inherited"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_activated"), true);
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

    private ResourceLocation resolveDimensionId() {
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

    private boolean infinityDimensionExists(MinecraftServer server, ResourceLocation dimensionId) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        return server.getLevel(key) != null || server.levelKeys().contains(key);
    }

    private String createRandomDimensionId() {
        ResourceLocation dimensionId = InfinityCompat.get().getRandomDimensionId(this.random);
        if (dimensionId == null) {
            return "";
        }
        return dimensionId.toString();
    }

    private boolean isInInfinityDimension(ServerLevel level) {
        return INFINITY_NAMESPACE.equals(level.dimension().location().getNamespace());
    }

    private ResourceLocation parseDimensionId(String storedDimensionId) {
        try {
            return ResourceLocation.parse(storedDimensionId);
        } catch (Throwable throwable) {
            return null;
        }
    }

    private boolean teleportToInfinity(ServerPlayer player, ResourceLocation dimensionId) {
        if (!InfinityCompat.get().requestWarp(player, dimensionId)) {
            this.deleteGeneratedArtifacts(player.server, dimensionId);
            this.markForRerollOnNextReagent();
            player.displayClientMessage(Component.translatable("message.antarchy.rainbow_ant_failed_dimension_create"), true);
            return false;
        }
        return true;
    }

    private void deleteGeneratedArtifacts(MinecraftServer server, ResourceLocation dimensionId) {
        if (!INFINITY_NAMESPACE.equals(dimensionId.getNamespace())) {
            return;
        }

        Path datapackPath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(dimensionId.getPath());
        Path dimensionPath = server.getWorldPath(LevelResource.ROOT)
                .resolve("dimensions")
                .resolve(dimensionId.getNamespace())
                .resolve(dimensionId.getPath());

        this.deletePathRecursively(datapackPath);
        if (dimensionPath != null) {
            this.deletePathRecursively(dimensionPath);
        }
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
