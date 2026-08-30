package com.craisinlord.antarchy.content.time;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class TimeDilationCommand {
    private TimeDilationCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("timedilation")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("create")
                                .then(Commands.argument("center", Vec3Argument.vec3())
                                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.5D, 128.0D))
                                                .then(Commands.argument("rate", DoubleArgumentType.doubleArg(TimeDilationMath.MIN_RATE, 1.0D))
                                                        .then(Commands.argument("durationTicks", IntegerArgumentType.integer(-1))
                                                                .executes(TimeDilationCommand::create))))))
                        .then(Commands.literal("create_here")
                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.5D, 128.0D))
                                        .then(Commands.argument("rate", DoubleArgumentType.doubleArg(TimeDilationMath.MIN_RATE, 1.0D))
                                                .then(Commands.argument("durationTicks", IntegerArgumentType.integer(-1))
                                                        .executes(TimeDilationCommand::createHere)))))
                        .then(Commands.literal("list").executes(TimeDilationCommand::list))
                        .then(Commands.literal("clear").executes(TimeDilationCommand::clear))
        );
    }

    private static int create(CommandContext<CommandSourceStack> context) {
        return createField(context, Vec3Argument.getVec3(context, "center"));
    }

    private static int createHere(CommandContext<CommandSourceStack> context) {
        return createField(context, context.getSource().getPosition());
    }

    private static int createField(CommandContext<CommandSourceStack> context, Vec3 center) {
        ServerLevel level = context.getSource().getLevel();
        double radius = DoubleArgumentType.getDouble(context, "radius");
        double rate = DoubleArgumentType.getDouble(context, "rate");
        int durationTicks = IntegerArgumentType.getInteger(context, "durationTicks");
        TimeDilationFieldEntity field = TimeDilationApi.createField(level, center, radius, rate, durationTicks);
        context.getSource().sendSuccess(
                () -> Component.literal("Created time dilation field " + field.getUUID() + " radius=" + radius
                        + " rate=" + rate + " durationTicks=" + durationTicks),
                true
        );
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        int count = 0;
        for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof TimeDilationFieldEntity field)) {
                    continue;
                }
                count++;
                context.getSource().sendSuccess(
                        () -> Component.literal(field.getUUID() + " " + level.dimension().location() + " @ "
                                + format(field.position()) + " radius=" + field.fieldRadius() + " rate=" + field.fieldRate()
                                + " age=" + field.fieldAge()
                                + (field.isInfinite() ? " duration=infinite" : " duration=" + field.fieldDurationTicks())),
                        false
                );
            }
        }
        return count;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        List<TimeDilationFieldEntity> fields = new ArrayList<>();
        for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof TimeDilationFieldEntity field) {
                    fields.add(field);
                }
            }
        }
        fields.forEach(Entity::discard);
        int count = fields.size();
        context.getSource().sendSuccess(() -> Component.literal("Cleared " + count + " time dilation field(s)"), true);
        return count;
    }

    private static String format(Vec3 vec) {
        return String.format(java.util.Locale.ROOT, "%.2f %.2f %.2f", vec.x, vec.y, vec.z);
    }
}
