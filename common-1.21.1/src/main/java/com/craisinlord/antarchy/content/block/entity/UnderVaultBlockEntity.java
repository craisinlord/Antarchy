package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Optional;
import java.util.function.Supplier;

public final class UnderVaultBlockEntity extends BlockEntity {
    private final VaultBlockEntity vaultData;

    public UnderVaultBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> type) {
        super(type.get(), pos, state);
        this.vaultData = new VaultBlockEntity(pos, Blocks.VAULT.defaultBlockState());
        this.vaultData.setConfig(createConfig());
    }

    private static VaultConfig createConfig() {
        return new VaultConfig(ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "chests/undertrial_vault")), 4.0D, 4.0D,
                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "undertrial_key"))),
                Optional.empty());
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UnderVaultBlockEntity blockEntity) {
        blockEntity.vaultData.setLevel(level);
        VaultBlockEntity.Server.tick(level, pos, state, blockEntity.vaultData.getConfig(), blockEntity.vaultData.getServerData(), blockEntity.vaultData.getSharedData());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, UnderVaultBlockEntity blockEntity) {
        blockEntity.vaultData.setLevel(level);
        VaultBlockEntity.Client.tick(level, pos, state, blockEntity.vaultData.getClientData(), blockEntity.vaultData.getSharedData());
        if (level instanceof ClientLevel clientLevel && clientLevel.random.nextInt(4) == 0
                && (vaultState(state) == VaultState.ACTIVE || vaultState(state) == VaultState.UNLOCKING)) {
            Player player = clientLevel.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8.0D, false);
            if (player != null) {
                Vec3 start = Vec3.atCenterOf(pos);
                Vec3 end = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
                double progress = 0.15D + clientLevel.random.nextDouble() * 0.7D;
                Vec3 point = start.lerp(end, progress);
                clientLevel.addParticle((SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "undervault_connection")), point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        vaultData.loadCustomOnly(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.merge(vaultData.saveCustomOnly(registries));
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
        return (VaultState) state.getValues().entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals("vault_state"))
                .findFirst()
                .orElseThrow()
                .getValue();
    }
}
