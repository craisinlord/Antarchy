package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.UndertrialSpawnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

import java.util.List;
import java.util.function.Supplier;

public final class UndertrialSpawnerBlockEntity extends BlockEntity {
    private static final String COOLDOWN_TAG = "Cooldown";
    private static final String WAVES_TAG = "Waves";
    private static final int MAX_WAVES = 6;
    private int cooldown;
    private int waves;

    public UndertrialSpawnerBlockEntity(BlockPos pos, BlockState state,
                                        Supplier<? extends BlockEntityType<UndertrialSpawnerBlockEntity>> type) {
        super(type.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, UndertrialSpawnerBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            if (level.isClientSide && state.getValue(UndertrialSpawnerBlock.ACTIVE)
                    && level.random.nextInt(2) == 0) {
                level.addParticle(particle("undertrial_omen"), pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.7D,
                        pos.getY() + 0.45D + level.random.nextDouble() * 0.35D,
                        pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.7D, 0.0D, 0.01D, 0.0D);
            }
            return;
        }
        if (blockEntity.cooldown > 0) {
            blockEntity.cooldown--;
            return;
        }
        if (!state.getValue(UndertrialSpawnerBlock.ACTIVE)) {
            return;
        }
        if (serverLevel.getNearestPlayer(pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, 16.0D, false) == null) {
            return;
        }
        long livingTrialMobs = serverLevel.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(16.0D),
                mob -> mob.getTags().contains("antarchy_undertrial_mob")).size();
        if (livingTrialMobs > 0) {
            return;
        }
        if (blockEntity.waves >= MAX_WAVES) {
            blockEntity.ejectReward(serverLevel, pos);
            serverLevel.setBlock(pos, state.setValue(UndertrialSpawnerBlock.ACTIVE, false), 3);
            return;
        }
        Zombie zombie = EntityType.ZOMBIE.create(serverLevel);
        if (zombie != null) {
            zombie.moveTo(pos.getX() + 0.5D, pos.getY() - 1.0D, pos.getZ() + 0.5D,
                    serverLevel.random.nextFloat() * 360.0F, 0.0F);
            zombie.addTag("antarchy_undertrial_mob");
            serverLevel.addFreshEntity(zombie);
            blockEntity.waves++;
            blockEntity.cooldown = 80;
            blockEntity.setChanged();
        }
    }

    private static SimpleParticleType particle(String id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(Antarchy.MODID, id));
    }

    private void ejectReward(ServerLevel level, BlockPos pos) {
        ResourceLocation id = new ResourceLocation(Antarchy.MODID, "spawners/undertrial");
        LootTable table = level.getServer().getLootData().getLootTable(id);
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        for (ItemStack stack : table.getRandomItems(params)) {
            DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.DOWN,
                    Vec3.atBottomCenterOf(pos).relative(Direction.DOWN, 1.2D));
        }
        level.levelEvent(3014, pos, 0);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cooldown = tag.getInt(COOLDOWN_TAG);
        waves = tag.getInt(WAVES_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(COOLDOWN_TAG, cooldown);
        tag.putInt(WAVES_TAG, waves);
    }
}
