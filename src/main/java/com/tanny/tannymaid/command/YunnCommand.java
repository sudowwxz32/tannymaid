package com.tanny.tannymaid.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tanny.tannymaid.cobblemon.CobblemonBridge;
import com.tanny.tannymaid.yunn.YunnEntity;
import com.tanny.tannymaid.yunn.YunnLookup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /yunn curar | potenciar | botiquin | pokedex <p> | donde <p> | aqui | radar | aldea | seguir | quedarse | tp | chansey | cinderace */
public final class YunnCommand {
    private YunnCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("yunn")
                .executes(c -> run(c, YunnEntity.A_HELP, ""))
                .then(Commands.literal("curar").executes(c -> run(c, YunnEntity.A_HEAL, "")))
                .then(Commands.literal("potenciar").executes(c -> run(c, YunnEntity.A_BOOST, "")))
                .then(Commands.literal("botiquin").executes(c -> run(c, YunnEntity.A_KIT, "")))
                .then(Commands.literal("pokedex").then(Commands.argument("pokemon", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(CobblemonBridge.allSpecies(), b))
                        .executes(c -> run(c, YunnEntity.A_DEX, StringArgumentType.getString(c, "pokemon")))))
                .then(Commands.literal("donde").then(Commands.argument("pokemon", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(CobblemonBridge.allSpecies(), b))
                        .executes(c -> run(c, YunnEntity.A_WHERE, StringArgumentType.getString(c, "pokemon")))))
                .then(Commands.literal("aqui").executes(c -> run(c, YunnEntity.A_HERE, "")))
                .then(Commands.literal("radar").executes(c -> run(c, YunnEntity.A_RADAR, "")))
                .then(Commands.literal("aldea").executes(c -> run(c, YunnEntity.A_VILLAGE, "")))
                .then(Commands.literal("seguir").executes(c -> run(c, YunnEntity.A_FOLLOW, "")))
                .then(Commands.literal("quedarse").executes(c -> run(c, YunnEntity.A_STAY, "")))
                .then(Commands.literal("tp").executes(c -> run(c, YunnEntity.A_TP, "")))
                .then(Commands.literal("chansey").executes(c -> run(c, YunnEntity.A_CHANSEY, "")))
                .then(Commands.literal("cinderace").executes(c -> run(c, YunnEntity.A_CINDERACE, ""))));
    }

    private static int run(CommandContext<CommandSourceStack> c, int action, String text) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        YunnEntity y = YunnLookup.find(p);
        if (y == null) {
            p.sendSystemMessage(Component.literal("§7Yunn no está cargada ahora. Usa un §dContrato de Yunn§7 "
                    + "(lingote de oro + rodaja de sandía reluciente) o acércate a donde la dejaste."));
            return 0;
        }
        y.doAction(p, action, text);
        return 1;
    }
}
