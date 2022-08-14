package com.black_dog20.permissionlevels.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.OpEntry;
import net.minecraft.server.management.OpList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;
import java.util.stream.Stream;

public class CommandBypassPlayerLimit {

    private static final SimpleCommandExceptionType NOTHING_CHANGED  = new SimpleCommandExceptionType(new StringTextComponent("Nothing changed"));

    public static final SuggestionProvider<CommandSource> SUGGESTIONS_PROVIDER = (context, suggestionsBuilder) -> {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Stream<String> playerNames = playerlist.getPlayers().stream()
                .map(CommandBypassPlayerLimit::getName);
        return ISuggestionProvider.suggest(playerNames, suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(getBuilder());
    }

    private static LiteralArgumentBuilder<CommandSource> getBuilder() {
        return Commands.literal("bypassPlayerLimit")
                .requires(source -> source.hasPermissionLevel(3))
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests(SUGGESTIONS_PROVIDER)
                        .then(Commands.argument("canBypass", BoolArgumentType.bool())
                                .executes(CommandBypassPlayerLimit::execute)));
    }

    private static int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(context, "targets");
        boolean canBypass = BoolArgumentType.getBool(context, "canBypass");
        int i = 0;

        for(GameProfile gameprofile : gameProfiles) {
            if (playerlist.canSendCommands(gameprofile) && (playerlist.bypassesPlayerLimit(gameprofile) != canBypass)) {
                setBypass(playerlist, gameprofile, canBypass);
                ++i;
                if (canBypass) {
                    context.getSource().sendFeedback(new StringTextComponent(String.format("Allowed %s to bypass player limit", gameprofile.getName())), true);
                } else {
                    context.getSource().sendFeedback(new StringTextComponent(String.format("Denied %s to bypass player limit", gameprofile.getName())), true);
                }
            }
        }

        if (i == 0) {
            throw NOTHING_CHANGED.create();
        } else {
            return i;
        }
    }

    public static void setBypass(PlayerList playerList, GameProfile profile, boolean bypassPlayerLimit) {
        OpList ops = playerList.getOppedPlayers();
        ops.addEntry(new OpEntry(profile, playerList.getServer().getPermissionLevel(profile), bypassPlayerLimit));
    }
    private static String getName(ServerPlayerEntity player) {
        return player.getGameProfile().getName();
    }
}
