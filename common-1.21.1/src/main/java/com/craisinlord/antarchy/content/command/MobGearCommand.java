package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.MobGearScaling;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

public final class MobGearCommand {
    private static final int ROLLS = 500;
    private static final int SPAWNS = 10;
    private static final String[] TIERS = {"none", "iron", "diamond", "netherite", "Ultimate"};

    private MobGearCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mobgear")
                .requires(source -> source.hasPermission(2))
                .executes(MobGearCommand::report)
                .then(Commands.literal("spawn")
                        .executes(context -> spawn(context, ResourceLocation.withDefaultNamespace("zombie")))
                        .then(Commands.argument("entity", ResourceLocationArgument.id())
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                .executes(context -> spawn(context, ResourceLocationArgument.getId(context, "entity"))))));
    }

    private static int report(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        double score = MobGearScaling.score(player);
        int nearby = MobGearScaling.nearbyTier(level, player.position());
        double multiplier = MobGearScaling.chanceMultiplier(level);
        line(context, String.format(Locale.ROOT, "Your score is %.1f (%s). Mobs here use %s.", score, TIERS[MobGearScaling.tierFor(score)],
                nearby < 0 ? "nothing, because nobody in Survival is in range" : nearby == 0 ? "no gear by default" : TIERS[nearby] + " gear"));
        line(context, String.format(Locale.ROOT, "Difficulty %s%s: each chance x%.2f, %.0f%% of pieces a tier higher.", level.getDifficulty().getKey(),
                MobGearScaling.extremeDifficulty() ? " (above Hard)" : "", multiplier, MobGearScaling.higherTierChance(level) * 100.0D));
        if (!AntarchySettings.mobGearScalingEnabled()) {
            line(context, "Mob gear is turned off in the config.");
        }
        if (nearby < 0) {
            return 0;
        }
        Map<String, Integer> materials = new TreeMap<>();
        int pieces = 0;
        int geared = 0;
        int swords = 0;
        BlockPos pos = player.blockPosition();
        for (int i = 0; i < ROLLS; i++) {
            Zombie zombie = EntityType.ZOMBIE.create(level);
            if (zombie == null) {
                return 0;
            }
            zombie.moveTo(pos.getX(), pos.getY(), pos.getZ());
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
            int worn = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = zombie.getItemBySlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (slot == EquipmentSlot.MAINHAND) {
                    swords++;
                } else if (slot.isArmor()) {
                    worn++;
                    String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                    materials.merge(path.contains("_") ? path.substring(0, path.indexOf('_')) : path, 1, Integer::sum);
                }
            }
            pieces += worn;
            geared += worn > 0 ? 1 : 0;
        }
        line(context, String.format(Locale.ROOT, "%d test zombies: %.0f%% have armor, %.2f pieces each, %.0f%% hold something. Pieces: %s", ROLLS,
                geared * 100.0D / ROLLS, (double) pieces / ROLLS, swords * 100.0D / ROLLS, materials));
        return geared;
    }

    private static int spawn(CommandContext<CommandSourceStack> context, ResourceLocation id) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            context.getSource().sendFailure(Component.literal("No entity called " + id));
            return 0;
        }
        int spawned = 0;
        int geared = 0;
        for (int i = 0; i < SPAWNS; i++) {
            double angle = Math.PI * 2.0D * i / SPAWNS;
            BlockPos pos = player.blockPosition().offset((int) Math.round(Math.cos(angle) * 6.0D), 1, (int) Math.round(Math.sin(angle) * 6.0D));
            if (type.spawn(level, pos, MobSpawnType.NATURAL) instanceof Mob mob) {
                spawned++;
                for (ItemStack stack : mob.getArmorSlots()) {
                    if (!stack.isEmpty()) {
                        geared++;
                        break;
                    }
                }
            }
        }
        line(context, "Spawned " + spawned + " " + id + ", " + geared + " wearing armor.");
        return geared;
    }

    private static void line(CommandContext<CommandSourceStack> context, String text) {
        context.getSource().sendSuccess(() -> Component.literal(text), false);
    }
}
