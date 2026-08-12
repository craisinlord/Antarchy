package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class TigerEyeArmorUtil {
    public static final double DETECTION_REDUCTION_PER_PIECE = 0.10D;
    public static final double MAX_PASSIVE_REDUCTION = 0.40D;
    public static final double WALKING_MULTIPLIER = 0.50D;
    public static final double SPRINTING_MULTIPLIER = 0.25D;
    public static final double ACTIVE_CAMOUFLAGE_MULTIPLIER = 2.0D;
    public static final double MAX_ACTIVE_REDUCTION = 0.80D;
    private static final double STATIONARY_HORIZONTAL_SPEED_SQR = 0.0009D;

    private TigerEyeArmorUtil() {
    }

    public static int countEquippedPieces(Player player) {
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).is(AntarchyTags.Items.TIGER_EYE_ARMOR)) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(AntarchyTags.Items.TIGER_EYE_ARMOR)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(AntarchyTags.Items.TIGER_EYE_ARMOR)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(AntarchyTags.Items.TIGER_EYE_ARMOR)
                && player.getItemBySlot(EquipmentSlot.FEET).is(AntarchyTags.Items.TIGER_EYE_ARMOR);
    }

    public static double getPassiveMaximumReduction(Player player) {
        return Math.min(MAX_PASSIVE_REDUCTION, countEquippedPieces(player) * DETECTION_REDUCTION_PER_PIECE);
    }

    public static double getMovementMultiplier(Player player) {
        if (player.isCrouching()) {
            return 1.0D;
        }

        Vec3 delta = player.getDeltaMovement();
        double horizontalSpeedSqr = delta.x * delta.x + delta.z * delta.z;
        if (horizontalSpeedSqr < STATIONARY_HORIZONTAL_SPEED_SQR) {
            return 1.0D;
        }

        if (player.isSprinting()) {
            return SPRINTING_MULTIPLIER;
        }

        return WALKING_MULTIPLIER;
    }

    public static double getDetectionReduction(Player player) {
        double reduction = getPassiveMaximumReduction(player) * getMovementMultiplier(player);
        if (isAdaptiveCamouflageActive(player)) {
            reduction = Math.min(MAX_ACTIVE_REDUCTION, reduction * ACTIVE_CAMOUFLAGE_MULTIPLIER);
        }
        return reduction;
    }

    public static boolean mayActivateAdaptiveCamouflage(Player player) {
        return hasFullSet(player) && !player.isSpectator() && player.isAlive();
    }

    public static boolean isAdaptiveCamouflageActive(Player player) {
        return player instanceof TigerEyeCamouflageAccess access && access.antarchy$isTigerEyeCamouflageActive();
    }

    public static boolean isValidCamouflageBlock(BlockState state) {
        if (state.isAir() || state.is(AntarchyTags.Blocks.TIGER_EYE_CAMOUFLAGE_BLACKLIST)) {
            return false;
        }
        if (state.getRenderShape() == RenderShape.INVISIBLE) {
            return false;
        }
        if (state.is(Blocks.BARRIER) || state.is(Blocks.STRUCTURE_VOID) || state.is(Blocks.LIGHT)) {
            return false;
        }
        if (state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY)) {
            return false;
        }
        if (!Block.isShapeFullBlock(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))) {
            return false;
        }

        FluidState fluidState = state.getFluidState();
        return fluidState == null || fluidState.isEmpty();
    }

    public static boolean shouldReduceDetection(Mob observer, Player player) {
        if (countEquippedPieces(player) <= 0) {
            return false;
        }
        if (observer.getType().is(AntarchyTags.Entities.TIGER_EYE_DETECTION_IMMUNE)) {
            return false;
        }
        if (observer.getTarget() == player || observer.getLastHurtByMob() == player) {
            return false;
        }
        if (observer instanceof net.minecraft.world.entity.NeutralMob neutralMob && neutralMob.isAngryAt(player)) {
            return false;
        }
        return observer.canAttack(player);
    }

    public static BlockPos getCamouflageSamplePos(Player player) {
        double centerX = (player.getBoundingBox().minX + player.getBoundingBox().maxX) * 0.5D;
        double centerZ = (player.getBoundingBox().minZ + player.getBoundingBox().maxZ) * 0.5D;
        return BlockPos.containing(centerX, player.getBoundingBox().minY - 0.05D, centerZ);
    }
}
