package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.BluestoneSignalHelper;
import com.craisinlord.antarchy.content.block.VortexLensBlock;
import com.craisinlord.antarchy.content.entity.vortex.WindVortexEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class VortexLensBlockEntity extends BlockEntity {
    private static final String ACTIVE_VORTEX_KEY = "ActiveVortex";
    private static final int LENS_VORTEX_REFRESH_DURATION = 40;

    @Nullable
    private UUID activeVortexUuid;
    private boolean refreshNow = true;

    public VortexLensBlockEntity(BlockPos pos, BlockState state,
            Supplier<? extends BlockEntityType<VortexLensBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, VortexLensBlockEntity blockEntity) {
        blockEntity.tickLens(level, pos, state);
    }

    public void refreshImmediately() {
        this.refreshNow = true;
    }

    public void removeActiveVortex() {
        if (this.level instanceof ServerLevel serverLevel) {
            WindVortexEntity vortex = this.getActiveVortex(serverLevel);
            if (vortex != null) {
                vortex.removeFromLens();
            }
        }
        this.activeVortexUuid = null;
        this.setChanged();
    }

    private void tickLens(ServerLevel level, BlockPos pos, BlockState state) {
        if (!this.refreshNow && (level.getGameTime() & 1L) == 1L) {
            return;
        }
        this.refreshNow = false;

        int bluestoneSignal = BluestoneSignalHelper.getBestNeighborSignal(level, pos);
        int redstoneSignal = level.getBestNeighborSignal(pos);
        boolean pushMode = bluestoneSignal > 0;
        int signal = pushMode ? bluestoneSignal : redstoneSignal;

        if (signal <= 0) {
            this.removeActiveVortex();
            this.updateBlockState(level, pos, state, false, false);
            return;
        }

        WindVortexEntity vortex = this.getOrCreateVortex(level, pos, state);
        if (vortex == null) {
            return;
        }

        Direction facing = state.getValue(VortexLensBlock.FACING);
        double signalScale = Mth.clamp(signal / 15.0D, 0.0D, 1.0D);
        double radius = Mth.lerp(signalScale, AntarchySettings.vortexLensMinRadius(), AntarchySettings.vortexLensMaxRadius());
        double height = Mth.lerp(signalScale, AntarchySettings.vortexLensMinHeight(), AntarchySettings.vortexLensMaxHeight());
        Vec3 axis = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 origin = Vec3.atCenterOf(pos).add(axis.scale(0.65D));
        WindVortexEntity.VortexMode mode = pushMode ? WindVortexEntity.VortexMode.LENS_PUSH : WindVortexEntity.VortexMode.LENS_PULL;
        double strength = pushMode ? AntarchySettings.vortexLensPushStrength() : AntarchySettings.vortexLensPullStrength();

        vortex.setPos(origin.x, origin.y, origin.z);
        vortex.setDeltaMovement(Vec3.ZERO);
        vortex.setAxis(facing);
        vortex.setMode(mode);
        vortex.setVortexSize((float) height, (float) radius);
        vortex.setVortexDurationTicks(LENS_VORTEX_REFRESH_DURATION);
        vortex.resetVortexAge();
        vortex.setVortexStrengths(strength, AntarchySettings.vortexLensLaunchStrength());
        vortex.setDamaging(false);
        this.updateBlockState(level, pos, state, true, pushMode);
    }

    @Nullable
    private WindVortexEntity getOrCreateVortex(ServerLevel level, BlockPos pos, BlockState state) {
        WindVortexEntity vortex = this.getActiveVortex(level);
        if (vortex != null) {
            return vortex;
        }

        Direction facing = state.getValue(VortexLensBlock.FACING);
        Vec3 axis = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 origin = Vec3.atCenterOf(pos).add(axis.scale(0.65D));
        vortex = WindVortexEntity.create(level, AntarchyObjects.WIND_VORTEX.get(), origin, Vec3.ZERO, null, false);
        vortex.setAxis(facing);
        vortex.setMode(WindVortexEntity.VortexMode.LENS_PULL);
        vortex.setVortexDurationTicks(LENS_VORTEX_REFRESH_DURATION);
        vortex.resetVortexAge();
        level.addFreshEntity(vortex);
        this.activeVortexUuid = vortex.getUUID();
        this.setChanged();
        return vortex;
    }

    @Nullable
    private WindVortexEntity getActiveVortex(ServerLevel level) {
        if (this.activeVortexUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.activeVortexUuid);
        if (entity instanceof WindVortexEntity vortex && vortex.isAlive()) {
            return vortex;
        }
        this.activeVortexUuid = null;
        this.setChanged();
        return null;
    }

    private void updateBlockState(ServerLevel level, BlockPos pos, BlockState state, boolean powered, boolean pushing) {
        if (!(state.getBlock() instanceof VortexLensBlock)) {
            return;
        }
        if (state.getValue(VortexLensBlock.POWERED) != powered || state.getValue(VortexLensBlock.PUSHING) != pushing) {
            level.setBlock(pos,
                    state.setValue(VortexLensBlock.POWERED, powered).setValue(VortexLensBlock.PUSHING, pushing),
                    Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.activeVortexUuid != null) {
            tag.putUUID(ACTIVE_VORTEX_KEY, this.activeVortexUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.activeVortexUuid = tag.hasUUID(ACTIVE_VORTEX_KEY) ? tag.getUUID(ACTIVE_VORTEX_KEY) : null;
    }
}
