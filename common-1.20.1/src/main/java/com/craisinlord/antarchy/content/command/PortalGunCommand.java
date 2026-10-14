package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

public final class PortalGunCommand {
    private PortalGunCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("portalgun")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("clear")
                                .then(Commands.literal("all")
                                        .executes(PortalGunCommand::clearAll))
                                .then(Commands.argument("owners", GameProfileArgument.gameProfile())
                                        .executes(PortalGunCommand::clearOwners)))
        );
    }

    private static int clearAll(CommandContext<CommandSourceStack> context) {
        int cleared = PortalGunSavedData.clearEverything(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("commands.antarchy.portalgun.clear.all", cleared), true);
        return cleared;
    }

    private static int clearOwners(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "owners");
        Set<UUID> ownerIds = new HashSet<>();
        for (GameProfile profile : profiles) {
            ownerIds.add(profile.getId());
        }
        int cleared = PortalGunSavedData.clearOwnedBy(context.getSource().getServer(), ownerIds);
        context.getSource().sendSuccess(() -> Component.translatable("commands.antarchy.portalgun.clear.owners", cleared, profiles.size()), true);
        return cleared;
    }
}
