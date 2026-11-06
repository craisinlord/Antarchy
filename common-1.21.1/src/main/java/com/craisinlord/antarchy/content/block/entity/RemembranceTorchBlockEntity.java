package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.throwback.ThrowbackZoneTracker;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RemembranceTorchBlockEntity extends BlockEntity {
    private static final String RADIUS_KEY = "Radius";

    private int radius;

    public RemembranceTorchBlockEntity(BlockPos pos, BlockState state) {
        super(AntarchyObjects.REMEMBRANCE_TORCH_BLOCK_ENTITY.get(), pos, state);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        this.setChanged();
    }

    public int radius() {
        return this.radius > 0 ? this.radius : Math.max(1, AntarchySettings.throwbackTorchRadius());
    }

    public static void serverTick(ServerLevel level, BlockPos pos, RemembranceTorchBlockEntity torch) {
        if (!ThrowbackZoneTracker.isTorchTick(level.getGameTime()) || !AntarchySettings.throwbackZonesEnabled()) {
            return;
        }
        int radius = torch.radius();
        boolean wild = torch.radius > 0;
        for (ServerPlayer player : level.players()) {
            ThrowbackZones.Influence influence = ThrowbackZones.torchInfluence(pos, radius, player.blockPosition());
            if (influence.strength() > 0.0F && !(wild && ThrowbackZones.isInsideOtherStructure(level, player.blockPosition()))) {
                ThrowbackZoneTracker.contribute(player, influence);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.radius = tag.getInt(RADIUS_KEY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.radius > 0) {
            tag.putInt(RADIUS_KEY, this.radius);
        }
    }
}
