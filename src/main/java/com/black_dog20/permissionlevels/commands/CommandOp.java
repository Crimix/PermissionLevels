package com.black_dog20.permissionlevels.commands;

import com.black_dog20.bml.utils.translate.TranslationUtil;
import com.black_dog20.permissionlevels.PermissionLevels;
import com.black_dog20.permissionlevels.utils.Translations;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommandOp {

    private static final SimpleCommandExceptionType ALREADY_OP = new SimpleCommandExceptionType(new TranslatableComponent("commands.op.failed"));

    public static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_PROVIDER = (context, suggestionsBuilder) -> {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Stream<String> playerNames = playerlist.getPlayers().stream()
                .filter(getNonOps(playerlist))
                .map(CommandOp::getName);
        return SharedSuggestionProvider.suggest(playerNames, suggestionsBuilder);
    };

    public static final SuggestionProvider<CommandSourceStack> LEVEL_SUGGESTIONS_PROVIDER = (context, suggestionsBuilder) -> {
        int level = context.getSource().permissionLevel;
        Stream<String> levels = IntStream.rangeClosed(1, level)
                .mapToObj(Integer::toString);
        return SharedSuggestionProvider.suggest(levels, suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(getBuilder("op"));
        dispatcher.register(getBuilder("xop"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getBuilder(String literal) {
        return Commands.literal(literal)
                .requires(source -> source.hasPermission(3))
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

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        int serverOpLevel = playerlist.getServer().getOperatorUserPermissionLevel();
        int commandOpLevel = context.getSource().permissionLevel;
        int level = Math.min(serverOpLevel, commandOpLevel);
        return baseExecute(context, level);
    }

    private static int executeLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(context, "level");
        int commandOpLevel = context.getSource().permissionLevel;
        if(level > commandOpLevel) {
            throw getLevelTooHigh(context).create();
        }
        return baseExecute(context,  level);
    }

    private static int baseExecute(CommandContext<CommandSourceStack> context, int level) throws CommandSyntaxException {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(context, "targets");
        Optional<Boolean> optionalBypassPlayerLimit = getArgument(() -> BoolArgumentType.getBool(context, "bypassPlayerLimit"));
        int i = 0;

        for(GameProfile gameprofile : gameProfiles) {
            boolean bypassPlayerLimitBefore = playerlist.canBypassPlayerLimit(gameprofile);
            boolean bypassPlayerLimit = optionalBypassPlayerLimit.orElseGet(() -> bypassPlayerLimitBefore);
            if (!playerlist.isOp(gameprofile) || playerlist.getServer().getProfilePermissions(gameprofile) != level || (bypassPlayerLimitBefore != bypassPlayerLimit)) {
                ServerPlayer serverPlayerEntity = playerlist.getPlayer(gameprofile.getId());
                addOp(playerlist, gameprofile, level, bypassPlayerLimit);
                ++i;
                if (bypassPlayerLimit && !bypassPlayerLimitBefore) {
                    context.getSource().sendSuccess(TranslationUtil.createPossibleEagerTranslation(Translations.OP_WITH_LEVEL_BYPASS.get(gameprofile.getName(), level), isModPresent(serverPlayerEntity)), true);
                } else {
                    context.getSource().sendSuccess(TranslationUtil.createPossibleEagerTranslation(Translations.OP_WITH_LEVEL.get(gameprofile.getName(), level), isModPresent(serverPlayerEntity)), true);
                }
            }
        }

        if (i == 0) {
            throw ALREADY_OP.create();
        } else {
            return i;
        }
    }

    private static void addOp(PlayerList playerList, GameProfile profile, int level, boolean bypassPlayerLimit) {
        ServerOpList ops = playerList.getOps();
        ops.add(new ServerOpListEntry(profile, level, bypassPlayerLimit));
        ServerPlayer serverPlayerEntity = playerList.getPlayer(profile.getId());
        if (serverPlayerEntity != null) {
            playerList.sendPlayerPermissionLevel(serverPlayerEntity);
        }
    }

    private static Predicate<ServerPlayer> getNonOps(PlayerList playerList) {
        return (player) -> !playerList.isOp(player.getGameProfile());
    }

    private static String getName(ServerPlayer player) {
        return player.getGameProfile().getName();
    }

    private static Optional<ServerPlayer> getPlayerOrNon(CommandContext<CommandSourceStack> context) {
        Entity entity = context.getSource().getEntity();
        if (entity == null)
            return Optional.empty();
        else if (entity instanceof ServerPlayer player)
            return Optional.of(player);
        else
            return Optional.empty();
    }

    private static boolean isModPresent(@Nullable ServerPlayer client) {
        return client != null && PermissionLevels.NETWORK.isRemotePresent(client.connection.getConnection());
    }

    private static SimpleCommandExceptionType getLevelTooHigh(CommandContext<CommandSourceStack> context) {
        boolean translateOnClient = getPlayerOrNon(context)
                .map(CommandOp::isModPresent)
                .orElse(false);

        return new SimpleCommandExceptionType(TranslationUtil.createPossibleEagerTranslation(Translations.LEVEL_TOO_HIGH.get(), translateOnClient));
    }

    private static <V> Optional<V> getArgument(Supplier<V> optionalFunction) {
        try {
            return Optional.of(optionalFunction.get());
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
