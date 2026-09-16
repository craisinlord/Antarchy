package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

import java.util.function.Supplier;

public final class UnderVaultBlockEntity extends BlockEntity {
    private boolean unlocked;

    public UnderVaultBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
                                 Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> type) {
        super(type.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
                             UnderVaultBlockEntity blockEntity) {
        if (level.isClientSide && state.getValue(com.craisinlord.antarchy.content.block.UnderVaultBlock.ACTIVE)
                && level.random.nextInt(4) == 0) {
            Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8.0D, false);
            if (player != null) {
                Vec3 start = Vec3.atCenterOf(pos);
                Vec3 end = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
                Vec3 point = start.lerp(end, 0.15D + level.random.nextDouble() * 0.7D);
                level.addParticle(particle("undervault_connection"), point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static SimpleParticleType particle(String id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(Antarchy.MODID, id));
    }

    public boolean unlock(ServerLevel level, BlockPos pos, Player player, ItemStack key) {
        if (unlocked) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            key.shrink(1);
        }
        LootTable table = level.getServer().getLootData().getLootTable(
                new ResourceLocation(Antarchy.MODID, "chests/undertrial_vault"));
        LootParams params = new LootParams.Builder(level).withParameter(
                net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
        for (ItemStack stack : table.getRandomItems(params)) {
            DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.DOWN,
                    Vec3.atBottomCenterOf(pos).relative(Direction.DOWN, 1.2D));
        }
        unlocked = true;
        setChanged();
        level.levelEvent(3011, pos, 0);
        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        unlocked = tag.getBoolean("Unlocked");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Unlocked", unlocked);
    }
}
