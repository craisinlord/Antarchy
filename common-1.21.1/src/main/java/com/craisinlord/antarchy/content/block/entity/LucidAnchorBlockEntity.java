package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.LucidAnchorBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LucidAnchorBlockEntity extends BlockEntity {
    private static final int EFFECT_INTERVAL_TICKS = 20;
    private static final int VALIDATION_INTERVAL_TICKS = 60;
    private static final int EFFECT_DURATION_TICKS = 60;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 30;
    private static final int DEFAULT_BEAM_COLOR = 0xFFFF1A1A;

    private int pyramidLevel;
    private boolean active;
    private boolean beamPathClear;
    private int beamLength;
    private boolean structureDirty = true;
    private long lastValidationTick = Long.MIN_VALUE;
    private List<BeamSection> beamSections = List.of();

    public LucidAnchorBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.LUCID_ANCHOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, LucidAnchorBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public void markStructureDirty() {
        this.structureDirty = true;
        this.setChanged();
    }

    public boolean isActive() {
        return this.active;
    }

    public int getPyramidLevel() {
        return this.pyramidLevel;
    }

    public int getBeamLength() {
        return this.beamLength;
    }

    public List<BeamSection> getBeamSections() {
        return this.beamSections;
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        long gameTime = level.getGameTime();
        if (this.structureDirty || gameTime - this.lastValidationTick >= VALIDATION_INTERVAL_TICKS) {
            this.revalidate(level, pos, state);
            this.structureDirty = false;
            this.lastValidationTick = gameTime;
        }

        if (this.active && gameTime % EFFECT_INTERVAL_TICKS == 0L) {
            this.applyInvertedField(level, pos);
        }
    }

    private void revalidate(ServerLevel level, BlockPos pos, BlockState state) {
        int newPyramidLevel = calculatePyramidLevel(level, pos);
        BeamValidation beamValidation = validateBeamPath(level, pos);
        boolean newActive = newPyramidLevel > 0 && beamValidation.clear();
        boolean stateChanged = this.pyramidLevel != newPyramidLevel
                || this.beamPathClear != beamValidation.clear()
                || this.active != newActive
                || this.beamLength != beamValidation.length()
                || !this.beamSections.equals(beamValidation.sections());

        boolean activeChanged = this.active != newActive;
        this.pyramidLevel = newPyramidLevel;
        this.beamPathClear = beamValidation.clear();
        this.beamLength = beamValidation.length();
        this.beamSections = beamValidation.sections();
        this.active = newActive;

        if (state.getBlock() instanceof LucidAnchorBlock && state.getValue(LucidAnchorBlock.ACTIVE) != newActive) {
            level.setBlock(pos, state.setValue(LucidAnchorBlock.ACTIVE, newActive), Block.UPDATE_CLIENTS);
            state = level.getBlockState(pos);
        }

        if (activeChanged) {
            level.playSound(null, pos, newActive ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (stateChanged) {
            this.syncVisualState(state);
        }
    }

    private void applyInvertedField(ServerLevel level, BlockPos pos) {
        if (this.pyramidLevel <= 0) {
            return;
        }

        int radius = 10 + this.pyramidLevel * 10;
        AABB area = new AABB(
                pos.getX() - radius,
                level.getMinBuildHeight(),
                pos.getZ() - radius,
                pos.getX() + radius + 1.0D,
                pos.getY() + 1.0D,
                pos.getZ() + radius + 1.0D
        );
        Holder<MobEffect> invertedEffect = AntarchyObjects.INVERTED_EFFECT.get();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, entity -> EntitySelector.NO_SPECTATORS.test(entity) && entity.isAlive())) {
            if (entity.getType().is(AntarchyTags.Entities.LUCID_ANCHOR_IMMUNE) || entity.getType().is(AntarchyTags.Entities.INVERTED_IMMUNE)) {
                continue;
            }

            MobEffectInstance current = entity.getEffect(invertedEffect);
            if (current != null && current.getDuration() > EFFECT_REFRESH_THRESHOLD_TICKS && current.getAmplifier() == 0) {
                continue;
            }

            entity.addEffect(new MobEffectInstance(invertedEffect, EFFECT_DURATION_TICKS, 0, true, true, true));
        }
    }

    private void syncVisualState(BlockState state) {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("PyramidLevel", this.pyramidLevel);
        tag.putBoolean("Active", this.active);
        tag.putBoolean("BeamPathClear", this.beamPathClear);
        tag.putInt("BeamLength", this.beamLength);
        tag.putBoolean("StructureDirty", this.structureDirty);
        int[] colors = new int[this.beamSections.size()];
        int[] lengths = new int[this.beamSections.size()];
        for (int i = 0; i < this.beamSections.size(); i++) {
            BeamSection section = this.beamSections.get(i);
            colors[i] = section.color();
            lengths[i] = section.length();
        }
        tag.put("BeamColors", new IntArrayTag(colors));
        tag.put("BeamLengths", new IntArrayTag(lengths));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.pyramidLevel = tag.getInt("PyramidLevel");
        this.active = tag.getBoolean("Active");
        this.beamPathClear = tag.getBoolean("BeamPathClear");
        this.beamLength = tag.getInt("BeamLength");
        this.structureDirty = true;
        int[] colors = tag.getIntArray("BeamColors");
        int[] lengths = tag.getIntArray("BeamLengths");
        List<BeamSection> sections = new ArrayList<>();
        for (int i = 0; i < Math.min(colors.length, lengths.length); i++) {
            if (lengths[i] > 0) {
                sections.add(new BeamSection(colors[i], lengths[i]));
            }
        }
        this.beamSections = sections.isEmpty() ? List.of() : List.copyOf(sections);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static int calculatePyramidLevel(Level level, BlockPos anchorPos) {
        int completedLayers = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int layer = 1; layer <= 4; layer++) {
            int y = anchorPos.getY() + layer;
            if (y > level.getMaxBuildHeight()) {
                break;
            }

            boolean complete = true;
            for (int x = anchorPos.getX() - layer; x <= anchorPos.getX() + layer && complete; x++) {
                for (int z = anchorPos.getZ() - layer; z <= anchorPos.getZ() + layer; z++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(AntarchyTags.Blocks.LUCID_ANCHOR_BASE_BLOCKS)) {
                        complete = false;
                        break;
                    }
                }
            }

            if (!complete) {
                break;
            }
            completedLayers = layer;
        }
        return completedLayers;
    }

    private static BeamValidation validateBeamPath(Level level, BlockPos anchorPos) {
        int minBuildHeight = level.getMinBuildHeight();
        int floorTolerance = minBuildHeight + 5;
        if (anchorPos.getY() <= minBuildHeight) {
            return BeamValidation.inactive();
        }

        BlockPos.MutableBlockPos cursor = anchorPos.below().mutable();
        List<BeamSection> sections = new ArrayList<>();
        int currentColor = DEFAULT_BEAM_COLOR;
        int currentLength = 0;
        int totalLength = 0;
        boolean clear = false;

        for (int y = anchorPos.getY() - 1; y >= minBuildHeight; y--) {
            cursor.set(anchorPos.getX(), y, anchorPos.getZ());
            BlockState state = level.getBlockState(cursor);

            if (state.is(Blocks.BEDROCK)) {
                if (y <= floorTolerance) {
                    currentLength++;
                    totalLength++;
                    clear = true;
                }
                break;
            }

            if (!isBeamPassable(level, cursor, state)) {
                if (y <= floorTolerance) {
                    currentLength++;
                    totalLength++;
                    clear = true;
                    break;
                }
                return BeamValidation.inactive();
            }

            int encounteredColor = colorForBeamState(state, currentColor);
            if (encounteredColor != currentColor && currentLength > 0) {
                sections.add(new BeamSection(currentColor, currentLength));
                currentColor = encounteredColor;
                currentLength = 0;
            } else {
                currentColor = encounteredColor;
            }

            currentLength++;
            totalLength++;
            if (y <= floorTolerance && state.isAir()) {
                clear = true;
            }
            if (y == minBuildHeight) {
                clear = true;
            }
        }

        if (!clear || totalLength <= 0) {
            return BeamValidation.inactive();
        }

        if (currentLength > 0) {
            sections.add(new BeamSection(currentColor, currentLength));
        }

        return new BeamValidation(true, totalLength, List.copyOf(sections));
    }

    private static boolean isBeamPassable(Level level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            return true;
        }
        if (state.getBlock() instanceof BeaconBeamBlock) {
            return true;
        }
        return state.getLightBlock(level, pos) < 15;
    }

    private static int colorForBeamState(BlockState state, int fallbackColor) {
        if (!(state.getBlock() instanceof BeaconBeamBlock beamBlock)) {
            return fallbackColor;
        }

        int diffuse = beamBlock.getColor().getTextureDiffuseColor();
        int red = (diffuse >> 16) & 0xFF;
        int green = (diffuse >> 8) & 0xFF;
        int blue = diffuse & 0xFF;
        if (fallbackColor == DEFAULT_BEAM_COLOR) {
            return 0xFF000000 | diffuse;
        }

        int fallbackRed = (fallbackColor >> 16) & 0xFF;
        int fallbackGreen = (fallbackColor >> 8) & 0xFF;
        int fallbackBlue = fallbackColor & 0xFF;
        return 0xFF000000
                | (((fallbackRed + red) / 2) << 16)
                | (((fallbackGreen + green) / 2) << 8)
                | ((fallbackBlue + blue) / 2);
    }

    public record BeamSection(int color, int length) {
    }

    private record BeamValidation(boolean clear, int length, List<BeamSection> sections) {
        private static BeamValidation inactive() {
            return new BeamValidation(false, 0, List.of());
        }
    }
}
