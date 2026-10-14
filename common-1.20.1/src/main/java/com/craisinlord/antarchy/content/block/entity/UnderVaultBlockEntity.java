package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.UnderVaultBlock;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultConfig;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultData;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultLogic;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Supplier;

public final class UnderVaultBlockEntity extends BlockEntity {
    private final UnderVaultData vaultData;
    private boolean playerWasNearby;

    public UnderVaultBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> type) {
        super(type.get(), pos, state);
        this.vaultData = new UnderVaultData();
        this.vaultData.setConfig(createConfig());
    }

    private static UnderVaultConfig createConfig() {
        return new UnderVaultConfig(new ResourceLocation(Antarchy.MODID, "chests/undertrial_vault"), 4.0D, 4.0D,
                new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(Antarchy.MODID, "undertrial_key"))),
                Optional.empty());
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UnderVaultBlockEntity blockEntity) {
        blockEntity.vaultData.setLevel(level);
        boolean playerNearby = !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(4.0D), player -> !player.isSpectator()).isEmpty();
        if (state.getValue(UnderVaultBlock.STATE) == UnderVaultState.INACTIVE && playerNearby && !blockEntity.playerWasNearby) {
            state = state.setValue(UnderVaultBlock.STATE, UnderVaultState.ACTIVE);
            level.setBlockAndUpdate(pos, state);
        }
        blockEntity.playerWasNearby = playerNearby;
        UnderVaultLogic.Server.tick(level, pos, state, blockEntity.vaultData.getConfig(), blockEntity.vaultData.getServerData(), blockEntity.vaultData.getSharedData());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, UnderVaultBlockEntity blockEntity) {
        blockEntity.vaultData.setLevel(level);
        UnderVaultLogic.Client.tick(level, pos, state, blockEntity.vaultData.getClientData(), blockEntity.vaultData.getSharedData());
        UnderVaultState vaultState = state.getValue(UnderVaultBlock.STATE);
        if (level.isClientSide && level.random.nextInt(4) == 0
                && (vaultState == UnderVaultState.ACTIVE || vaultState == UnderVaultState.UNLOCKING)) {
            Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8.0D, false);
            if (player != null) {
                Vec3 start = Vec3.atCenterOf(pos);
                Vec3 end = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
                double progress = 0.15D + level.random.nextDouble() * 0.7D;
                Vec3 point = start.lerp(end, progress);
                level.addParticle((SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(Antarchy.MODID, "undervault_connection")), point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.vaultData.setLevel(level);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        vaultData.load(tag);
        vaultData.setConfig(createConfig());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(vaultData.save());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return vaultData.save();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public UnderVaultData getVaultData() {
        return vaultData;
    }
}
