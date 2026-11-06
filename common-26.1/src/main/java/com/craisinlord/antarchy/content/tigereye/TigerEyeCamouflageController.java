package com.craisinlord.antarchy.content.tigereye;

import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TigerEyeCamouflageController {
    public enum ToggleResult {
        ACTIVATED,
        DEACTIVATED,
        FULL_SET_REQUIRED,
        INVALID_BLOCK,
        NO_CHANGE
    }

    private TigerEyeCamouflageController() {
    }

    public static ToggleResult toggle(ServerPlayer player) {
        if (!(player instanceof TigerEyeCamouflageAccess access)) {
            return ToggleResult.NO_CHANGE;
        }

        if (access.antarchy$isTigerEyeCamouflageActive()) {
            deactivate(player, true);
            return ToggleResult.DEACTIVATED;
        }

        if (!TigerEyeArmorUtil.mayActivateAdaptiveCamouflage(player)) {
            return ToggleResult.FULL_SET_REQUIRED;
        }

        BlockPos samplePos = TigerEyeArmorUtil.getCamouflageSamplePos(player);
        BlockState state = player.level().getBlockState(samplePos);
        if (!TigerEyeArmorUtil.isValidCamouflageBlock(state)) {
            return ToggleResult.INVALID_BLOCK;
        }

        access.antarchy$setTigerEyeCamouflageBlockStateId(Block.getId(state));
        access.antarchy$setTigerEyeCamouflageActive(true);
        spawnEffects(player, state, true);
        return ToggleResult.ACTIVATED;
    }

    public static boolean validateOrDeactivate(ServerPlayer player) {
        if (!(player instanceof TigerEyeCamouflageAccess access) || !access.antarchy$isTigerEyeCamouflageActive()) {
            return false;
        }
        if (TigerEyeArmorUtil.hasFullSet(player)) {
            return false;
        }
        deactivate(player, true);
        return true;
    }

    public static void deactivate(ServerPlayer player, boolean spawnEffects) {
        if (!(player instanceof TigerEyeCamouflageAccess access)) {
            return;
        }
        boolean wasActive = access.antarchy$isTigerEyeCamouflageActive();
        int blockStateId = access.antarchy$getTigerEyeCamouflageBlockStateId();
        access.antarchy$setTigerEyeCamouflageActive(false);
        access.antarchy$setTigerEyeCamouflageBlockStateId(0);
        if (spawnEffects && wasActive) {
            spawnEffects(player, Block.stateById(blockStateId), false);
        }
    }

    private static void spawnEffects(ServerPlayer player, BlockState state, boolean activate) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int count = activate ? 18 : 8;
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                count,
                0.35D,
                0.6D,
                0.35D,
                0.03D
        );
        serverLevel.playSound(
                null,
                player.blockPosition(),
                activate ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.AMETHYST_BLOCK_HIT,
                SoundSource.PLAYERS,
                0.8F,
                activate ? 0.9F : 0.7F
        );
    }
}
