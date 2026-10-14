package com.craisinlord.antarchy.content.block.entity.undervault;

import com.craisinlord.antarchy.content.block.UnderVaultBlock;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UnderVaultLogic {
    private UnderVaultLogic() {
    }

    public static final class Client {
        private static final int PARTICLE_TICK_RATE = 20;
        private static final float IDLE_PARTICLE_CHANCE = 0.5F;
        private static final float AMBIENT_SOUND_CHANCE = 0.02F;
        private static final int ACTIVATION_PARTICLE_COUNT = 20;
        private static final int DEACTIVATION_PARTICLE_COUNT = 20;

        private Client() {
        }

        public static void tick(Level level, BlockPos pos, BlockState state, UnderVaultClientData clientData, UnderVaultSharedData sharedData) {
            clientData.updateDisplayItemSpin();
            if (level.getGameTime() % PARTICLE_TICK_RATE == 0L) {
                emitConnectionParticlesForNearbyPlayers(level, pos, state, sharedData);
            }
            emitIdleParticles(level, pos, sharedData, state.getValue(UnderVaultBlock.OMINOUS) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
            playIdleSounds(level, pos, sharedData);
        }

        public static void emitActivationParticles(Level level, BlockPos pos, BlockState state, UnderVaultSharedData sharedData, ParticleOptions particle) {
            emitConnectionParticlesForNearbyPlayers(level, pos, state, sharedData);
            RandomSource random = level.random;
            for (int i = 0; i < ACTIVATION_PARTICLE_COUNT; i++) {
                Vec3 position = randomPosInsideCage(pos, random);
                level.addParticle(ParticleTypes.SMOKE, position.x(), position.y(), position.z(), 0.0D, 0.0D, 0.0D);
                level.addParticle(particle, position.x(), position.y(), position.z(), 0.0D, 0.0D, 0.0D);
            }
        }

        public static void emitDeactivationParticles(Level level, BlockPos pos, ParticleOptions particle) {
            RandomSource random = level.random;
            for (int i = 0; i < DEACTIVATION_PARTICLE_COUNT; i++) {
                Vec3 position = randomPosCenterOfCage(pos, random);
                Vec3 velocity = new Vec3(random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
                level.addParticle(particle, position.x(), position.y(), position.z(), velocity.x(), velocity.y(), velocity.z());
            }
        }

        private static void emitIdleParticles(Level level, BlockPos pos, UnderVaultSharedData sharedData, ParticleOptions particle) {
            RandomSource random = level.getRandom();
            if (random.nextFloat() <= IDLE_PARTICLE_CHANCE) {
                Vec3 position = randomPosInsideCage(pos, random);
                level.addParticle(ParticleTypes.SMOKE, position.x(), position.y(), position.z(), 0.0D, 0.0D, 0.0D);
                if (shouldDisplayActiveEffects(sharedData)) {
                    level.addParticle(particle, position.x(), position.y(), position.z(), 0.0D, 0.0D, 0.0D);
                }
            }
        }

        private static void emitConnectionParticlesForPlayer(Level level, Vec3 keyhole, Player player) {
            RandomSource random = level.random;
            Vec3 toPlayer = keyhole.vectorTo(player.position().add(0.0D, player.getBbHeight() / 2.0F, 0.0D));
            int count = Mth.nextInt(random, 2, 5);
            for (int i = 0; i < count; i++) {
                Vec3 offset = toPlayer.offsetRandom(random, 1.0F);
                level.addParticle(ParticleTypes.ENCHANT, keyhole.x(), keyhole.y(), keyhole.z(), offset.x(), offset.y(), offset.z());
            }
        }

        private static void emitConnectionParticlesForNearbyPlayers(Level level, BlockPos pos, BlockState state, UnderVaultSharedData sharedData) {
            Set<UUID> players = sharedData.getConnectedPlayers();
            if (players.isEmpty()) {
                return;
            }
            Vec3 keyhole = keyholePos(pos, state.getValue(UnderVaultBlock.FACING));
            for (UUID uuid : players) {
                Player player = level.getPlayerByUUID(uuid);
                if (player != null && isWithinConnectionRange(pos, sharedData, player)) {
                    emitConnectionParticlesForPlayer(level, keyhole, player);
                }
            }
        }

        private static boolean isWithinConnectionRange(BlockPos pos, UnderVaultSharedData sharedData, Player player) {
            return player.blockPosition().distSqr(pos) <= Mth.square(sharedData.connectedParticlesRange());
        }

        private static void playIdleSounds(Level level, BlockPos pos, UnderVaultSharedData sharedData) {
            if (shouldDisplayActiveEffects(sharedData)) {
                RandomSource random = level.getRandom();
                if (random.nextFloat() <= AMBIENT_SOUND_CHANCE) {
                    UndertrialSounds.playLocal(level, pos, UndertrialSounds.VAULT_AMBIENT, SoundSource.BLOCKS,
                            random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
                }
            }
        }

        public static boolean shouldDisplayActiveEffects(UnderVaultSharedData sharedData) {
            return sharedData.hasDisplayItem();
        }

        private static Vec3 randomPosCenterOfCage(BlockPos pos, RandomSource random) {
            return Vec3.atLowerCornerOf(pos).add(Mth.nextDouble(random, 0.4D, 0.6D), Mth.nextDouble(random, 0.4D, 0.6D), Mth.nextDouble(random, 0.4D, 0.6D));
        }

        private static Vec3 randomPosInsideCage(BlockPos pos, RandomSource random) {
            return Vec3.atLowerCornerOf(pos).add(Mth.nextDouble(random, 0.1D, 0.9D), Mth.nextDouble(random, 0.25D, 0.75D), Mth.nextDouble(random, 0.1D, 0.9D));
        }

        private static Vec3 keyholePos(BlockPos pos, Direction direction) {
            return Vec3.atBottomCenterOf(pos).add(direction.getStepX() * 0.5D, 1.75D, direction.getStepZ() * 0.5D);
        }
    }

    public static final class Server {
        private static final int UNLOCKING_DELAY_TICKS = 14;
        private static final int DISPLAY_CYCLE_TICK_RATE = 20;
        private static final int INSERT_FAIL_SOUND_BUFFER_TICKS = 15;

        private Server() {
        }

        public static void tick(ServerLevel level, BlockPos pos, BlockState state, UnderVaultConfig config,
                                UnderVaultServerData serverData, UnderVaultSharedData sharedData) {
            UnderVaultState vaultState = state.getValue(UnderVaultBlock.STATE);
            if (shouldCycleDisplayItem(level.getGameTime(), vaultState)) {
                cycleDisplayItemFromLootTable(level, vaultState, config, sharedData, pos);
            }
            BlockState nextState = state;
            if (level.getGameTime() >= serverData.stateUpdatingResumesAt()) {
                nextState = state.setValue(UnderVaultBlock.STATE, vaultState.tickAndGetNext(level, pos, config, serverData, sharedData));
                if (!state.equals(nextState)) {
                    setVaultState(level, pos, state, nextState, config, sharedData);
                }
            }
            if (serverData.isDirty || sharedData.isDirty) {
                level.blockEntityChanged(pos);
                if (!state.isAir()) {
                    level.updateNeighbourForOutputSignal(pos, state.getBlock());
                }
                if (sharedData.isDirty) {
                    level.sendBlockUpdated(pos, state, nextState, 2);
                }
                serverData.isDirty = false;
                sharedData.isDirty = false;
            }
        }

        public static void tryInsertKey(ServerLevel level, BlockPos pos, BlockState state, UnderVaultConfig config,
                                        UnderVaultServerData serverData, UnderVaultSharedData sharedData, Player player, ItemStack stack) {
            UnderVaultState vaultState = state.getValue(UnderVaultBlock.STATE);
            if (!canEjectReward(config, vaultState)) {
                return;
            }
            if (!isValidToInsert(config, stack)) {
                playInsertFailSound(level, serverData, pos, UndertrialSounds.VAULT_INSERT_ITEM_FAIL);
            } else if (serverData.hasRewardedPlayer(player)) {
                playInsertFailSound(level, serverData, pos, UndertrialSounds.VAULT_REJECT_REWARDED_PLAYER);
            } else {
                List<ItemStack> items = resolveItemsToEject(level, config, pos, player);
                if (!items.isEmpty()) {
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(config.keyItem().getCount());
                    }
                    unlock(level, state, pos, config, serverData, sharedData, items);
                    serverData.addToRewardedPlayers(player);
                    sharedData.updateConnectedPlayersWithinRange(level, pos, serverData, config, config.deactivationRange());
                }
            }
        }

        static void setVaultState(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState,
                                  UnderVaultConfig config, UnderVaultSharedData sharedData) {
            UnderVaultState from = oldState.getValue(UnderVaultBlock.STATE);
            UnderVaultState to = newState.getValue(UnderVaultBlock.STATE);
            level.setBlock(pos, newState, 3);
            from.onTransition(level, pos, to, config, sharedData, newState.getValue(UnderVaultBlock.OMINOUS));
        }

        static void cycleDisplayItemFromLootTable(ServerLevel level, UnderVaultState state, UnderVaultConfig config,
                                                  UnderVaultSharedData sharedData, BlockPos pos) {
            if (!canEjectReward(config, state)) {
                sharedData.setDisplayItem(ItemStack.EMPTY);
            } else {
                ItemStack stack = getRandomDisplayItemFromLootTable(level, pos, config.overrideLootTableToDisplay().orElse(config.lootTable()));
                sharedData.setDisplayItem(stack);
            }
        }

        private static ItemStack getRandomDisplayItemFromLootTable(ServerLevel level, BlockPos pos, ResourceLocation lootTableId) {
            LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .create(LootContextParamSets.CHEST);
            List<ItemStack> items = lootTable.getRandomItems(params);
            return items.isEmpty() ? ItemStack.EMPTY : Util.getRandom(items, level.getRandom());
        }

        private static void unlock(ServerLevel level, BlockState state, BlockPos pos, UnderVaultConfig config,
                                   UnderVaultServerData serverData, UnderVaultSharedData sharedData, List<ItemStack> items) {
            serverData.setItemsToEject(items);
            sharedData.setDisplayItem(serverData.getNextItemToEject());
            serverData.pauseStateUpdatingUntil(level.getGameTime() + UNLOCKING_DELAY_TICKS);
            setVaultState(level, pos, state, state.setValue(UnderVaultBlock.STATE, UnderVaultState.UNLOCKING), config, sharedData);
        }

        private static List<ItemStack> resolveItemsToEject(ServerLevel level, UnderVaultConfig config, BlockPos pos, Player player) {
            LootTable lootTable = level.getServer().getLootData().getLootTable(config.lootTable());
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withLuck(player.getLuck())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .create(LootContextParamSets.CHEST);
            return lootTable.getRandomItems(params);
        }

        private static boolean canEjectReward(UnderVaultConfig config, UnderVaultState state) {
            return !config.lootTable().equals(BuiltInLootTables.EMPTY) && !config.keyItem().isEmpty() && state != UnderVaultState.INACTIVE;
        }

        private static boolean isValidToInsert(UnderVaultConfig config, ItemStack stack) {
            return ItemStack.isSameItemSameTags(stack, config.keyItem()) && stack.getCount() >= config.keyItem().getCount();
        }

        private static boolean shouldCycleDisplayItem(long gameTime, UnderVaultState state) {
            return gameTime % DISPLAY_CYCLE_TICK_RATE == 0L && state == UnderVaultState.ACTIVE;
        }

        private static void playInsertFailSound(ServerLevel level, UnderVaultServerData serverData, BlockPos pos, UndertrialSounds.Entry sound) {
            if (level.getGameTime() >= serverData.getLastInsertFailTimestamp() + INSERT_FAIL_SOUND_BUFFER_TICKS) {
                UndertrialSounds.playServer(level, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                serverData.setLastInsertFailTimestamp(level.getGameTime());
            }
        }
    }
}
