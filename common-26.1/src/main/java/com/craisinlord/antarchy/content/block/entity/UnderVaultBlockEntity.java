package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.UnderVaultBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
import java.util.function.Supplier;

public final class UnderVaultBlockEntity extends BlockEntity {
    private final VaultBlockEntity vaultData;
    private boolean playerWasNearby;

    public UnderVaultBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> type) {
        super(type.get(), pos, state);
        this.vaultData = new VaultBlockEntity(pos, Blocks.VAULT.defaultBlockState());
        this.vaultData.setConfig(createConfig());
    }

    private static VaultConfig createConfig() {
        return new VaultConfig(ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                        Identifier.fromNamespaceAndPath(Antarchy.MODID, "chests/undertrial_vault")), 4.0D, 4.0D,
                new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, "undertrial_key"))),
                Optional.empty());
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UnderVaultBlockEntity blockEntity) {
        blockEntity.vaultData.setLevel(level);
        boolean playerNearby = !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(4.0D), player -> !player.isSpectator()).isEmpty();
        if (state.getValue(UnderVaultBlock.STATE) == VaultState.INACTIVE && playerNearby && !blockEntity.playerWasNearby) {
            state = state.setValue(UnderVaultBlock.STATE, VaultState.ACTIVE);
            level.setBlockAndUpdate(pos, state);
        }
        blockEntity.playerWasNearby = playerNearby;
        VaultBlockEntity.Server.tick(level, pos, state, blockEntity.vaultData.getConfig(), blockEntity.vaultData.getServerData(), blockEntity.vaultData.getSharedData());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, UnderVaultBlockEntity blockEntity) {
        blockEntity.vaultData.setLevel(level);
        VaultBlockEntity.Client.tick(level, pos, state, blockEntity.vaultData.getClientData(), blockEntity.vaultData.getSharedData());
        if (level instanceof ClientLevel clientLevel && clientLevel.getRandom().nextInt(4) == 0
                && (vaultState(state) == VaultState.ACTIVE || vaultState(state) == VaultState.UNLOCKING)) {
            Player player = clientLevel.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8.0D, false);
            if (player != null) {
                Vec3 start = Vec3.atCenterOf(pos);
                Vec3 end = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
                double progress = 0.15D + clientLevel.getRandom().nextDouble() * 0.7D;
                Vec3 point = start.lerp(end, progress);
                clientLevel.addParticle((SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, "undervault_connection")), point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        vaultData.loadCustomOnly(input);
        vaultData.setConfig(createConfig());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        vaultData.saveCustomOnly(output);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return vaultData.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public VaultBlockEntity getVaultData() {
        return vaultData;
    }

    private static VaultState vaultState(BlockState state) {
        return (VaultState) state.getValues()
                .filter(entry -> entry.property().getName().equals("vault_state"))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
