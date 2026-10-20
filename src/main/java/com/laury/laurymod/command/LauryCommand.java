package com.laury.laurymod.command;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.entity.LauryResource;
import com.laury.laurymod.util.LauryCrafting;
import com.laury.laurymod.util.LauryLookup;
import com.laury.laurymod.util.LaurySpeech;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

/** /laury dragon | humana | sigueme | quedate | ven | inventario | recolectar | craftear | carne | parar | estado | despedir */
public final class LauryCommand {

    private LauryCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("laury").executes(LauryCommand::help);
        root.then(Commands.literal("ayuda").executes(LauryCommand::help));
        root.then(Commands.literal("help").executes(LauryCommand::help));
        for (String n : new String[] {"dragon", "dragón"}) root.then(Commands.literal(n).executes(c -> run(c, (l, p) -> l.transform(true, p))));
        for (String n : new String[] {"humana", "humano", "human"}) root.then(Commands.literal(n).executes(c -> run(c, (l, p) -> l.transform(false, p))));
        for (String n : new String[] {"sigueme", "follow"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandFollow)));
        for (String n : new String[] {"quedate", "stay"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandStay)));
        for (String n : new String[] {"ven", "tp"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandTp)));
        for (String n : new String[] {"inventario", "inv"}) root.then(Commands.literal(n).executes(c -> run(c, (l, p) -> l.openInventory(p))));
        for (String n : new String[] {"carne", "meat"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandMeat)));
        for (String n : new String[] {"parar", "stop"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandStop)));
        for (String n : new String[] {"estado", "status"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandStatus)));
        for (String n : new String[] {"despedir", "dismiss"}) root.then(Commands.literal(n).executes(c -> run(c, LauryEntity::commandDismiss)));
        for (String n : new String[] {"recolectar", "gather"}) {
            root.then(Commands.literal(n).then(Commands.argument("recurso", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(LauryResource.KEYS, b))
                    .executes(c -> gather(c, 64))
                    .then(Commands.argument("cantidad", IntegerArgumentType.integer(1, 64))
                            .executes(c -> gather(c, IntegerArgumentType.getInteger(c, "cantidad"))))));
        }
        for (String n : new String[] {"craftear", "craft"}) {
            root.then(Commands.literal(n).then(Commands.argument("objeto", StringArgumentType.word())
                    .suggests((c, b) -> SharedSuggestionProvider.suggest(LauryCrafting.ITEMS.keySet(), b))
                    .executes(c -> craft(c, 1))
                    .then(Commands.argument("cantidad", IntegerArgumentType.integer(1, 64))
                            .executes(c -> craft(c, IntegerArgumentType.getInteger(c, "cantidad"))))));
        }
        d.register(root);
    }

    private static int gather(CommandContext<CommandSourceStack> c, int amount) throws CommandSyntaxException {
        LauryResource res = LauryResource.byName(StringArgumentType.getString(c, "recurso"));
        if (res == null) {
            LaurySpeech.system(c.getSource().getPlayerOrException(), "Recursos: madera, piedra, carbon, hierro, tierra, arena, carne.");
            return 0;
        }
        return run(c, (l, p) -> l.commandGather(p, res, amount));
    }

    private static int craft(CommandContext<CommandSourceStack> c, int amount) throws CommandSyntaxException {
        String name = StringArgumentType.getString(c, "objeto");
        return run(c, (l, p) -> LauryCrafting.craft(l, p, name, amount));
    }

    private static int run(CommandContext<CommandSourceStack> c, BiConsumer<LauryEntity, ServerPlayer> action) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        LauryEntity l = LauryLookup.find(p);
        if (l == null) {
            String last = LauryLookup.describeLastPos(p);
            LaurySpeech.system(p, last != null ? "Laury no está cargada. Última posición: §f" + last + "§7. Acércate a esa zona."
                    : "Aún no tienes a Laury. Craftea el §9Cuerno de Invocación§7 (2 lapislázuli, 1 diamante, 1 oro, 1 cuerno de cabra) y sóplalo.");
            return 0;
        }
        action.accept(l, p);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String[] lines = {
                "§9Órdenes de Laury:",
                "§f/laury dragon§7 – se transforma en un gran dragón azul (clic derecho para montarla)",
                "§f/laury humana§7 – vuelve a su forma humana",
                "§f/laury sigueme§7 / §f/laury quedate§7 – seguirte o quedarse quieta",
                "§f/laury ven§7 – se teletransporta a ti",
                "§f/laury inventario§7 – abre su inventario (vida, armadura, herramientas)",
                "§f/laury recolectar <madera|piedra|carbon|hierro|tierra|arena|carne> [1-64]",
                "§f/laury craftear <objeto> [cantidad]§7 – con lo que tenga en su mochila",
                "§f/laury carne§7 – te da 24 de carne cocinada",
                "§f/laury parar§7 · §f/laury estado§7 · §f/laury despedir",
                "§7Volar: W avanza hacia donde miras (arriba sube, abajo = picada). Gira con el mouse.",
                "§7Mantén y suelta ESPACIO para un aletazo de impulso hacia arriba. Shift para bajarte.",
                "§7Hablar: escribe en el chat §f\"Laury, hola\"§7."
        };
        for (String s : lines) LaurySpeech.system(p, s);
        return 1;
    }
}
