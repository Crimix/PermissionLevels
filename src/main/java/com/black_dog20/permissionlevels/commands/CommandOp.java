package com.black_dog20.permissionlevels.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommandOp {

    private static final SimpleCommandExceptionType ALREADY_OP = new SimpleCommandExceptionType(new TranslationTextComponent("commands.op.failed"));
    private static final SimpleCommandExceptionType LEVEL_TOO_HIGH = new SimpleCommandExceptionType(new StringTextComponent("Cannot op player to higher level than your level"));

    public static final SuggestionProvider<CommandSource> SUGGESTIONS_PROVIDER = (context, suggestionsBuilder) -> {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Stream<String> playerNames = playerlist.getPlayers().stream()
                .filter(getNonOps(playerlist))
                .map(CommandOp::getName);
        return ISuggestionProvider.suggest(playerNames, suggestionsBuilder);
    };

    public static final SuggestionProvider<CommandSource> LEVEL_SUGGESTIONS_PROVIDER = (context, suggestionsBuilder) -> {
        int level = context.getSource().permissionLevel;
        Stream<String> levels = IntStream.rangeClosed(1, level)
                .mapToObj(Integer::toString);
        return ISuggestionProvider.suggest(levels, suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(getBuilder("op"));
        dispatcher.register(getBuilder("xop"));
    }

    private static LiteralArgumentBuilder<CommandSource> getBuilder(String literal) {
        return Commands.literal(literal)
                .requires(source -> source.hasPermissionLevel(3))
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests(SUGGESTIONS_PROVIDER)
                        .executes(CommandOp::execute)
                        .then(Commands.argument("bypassPlayerLimit", BoolArgumentType.bool())
                                .executes(CommandOp::execute))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 4))
                                .suggests(LEVEL_SUGGESTIONS_PROVIDER)
                                .executes(CommandOp::executeLevel)
                                .then(Commands.argument("bypassPlayerLimit", BoolArgumentType.bool())
                                        .executes(CommandOp::executeLevel))));
    }

    public static int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        int serverOpLevel = playerlist.getServer().getOpPermissionLevel();
        int commandOpLevel = context.getSource().permissionLevel;
        int level = Math.min(serverOpLevel, commandOpLevel);
        return baseExecute(context, level);
    }

    public static int executeLevel(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(context, "level");
        int commandOpLevel = context.getSource().permissionLevel;
        if(level > commandOpLevel) {
            throw LEVEL_TOO_HIGH.create();
        }
        return baseExecute(context, level);
    }

    private static int baseExecute(CommandContext<CommandSource> context, int level) throws CommandSyntaxException {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(context, "targets");
        Optional<Boolean> optionalBypassPlayerLimit = getArgument(() -> BoolArgumentType.getBool(context, "bypassPlayerLimit"));
        int i = 0;

        for(GameProfile gameprofile : gameProfiles) {
            boolean bypassPlayerLimitBefore = playerlist.bypassesPlayerLimit(gameprofile);
            boolean bypassPlayerLimit = optionalBypassPlayerLimit.orElseGet(() -> bypassPlayerLimitBefore);
            if (!playerlist.canSendCommands(gameprofile) || playerlist.getServer().getPermissionLevel(gameprofile) != level || (bypassPlayerLimitBefore != bypassPlayerLimit)) {
                addOp(playerlist, gameprofile, level, bypassPlayerLimit);
                ++i;
                if (bypassPlayerLimit && !bypassPlayerLimitBefore) {
                    context.getSource().sendFeedback(new StringTextComponent(String.format("Made %s a server operator with permission level %d and allowed to bypass player limit", gameprofile.getName(), level)), true);
                } else {
                    context.getSource().sendFeedback(new StringTextComponent(String.format("Made %s a server operator with permission level %d", gameprofile.getName(), level)), true);
                }
            }
        }

        if (i == 0) {
            throw ALREADY_OP.create();
        } else {
            return i;
        }
    }

    public static void addOp(PlayerList playerList, GameProfile profile, int level, boolean bypassPlayerLimit) {
        OpList ops = playerList.getOppedPlayers();
        ops.addEntry(new OpEntry(profile, level, bypassPlayerLimit));
        ServerPlayerEntity serverPlayerEntity = playerList.getPlayerByUUID(profile.getId());
        if (serverPlayerEntity != null) {
            playerList.updatePermissionLevel(serverPlayerEntity);
        }
    }

    private static Predicate<ServerPlayerEntity> getNonOps(PlayerList playerList) {
        return (player) -> !playerList.canSendCommands(player.getGameProfile());
    }

    private static String getName(ServerPlayerEntity player) {
        return player.getGameProfile().getName();
    }

    private static <V> Optional<V> getArgument(Supplier<V> optionalFunction) {
        try {
            return Optional.of(optionalFunction.get());
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
