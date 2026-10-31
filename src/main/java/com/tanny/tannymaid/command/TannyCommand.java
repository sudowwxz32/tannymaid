package com.tanny.tannymaid.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tanny.tannymaid.entity.TannyResource;
import com.tanny.tannymaid.util.TannyCrafting;
import net.minecraft.commands.SharedSuggestionProvider;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannyLookup;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import com.tanny.tannymaid.cobblemon.CobblemonBridge;
import com.tanny.tannymaid.cobblemon.GardevoirCompanion;
import com.tanny.tannymaid.menu.TannyMenu;
import com.tanny.tannymaid.util.PokemonWatch;

/** /tanny follow | stay | home | sethome | tp | organize | farm | defend | inventory | lucario | collect | status | dismiss */
public final class TannyCommand {

    private TannyCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tanny")
                .executes(TannyCommand::help)
                .then(Commands.literal("help").executes(TannyCommand::help))
                .then(Commands.literal("follow").executes(ctx -> run(ctx, TannyEntity::commandFollow)))
                .then(Commands.literal("stay").executes(ctx -> run(ctx, TannyEntity::commandStay)))
                .then(Commands.literal("home").executes(ctx -> run(ctx, TannyEntity::commandHome)))
                .then(Commands.literal("sethome").executes(ctx -> run(ctx, TannyEntity::commandSetHome)))
                .then(Commands.literal("tp").executes(ctx -> run(ctx, TannyEntity::commandTp)))
                .then(Commands.literal("organize").executes(ctx -> run(ctx, TannyEntity::commandOrganize)))
                .then(Commands.literal("farm").executes(ctx -> run(ctx, TannyEntity::commandFarm)))
                .then(Commands.literal("defend").executes(ctx -> run(ctx, TannyEntity::commandDefend)))
                .then(Commands.literal("inventory").executes(ctx -> run(ctx, (t, p) -> t.openInventory(p))))
                .then(Commands.literal("inv").executes(ctx -> run(ctx, (t, p) -> t.openInventory(p))))
                .then(Commands.literal("lucario").executes(ctx -> run(ctx, (t, p) -> t.getLucario().toggleManual(p))))
                .then(Commands.literal("collect").executes(ctx -> run(ctx, TannyEntity::commandCollect)))
                .then(Commands.literal("status").executes(ctx -> run(ctx, TannyEntity::commandStatus)))
                .then(Commands.literal("dismiss").executes(ctx -> run(ctx, TannyEntity::commandDismiss)))
                .then(gather("recolectar"))
                .then(gather("gather"))
                .then(craft("craftear"))
                .then(craft("craft"))
                .then(Commands.literal("pan").executes(ctx -> run(ctx, TannyEntity::commandBread)))
                .then(Commands.literal("bread").executes(ctx -> run(ctx, TannyEntity::commandBread)))
                .then(Commands.literal("seguro").executes(ctx -> run(ctx, TannyEntity::commandSafe)))
                .then(Commands.literal("safe").executes(ctx -> run(ctx, TannyEntity::commandSafe)))
                .then(Commands.literal("gardevoir").executes(ctx -> run(ctx, (t, p) -> GardevoirCompanion.toggle(p))))
                .then(Commands.literal("bayas").executes(ctx -> run(ctx, TannyEntity::commandBerries)))
                .then(Commands.literal("berries").executes(ctx -> run(ctx, TannyEntity::commandBerries)))
                .then(Commands.literal("pc").executes(ctx -> run(ctx, TannyCommand::openPc)))
                .then(Commands.literal("pokebolas").executes(ctx -> run(ctx, (t, p) -> t.openInventory(p, TannyMenu.PAGE_BALLS))))
                .then(Commands.literal("avisar")
                        .then(Commands.argument("pokemon", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(CobblemonBridge.allSpecies(), b))
                                .executes(ctx -> run(ctx, (t, p) -> watchAdd(t, p, StringArgumentType.getString(ctx, "pokemon"))))))
                .then(Commands.literal("noavisar")
                        .then(Commands.literal("todos").executes(ctx -> run(ctx, (t, p) -> {
                            PokemonWatch.clear(p);
                            TannySpeech.say(t, p, "Entendido, Amo. Ya no vigilo a ningún Pokémon ♡");
                        })))
                        .then(Commands.argument("pokemon", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        c.getSource().getPlayer() != null ? PokemonWatch.list(c.getSource().getPlayer()) : java.util.List.of(), b))
                                .executes(ctx -> run(ctx, (t, p) -> {
                                    String n = StringArgumentType.getString(ctx, "pokemon");
                                    TannySpeech.say(t, p, PokemonWatch.remove(p, n)
                                            ? "Listo, Amo. Ya no estaré atenta a " + n + "."
                                            : "Amo, no estaba vigilando a " + n + ".");
                                }))))
                .then(Commands.literal("avisos").executes(ctx -> run(ctx, (t, p) ->
                        TannySpeech.say(t, p, "Estoy atenta a: " + PokemonWatch.describe(p) + " ♡"))))
                .then(Commands.literal("parar").executes(ctx -> run(ctx, TannyEntity::commandStopWork)))
                .then(Commands.literal("stop").executes(ctx -> run(ctx, TannyEntity::commandStopWork))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> gather(String name) {
        return Commands.literal(name)
                .then(Commands.argument("recurso", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(TannyResource.KEYS, b))
                        .executes(ctx -> doGather(ctx, 64))
                        .then(Commands.argument("cantidad", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> doGather(ctx, IntegerArgumentType.getInteger(ctx, "cantidad")))));
    }

    private static int doGather(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "recurso");
        TannyResource res = TannyResource.byName(name);
        if (res == null) {
            TannySpeech.system(ctx.getSource().getPlayerOrException(),
                    "Recurso desconocido. Usa: madera, piedra, carbon, hierro o tierra.");
            return 0;
        }
        return run(ctx, (t, p) -> t.commandGather(p, res, amount));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> craft(String name) {
        return Commands.literal(name)
                .then(Commands.argument("objeto", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(TannyCrafting.ITEMS.keySet(), b))
                        .executes(ctx -> doCraft(ctx, 1))
                        .then(Commands.argument("cantidad", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> doCraft(ctx, IntegerArgumentType.getInteger(ctx, "cantidad")))));
    }

    private static int doCraft(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "objeto");
        return run(ctx, (t, p) -> TannyCrafting.craft(t, p, name, amount));
    }

    private static void openPc(TannyEntity t, ServerPlayer p) {
        if (!CobblemonBridge.isLoaded()) {
            TannySpeech.say(t, p, "Amo, sin Cobblemon no tengo cajas de Pokémon que mostrarte.");
            return;
        }
        if (!CobblemonBridge.openPC(p)) {
            TannySpeech.say(t, p, "Perdón, Amo... no pude abrir tus cajas esta vez.");
        }
    }

    private static void watchAdd(TannyEntity t, ServerPlayer p, String name) {
        java.util.Collection<String> all = CobblemonBridge.allSpecies();
        String n = PokemonWatch.normalize(name);
        if (!all.isEmpty() && !all.contains(n)) {
            TannySpeech.say(t, p, "Mmm... no conozco a ningún Pokémon llamado \"" + name + "\", Amo. ¿Lo escribiste bien?");
            return;
        }
        if (PokemonWatch.add(p, n)) {
            TannySpeech.say(t, p, "¡Entendido, Amo! Estaré atenta a " + n + ". Si lo veo, te aviso y te marco los metros ♡");
        } else {
            TannySpeech.say(t, p, "Ya estoy vigilando a " + n + ", Amo. ¡No se me escapará!");
        }
    }

    private static int run(CommandContext<CommandSourceStack> ctx, BiConsumer<TannyEntity, ServerPlayer> action)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TannyEntity tanny = TannyLookup.find(player);
        if (tanny == null) {
            String last = TannyLookup.describeLastPos(player);
            if (last != null) {
                TannySpeech.system(player, "Tanny no está cargada ahora. Su última posición conocida: §f" + last
                        + "§7. Acércate a esa zona y vuelve a intentarlo.");
            } else {
                TannySpeech.system(player, "Aún no tienes a Tanny. Craftea un §dContrato de Tanny Maid§7 "
                        + "(lingote de oro + mapa) y úsalo.");
            }
            return 0;
        }
        action.accept(tanny, player);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        TannySpeech.system(p, "§dÓrdenes de Tanny:");
        TannySpeech.system(p, "§f/tanny follow§7 – te sigue (y te defiende)");
        TannySpeech.system(p, "§f/tanny stay§7 – se queda en el punto actual");
        TannySpeech.system(p, "§f/tanny sethome§7 – marca tu posición como casa");
        TannySpeech.system(p, "§f/tanny home§7 – va a casa y se queda ahí");
        TannySpeech.system(p, "§f/tanny tp§7 – se teletransporta a ti");
        TannySpeech.system(p, "§f/tanny organize§7 – ordena cofres y barriles cercanos a ella");
        TannySpeech.system(p, "§f/tanny farm§7 – cosecha y siembra en el área donde está");
        TannySpeech.system(p, "§f/tanny defend§7 – modo defensa activa (radio amplio)");
        TannySpeech.system(p, "§f/tanny inventory§7 – abre su inventario");
        TannySpeech.system(p, "§f/tanny lucario§7 – saca o guarda a su Lucario");
        TannySpeech.system(p, "§f/tanny collect§7 – activa/desactiva recoger objetos del suelo");
        TannySpeech.system(p, "§f/tanny status§7 – vida, modo y posición");
        TannySpeech.system(p, "§f/tanny dismiss§7 – la despide y te devuelve el contrato");
        TannySpeech.system(p, "§f/tanny recolectar <madera|piedra|carbon|hierro|tierra> [1-64]§7 – recolecta bloques");
        TannySpeech.system(p, "§f/tanny craftear <objeto> [cantidad]§7 – palos, herramientas, escudo, Poké Balls");
        TannySpeech.system(p, "§f/tanny pan§7 – te da 24 panes (cada 5 min)");
        TannySpeech.system(p, "§f/tanny seguro§7 – te lleva al lugar seguro más cercano");
        TannySpeech.system(p, "§f/tanny parar§7 – deja de recolectar");
        TannySpeech.system(p, "§f/tanny gardevoir§7 – saca o guarda a Gardevoir (siempre a tu lado)");
        TannySpeech.system(p, "§f/tanny bayas§7 – te da bayas curativas de Cobblemon (cada 5 min)");
        TannySpeech.system(p, "§f/tanny pc§7 – abre tus cajas de Pokémon (PC de Cobblemon)");
        TannySpeech.system(p, "§f/tanny pokebolas§7 – abre su bolsa de Poké Balls");
        TannySpeech.system(p, "§f/tanny avisar <pokémon>§7 – te avisa cuando lo vea y marca los metros");
        TannySpeech.system(p, "§f/tanny noavisar <pokémon|todos>§7 · §f/tanny avisos§7 – quitar / ver la lista");
        TannySpeech.system(p, "Clic derecho: inventario · Shift+clic derecho: cambiar modo · Clic con comida/poción: curarla");
        return 1;
    }
}
