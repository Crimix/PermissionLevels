package com.black_dog20.permissionlevels.commands;

import com.black_dog20.bml.utils.translate.TranslationUtil;
import com.black_dog20.permissionlevels.PermissionLevels;
import com.black_dog20.permissionlevels.utils.Translations;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

public class CommandBypassPlayerLimit {

    private static final SimpleCommandExceptionType NOTHING_CHANGED = new SimpleCommandExceptionType(Component.translatable("permissionlevels.command.bypass_player_limit.failed"));

      public static final SuggestionProvider<CommandSourceStack> SUGGESTIONS_PROVIDER = (context, suggestionsBuilder) -> {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Stream<String> playerNames = playerlist.getPlayers().stream()
                .map(CommandBypassPlayerLimit::getName);
        return SharedSuggestionProvider.suggest(playerNames, suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(getBuilder());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getBuilder() {
        return Commands.literal("bypassPlayerLimit")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests(SUGGESTIONS_PROVIDER)
                        .then(Commands.argument("canBypass", BoolArgumentType.bool())
                                .executes(CommandBypassPlayerLimit::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerList playerlist = context.getSource().getServer().getPlayerList();
        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(context, "targets");
        boolean canBypass = BoolArgumentType.getBool(context, "canBypass");
        int i = 0;

        for(GameProfile gameprofile : gameProfiles) {
            if (playerlist.isOp(gameprofile) && (playerlist.canBypassPlayerLimit(gameprofile) != canBypass)) {
                ServerPlayer serverPlayerEntity = playerlist.getPlayer(gameprofile.getId());
                setBypass(playerlist, gameprofile, canBypass);
                ++i;
                if (canBypass) {
                    context.getSource().sendSuccess(TranslationUtil.createPossibleEagerTranslation(Translations.PLAYER_BYPASS_ALLOWED.get(gameprofile.getName()), isModPresent(serverPlayerEntity)), true);
                } else {
                    context.getSource().sendSuccess(TranslationUtil.createPossibleEagerTranslation(Translations.PLAYER_BYPASS_DENIED.get(gameprofile.getName()), isModPresent(serverPlayerEntity)), true);
                }
            }
        }

        if (i == 0) {
            throw NOTHING_CHANGED.create();
        } else {
            return i;
        }
    }

    private static void setBypass(PlayerList playerList, GameProfile profile, boolean bypassPlayerLimit) {
        ServerOpList ops = playerList.getOps();
        ops.add(new ServerOpListEntry(profile, playerList.getServer().getProfilePermissions(profile), bypassPlayerLimit));
    }

    private static String getName(ServerPlayer player) {
        return player.getGameProfile().getName();
    }

    private static boolean isModPresent(@Nullable ServerPlayer client) {
        return client != null && PermissionLevels.NETWORK.isRemotePresent(client.connection.getConnection());
    }
}
