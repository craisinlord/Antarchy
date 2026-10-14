package com.craisinlord.antarchy.content.block.entity.undervault;

import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public enum UnderVaultState implements StringRepresentable {
    INACTIVE("inactive", 6) {
        @Override
        protected void onEnter(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData, boolean ominous) {
            sharedData.setDisplayItem(ItemStack.EMPTY);
            UnderVaultEffects.deactivate(level, pos, ominous);
        }
    },
    ACTIVE("active", 12) {
        @Override
        protected void onEnter(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData, boolean ominous) {
            if (!sharedData.hasDisplayItem()) {
                UnderVaultLogic.Server.cycleDisplayItemFromLootTable(level, this, config, sharedData, pos);
            }
        }
    },
    UNLOCKING("unlocking", 12) {
        @Override
        protected void onEnter(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData, boolean ominous) {
            UndertrialSounds.playServer(level, pos, UndertrialSounds.VAULT_INSERT_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    },
    EJECTING("ejecting", 12) {
        @Override
        protected void onEnter(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData, boolean ominous) {
            UndertrialSounds.playServer(level, pos, UndertrialSounds.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        @Override
        protected void onExit(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData) {
            UndertrialSounds.playServer(level, pos, UndertrialSounds.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    };

    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
    private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
    private final String stateName;
    private final int lightLevel;

    UnderVaultState(String stateName, int lightLevel) {
        this.stateName = stateName;
        this.lightLevel = lightLevel;
    }

    @Override
    public String getSerializedName() {
        return this.stateName;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public UnderVaultState tickAndGetNext(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultServerData serverData,
                                          UnderVaultSharedData sharedData) {
        switch (this) {
            case INACTIVE:
                return updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.activationRange());
            case ACTIVE:
                return updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.deactivationRange());
            case UNLOCKING:
                serverData.pauseStateUpdatingUntil(level.getGameTime() + DELAY_BEFORE_FIRST_EJECTION_TICKS);
                return EJECTING;
            case EJECTING:
            default:
                if (serverData.getItemsToEject().isEmpty()) {
                    serverData.markEjectionFinished();
                    return updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.deactivationRange());
                }
                float progress = serverData.ejectionProgress();
                this.ejectResultItem(level, pos, serverData.popNextItemToEject(), progress);
                sharedData.setDisplayItem(serverData.getNextItemToEject());
                boolean finished = serverData.getItemsToEject().isEmpty();
                int delay = finished ? DELAY_AFTER_LAST_EJECTION_TICKS : DELAY_BETWEEN_EJECTIONS_TICKS;
                serverData.pauseStateUpdatingUntil(level.getGameTime() + delay);
                return EJECTING;
        }
    }

    private static UnderVaultState updateStateForConnectedPlayers(ServerLevel level, BlockPos pos, UnderVaultConfig config,
                                                                  UnderVaultServerData serverData, UnderVaultSharedData sharedData,
                                                                  double range) {
        sharedData.updateConnectedPlayersWithinRange(level, pos, serverData, config, range);
        serverData.pauseStateUpdatingUntil(level.getGameTime() + UPDATE_CONNECTED_PLAYERS_TICK_RATE);
        return sharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onTransition(ServerLevel level, BlockPos pos, UnderVaultState next, UnderVaultConfig config,
                             UnderVaultSharedData sharedData, boolean ominous) {
        this.onExit(level, pos, config, sharedData);
        next.onEnter(level, pos, config, sharedData, ominous);
    }

    protected void onEnter(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData, boolean ominous) {
    }

    protected void onExit(ServerLevel level, BlockPos pos, UnderVaultConfig config, UnderVaultSharedData sharedData) {
    }

    private void ejectResultItem(ServerLevel level, BlockPos pos, ItemStack stack, float progress) {
        DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2D));
        UnderVaultEffects.ejectItem(level, pos);
        UndertrialSounds.playServer(level, pos, UndertrialSounds.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * progress);
    }
}
