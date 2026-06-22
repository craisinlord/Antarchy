package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import com.craisinlord.antarchy.content.item.BloodCrystalArmorItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import org.joml.Vector3f;

public final class BloodglassManager {
    private static final TagKey<DamageType> BYPASSES_BLOODGLASS = TagKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("antarchy", "bypasses_bloodglass")
    );

    private BloodglassManager() {}

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof Player player)) return true;
            if (entity.level().isClientSide()) return true;
            if (amount <= 1.0f) return true;
            if (source.is(BYPASSES_BLOODGLASS)) return true;

            BloodglassAccess access = (BloodglassAccess) player;
            if (access.antarchy$getTotalShieldsActive() <= 0) return true;

            if (access.antarchy$getAppleShieldsActive() > 0) {
                access.antarchy$setAppleShieldsActive(access.antarchy$getAppleShieldsActive() - 1);
                access.antarchy$setAppleShieldLostCount(access.antarchy$getAppleShieldLostCount() + 1);
                if (access.antarchy$getAppleRechargeTimer() == 0) {
                    access.antarchy$setAppleRechargeTimer(AntarchySettings.bloodCrystalAppleShieldRechargeTicks());
                }
            } else {
                access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
                if (access.antarchy$getArmorRechargeTimer() == 0) {
                    access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
                }
            }

            ServerLevel serverLevel = (ServerLevel) player.level();
            serverLevel.broadcastEntityEvent(player, (byte) 2);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(0.85f, 0.18f, 0.38f), 1.2f),
                    player.getX(), player.getEyeY(), player.getZ(),
                    35, 0.35, 0.55, 0.35, 0.12
            );

            syncBloodglass((ServerPlayer) player);
            return false;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> handleLogin(handler.player));
    }

    public static void syncBloodglass(ServerPlayer player) {
        BloodglassAccess access = (BloodglassAccess) player;
        AntarchyFabricNetworking.syncBloodglass(player,
                access.antarchy$getTotalShieldsActive(),
                access.antarchy$getTotalShieldsMax());
    }

    public static void handleArmorEquipChange(Player player, EquipmentSlot slot, ItemStack from, ItemStack to) {
        if (player.level().isClientSide()) return;
        if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST
                && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) return;

        boolean fromIsBC = from.getItem() instanceof BloodCrystalArmorItem;
        boolean toIsBC = to.getItem() instanceof BloodCrystalArmorItem;
        if (!fromIsBC && !toIsBC) return;

        BloodglassAccess access = (BloodglassAccess) player;

        if (fromIsBC && !toIsBC) {
            if (access.antarchy$getArmorShieldsActive() > 0) {
                access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
            } else if (access.antarchy$getArmorShieldLostCount() > 0) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
                if (access.antarchy$getArmorShieldLostCount() == 0) {
                    access.antarchy$setArmorRechargeTimer(0);
                }
            }
        } else if (!fromIsBC) {
            // Count BC pieces: explicitly include the new piece (to) since getItemBySlot(slot)
            // may not reflect the new item yet when onEquipItem fires on Fabric.
            int bcPieces = 1; // the piece being equipped now
            for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (s == slot) continue;
                if (player.getItemBySlot(s).getItem() instanceof BloodCrystalArmorItem) bcPieces++;
            }
            int currentTotal = access.antarchy$getArmorShieldsActive() + access.antarchy$getArmorShieldLostCount();
            if (currentTotal < bcPieces) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
                access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
            }
        } else {
            // BC → BC swap: remove old slot, queue new one
            if (access.antarchy$getArmorShieldsActive() > 0) {
                access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() - 1);
            } else if (access.antarchy$getArmorShieldLostCount() > 0) {
                access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
            }
            access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() + 1);
            access.antarchy$setArmorRechargeTimer(AntarchySettings.bloodCrystalArmorShieldRechargeTicks());
        }

        if (player instanceof ServerPlayer sp) syncBloodglass(sp);
    }

    public static void handleWardApplied(Player player, int amplifier) {
        if (player.level().isClientSide()) return;
        BloodglassAccess access = (BloodglassAccess) player;
        int cap = Math.max(0, AntarchySettings.bloodCrystalHardMaxShields()
                - access.antarchy$getArmorShieldsActive()
                - access.antarchy$getArmorShieldLostCount());
        access.antarchy$setAppleShieldsActive(Math.min(amplifier + 1, cap));
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        if (player instanceof ServerPlayer sp) syncBloodglass(sp);
    }

    public static void handleWardRemoved(Player player) {
        if (player.level().isClientSide()) return;
        BloodglassAccess access = (BloodglassAccess) player;
        access.antarchy$setAppleShieldsActive(0);
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        if (player instanceof ServerPlayer sp) syncBloodglass(sp);
    }

    public static void tickRecharge(ServerPlayer player) {
        BloodglassAccess access = (BloodglassAccess) player;
        boolean changed = false;
        int hardCap = AntarchySettings.bloodCrystalHardMaxShields();

        if (access.antarchy$getArmorShieldLostCount() > 0) {
            int timer = access.antarchy$getArmorRechargeTimer();
            if (timer > 1) {
                access.antarchy$setArmorRechargeTimer(timer - 1);
            } else if (timer == 1) {
                if (access.antarchy$getTotalShieldsActive() < hardCap) {
                    access.antarchy$setArmorShieldsActive(access.antarchy$getArmorShieldsActive() + 1);
                    access.antarchy$setArmorShieldLostCount(access.antarchy$getArmorShieldLostCount() - 1);
                    changed = true;
                }
                access.antarchy$setArmorRechargeTimer(access.antarchy$getArmorShieldLostCount() > 0
                        ? AntarchySettings.bloodCrystalArmorShieldRechargeTicks() : 0);
            }
        }

        if (access.antarchy$getAppleShieldLostCount() > 0 && player.hasEffect(AntarchyObjects.BLOODGLASS_WARD.get())) {
            int timer = access.antarchy$getAppleRechargeTimer();
            if (timer > 1) {
                access.antarchy$setAppleRechargeTimer(timer - 1);
            } else if (timer == 1) {
                if (access.antarchy$getTotalShieldsActive() < hardCap) {
                    access.antarchy$setAppleShieldsActive(access.antarchy$getAppleShieldsActive() + 1);
                    access.antarchy$setAppleShieldLostCount(access.antarchy$getAppleShieldLostCount() - 1);
                    changed = true;
                }
                access.antarchy$setAppleRechargeTimer(access.antarchy$getAppleShieldLostCount() > 0
                        ? AntarchySettings.bloodCrystalAppleShieldRechargeTicks() : 0);
            }
        }

        if (changed) {
            syncBloodglass(player);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.6f);
        }
    }

    public static void handleDeath(ServerPlayer player) {
        BloodglassAccess access = (BloodglassAccess) player;
        access.antarchy$setArmorShieldsActive(0);
        access.antarchy$setArmorShieldLostCount(0);
        access.antarchy$setArmorRechargeTimer(0);
        access.antarchy$setAppleShieldsActive(0);
        access.antarchy$setAppleShieldLostCount(0);
        access.antarchy$setAppleRechargeTimer(0);
        syncBloodglass(player);
    }

    public static void handleRespawn(ServerPlayer player) {
        BloodglassAccess access = (BloodglassAccess) player;
        int shields = Math.min(countBCArmorPieces(player), AntarchySettings.bloodCrystalHardMaxShields());
        access.antarchy$setArmorShieldsActive(shields);
        access.antarchy$setArmorShieldLostCount(0);
        access.antarchy$setArmorRechargeTimer(0);
        syncBloodglass(player);
    }

    private static void handleLogin(ServerPlayer player) {
        BloodglassAccess access = (BloodglassAccess) player;
        if (player.hasEffect(AntarchyObjects.BLOODGLASS_WARD.get())) {
            var effect = player.getEffect(AntarchyObjects.BLOODGLASS_WARD.get());
            if (effect != null) {
                int cap = Math.max(0, AntarchySettings.bloodCrystalHardMaxShields()
                        - access.antarchy$getArmorShieldsActive()
                        - access.antarchy$getArmorShieldLostCount());
                access.antarchy$setAppleShieldsActive(Math.min(effect.getAmplifier() + 1, cap));
                access.antarchy$setAppleShieldLostCount(0);
                access.antarchy$setAppleRechargeTimer(0);
            }
        }
        syncBloodglass(player);
    }

    private static int countBCArmorPieces(Player player) {
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).getItem() instanceof BloodCrystalArmorItem) count++;
        }
        return count;
    }
}
